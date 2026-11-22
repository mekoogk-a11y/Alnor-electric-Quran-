package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val AlNoorLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoftContainer,
    onPrimaryContainer = EmeraldDarkHeading,
    secondary = GoldAccent,
    onSecondary = Color(0xFF1D1602),
    secondaryContainer = GoldSoftContainer,
    onSecondaryContainer = Color(0xFF4A3804),
    tertiary = EmeraldDarkHeading,
    onTertiary = Color.White,
    background = WarmIvoryBackground,
    onBackground = InkPrimary,
    surface = CardWhiteSurface,
    onSurface = InkPrimary,
    surfaceVariant = MushafPaperCream,
    onSurfaceVariant = InkSecondary,
    outline = SoftBorderNeutral,
    outlineVariant = GoldBorder.copy(alpha = 0.35f)
)

private val AlNoorDarkColorScheme = darkColorScheme(
    primary = NightEmeraldAccent,
    onPrimary = Color(0xFF032116),
    primaryContainer = NightEmeraldCard,
    onPrimaryContainer = NightInkPrimary,
    secondary = NightGoldAccent,
    onSecondary = Color(0xFF261C00),
    secondaryContainer = Color(0xFF33290B),
    onSecondaryContainer = GoldSoftContainer,
    tertiary = GoldBright,
    onTertiary = Color(0xFF211A00),
    background = NightEmeraldBg,
    onBackground = NightInkPrimary,
    surface = NightEmeraldSurface,
    onSurface = NightInkPrimary,
    surfaceVariant = NightEmeraldCard,
    onSurfaceVariant = NightInkSecondary,
    outline = NightBorder,
    outlineVariant = NightGoldAccent.copy(alpha = 0.3f)
)

val AlNoorShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AlNoorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isArabic: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AlNoorDarkColorScheme else AlNoorLightColorScheme
    val typography = createAlNoorTypography(isArabic = isArabic)
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = AlNoorShapes,
            content = content
        )
    }
}
