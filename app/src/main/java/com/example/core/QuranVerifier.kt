package com.example.core

import com.example.data.local.AyahEntity
import com.example.data.local.SurahEntity

data class QuranVerificationReport(
    val isValid: Boolean,
    val totalChaptersCount: Int,
    val expectedChaptersCount: Int = 114,
    val expectedTotalVerses: Int = 6236,
    val cachedVersesCount: Int,
    val cachedPagesCount: Int,
    val chaptersWithCompleteVerses: Int,
    val corruptedEntriesCount: Int,
    val issues: List<String>,
    val sourceLabel: String = "Quran Foundation Content API v4 (api.quran.com/api/v4) - Uthmani"
)

object QuranVerifier {

    private val verseKeyRegex = Regex("^([1-9]|[1-9][0-9]|10[0-9]|11[0-4]):[1-9][0-9]*$")
    private val arabicDiacriticsRange = Regex("[\u064B-\u065F\u0670\u06D6-\u06ED]")

    fun validateVerse(ayah: AyahEntity): Boolean {
        if (!verseKeyRegex.matches(ayah.verseKey)) return false
        if (ayah.chapterId !in 1..114) return false
        if (ayah.verseNumber < 1) return false
        if (ayah.verseKey != "${ayah.chapterId}:${ayah.verseNumber}") return false
        if (ayah.pageNumber !in 1..604) return false
        if (ayah.juzNumber !in 1..30) return false
        if (ayah.textUthmani.isBlank()) return false
        // Ensure Arabic letters exist and diacritics were not stripped
        val hasArabic = ayah.textUthmani.any { it in '\u0621'..'\u064A' }
        val hasDiacritics = arabicDiacriticsRange.containsMatchIn(ayah.textUthmani)
        return hasArabic && hasDiacritics
    }

    fun verifyDatabaseIntegrity(
        surahs: List<SurahEntity>,
        verses: List<AyahEntity>
    ): QuranVerificationReport {
        val issues = mutableListOf<String>()

        if (surahs.size != 114) {
            issues.add("Chapter count mismatch: found ${surahs.size}, expected 114.")
        }

        val totalExpectedFromMetadata = surahs.sumOf { it.versesCount }
        if (totalExpectedFromMetadata != 6236) {
            issues.add("Surah verse sum mismatch: $totalExpectedFromMetadata vs 6236.")
        }

        var corruptedCount = 0
        for (ayah in verses) {
            if (!validateVerse(ayah)) {
                corruptedCount++
                if (issues.size < 10) {
                    issues.add("Invalid or stripped verse entry: ${ayah.verseKey}")
                }
            }
        }

        val versesByChapter = verses.groupBy { it.chapterId }
        var completeChapters = 0
        for (surah in surahs) {
            val chapterVerses = versesByChapter[surah.id].orEmpty()
            if (chapterVerses.size == surah.versesCount) {
                val expectedOrder = (1..surah.versesCount).toList()
                val actualOrder = chapterVerses.map { it.verseNumber }.sorted()
                if (actualOrder == expectedOrder) {
                    completeChapters++
                } else {
                    issues.add("Verse ordering gap in Surah ${surah.id} (${surah.nameSimple}).")
                }
            }
        }

        val distinctPages = verses.map { it.pageNumber }.toSet().size

        return QuranVerificationReport(
            isValid = issues.isEmpty() && corruptedCount == 0 && surahs.size == 114,
            totalChaptersCount = surahs.size,
            cachedVersesCount = verses.size,
            cachedPagesCount = distinctPages,
            chaptersWithCompleteVerses = completeChapters,
            corruptedEntriesCount = corruptedCount,
            issues = issues
        )
    }
}
