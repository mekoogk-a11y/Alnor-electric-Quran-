package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AppStrings
import com.example.data.local.AdhkarSeedData
import com.example.data.local.TasbihSessionEntity
import com.example.ui.components.toArabicIndicDigits
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccent

@Composable
fun TasbihScreen(
    strings: AppStrings,
    sessions: List<TasbihSessionEntity>,
    onIncrement: (TasbihSessionEntity) -> Unit,
    onDecrement: (TasbihSessionEntity) -> Unit,
    onReset: (TasbihSessionEntity) -> Unit,
    onUpdateTarget: (TasbihSessionEntity, Int) -> Unit
) {
    val activeList = sessions.ifEmpty { AdhkarSeedData.defaultTasbihPresets }
    var selectedId by remember { mutableStateOf(activeList.first().id) }
    val active = activeList.find { it.id == selectedId } ?: activeList.first()
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("tasbih_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(activeList, key = { it.id }) { session ->
                FilterChip(
                    selected = session.id == active.id,
                    onClick = { selectedId = session.id },
                    label = {
                        Text(
                            text = if (strings.isArabic) session.dhikrArabic else session.dhikrEnglish,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = active.dhikrArabic,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = AmiriFontFamily,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                if (!strings.isArabic) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = active.dhikrEnglish,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${strings.totalCount}: ${if (strings.isArabic) active.totalLifetimeCount.toArabicIndicDigits() else active.totalLifetimeCount}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Large Tactile Counter Ring
        val progress = if (active.targetCount > 0) {
            (active.currentCount % active.targetCount).toFloat() / active.targetCount.toFloat()
        } else 1f
        val ringProgress = if (active.currentCount > 0 && active.targetCount > 0 && active.currentCount % active.targetCount == 0) {
            1f
        } else progress

        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onIncrement(active)
                }
                .testTag("tasbih_tap_button"),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { ringProgress },
                modifier = Modifier.size(220.dp),
                strokeWidth = 10.dp,
                color = GoldAccent,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            Surface(
                modifier = Modifier.size(188.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (strings.isArabic) active.currentCount.toArabicIndicDigits() else active.currentCount.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.testTag("tasbih_count_text")
                    )
                    if (active.targetCount > 0) {
                        Text(
                            text = "/ ${if (strings.isArabic) active.targetCount.toArabicIndicDigits() else active.targetCount}",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Target Selector Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(strings.targetLabel, style = MaterialTheme.typography.labelLarge)
            listOf(33, 99, 100, 0).forEach { target ->
                FilterChip(
                    selected = active.targetCount == target,
                    onClick = { onUpdateTarget(active, target) },
                    label = { Text(if (target == 0) "∞" else target.toString()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onDecrement(active) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tasbih_undo_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.undo)
            }
            OutlinedButton(
                onClick = { onReset(active) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tasbih_reset_btn")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.reset)
            }
        }
    }
}
