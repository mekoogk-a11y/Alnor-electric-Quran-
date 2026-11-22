package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val id: Int,
    val nameArabic: String,
    val nameSimple: String,
    val nameEnglish: String,
    val revelationPlace: String,
    val versesCount: Int,
    val startPage: Int,
    val endPage: Int,
    val juzStart: Int,
    val bismillahPre: Boolean = id != 1 && id != 9
)

@Entity(
    tableName = "ayahs",
    indices = [
        Index(value = ["chapterId", "verseNumber"]),
        Index(value = ["pageNumber"]),
        Index(value = ["juzNumber"])
    ]
)
data class AyahEntity(
    @PrimaryKey val verseKey: String,
    val chapterId: Int,
    val verseNumber: Int,
    val textUthmani: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val hizbNumber: Int
)

@Entity(tableName = "tafsir_cache")
data class TafsirCacheEntity(
    @PrimaryKey val cacheKey: String,
    val tafsirId: Int,
    val verseKey: String,
    val resourceName: String,
    val textHtml: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "translation_cache")
data class TranslationCacheEntity(
    @PrimaryKey val cacheKey: String,
    val translationId: Int,
    val verseKey: String,
    val resourceName: String,
    val text: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pageNumber: Int,
    val surahId: Int,
    val surahNameArabic: String,
    val verseKey: String?,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memorization")
data class MemorizationEntity(
    @PrimaryKey val surahId: Int,
    val memorizedVersesCount: Int = 0,
    val totalVerses: Int,
    val status: String = "NOT_STARTED",
    val lastReviewedAt: Long? = null,
    val reminderEnabled: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "tasbih_sessions")
data class TasbihSessionEntity(
    @PrimaryKey val id: String,
    val dhikrArabic: String,
    val dhikrEnglish: String,
    val currentCount: Int = 0,
    val totalLifetimeCount: Int = 0,
    val targetCount: Int = 33,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_audio")
data class DownloadedAudioEntity(
    @PrimaryKey val key: String,
    val reciterId: Int,
    val chapterId: Int,
    val localFilePath: String,
    val fileSizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)
