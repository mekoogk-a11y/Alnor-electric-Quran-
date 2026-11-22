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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AppStrings
import com.example.data.local.DownloadedAudioEntity
import com.example.data.local.SurahEntity
import com.example.data.local.UserPreferences
import com.example.service.AudioPlaybackState
import com.example.service.AudioRepeatMode
import com.example.ui.components.IslamicStarRosette
import com.example.ui.components.toArabicIndicDigits
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.ExploreViewModel

@Composable
fun AudioScreen(
    strings: AppStrings,
    prefs: UserPreferences,
    surahs: List<SurahEntity>,
    downloadedAudio: List<DownloadedAudioEntity>,
    audioState: AudioPlaybackState,
    exploreViewModel: ExploreViewModel,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onChangeSpeed: (Float) -> Unit,
    onCycleRepeat: () -> Unit
) {
    val reciters by exploreViewModel.reciters.collectAsStateWithLifecycle()
    val downloadingKeys by exploreViewModel.downloadingAudioKeys.collectAsStateWithLifecycle()
    val audioError by exploreViewModel.audioActionError.collectAsStateWithLifecycle()

    LaunchedEffect(strings.isArabic) {
        exploreViewModel.loadReciters(if (strings.isArabic) "ar" else "en")
    }

    val activeReciter = remember(reciters, prefs.selectedReciterId) {
        reciters.find { it.id == prefs.selectedReciterId } ?: reciters.firstOrNull()
    }
    val activeReciterName = activeReciter?.translatedName?.name?.takeIf { strings.isArabic && it.isNotBlank() }
        ?: activeReciter?.reciterName ?: "مشاري راشد العفاسي"

    val downloadedKeysSet = remember(downloadedAudio) {
        downloadedAudio.map { it.key }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("audio_screen")
    ) {
        // Active Player Console Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "سورة ${audioState.currentSurahNameArabic}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = AmiriFontFamily),
                            color = Color.White
                        )
                        Text(
                            text = audioState.currentReciterName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = GoldAccent,
                        modifier = Modifier
                            .size(52.dp)
                            .clickable(onClick = onTogglePlayPause)
                            .testTag("audio_main_play_pause")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (audioState.isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF1A1400),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (audioState.isPlaying) strings.pause else strings.resume,
                                    tint = Color(0xFF1A1400),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val duration = audioState.durationMs.coerceAtLeast(1L)
                val pos = audioState.positionMs.coerceIn(0L, duration)
                Slider(
                    value = pos.toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = GoldAccent,
                        activeTrackColor = GoldAccent,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatDurationMs(audioState.positionMs)} / ${formatDurationMs(audioState.durationMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Repeat Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clickable(onClick = onCycleRepeat)
                                .testTag("audio_repeat_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (audioState.repeatMode == AudioRepeatMode.REPEAT_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                                    contentDescription = null,
                                    tint = if (audioState.repeatMode != AudioRepeatMode.NONE) GoldAccent else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val repLabel = when (audioState.repeatMode) {
                                    AudioRepeatMode.NONE -> strings.noRepeat
                                    AudioRepeatMode.REPEAT_ONE -> strings.repeatSurah
                                    AudioRepeatMode.REPEAT_ALL -> if (strings.isArabic) "تكرار الكل" else "Repeat All"
                                }
                                Text(repLabel, style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }

                        // Speed Button
                        val nextSpeed = when (audioState.playbackSpeed) {
                            0.75f -> 1.0f
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            else -> 0.75f
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clickable { onChangeSpeed(nextSpeed) }
                                .testTag("audio_speed_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${audioState.playbackSpeed}x", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }

                val errMsg = audioError ?: audioState.errorMessage
                if (!errMsg.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFD5D5)
                    )
                }
            }
        }

        // Reciter Selector Strip
        Text(
            text = strings.selectReciter,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reciters, key = { it.id }) { reciter ->
                val name = reciter.translatedName?.name?.takeIf { strings.isArabic && it.isNotBlank() }
                    ?: reciter.reciterName
                FilterChip(
                    selected = reciter.id == prefs.selectedReciterId,
                    onClick = { exploreViewModel.selectReciter(reciter.id) },
                    label = { Text(name, maxLines = 1) }
                )
            }
        }

        // Surah Audio Playlist
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(surahs, key = { it.id }) { surah ->
                val audioKey = "${prefs.selectedReciterId}:${surah.id}"
                val isDownloaded = downloadedKeysSet.contains(audioKey)
                val isDownloading = downloadingKeys.contains(audioKey)
                val isCurrentPlaying = audioState.currentSurahId == surah.id && audioState.isPlaying

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            exploreViewModel.playSurah(
                                reciterId = prefs.selectedReciterId,
                                reciterName = activeReciterName,
                                surahId = surah.id,
                                surahNameArabic = surah.nameArabic,
                                surahNameEnglish = surah.nameSimple
                            )
                        }
                        .testTag("audio_surah_${surah.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentPlaying) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IslamicStarRosette(
                            numberText = if (strings.isArabic) surah.id.toArabicIndicDigits() else surah.id.toString(),
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (strings.isArabic) "سورة ${surah.nameArabic}" else "${surah.nameSimple} — ${surah.nameArabic}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (isDownloaded) strings.downloadedOffline else activeReciterName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isDownloading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else if (isDownloaded) {
                            IconButton(onClick = { exploreViewModel.deleteChapterAudio(prefs.selectedReciterId, surah.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = strings.downloadedOffline,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            IconButton(onClick = { exploreViewModel.downloadChapterAudio(prefs.selectedReciterId, surah.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = strings.downloadAudio,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if (isCurrentPlaying) {
                                    onTogglePlayPause()
                                } else {
                                    exploreViewModel.playSurah(
                                        reciterId = prefs.selectedReciterId,
                                        reciterName = activeReciterName,
                                        surahId = surah.id,
                                        surahNameArabic = surah.nameArabic,
                                        surahNameEnglish = surah.nameSimple
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isCurrentPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = strings.playSurah,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
