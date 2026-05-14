package com.flowcast.demo.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF146C5B),
    secondary = Color(0xFF6B5E2E),
    tertiary = Color(0xFF8B3A4A),
    background = Color(0xFFF8FAF8),
    surface = Color(0xFFFFFFFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF70D6BF),
    secondary = Color(0xFFD7C56C),
    tertiary = Color(0xFFFFB1C1),
    background = Color(0xFF111412),
    surface = Color(0xFF1A1F1C),
)

@Composable
fun FlowCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = if (darkTheme) DarkColors else LightColors,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
