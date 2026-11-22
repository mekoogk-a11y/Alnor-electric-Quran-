package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.AppStrings
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldDarkHeading
import com.example.ui.theme.GoldBright
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    strings: AppStrings,
    onFinished: () -> Unit
) {
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(700))
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
            .clickable(onClick = onFinished)
            .testTag("splash_screen")
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_art_1795345560183),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.32f),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            EmeraldDarkHeading.copy(alpha = 0.55f),
                            EmeraldDeep.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 28.dp, vertical = 32.dp)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(132.dp)
                    .scale(scale.value),
                shape = CircleShape,
                color = EmeraldDarkHeading,
                shadowElevation = 12.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_alnoor_emblem_1795345559429),
                    contentDescription = strings.appTitle,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "مصحف النور الإلكتروني",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = GoldBright,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Al-Noor Digital Quran",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE8E2D0),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.45f),
                thickness = 1.dp,
                color = GoldBright.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Text(
                    text = strings.supervisingOrg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF3EFE4),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = strings.slogan,
                style = MaterialTheme.typography.titleMedium,
                color = GoldBright.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )
        }
    }
}
