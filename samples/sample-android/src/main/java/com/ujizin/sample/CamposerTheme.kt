package com.ujizin.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.kieronquinn.monetcompat.core.MonetCompat

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
fun CamposerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val monet = remember { MonetCompat.getInstance() }

    val monetPrimary = monet.getAccentColor(context)
    val monetSecondary = monet.getSecondaryColor(context)
    val monetBackground = monet.getBackgroundColor(context)

    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = Color(monetPrimary),
            secondary = Color(monetSecondary),
            background = Color(monetBackground),
            surface = Color(monetBackground),
        )
    } else {
        LightColorScheme.copy(
            primary = Color(monetPrimary),
            secondary = Color(monetSecondary),
            background = Color(monetBackground),
            surface = Color(monetBackground),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

@Deprecated("Use CamposerTheme instead", ReplaceWith("CamposerTheme(content = content)"))
@Composable
fun MonetTheme(content: @Composable () -> Unit) = CamposerTheme(content = content)
