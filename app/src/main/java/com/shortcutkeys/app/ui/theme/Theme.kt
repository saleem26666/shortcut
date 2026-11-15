package com.shortcutkeys.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F4C81),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E6F5),
    onPrimaryContainer = Color(0xFF06243F),
    secondary = Color(0xFF0E7C7B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F1F0),
    background = Color(0xFFF6F8FB),
    onBackground = Color(0xFF121820),
    surface = Color.White,
    onSurface = Color(0xFF121820),
    surfaceVariant = Color(0xFFE8EEF5),
    outline = Color(0xFFB7C3D1)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8EC2F4),
    onPrimary = Color(0xFF06243F),
    primaryContainer = Color(0xFF163A5F),
    onPrimaryContainer = Color(0xFFD6E6F5),
    secondary = Color(0xFF7FDEDC),
    onSecondary = Color(0xFF003332),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE8EEF5),
    surface = Color(0xFF171C22),
    onSurface = Color(0xFFE8EEF5),
    surfaceVariant = Color(0xFF243040),
    outline = Color(0xFF4D5D70)
)

@Composable
fun ShortcutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
