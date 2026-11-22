package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    // Surahs
    @Query("SELECT * FROM surahs ORDER BY id ASC")
    fun observeAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs ORDER BY id ASC")
    suspend fun getAllSurahsOnce(): List<SurahEntity>

    @Query("SELECT * FROM surahs WHERE id = :id LIMIT 1")
    suspend fun getSurahById(id: Int): SurahEntity?

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun getSurahCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    // Ayahs
    @Query("SELECT * FROM ayahs WHERE pageNumber = :pageNumber ORDER BY chapterId ASC, verseNumber ASC")
    fun observeVersesByPage(pageNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE pageNumber = :pageNumber ORDER BY chapterId ASC, verseNumber ASC")
    suspend fun getVersesByPageOnce(pageNumber: Int): List<AyahEntity>

    @Query("SELECT * FROM ayahs WHERE chapterId = :chapterId ORDER BY verseNumber ASC")
    fun observeVersesByChapter(chapterId: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE chapterId = :chapterId ORDER BY verseNumber ASC")
    suspend fun getVersesByChapterOnce(chapterId: Int): List<AyahEntity>

    @Query("SELECT * FROM ayahs WHERE verseKey = :verseKey LIMIT 1")
    suspend fun getVerseByKey(verseKey: String): AyahEntity?

    @Query("SELECT * FROM ayahs ORDER BY chapterId ASC, verseNumber ASC")
    suspend fun getAllVersesOnce(): List<AyahEntity>

    @Query("SELECT COUNT(*) FROM ayahs")
    fun observeCachedVersesCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT pageNumber) FROM ayahs")
    fun observeCachedPagesCount(): Flow<Int>

    @Query("SELECT * FROM ayahs WHERE textUthmani LIKE '%' || :query || '%' ORDER BY chapterId ASC, verseNumber ASC LIMIT 60")
    suspend fun searchVersesOffline(query: String): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<AyahEntity>)

    // Tafsir & Translation Cache
    @Query("SELECT * FROM tafsir_cache WHERE tafsirId = :tafsirId AND verseKey = :verseKey LIMIT 1")
    suspend fun getCachedTafsir(tafsirId: Int, verseKey: String): TafsirCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafsir(entry: TafsirCacheEntity)

    @Query("SELECT * FROM translation_cache WHERE translationId = :translationId AND verseKey IN (:verseKeys)")
    suspend fun getCachedTranslations(translationId: Int, verseKeys: List<String>): List<TranslationCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(entries: List<TranslationCacheEntity>)

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE pageNumber = :pageNumber)")
    fun observeIsPageBookmarked(pageNumber: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE pageNumber = :pageNumber")
    suspend fun deleteBookmarkByPage(pageNumber: Int)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Int)

    // Memorization
    @Query("SELECT * FROM memorization ORDER BY surahId ASC")
    fun observeMemorizationProgress(): Flow<List<MemorizationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMemorization(entry: MemorizationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMemorizationDefaults(entries: List<MemorizationEntity>)

    // Tasbih
    @Query("SELECT * FROM tasbih_sessions ORDER BY id ASC")
    fun observeTasbihSessions(): Flow<List<TasbihSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasbihSession(session: TasbihSessionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTasbihDefaults(sessions: List<TasbihSessionEntity>)

    // Downloaded Audio
    @Query("SELECT * FROM downloaded_audio")
    fun observeDownloadedAudio(): Flow<List<DownloadedAudioEntity>>

    @Query("SELECT * FROM downloaded_audio WHERE reciterId = :reciterId AND chapterId = :chapterId LIMIT 1")
    suspend fun getDownloadedAudio(reciterId: Int, chapterId: Int): DownloadedAudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedAudio(entry: DownloadedAudioEntity)

    @Query("DELETE FROM downloaded_audio WHERE key = :key")
    suspend fun deleteDownloadedAudio(key: String)
}
