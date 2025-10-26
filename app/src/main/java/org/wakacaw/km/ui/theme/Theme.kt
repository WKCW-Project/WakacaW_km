package org.wakacaw.km.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WakacawDark = darkColorScheme(
    primary = Color(0xFF00BCD4),
    secondary = Color(0xFF26C6DA),
    background = Color(0xFF0F1114),
    surface = Color(0xFF1C1E22),
)

@Composable
fun WakacawTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WakacawDark,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
