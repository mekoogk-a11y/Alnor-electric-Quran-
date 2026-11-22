package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AppStrings
import com.example.data.local.BookmarkEntity
import com.example.data.local.SurahEntity
import com.example.data.local.SurahSeedData
import com.example.ui.components.IslamicStarRosette
import com.example.ui.components.toArabicIndicDigits
import com.example.ui.theme.AmiriFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahIndexScreen(
    strings: AppStrings,
    surahs: List<SurahEntity>,
    bookmarks: List<BookmarkEntity>,
    onOpenPage: (Int) -> Unit,
    onDeleteBookmark: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSurahs = remember(surahs, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) surahs
        else surahs.filter {
            it.nameArabic.contains(q) ||
                it.nameSimple.contains(q, ignoreCase = true) ||
                it.nameEnglish.contains(q, ignoreCase = true) ||
                it.id.toString() == q
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("surah_index_screen")
    ) {
        val tabTitles = if (strings.isArabic) {
            listOf("السور (١١٤)", "الأجزاء (٣٠)", "العلامات")
        } else {
            listOf("Surahs (114)", "Juz (30)", "Bookmarks")
        }

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, maxLines = 1) },
                    modifier = Modifier.testTag("index_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(strings.searchSurahHint) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("surah_search_input")
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSurahs, key = { it.id }) { surah ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPage(surah.startPage) }
                                .testTag("surah_item_${surah.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IslamicStarRosette(
                                    numberText = if (strings.isArabic) surah.id.toArabicIndicDigits() else surah.id.toString(),
                                    size = 42.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (strings.isArabic) "سورة ${surah.nameArabic}" else "${surah.nameSimple} — ${surah.nameArabic}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val revLabel = if (surah.revelationPlace == "makkah") strings.makki else strings.madani
                                    Text(
                                        text = if (strings.isArabic) {
                                            "$revLabel • ${surah.versesCount.toArabicIndicDigits()} آية • ص ${surah.startPage.toArabicIndicDigits()}"
                                        } else {
                                            "${surah.nameEnglish} • $revLabel • ${surah.versesCount} verses • p.${surah.startPage}"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${strings.pageLabel} ${if (strings.isArabic) surah.startPage.toArabicIndicDigits() else surah.startPage}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SurahSeedData.juzStartPages, key = { it.first }) { (juzNumber, startPage) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPage(startPage) }
                                .testTag("juz_item_$juzNumber"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IslamicStarRosette(
                                        numberText = if (strings.isArabic) juzNumber.toArabicIndicDigits() else juzNumber.toString(),
                                        size = 40.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${strings.juzLabel} ${if (strings.isArabic) juzNumber.toArabicIndicDigits() else juzNumber}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Text(
                                    text = "${strings.pageLabel} ${if (strings.isArabic) startPage.toArabicIndicDigits() else startPage}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                if (bookmarks.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            modifier = Modifier.height(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (strings.isArabic) "لا توجد علامات مرجعية محفوظة بعد" else "No bookmarks saved yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bookmarks, key = { it.id }) { bm ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenPage(bm.pageNumber) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "سورة ${bm.surahNameArabic}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "${strings.pageLabel} ${bm.pageNumber} ${bm.verseKey?.let { "• $it" } ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeleteBookmark(bm.id) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Remove bookmark",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
