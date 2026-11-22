package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.core.QuranVerificationReport
import com.example.data.local.UserPreferences
import com.example.data.repository.OfflineDownloadProgress
import com.example.ui.components.toArabicIndicDigits
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    strings: AppStrings,
    prefs: UserPreferences,
    cachedVersesCount: Int,
    cachedPagesCount: Int,
    downloadProgress: OfflineDownloadProgress,
    verificationReport: QuranVerificationReport?,
    onSetArabic: (Boolean) -> Unit,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetFontSize: (Float) -> Unit,
    onSetFlipMode: (Boolean) -> Unit,
    onStartFullDownload: () -> Unit,
    onRunVerification: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Language & Theme
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.languageSetting, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = prefs.isArabic,
                            onClick = { onSetArabic(true) },
                            label = { Text("العربية") },
                            modifier = Modifier.testTag("lang_ar_chip")
                        )
                        FilterChip(
                            selected = !prefs.isArabic,
                            onClick = { onSetArabic(false) },
                            label = { Text("English") },
                            modifier = Modifier.testTag("lang_en_chip")
                        )
                    }

                    Text(strings.themeSetting, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = !prefs.isDarkTheme,
                            onClick = { onSetDarkTheme(false) },
                            label = { Text(strings.lightTheme) }
                        )
                        FilterChip(
                            selected = prefs.isDarkTheme,
                            onClick = { onSetDarkTheme(true) },
                            label = { Text(strings.darkTheme) }
                        )
                    }

                    Text(
                        text = "${strings.fontSizeSetting}: ${prefs.mushafFontSizeSp.roundToInt()} sp",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = prefs.mushafFontSizeSp,
                        onValueChange = onSetFontSize,
                        valueRange = 18f..38f
                    )

                    Text(strings.readingModeSetting, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = prefs.isFlipMode,
                            onClick = { onSetFlipMode(true) },
                            label = { Text(strings.flipMode) }
                        )
                        FilterChip(
                            selected = !prefs.isFlipMode,
                            onClick = { onSetFlipMode(false) },
                            label = { Text(strings.scrollMode) }
                        )
                    }
                }
            }
        }

        // Offline Quran Download Manager
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.offlineManagerTitle, style = MaterialTheme.typography.titleMedium)
                    }

                    Text(
                        text = if (strings.isArabic) {
                            "الصفحات المحفوظة: ${cachedPagesCount.toArabicIndicDigits()} / ٦٠٤ • الآيات المحفوظة: ${cachedVersesCount.toArabicIndicDigits()} / ٦٢٣٦"
                        } else {
                            "Cached Pages: $cachedPagesCount / 604 • Cached Verses: $cachedVersesCount / 6,236"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val progress = if (downloadProgress.isDownloading) {
                        downloadProgress.completedSurahs / 114f
                    } else {
                        (cachedPagesCount / 604f).coerceIn(0f, 1f)
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                    )

                    if (downloadProgress.errorMessage != null) {
                        Text(
                            text = downloadProgress.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = onStartFullDownload,
                        enabled = !downloadProgress.isDownloading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_download_full_quran_btn")
                    ) {
                        Text(
                            if (downloadProgress.isDownloading) {
                                "${strings.downloading} (${downloadProgress.completedSurahs}/114)"
                            } else {
                                strings.downloadFullQuran
                            }
                        )
                    }
                }
            }
        }

        // Quranic Accuracy & Verification Panel
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.verificationTitle, style = MaterialTheme.typography.titleMedium)
                    }

                    Text(
                        text = strings.sourceAttribution,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    verificationReport?.let { rep ->
                        val statusText = if (rep.isValid) {
                            if (strings.isArabic) "الحالة: سليم ومطابق للرسم العثماني ✓" else "Status: Verified & Intact ✓"
                        } else {
                            if (strings.isArabic) "الحالة: توجد ملاحظات تحتاج مراجعة" else "Status: Issues Detected"
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (rep.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (strings.isArabic) {
                                "السور: ${rep.totalChaptersCount}/١١٤ • السور المكتملة محلياً: ${rep.chaptersWithCompleteVerses}/١١٤ • الآيات المفحوصة: ${rep.cachedVersesCount} • الأخطاء: ${rep.corruptedEntriesCount}"
                            } else {
                                "Chapters: ${rep.totalChaptersCount}/114 • Complete Offline Surahs: ${rep.chaptersWithCompleteVerses}/114 • Verified Verses: ${rep.cachedVersesCount} • Corrupted: ${rep.corruptedEntriesCount}"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        rep.issues.forEach { issue ->
                            Text("• $issue", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    OutlinedButton(
                        onClick = onRunVerification,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_verify_btn")
                    ) {
                        Text(strings.runVerification)
                    }
                }
            }
        }
    }
}
