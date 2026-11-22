package com.example.data.repository

import android.content.Context
import com.example.core.QuranVerificationReport
import com.example.core.QuranVerifier
import com.example.data.local.AyahEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.DownloadedAudioEntity
import com.example.data.local.MemorizationEntity
import com.example.data.local.QuranDao
import com.example.data.local.SurahEntity
import com.example.data.local.SurahSeedData
import com.example.data.local.TasbihSessionEntity
import com.example.data.local.TafsirCacheEntity
import com.example.data.local.TranslationCacheEntity
import com.example.data.remote.QuranApiClient
import com.example.data.remote.QuranApiService
import com.example.data.remote.RecitationResourceDto
import com.example.data.remote.TafsirResourceDto
import com.example.data.remote.TranslationResourceDto
import com.example.data.remote.VerseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

data class OfflineDownloadProgress(
    val isDownloading: Boolean = false,
    val completedSurahs: Int = 0,
    val totalSurahs: Int = 114,
    val errorMessage: String? = null
)

data class SearchVerseResult(
    val verseKey: String,
    val chapterId: Int,
    val verseNumber: Int,
    val textUthmani: String,
    val translationSnippet: String?,
    val pageNumber: Int
)

data class TafsirResult(
    val verseKey: String,
    val verseTextUthmani: String,
    val tafsirId: Int,
    val resourceName: String,
    val tafsirTextHtml: String
)

class QuranRepository(
    private val context: Context,
    private val dao: QuranDao,
    private val api: QuranApiService = QuranApiClient.apiService
) {

    private val _downloadProgress = MutableStateFlow(OfflineDownloadProgress())
    val downloadProgress: StateFlow<OfflineDownloadProgress> = _downloadProgress.asStateFlow()

    val surahsFlow: Flow<List<SurahEntity>> = dao.observeAllSurahs()
    val bookmarksFlow: Flow<List<BookmarkEntity>> = dao.observeBookmarks()
    val memorizationFlow: Flow<List<MemorizationEntity>> = dao.observeMemorizationProgress()
    val tasbihFlow: Flow<List<TasbihSessionEntity>> = dao.observeTasbihSessions()
    val downloadedAudioFlow: Flow<List<DownloadedAudioEntity>> = dao.observeDownloadedAudio()
    val cachedVersesCountFlow: Flow<Int> = dao.observeCachedVersesCount()
    val cachedPagesCountFlow: Flow<Int> = dao.observeCachedPagesCount()

    suspend fun ensureSeedData() = withContext(Dispatchers.IO) {
        if (dao.getSurahCount() < 114) {
            dao.insertSurahs(SurahSeedData.allSurahs)
        }
    }

    fun observeVersesByPage(pageNumber: Int): Flow<List<AyahEntity>> = dao.observeVersesByPage(pageNumber)

    fun observeVersesByChapter(chapterId: Int): Flow<List<AyahEntity>> = dao.observeVersesByChapter(chapterId)

    fun observeIsPageBookmarked(pageNumber: Int): Flow<Boolean> = dao.observeIsPageBookmarked(pageNumber)

    suspend fun fetchAndCachePage(pageNumber: Int, translationId: Int? = null): Result<List<AyahEntity>> =
        withContext(Dispatchers.IO) {
            val safePage = pageNumber.coerceIn(1, 604)
            val existing = dao.getVersesByPageOnce(safePage)
            if (existing.isNotEmpty() && translationId == null) {
                return@withContext Result.success(existing)
            }
            return@withContext try {
                val allDtos = mutableListOf<VerseDto>()
                var currentPage = 1
                do {
                    val response = api.getVersesByPage(
                        pageNumber = safePage,
                        translations = translationId?.toString(),
                        perPage = 50,
                        page = currentPage
                    )
                    allDtos.addAll(response.verses)
                    currentPage = response.pagination?.nextPage ?: 0
                } while (currentPage > 0)

                val mapped = allDtos.mapNotNull { dto ->
                    val chId = dto.chapterId ?: dto.verseKey.substringBefore(":").toIntOrNull() ?: return@mapNotNull null
                    val text = dto.textUthmani?.trim().orEmpty()
                    if (text.isEmpty()) return@mapNotNull null
                    AyahEntity(
                        verseKey = dto.verseKey,
                        chapterId = chId,
                        verseNumber = dto.verseNumber,
                        textUthmani = text,
                        pageNumber = dto.pageNumber,
                        juzNumber = dto.juzNumber,
                        hizbNumber = dto.hizbNumber
                    )
                }.filter { QuranVerifier.validateVerse(it) }

                if (mapped.isNotEmpty()) {
                    dao.insertVerses(mapped)
                }

                if (translationId != null) {
                    val trEntries = allDtos.mapNotNull { dto ->
                        val tr = dto.translations?.firstOrNull() ?: return@mapNotNull null
                        TranslationCacheEntity(
                            cacheKey = "$translationId:${dto.verseKey}",
                            translationId = translationId,
                            verseKey = dto.verseKey,
                            resourceName = tr.resourceName ?: "",
                            text = stripHtmlTags(tr.text)
                        )
                    }
                    if (trEntries.isNotEmpty()) {
                        dao.insertTranslations(trEntries)
                    }
                }

                val updated = dao.getVersesByPageOnce(safePage)
                if (updated.isNotEmpty()) {
                    Result.success(updated)
                } else {
                    Result.failure(IllegalStateException("No verified verses returned for page $safePage"))
                }
            } catch (e: Exception) {
                if (existing.isNotEmpty()) Result.success(existing) else Result.failure(e)
            }
        }

    suspend fun fetchAndCacheChapter(chapterId: Int, translationId: Int? = null): Result<List<AyahEntity>> =
        withContext(Dispatchers.IO) {
            val safeChapter = chapterId.coerceIn(1, 114)
            val surah = dao.getSurahById(safeChapter)
            val existing = dao.getVersesByChapterOnce(safeChapter)
            if (surah != null && existing.size == surah.versesCount && translationId == null) {
                return@withContext Result.success(existing)
            }
            return@withContext try {
                val allDtos = mutableListOf<VerseDto>()
                var currentPage = 1
                do {
                    val response = api.getVersesByChapter(
                        chapterNumber = safeChapter,
                        translations = translationId?.toString(),
                        perPage = 50,
                        page = currentPage
                    )
                    allDtos.addAll(response.verses)
                    currentPage = response.pagination?.nextPage ?: 0
                } while (currentPage > 0)

                val mapped = allDtos.mapNotNull { dto ->
                    val chId = dto.chapterId ?: dto.verseKey.substringBefore(":").toIntOrNull() ?: safeChapter
                    val text = dto.textUthmani?.trim().orEmpty()
                    if (text.isEmpty()) return@mapNotNull null
                    AyahEntity(
                        verseKey = dto.verseKey,
                        chapterId = chId,
                        verseNumber = dto.verseNumber,
                        textUthmani = text,
                        pageNumber = dto.pageNumber,
                        juzNumber = dto.juzNumber,
                        hizbNumber = dto.hizbNumber
                    )
                }.filter { QuranVerifier.validateVerse(it) }

                if (mapped.isNotEmpty()) {
                    dao.insertVerses(mapped)
                }

                if (translationId != null) {
                    val trEntries = allDtos.mapNotNull { dto ->
                        val tr = dto.translations?.firstOrNull() ?: return@mapNotNull null
                        TranslationCacheEntity(
                            cacheKey = "$translationId:${dto.verseKey}",
                            translationId = translationId,
                            verseKey = dto.verseKey,
                            resourceName = tr.resourceName ?: "",
                            text = stripHtmlTags(tr.text)
                        )
                    }
                    if (trEntries.isNotEmpty()) {
                        dao.insertTranslations(trEntries)
                    }
                }

                val updated = dao.getVersesByChapterOnce(safeChapter)
                if (updated.isNotEmpty()) Result.success(updated)
                else Result.failure(IllegalStateException("Unable to load Surah $safeChapter"))
            } catch (e: Exception) {
                if (existing.isNotEmpty()) Result.success(existing) else Result.failure(e)
            }
        }

    suspend fun getCachedTranslations(translationId: Int, verseKeys: List<String>): Map<String, String> =
        withContext(Dispatchers.IO) {
            if (verseKeys.isEmpty()) return@withContext emptyMap()
            dao.getCachedTranslations(translationId, verseKeys).associate { it.verseKey to it.text }
        }

    suspend fun downloadFullQuranOffline() = withContext(Dispatchers.IO) {
        if (_downloadProgress.value.isDownloading) return@withContext
        _downloadProgress.value = OfflineDownloadProgress(isDownloading = true, completedSurahs = 0, totalSurahs = 114)
        try {
            ensureSeedData()
            val surahs = dao.getAllSurahsOnce()
            var completed = 0
            for (surah in surahs) {
                val existing = dao.getVersesByChapterOnce(surah.id)
                if (existing.size < surah.versesCount) {
                    val result = fetchAndCacheChapter(surah.id)
                    if (result.isFailure) {
                        throw result.exceptionOrNull() ?: IllegalStateException("Failed at Surah ${surah.id}")
                    }
                }
                completed++
                _downloadProgress.value = OfflineDownloadProgress(
                    isDownloading = true,
                    completedSurahs = completed,
                    totalSurahs = 114
                )
            }
            _downloadProgress.value = OfflineDownloadProgress(
                isDownloading = false,
                completedSurahs = 114,
                totalSurahs = 114
            )
        } catch (e: Exception) {
            _downloadProgress.value = _downloadProgress.value.copy(
                isDownloading = false,
                errorMessage = e.message ?: "Download interrupted"
            )
        }
    }

    suspend fun getTafsirResources(language: String): List<TafsirResourceDto> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            TafsirResourceDto(16, "التفسير الميسر", "مجمع الملك فهد", "ar-tafsir-muyassar", "arabic"),
            TafsirResourceDto(14, "تفسير ابن كثير", "الإمام ابن كثير", "ar-tafsir-ibn-kathir", "arabic"),
            TafsirResourceDto(91, "تفسير السعدي", "عبد الرحمن السعدي", "ar-tafseer-al-saddi", "arabic"),
            TafsirResourceDto(169, "Tafsir Ibn Kathir (Abridged)", "Ibn Kathir", "en-tafisr-ibn-kathir", "english")
        )
        return@withContext try {
            val remote = api.getTafsirResources(language).tafsirs
            if (remote.isNotEmpty()) remote else fallback
        } catch (_: Exception) {
            fallback
        }
    }

    suspend fun getTafsirForVerse(tafsirId: Int, verseKey: String): Result<TafsirResult> =
        withContext(Dispatchers.IO) {
            val localVerse = dao.getVerseByKey(verseKey)
            val cachedTafsir = dao.getCachedTafsir(tafsirId, verseKey)
            if (cachedTafsir != null && localVerse != null) {
                return@withContext Result.success(
                    TafsirResult(
                        verseKey = verseKey,
                        verseTextUthmani = localVerse.textUthmani,
                        tafsirId = tafsirId,
                        resourceName = cachedTafsir.resourceName,
                        tafsirTextHtml = cachedTafsir.textHtml
                    )
                )
            }

            return@withContext try {
                val verseResp = api.getVerseByKey(verseKey = verseKey, tafsirs = tafsirId.toString()).verse
                val verseText = localVerse?.textUthmani ?: verseResp?.textUthmani.orEmpty()
                if (localVerse == null && verseResp != null && verseText.isNotBlank()) {
                    val chId = verseResp.chapterId ?: verseKey.substringBefore(":").toIntOrNull() ?: 1
                    val entity = AyahEntity(
                        verseKey = verseResp.verseKey,
                        chapterId = chId,
                        verseNumber = verseResp.verseNumber,
                        textUthmani = verseText,
                        pageNumber = verseResp.pageNumber,
                        juzNumber = verseResp.juzNumber,
                        hizbNumber = verseResp.hizbNumber
                    )
                    if (QuranVerifier.validateVerse(entity)) dao.insertVerses(listOf(entity))
                }

                val inlineTafsir = verseResp?.tafsirs?.firstOrNull()
                val tafsirPair = if (inlineTafsir != null && inlineTafsir.text.isNotBlank()) {
                    (inlineTafsir.resourceName ?: "") to inlineTafsir.text
                } else {
                    val direct = api.getTafsirByAyah(tafsirId, verseKey).tafsir
                    (direct?.resourceName ?: "") to (direct?.text ?: "")
                }

                if (tafsirPair.second.isNotBlank()) {
                    dao.insertTafsir(
                        TafsirCacheEntity(
                            cacheKey = "$tafsirId:$verseKey",
                            tafsirId = tafsirId,
                            verseKey = verseKey,
                            resourceName = tafsirPair.first,
                            textHtml = tafsirPair.second
                        )
                    )
                }

                Result.success(
                    TafsirResult(
                        verseKey = verseKey,
                        verseTextUthmani = verseText,
                        tafsirId = tafsirId,
                        resourceName = tafsirPair.first,
                        tafsirTextHtml = tafsirPair.second
                    )
                )
            } catch (e: Exception) {
                if (cachedTafsir != null) {
                    Result.success(
                        TafsirResult(
                            verseKey = verseKey,
                            verseTextUthmani = localVerse?.textUthmani.orEmpty(),
                            tafsirId = tafsirId,
                            resourceName = cachedTafsir.resourceName,
                            tafsirTextHtml = cachedTafsir.textHtml
                        )
                    )
                } else {
                    Result.failure(e)
                }
            }
        }

    suspend fun getRecitations(language: String): List<RecitationResourceDto> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            RecitationResourceDto(7, "مشاري راشد العفاسي", "Murattal"),
            RecitationResourceDto(2, "عبد الباسط عبد الصمد", "Murattal"),
            RecitationResourceDto(3, "عبد الرحمن السديس", "Murattal"),
            RecitationResourceDto(5, "هاني الرفاعي", "Murattal"),
            RecitationResourceDto(6, "محمود خليل الحصري", "Murattal"),
            RecitationResourceDto(10, "سعود الشريم", "Murattal")
        )
        return@withContext try {
            val remote = api.getRecitationResources(language).recitations
            if (remote.isNotEmpty()) remote else fallback
        } catch (_: Exception) {
            fallback
        }
    }

    suspend fun getTranslationResources(language: String): List<TranslationResourceDto> = withContext(Dispatchers.IO) {
        val fallback = listOf(
            TranslationResourceDto(131, "Dr. Mustafa Khattab, The Clear Quran", "Dr. Mustafa Khattab", "english"),
            TranslationResourceDto(20, "Saheeh International", "Saheeh International", "english")
        )
        return@withContext try {
            val remote = api.getTranslationResources(language).translations
            if (remote.isNotEmpty()) remote else fallback
        } catch (_: Exception) {
            fallback
        }
    }

    suspend fun resolveChapterAudioUri(reciterId: Int, chapterId: Int): Result<Pair<String, Boolean>> =
        withContext(Dispatchers.IO) {
            val local = dao.getDownloadedAudio(reciterId, chapterId)
            if (local != null && File(local.localFilePath).exists()) {
                return@withContext Result.success(File(local.localFilePath).toURI().toString() to true)
            }
            return@withContext try {
                val resp = api.getChapterRecitationAudio(reciterId, chapterId)
                val rawUrl = resp.audioFile?.audioUrl.orEmpty()
                if (rawUrl.isBlank()) {
                    Result.failure(IllegalStateException("Audio URL unavailable"))
                } else {
                    Result.success(QuranApiClient.normalizeAudioUrl(rawUrl) to false)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun downloadChapterAudio(reciterId: Int, chapterId: Int): Result<DownloadedAudioEntity> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val resp = api.getChapterRecitationAudio(reciterId, chapterId)
                val rawUrl = resp.audioFile?.audioUrl.orEmpty()
                if (rawUrl.isBlank()) return@withContext Result.failure(IllegalStateException("Audio URL missing"))
                val fullUrl = QuranApiClient.normalizeAudioUrl(rawUrl)

                val dir = File(context.filesDir, "quran_audio/$reciterId").apply { mkdirs() }
                val targetFile = File(dir, String.format("%03d.mp3", chapterId))

                val request = Request.Builder().url(fullUrl).build()
                QuranApiClient.okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(IllegalStateException("HTTP ${response.code}"))
                    }
                    val body = response.body ?: return@withContext Result.failure(IllegalStateException("Empty audio body"))
                    body.byteStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val entity = DownloadedAudioEntity(
                    key = "$reciterId:$chapterId",
                    reciterId = reciterId,
                    chapterId = chapterId,
                    localFilePath = targetFile.absolutePath,
                    fileSizeBytes = targetFile.length()
                )
                dao.insertDownloadedAudio(entity)
                Result.success(entity)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteDownloadedAudio(reciterId: Int, chapterId: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getDownloadedAudio(reciterId, chapterId)
        if (existing != null) {
            File(existing.localFilePath).delete()
            dao.deleteDownloadedAudio(existing.key)
        }
    }

    suspend fun searchQuran(query: String, language: String): List<SearchVerseResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val offlineMatches = dao.searchVersesOffline(trimmed).map {
            SearchVerseResult(
                verseKey = it.verseKey,
                chapterId = it.chapterId,
                verseNumber = it.verseNumber,
                textUthmani = it.textUthmani,
                translationSnippet = null,
                pageNumber = it.pageNumber
            )
        }
        if (offlineMatches.isNotEmpty()) return@withContext offlineMatches

        return@withContext try {
            val remote = api.searchQuran(query = trimmed, size = 25, page = 1, language = language)
            val results = remote.search?.results.orEmpty()
            results.mapNotNull { item ->
                val parts = item.verseKey.split(":")
                val ch = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val ay = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                val cached = dao.getVerseByKey(item.verseKey)
                val surahStartPage = dao.getSurahById(ch)?.startPage ?: 1
                SearchVerseResult(
                    verseKey = item.verseKey,
                    chapterId = ch,
                    verseNumber = ay,
                    textUthmani = cached?.textUthmani ?: stripHtmlTags(item.text),
                    translationSnippet = item.translations?.firstOrNull()?.text?.let { stripHtmlTags(it) },
                    pageNumber = cached?.pageNumber ?: surahStartPage
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun addBookmark(pageNumber: Int, surahId: Int, surahNameArabic: String, verseKey: String? = null) =
        withContext(Dispatchers.IO) {
            dao.insertBookmark(
                BookmarkEntity(
                    pageNumber = pageNumber,
                    surahId = surahId,
                    surahNameArabic = surahNameArabic,
                    verseKey = verseKey
                )
            )
        }

    suspend fun removeBookmarkByPage(pageNumber: Int) = withContext(Dispatchers.IO) {
        dao.deleteBookmarkByPage(pageNumber)
    }

    suspend fun removeBookmarkById(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteBookmarkById(id)
    }

    suspend fun updateMemorization(entry: MemorizationEntity) = withContext(Dispatchers.IO) {
        dao.upsertMemorization(entry)
    }

    suspend fun updateTasbihSession(session: TasbihSessionEntity) = withContext(Dispatchers.IO) {
        dao.upsertTasbihSession(session)
    }

    suspend fun runIntegrityVerification(): QuranVerificationReport = withContext(Dispatchers.IO) {
        ensureSeedData()
        val surahs = dao.getAllSurahsOnce()
        val verses = dao.getAllVersesOnce()
        QuranVerifier.verifyDatabaseIntegrity(surahs, verses)
    }

    fun stripHtmlTags(raw: String): String {
        return raw.replace(Regex("<[^>]*>"), " ")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&apos;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
