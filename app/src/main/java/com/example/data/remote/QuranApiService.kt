package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuranApiService {

    @GET("chapters")
    suspend fun getChapters(
        @Query("language") language: String = "ar"
    ): ChaptersResponseDto

    @GET("verses/by_page/{pageNumber}")
    suspend fun getVersesByPage(
        @Path("pageNumber") pageNumber: Int,
        @Query("words") words: Boolean = false,
        @Query("fields") fields: String = "text_uthmani,verse_key,verse_number,chapter_id,page_number,juz_number,hizb_number",
        @Query("translations") translations: String? = null,
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): VersesResponseDto

    @GET("verses/by_chapter/{chapterNumber}")
    suspend fun getVersesByChapter(
        @Path("chapterNumber") chapterNumber: Int,
        @Query("words") words: Boolean = false,
        @Query("fields") fields: String = "text_uthmani,verse_key,verse_number,chapter_id,page_number,juz_number,hizb_number",
        @Query("translations") translations: String? = null,
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): VersesResponseDto

    @GET("verses/by_key/{verseKey}")
    suspend fun getVerseByKey(
        @Path("verseKey") verseKey: String,
        @Query("fields") fields: String = "text_uthmani,verse_key,verse_number,chapter_id,page_number,juz_number,hizb_number",
        @Query("tafsirs") tafsirs: String? = null,
        @Query("translations") translations: String? = null
    ): SingleVerseResponseDto

    @GET("resources/tafsirs")
    suspend fun getTafsirResources(
        @Query("language") language: String = "ar"
    ): TafsirResourcesResponseDto

    @GET("tafsirs/{tafsirId}/by_ayah/{verseKey}")
    suspend fun getTafsirByAyah(
        @Path("tafsirId") tafsirId: Int,
        @Path("verseKey") verseKey: String
    ): TafsirByAyahResponseDto

    @GET("resources/translations")
    suspend fun getTranslationResources(
        @Query("language") language: String = "en"
    ): TranslationsResourcesResponseDto

    @GET("resources/recitations")
    suspend fun getRecitationResources(
        @Query("language") language: String = "ar"
    ): RecitationsResourcesResponseDto

    @GET("chapter_recitations/{recitationId}/{chapterNumber}")
    suspend fun getChapterRecitationAudio(
        @Path("recitationId") recitationId: Int,
        @Path("chapterNumber") chapterNumber: Int
    ): ChapterAudioResponseDto

    @GET("recitations/{recitationId}/by_chapter/{chapterNumber}")
    suspend fun getVerseAudioByChapter(
        @Path("recitationId") recitationId: Int,
        @Path("chapterNumber") chapterNumber: Int,
        @Query("per_page") perPage: Int = 300
    ): AyahAudioFilesResponseDto

    @GET("search")
    suspend fun searchQuran(
        @Query("q") query: String,
        @Query("size") size: Int = 25,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "ar"
    ): QuranSearchResponseDto
}
