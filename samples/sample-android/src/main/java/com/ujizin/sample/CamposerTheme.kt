package com.ujizin.sample

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.toArgb

@Composable
fun CamposerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val monet = MonetCompat.getInstance()
    val view = LocalView.current
    val context = view.context

    // 获取 Monet 动态颜色
    val monetPrimary = monet.getMonetColors().accent1[700]?.toArgb() ?: Color(0xFF1976D2).toArgb()
    val monetSecondary = monet.getMonetColors().accent2[500]?.toArgb() ?: Color(0xFF00BCD4).toArgb()
    val monetTertiary = monet.getMonetColors().accent3[500]?.toArgb() ?: Color(0xFF4CAF50).toArgb()
    val monetBackground = monet.getMonetColors().neutral1[if (darkTheme) 900 else 50]?.toArgb()
        ?: (if (darkTheme) Color(0xFF121212) else Color(0xFFF5F5F5)).toArgb()
    val monetSurface = monet.getMonetColors().neutral1[if (darkTheme) 800 else 0]?.toArgb()
        ?: (if (darkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)).toArgb()

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(monet.getMonetColors().accent1[200]?.toArgb() ?: monetPrimary),
            secondary = Color(monet.getMonetColors().accent2[200]?.toArgb() ?: monetSecondary),
            tertiary = Color(monet.getMonetColors().accent3[200]?.toArgb() ?: monetTertiary),
            background = Color(monetBackground),
            surface = Color(monetSurface),
        )
    } else {
        lightColorScheme(
            primary = Color(monetPrimary),
            secondary = Color(monetSecondary),
            tertiary = Color(monetTertiary),
            background = Color(monetBackground),
            surface = Color(monetSurface),
        )
    }

    // 设置状态栏颜色
    SideEffect {
        val window = (context as? Activity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
