package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AlNoorDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.SurahSeedData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertTrue(appName == "مصحف النور" || appName == "Al-Noor Quran")
    }

    @Test
    fun `offline room database seeds surahs and stores bookmarks`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AlNoorDatabase.getInstance(context)
        val dao = db.quranDao()
        dao.insertSurahs(SurahSeedData.allSurahs)

        val surahs = dao.getAllSurahsOnce()
        assertEquals(114, surahs.size)
        assertEquals(6236, surahs.sumOf { it.versesCount })

        dao.insertBookmark(
            BookmarkEntity(
                pageNumber = 1,
                surahId = 1,
                surahNameArabic = "الفاتحة",
                verseKey = "1:1"
            )
        )
        val bookmarks = dao.observeBookmarks().first()
        assertTrue(bookmarks.any { it.pageNumber == 1 && it.surahId == 1 })
    }
}
