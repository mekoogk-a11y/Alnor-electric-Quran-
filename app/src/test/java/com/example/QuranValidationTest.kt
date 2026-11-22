package com.example

import com.example.core.PrayerCalculationMethod
import com.example.core.PrayerTimesCalculator
import com.example.core.QuranVerifier
import com.example.data.local.AyahEntity
import com.example.data.local.SurahSeedData
import com.example.data.remote.QuranApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranValidationTest {

    @Test
    fun `all 114 surahs metadata sums to 6236 verses and 604 pages`() {
        val surahs = SurahSeedData.allSurahs
        assertEquals(114, surahs.size)
        assertEquals((1..114).toList(), surahs.map { it.id })
        assertEquals(6236, surahs.sumOf { it.versesCount })
        assertEquals(1, surahs.first().startPage)
        assertEquals(604, surahs.last().endPage)
        assertEquals(30, SurahSeedData.juzStartPages.size)
    }

    @Test
    fun `verifier rejects stripped diacritics and invalid keys`() {
        val valid = AyahEntity(
            verseKey = "1:1",
            chapterId = 1,
            verseNumber = 1,
            textUthmani = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            pageNumber = 1,
            juzNumber = 1,
            hizbNumber = 1
        )
        assertTrue(QuranVerifier.validateVerse(valid))

        val strippedDiacritics = valid.copy(textUthmani = "بسم الله الرحمن الرحيم")
        assertFalse(QuranVerifier.validateVerse(strippedDiacritics))

        val badPage = valid.copy(pageNumber = 605)
        assertFalse(QuranVerifier.validateVerse(badPage))

        val badKey = valid.copy(verseKey = "2:1")
        assertFalse(QuranVerifier.validateVerse(badKey))
    }

    @Test
    fun `prayer times and qibla bearing produce valid ordered times`() {
        val makkah = PrayerTimesCalculator.presetCities.first { it.id == "makkah" }
        val result = PrayerTimesCalculator.calculate(makkah, PrayerCalculationMethod.UMM_AL_QURA)
        assertTrue(result.fajrMinutes < result.sunriseMinutes)
        assertTrue(result.sunriseMinutes < result.dhuhrMinutes)
        assertTrue(result.dhuhrMinutes < result.asrMinutes)
        assertTrue(result.asrMinutes < result.maghribMinutes)
        assertTrue(result.maghribMinutes < result.ishaMinutes)

        val khartoum = PrayerTimesCalculator.presetCities.first { it.id == "khartoum" }
        val qibla = PrayerTimesCalculator.calculateQiblaBearing(khartoum.latitude, khartoum.longitude)
        assertTrue(qibla in 35.0..65.0)
    }

    @Test
    fun `audio url normalizer resolves relative and protocol-relative urls`() {
        assertEquals(
            "https://verses.quran.foundation/Alafasy/mp3/001001.mp3",
            QuranApiClient.normalizeAudioUrl("Alafasy/mp3/001001.mp3")
        )
        assertEquals(
            "https://cdn.islamic.network/001.mp3",
            QuranApiClient.normalizeAudioUrl("//cdn.islamic.network/001.mp3")
        )
        assertEquals(
            "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/001.mp3",
            QuranApiClient.normalizeAudioUrl("https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/001.mp3")
        )
    }
}
