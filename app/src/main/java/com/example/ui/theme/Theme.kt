package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkThemeGlobal) {
        darkColorScheme(
            primary = ArcheryGold,
            secondary = ArcheryRed,
            tertiary = ArcheryBlue,
            background = CharcoalBg,
            surface = CharcoalSurface,
            onPrimary = CharcoalBg,
            onSecondary = CrispWhite,
            onTertiary = CrispWhite,
            onBackground = CrispWhite,
            onSurface = CrispWhite,
            surfaceVariant = CharcoalCard,
            onSurfaceVariant = CoolGray
        )
    } else {
        lightColorScheme(
            primary = ArcheryGold,
            secondary = ArcheryRed,
            tertiary = ArcheryBlue,
            background = CharcoalBg,
            surface = CharcoalSurface,
            onPrimary = CharcoalBg,
            onSecondary = CrispWhite,
            onTertiary = CrispWhite,
            onBackground = CrispWhite,
            onSurface = CrispWhite,
            surfaceVariant = CharcoalCard,
            onSurfaceVariant = CoolGray
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
