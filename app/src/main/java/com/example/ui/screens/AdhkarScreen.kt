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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AppStrings
import com.example.data.local.AdhkarSeedData
import com.example.data.local.DhikrCategory
import com.example.ui.components.toArabicIndicDigits
import com.example.ui.theme.AmiriFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    strings: AppStrings,
    onOpenMushafPage: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val counts = remember { mutableStateMapOf<String, Int>() }
    val haptic = LocalHapticFeedback.current

    val categories = listOf(
        DhikrCategory.MORNING to strings.morningAdhkar,
        DhikrCategory.EVENING to strings.eveningAdhkar,
        DhikrCategory.POST_PRAYER to strings.postPrayerAdhkar
    )

    val activeItems = remember(selectedTab) {
        val cat = categories[selectedTab].first
        AdhkarSeedData.allAdhkar.filter { it.category == cat }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("adhkar_screen")
    ) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            categories.forEachIndexed { idx, (_, label) ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, maxLines = 1) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activeItems, key = { it.id }) { item ->
                val current = counts[item.id] ?: 0
                val remaining = (item.targetCount - current).coerceAtLeast(0)
                val completed = remaining == 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (remaining > 0) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                counts[item.id] = current + 1
                            }
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (completed) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (strings.isArabic) item.titleArabic else item.titleEnglish,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = if (strings.isArabic) {
                                        "${remaining.toArabicIndicDigits()} / ${item.targetCount.toArabicIndicDigits()}"
                                    } else {
                                        "$remaining / ${item.targetCount}"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (completed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.textArabic,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = AmiriFontFamily,
                                fontSize = 21.sp,
                                lineHeight = 36.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!strings.isArabic) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.translationEnglish,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (item.linkedVerseKeys.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { onOpenMushafPage(42) }) {
                                Text(if (strings.isArabic) "فتح الآية في المصحف" else "Open in Mushaf")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${strings.sourceLabel}: ${if (strings.isArabic) item.sourceArabic else item.sourceEnglish}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
