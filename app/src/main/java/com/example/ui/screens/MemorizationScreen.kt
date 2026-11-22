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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.data.local.MemorizationEntity
import com.example.data.local.SurahEntity
import com.example.ui.components.IslamicStarRosette
import com.example.ui.components.toArabicIndicDigits
import com.example.ui.theme.GoldAccent

@Composable
fun MemorizationScreen(
    strings: AppStrings,
    surahs: List<SurahEntity>,
    memorizationList: List<MemorizationEntity>,
    reminderEnabled: Boolean,
    onToggleReminder: (Boolean) -> Unit,
    onUpdateMemorization: (MemorizationEntity) -> Unit,
    onOpenSurahPage: (Int) -> Unit
) {
    val memoMap = remember(memorizationList) {
        memorizationList.associateBy { it.surahId }
    }
    val totalMemorized = remember(memorizationList) {
        memorizationList.sumOf { it.memorizedVersesCount }
    }
    val memorizedSurahsCount = remember(memorizationList) {
        memorizationList.count { it.status == "MEMORIZED" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("memorization_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = strings.memorizationTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (strings.isArabic) {
                            "المحفوظ: ${totalMemorized.toArabicIndicDigits()} / ٦٢٣٦ آية (${memorizedSurahsCount.toArabicIndicDigits()} سورة)"
                        } else {
                            "Memorized: $totalMemorized / 6,236 verses ($memorizedSurahsCount surahs)"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (totalMemorized / 6236f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = GoldAccent,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(strings.dailyReminder, style = MaterialTheme.typography.titleMedium)
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = onToggleReminder,
                        modifier = Modifier.testTag("memo_reminder_switch")
                    )
                }
            }
        }

        items(surahs, key = { it.id }) { surah ->
            val current = memoMap[surah.id] ?: MemorizationEntity(
                surahId = surah.id,
                memorizedVersesCount = 0,
                totalVerses = surah.versesCount,
                status = "NOT_STARTED"
            )
            val isDone = current.memorizedVersesCount >= surah.versesCount

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IslamicStarRosette(
                            numberText = if (strings.isArabic) surah.id.toArabicIndicDigits() else surah.id.toString(),
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenSurahPage(surah.startPage) }
                        ) {
                            Text(
                                text = if (strings.isArabic) "سورة ${surah.nameArabic}" else surah.nameSimple,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${current.memorizedVersesCount} / ${surah.versesCount} ${strings.ayahsLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                val next = (current.memorizedVersesCount - 1).coerceAtLeast(0)
                                onUpdateMemorization(
                                    current.copy(
                                        memorizedVersesCount = next,
                                        status = if (next == 0) "NOT_STARTED" else "IN_PROGRESS",
                                        lastReviewedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease memorized")
                        }
                        IconButton(
                            onClick = {
                                val next = (current.memorizedVersesCount + 1).coerceAtMost(surah.versesCount)
                                onUpdateMemorization(
                                    current.copy(
                                        memorizedVersesCount = next,
                                        status = if (next >= surah.versesCount) "MEMORIZED" else "IN_PROGRESS",
                                        lastReviewedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase memorized")
                        }
                        IconButton(
                            onClick = {
                                val next = if (isDone) 0 else surah.versesCount
                                onUpdateMemorization(
                                    current.copy(
                                        memorizedVersesCount = next,
                                        status = if (next == surah.versesCount) "MEMORIZED" else "NOT_STARTED",
                                        lastReviewedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Toggle full surah memorized",
                                tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (surah.versesCount == 0) 0f
                            else (current.memorizedVersesCount.toFloat() / surah.versesCount).coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}
