package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppStrings
import com.example.core.PrayerCalculationMethod
import com.example.core.PrayerTimesCalculator
import com.example.data.local.SurahEntity
import com.example.data.local.UserPreferences
import com.example.data.repository.OfflineDownloadProgress
import com.example.ui.components.toArabicIndicDigits
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import java.util.Calendar
import java.util.TimeZone

private data class DashboardTile(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String,
    val accentColor: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    strings: AppStrings,
    prefs: UserPreferences,
    surahs: List<SurahEntity>,
    cachedPagesCount: Int,
    downloadProgress: OfflineDownloadProgress,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenMushaf: (page: Int) -> Unit,
    onOpenIndex: () -> Unit,
    onOpenAudio: () -> Unit,
    onOpenTafsir: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenMemorization: () -> Unit,
    onOpenAdhkar: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenPrayerTimes: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartOfflineDownload: () -> Unit
) {
    val lastSurah = remember(surahs, prefs.lastReadSurahId) {
        surahs.find { it.id == prefs.lastReadSurahId }
    }

    val nextPrayerInfo = remember(prefs.selectedCityId, prefs.selectedCalculationMethodId, prefs.customLatitude, prefs.customLongitude, strings.isArabic) {
        val city = if (prefs.selectedCityId == "custom" && prefs.customLatitude != null && prefs.customLongitude != null) {
            PrayerTimesCalculator.presetCities.first().copy(
                id = "custom",
                nameArabic = prefs.customCityName ?: "موقعي",
                nameEnglish = prefs.customCityName ?: "My Location",
                latitude = prefs.customLatitude,
                longitude = prefs.customLongitude,
                timeZoneId = TimeZone.getDefault().id
            )
        } else {
            PrayerTimesCalculator.presetCities.find { it.id == prefs.selectedCityId }
                ?: PrayerTimesCalculator.presetCities.first()
        }
        val method = PrayerCalculationMethod.entries.find { it.id == prefs.selectedCalculationMethodId }
            ?: PrayerCalculationMethod.UMM_AL_QURA
        val times = PrayerTimesCalculator.calculate(city, method)

        val nowCal = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId))
        val nowMin = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
        val entries = listOf(
            strings.fajr to Pair(times.fajr, times.fajrMinutes),
            strings.dhuhr to Pair(times.dhuhr, times.dhuhrMinutes),
            strings.asr to Pair(times.asr, times.asrMinutes),
            strings.maghrib to Pair(times.maghrib, times.maghribMinutes),
            strings.isha to Pair(times.isha, times.ishaMinutes)
        )
        val upcoming = entries.firstOrNull { it.second.second > nowMin } ?: entries.first()
        Triple(if (strings.isArabic) city.nameArabic else city.nameEnglish, upcoming.first, upcoming.second.first)
    }

    val tiles = listOf(
        DashboardTile(
            title = strings.openMushaf,
            subtitle = if (strings.isArabic) "٦٠٤ صفحة بالرسم العثماني" else "604 Uthmani Pages",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            testTag = "tile_mushaf",
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = { onOpenMushaf(prefs.lastReadPage) }
        ),
        DashboardTile(
            title = strings.quranIndex,
            subtitle = strings.surahsCountLabel,
            icon = Icons.Filled.FormatListNumbered,
            testTag = "tile_index",
            accentColor = GoldAccent,
            onClick = onOpenIndex
        ),
        DashboardTile(
            title = strings.audio,
            subtitle = if (strings.isArabic) "تلاوات خاشعة وتحميل" else "Reciters & Offline Audio",
            icon = Icons.Filled.Headphones,
            testTag = "tile_audio",
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = onOpenAudio
        ),
        DashboardTile(
            title = strings.tafsirTitle,
            subtitle = if (strings.isArabic) "الميسر وابن كثير والسعدي" else "Ibn Kathir & Al-Muyassar",
            icon = Icons.Filled.LibraryBooks,
            testTag = "tile_tafsir",
            accentColor = GoldAccent,
            onClick = onOpenTafsir
        ),
        DashboardTile(
            title = strings.searchQuran,
            subtitle = if (strings.isArabic) "بحث فوري في الآيات" else "Search Verses & Words",
            icon = Icons.Filled.Search,
            testTag = "tile_search",
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = onOpenSearch
        ),
        DashboardTile(
            title = strings.memorizationTitle,
            subtitle = if (strings.isArabic) "متابعة الحفظ والمراجعة" else "Hifz Tracker & Reminders",
            icon = Icons.Filled.Psychology,
            testTag = "tile_memorization",
            accentColor = GoldAccent,
            onClick = onOpenMemorization
        ),
        DashboardTile(
            title = strings.adhkarTitle,
            subtitle = if (strings.isArabic) "أذكار الصباح والمساء" else "Morning & Evening Adhkar",
            icon = Icons.Filled.Spa,
            testTag = "tile_adhkar",
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = onOpenAdhkar
        ),
        DashboardTile(
            title = strings.tasbihTitle,
            subtitle = if (strings.isArabic) "عدّاد ذكي وحفظ الذكر" else "Digital Dhikr Counter",
            icon = Icons.Filled.RadioButtonChecked,
            testTag = "tile_tasbih",
            accentColor = GoldAccent,
            onClick = onOpenTasbih
        ),
        DashboardTile(
            title = strings.prayerTimesTitle,
            subtitle = "${nextPrayerInfo.second} • ${nextPrayerInfo.third}",
            icon = Icons.Filled.AccessTime,
            testTag = "tile_prayer",
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = onOpenPrayerTimes
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = if (strings.isArabic) AmiriFontFamily else MaterialTheme.typography.headlineMedium.fontFamily
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.slogan,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.testTag("home_language_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = strings.languageSetting,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("home_theme_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Brightness4,
                            contentDescription = strings.themeSetting,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Supervising Org Ribbon
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, GoldBorder.copy(alpha = 0.35f))
            ) {
                Text(
                    text = strings.supervisingOrg,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }

        // Hero Continue Reading Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMushaf(prefs.lastReadPage) }
                    .testTag("hero_continue_reading"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_mushaf_1795345560783),
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.18f),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.80f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GoldAccent.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.continueReading,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = GoldAccent
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "${nextPrayerInfo.first}: ${nextPrayerInfo.second} ${nextPrayerInfo.third}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val surahTitle = if (strings.isArabic) {
                            "سورة ${lastSurah?.nameArabic ?: "الفاتحة"}"
                        } else {
                            "Surah ${lastSurah?.nameSimple ?: "Al-Fatihah"}"
                        }
                        Text(
                            text = surahTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = if (strings.isArabic) AmiriFontFamily else MaterialTheme.typography.headlineMedium.fontFamily
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val pageText = if (strings.isArabic) {
                            "صفحة ${prefs.lastReadPage.toArabicIndicDigits()} • ${strings.juzLabel} ${(lastSurah?.juzStart ?: 1).toArabicIndicDigits()}"
                        } else {
                            "Page ${prefs.lastReadPage} • Juz ${lastSurah?.juzStart ?: 1}"
                        }
                        Text(
                            text = pageText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onOpenMushaf(prefs.lastReadPage) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = Color(0xFF1A1400)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("hero_open_mushaf_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.openMushaf,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // Offline Sync Status Strip
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSettings)
                    .testTag("offline_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (cachedPagesCount >= 604) {
                                        strings.offlineReady
                                    } else if (strings.isArabic) {
                                        "المحفوظ دون إنترنت: $cachedPagesCount / ٦٠٤ صفحة"
                                    } else {
                                        "Cached Offline: $cachedPagesCount / 604 pages"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (downloadProgress.isDownloading) {
                                    Text(
                                        text = if (strings.isArabic) {
                                            "جارٍ تنزيل السور (${downloadProgress.completedSurahs}/${downloadProgress.totalSurahs})"
                                        } else {
                                            "Downloading Surahs (${downloadProgress.completedSurahs}/${downloadProgress.totalSurahs})"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (!downloadProgress.isDownloading && cachedPagesCount < 604) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .clickable(onClick = onStartOfflineDownload)
                                    .testTag("quick_download_quran_btn")
                            ) {
                                Text(
                                    text = if (strings.isArabic) "تنزيل الكل" else "Sync All",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    if (downloadProgress.isDownloading || cachedPagesCount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val progress = if (downloadProgress.isDownloading) {
                            downloadProgress.completedSurahs / 114f
                        } else {
                            (cachedPagesCount / 604f).coerceIn(0f, 1f)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Feature Dashboard Grid (2-column rows)
        val rows = tiles.chunked(2)
        items(rows.size) { rowIndex ->
            val rowTiles = rows[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowTiles.forEach { tile ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = tile.onClick)
                            .testTag(tile.testTag),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(tile.accentColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tile.icon,
                                    contentDescription = tile.title,
                                    tint = tile.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = tile.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tile.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (rowTiles.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
