package com.kaminga.reader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KamingaColorScheme = darkColorScheme(
    primary = Purple,
    onPrimary = Night,
    secondary = Cyan,
    onSecondary = Night,
    background = Night,
    onBackground = Color.White,
    surface = Panel,
    onSurface = Color.White,
    surfaceVariant = PanelHigh,
    onSurfaceVariant = Muted,
    outline = OutlineColor,
    error = ErrorRed
)

@Composable
fun KamingaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = KamingaColorScheme, content = content)
}
