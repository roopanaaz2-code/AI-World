package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CreativeColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = MidnightNavyDark,
    primaryContainer = ElectricBlueDeep,
    onPrimaryContainer = TextWhite,
    secondary = MetallicGold,
    onSecondary = MidnightNavyDark,
    secondaryContainer = MetallicGoldRich,
    onSecondaryContainer = MidnightNavyDark,
    tertiary = MetallicGoldSoft,
    onTertiary = MidnightNavyDark,
    background = MidnightNavyDark,
    onBackground = TextWhite,
    surface = MidnightNavySurface,
    onSurface = TextWhite,
    surfaceVariant = MidnightNavyElevated,
    onSurfaceVariant = TextSilver,
    outline = GlassBorderBlue,
    outlineVariant = GlassBorderGold,
    error = ErrorRed,
    onError = TextWhite
)

@Composable
fun AiCreativeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CreativeColorScheme,
        typography = Typography,
        content = content
    )
}
