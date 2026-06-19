package com.ujizin.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.kieronquinn.monetcompat.MonetCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF80DEEA),
    tertiary = Color(0xFFA5D6A7),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    secondary = Color(0xFF00BCD4),
    tertiary = Color(0xFF4CAF50),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
)

@Composable
fun MonetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val monet = remember { MonetCompat.getInstance() }
    val monetColors = remember { monet.monetColors }

    val primaryColor = monetColors?.primary?.let { Color(it) } ?: if (darkTheme) DarkColorScheme.primary else LightColorScheme.primary
    val secondaryColor = monetColors?.secondary?.let { Color(it) } ?: if (darkTheme) DarkColorScheme.secondary else LightColorScheme.secondary
    val tertiaryColor = monetColors?.tertiary?.let { Color(it) } ?: if (darkTheme) DarkColorScheme.tertiary else LightColorScheme.tertiary
    val backgroundColor = monetColors?.background?.let { Color(it) } ?: if (darkTheme) DarkColorScheme.background else LightColorScheme.background
    val surfaceColor = monetColors?.surface?.let { Color(it) } ?: if (darkTheme) DarkColorScheme.surface else LightColorScheme.surface

    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = tertiaryColor,
            background = backgroundColor,
            surface = surfaceColor,
        )
    } else {
        LightColorScheme.copy(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = tertiaryColor,
            background = backgroundColor,
            surface = surfaceColor,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
