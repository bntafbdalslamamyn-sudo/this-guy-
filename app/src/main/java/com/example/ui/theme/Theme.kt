package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val BodyCompleteDarkColorScheme = darkColorScheme(
    primary = ElectricBluePrimary,
    onPrimary = PureWhiteText,
    primaryContainer = ElevatedCardSurface,
    onPrimaryContainer = NeonCyanAccent,
    secondary = NeonCyanAccent,
    onSecondary = ObsidianBlack,
    secondaryContainer = SubtleCardInner,
    onSecondaryContainer = PureWhiteText,
    tertiary = CalorieFlameGold,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = PureWhiteText,
    surface = DarkCardSurface,
    onSurface = PureWhiteText,
    surfaceVariant = ElevatedCardSurface,
    onSurfaceVariant = MutedSteelText,
    outline = GlowingBorderBlue,
    error = CrimsonAlert,
    onError = PureWhiteText
)

val BodyCompleteShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BodyCompleteTheme(
    isArabicRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val direction = if (isArabicRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        MaterialTheme(
            colorScheme = BodyCompleteDarkColorScheme,
            typography = Typography,
            shapes = BodyCompleteShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BodyCompleteTheme(isArabicRtl = true, content = content)
}
