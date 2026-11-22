package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AudioPlaybackState
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder

fun Int.toArabicIndicDigits(): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

@Composable
fun IslamicStarRosette(
    numberText: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    strokeColor: Color = GoldBorder,
    fillColor: Color = GoldAccent.copy(alpha = 0.14f),
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val side = this.size.minDimension * 0.66f
            val topLeft = Offset((this.size.width - side) / 2f, (this.size.height - side) / 2f)
            // First square
            drawRoundRect(
                color = fillColor,
                topLeft = topLeft,
                size = Size(side, side),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = strokeColor,
                topLeft = topLeft,
                size = Size(side, side),
                cornerRadius = CornerRadius(3f, 3f),
                style = Stroke(width = 2.2f)
            )
            // Rotated 45 degrees square for 8-pointed Rub al-Hizb star
            rotate(degrees = 45f, pivot = center) {
                drawRoundRect(
                    color = fillColor,
                    topLeft = topLeft,
                    size = Size(side, side),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawRoundRect(
                    color = strokeColor,
                    topLeft = topLeft,
                    size = Size(side, side),
                    cornerRadius = CornerRadius(3f, 3f),
                    style = Stroke(width = 2.2f)
                )
            }
        }
        Text(
            text = numberText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun MushafOrnamentalBorderBox(
    modifier: Modifier = Modifier,
    paperColor: Color,
    outerBorderColor: Color = GoldBorder,
    innerAccentColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(paperColor)
            .border(1.5.dp, outerBorderColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(5.dp)
            .border(0.8.dp, innerAccentColor, RoundedCornerShape(10.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val inset = 14f
            val r = 6f
            val corners = listOf(
                Offset(inset, inset),
                Offset(size.width - inset, inset),
                Offset(inset, size.height - inset),
                Offset(size.width - inset, size.height - inset)
            )
            corners.forEach { pt ->
                drawCircle(
                    color = outerBorderColor.copy(alpha = 0.55f),
                    radius = r,
                    center = pt,
                    style = Stroke(width = 1.6f)
                )
                drawCircle(
                    color = outerBorderColor.copy(alpha = 0.35f),
                    radius = 2.5f,
                    center = pt
                )
            }
        }
        content()
    }
}

@Composable
fun SurahHeaderCartouche(
    surahNameArabic: String,
    revelationLabel: String,
    versesCountLabel: String,
    showBismillah: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            border = BorderStroke(1.2.dp, GoldBorder.copy(alpha = 0.75f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = revelationLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "سُورَةُ $surahNameArabic",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 21.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = versesCountLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        if (showBismillah) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = AmiriQuranFontFamily,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MiniAudioPlayerBar(
    audioState: AudioPlaybackState,
    onTogglePlayPause: () -> Unit,
    onOpenAudioScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!audioState.isPlaying && audioState.durationMs == 0L && !audioState.isBuffering) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onOpenAudioScreen)
            .testTag("mini_audio_bar"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(GoldAccent.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = GoldAccent
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "سورة ${audioState.currentSurahNameArabic}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = audioState.currentReciterName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.testTag("mini_audio_play_pause")
            ) {
                Icon(
                    imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (audioState.isPlaying) "Pause" else "Play",
                    tint = GoldAccent
                )
            }
        }
    }
}
