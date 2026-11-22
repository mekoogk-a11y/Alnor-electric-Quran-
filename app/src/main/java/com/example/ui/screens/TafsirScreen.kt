package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AppStrings
import com.example.data.local.SurahEntity
import com.example.data.local.UserPreferences
import com.example.data.repository.QuranRepository
import com.example.ui.components.toArabicIndicDigits
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.MushafPaperCream
import com.example.ui.theme.NightMushafPaper
import com.example.ui.viewmodel.ExploreViewModel

@Composable
fun TafsirScreen(
    strings: AppStrings,
    prefs: UserPreferences,
    surahs: List<SurahEntity>,
    initialVerseKey: String?,
    exploreViewModel: ExploreViewModel
) {
    val tafsirResources by exploreViewModel.tafsirResources.collectAsStateWithLifecycle()
    val tafsirResult by exploreViewModel.tafsirResult.collectAsStateWithLifecycle()
    val isLoading by exploreViewModel.isTafsirLoading.collectAsStateWithLifecycle()
    val error by exploreViewModel.tafsirError.collectAsStateWithLifecycle()

    val initialParts = initialVerseKey?.split(":")
    var selectedSurahId by remember(initialVerseKey) {
        mutableIntStateOf(initialParts?.getOrNull(0)?.toIntOrNull()?.coerceIn(1, 114) ?: 1)
    }
    var selectedAyahNum by remember(initialVerseKey) {
        mutableIntStateOf(initialParts?.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(1) ?: 1)
    }
    var selectedTafsirId by remember(prefs.selectedTafsirId) {
        mutableIntStateOf(prefs.selectedTafsirId)
    }

    val activeSurah = remember(surahs, selectedSurahId) {
        surahs.find { it.id == selectedSurahId }
    }
    val maxAyah = activeSurah?.versesCount ?: 7

    LaunchedEffect(strings.isArabic) {
        exploreViewModel.loadTafsirResources(if (strings.isArabic) "ar" else "en")
    }

    LaunchedEffect(selectedTafsirId, selectedSurahId, selectedAyahNum) {
        val safeAyah = selectedAyahNum.coerceIn(1, maxAyah)
        if (safeAyah != selectedAyahNum) selectedAyahNum = safeAyah
        exploreViewModel.loadTafsirForVerse(selectedTafsirId, "$selectedSurahId:$safeAyah")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tafsir_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = strings.selectTafsir,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tafsirResources, key = { it.id }) { res ->
                    val label = res.translatedName?.name?.takeIf { !strings.isArabic && it.isNotBlank() } ?: res.name
                    FilterChip(
                        selected = res.id == selectedTafsirId,
                        onClick = { selectedTafsirId = res.id },
                        label = { Text(label, maxLines = 1) }
                    )
                }
            }
        }

        // Surah & Verse Selector Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (selectedSurahId > 1) {
                                selectedSurahId -= 1
                                selectedAyahNum = 1
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Prev Surah")
                        }
                        Text(
                            text = if (strings.isArabic) {
                                "سورة ${activeSurah?.nameArabic ?: "الفاتحة"}"
                            } else {
                                "Surah ${activeSurah?.nameSimple ?: "Al-Fatihah"}"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = {
                            if (selectedSurahId < 114) {
                                selectedSurahId += 1
                                selectedAyahNum = 1
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Next Surah")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (selectedAyahNum > 1) selectedAyahNum -= 1
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Prev Ayah")
                        }
                        Text(
                            text = if (strings.isArabic) {
                                "الآية ${selectedAyahNum.toArabicIndicDigits()} من ${maxAyah.toArabicIndicDigits()}"
                            } else {
                                "Verse $selectedAyahNum of $maxAyah"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = {
                            if (selectedAyahNum < maxAyah) selectedAyahNum += 1
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Next Ayah")
                        }
                    }
                }
            }
        }

        // Tafsir Content
        item {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null && tafsirResult == null -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error ?: "Network unavailable",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = {
                                exploreViewModel.loadTafsirForVerse(selectedTafsirId, "$selectedSurahId:$selectedAyahNum")
                            }) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.retry)
                            }
                        }
                    }
                }

                tafsirResult != null -> {
                    val res = tafsirResult!!
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (prefs.isDarkTheme) NightMushafPaper else MushafPaperCream
                        ),
                        border = BorderStroke(1.2.dp, GoldBorder.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${res.verseTextUthmani} ﴿${selectedAyahNum.toArabicIndicDigits()}﴾",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = AmiriQuranFontFamily,
                                    fontSize = 23.sp,
                                    lineHeight = 42.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (res.resourceName.isNotBlank()) {
                                Text(
                                    text = res.resourceName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            val cleanText = res.tafsirTextHtml
                                .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                                .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
                                .replace(Regex("<[^>]*>"), "")
                                .replace("&quot;", "\"")
                                .replace("&amp;", "&")
                                .trim()

                            Text(
                                text = cleanText,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
