package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChaptersResponseDto(
    @Json(name = "chapters") val chapters: List<ChapterDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChapterDto(
    @Json(name = "id") val id: Int,
    @Json(name = "revelation_place") val revelationPlace: String = "makkah",
    @Json(name = "bismillah_pre") val bismillahPre: Boolean = true,
    @Json(name = "name_simple") val nameSimple: String = "",
    @Json(name = "name_arabic") val nameArabic: String = "",
    @Json(name = "verses_count") val versesCount: Int = 0,
    @Json(name = "pages") val pages: List<Int> = emptyList(),
    @Json(name = "translated_name") val translatedName: TranslatedNameDto? = null
)

@JsonClass(generateAdapter = true)
data class TranslatedNameDto(
    @Json(name = "language_name") val languageName: String = "",
    @Json(name = "name") val name: String = ""
)

@JsonClass(generateAdapter = true)
data class VersesResponseDto(
    @Json(name = "verses") val verses: List<VerseDto> = emptyList(),
    @Json(name = "pagination") val pagination: PaginationDto? = null
)

@JsonClass(generateAdapter = true)
data class SingleVerseResponseDto(
    @Json(name = "verse") val verse: VerseDto? = null
)

@JsonClass(generateAdapter = true)
data class VerseDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "chapter_id") val chapterId: Int? = null,
    @Json(name = "verse_number") val verseNumber: Int = 0,
    @Json(name = "verse_key") val verseKey: String = "",
    @Json(name = "text_uthmani") val textUthmani: String? = null,
    @Json(name = "juz_number") val juzNumber: Int = 1,
    @Json(name = "hizb_number") val hizbNumber: Int = 1,
    @Json(name = "page_number") val pageNumber: Int = 1,
    @Json(name = "tafsirs") val tafsirs: List<VerseTafsirDto>? = null,
    @Json(name = "translations") val translations: List<VerseTranslationDto>? = null
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    @Json(name = "per_page") val perPage: Int = 10,
    @Json(name = "current_page") val currentPage: Int = 1,
    @Json(name = "next_page") val nextPage: Int? = null,
    @Json(name = "total_pages") val totalPages: Int = 1,
    @Json(name = "total_records") val totalRecords: Int = 0
)

@JsonClass(generateAdapter = true)
data class VerseTafsirDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "resource_id") val resourceId: Int = 0,
    @Json(name = "resource_name") val resourceName: String? = null,
    @Json(name = "text") val text: String = ""
)

@JsonClass(generateAdapter = true)
data class VerseTranslationDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "resource_id") val resourceId: Int = 0,
    @Json(name = "resource_name") val resourceName: String? = null,
    @Json(name = "text") val text: String = ""
)

@JsonClass(generateAdapter = true)
data class TafsirResourcesResponseDto(
    @Json(name = "tafsirs") val tafsirs: List<TafsirResourceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TafsirResourceDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String = "",
    @Json(name = "author_name") val authorName: String? = null,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "language_name") val languageName: String? = null,
    @Json(name = "translated_name") val translatedName: TranslatedNameDto? = null
)

@JsonClass(generateAdapter = true)
data class TafsirByAyahResponseDto(
    @Json(name = "tafsir") val tafsir: TafsirContentDto? = null
)

@JsonClass(generateAdapter = true)
data class TafsirContentDto(
    @Json(name = "resource_id") val resourceId: Int = 0,
    @Json(name = "resource_name") val resourceName: String? = null,
    @Json(name = "text") val text: String = "",
    @Json(name = "verse_key") val verseKey: String? = null
)

@JsonClass(generateAdapter = true)
data class TranslationsResourcesResponseDto(
    @Json(name = "translations") val translations: List<TranslationResourceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TranslationResourceDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String = "",
    @Json(name = "author_name") val authorName: String? = null,
    @Json(name = "language_name") val languageName: String? = null
)

@JsonClass(generateAdapter = true)
data class RecitationsResourcesResponseDto(
    @Json(name = "recitations") val recitations: List<RecitationResourceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RecitationResourceDto(
    @Json(name = "id") val id: Int,
    @Json(name = "reciter_name") val reciterName: String = "",
    @Json(name = "style") val style: String? = null,
    @Json(name = "translated_name") val translatedName: TranslatedNameDto? = null
)

@JsonClass(generateAdapter = true)
data class ChapterAudioResponseDto(
    @Json(name = "audio_file") val audioFile: ChapterAudioFileDto? = null
)

@JsonClass(generateAdapter = true)
data class ChapterAudioFileDto(
    @Json(name = "chapter_id") val chapterId: Int = 0,
    @Json(name = "file_size") val fileSize: Long = 0L,
    @Json(name = "format") val format: String = "mp3",
    @Json(name = "audio_url") val audioUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class AyahAudioFilesResponseDto(
    @Json(name = "audio_files") val audioFiles: List<AyahAudioFileDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AyahAudioFileDto(
    @Json(name = "verse_key") val verseKey: String = "",
    @Json(name = "url") val url: String = ""
)

@JsonClass(generateAdapter = true)
data class QuranSearchResponseDto(
    @Json(name = "search") val search: QuranSearchBlockDto? = null
)

@JsonClass(generateAdapter = true)
data class QuranSearchBlockDto(
    @Json(name = "query") val query: String = "",
    @Json(name = "total_results") val totalResults: Int = 0,
    @Json(name = "results") val results: List<QuranSearchResultDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuranSearchResultDto(
    @Json(name = "verse_key") val verseKey: String = "",
    @Json(name = "verse_id") val verseId: Int = 0,
    @Json(name = "text") val text: String = "",
    @Json(name = "translations") val translations: List<SearchTranslationDto>? = null
)

@JsonClass(generateAdapter = true)
data class SearchTranslationDto(
    @Json(name = "text") val text: String = "",
    @Json(name = "name") val name: String? = null
)
