package com.ujizin.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.kieronquinn.monetcompat.core.MonetCompat

/**
 * 使用 MonetCompat 动态取色的相机主题
 * 从用户壁纸提取动态色 - 支持亮色模式与暗色模式
 */
@Composable
fun CamposerTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val monet = MonetCompat.getInstance()
  val mc = monet.getMonetColors()

  val colorScheme = if (darkTheme) {
    darkColorScheme(
      primary = mc.accent1[200]?.let { Color(it.color) } ?: Color(0xFFD0BCFF),
      secondary = mc.accent2[200]?.let { Color(it.color) } ?: Color(0xFFCCC2DC),
      tertiary = mc.accent3[200]?.let { Color(it.color) } ?: Color(0xFFEFB8C8),
      background = mc.neutral1[900]?.let { Color(it.color) } ?: Color(0xFF141218),
      surface = mc.neutral2[800]?.let { Color(it.color) } ?: Color(0xFF1D1B20),
      onPrimary = Color(0xFF381E72),
      onSecondary = Color(0xFF332D41),
      onTertiary = Color(0xFF492532),
      onBackground = Color.White,
      onSurface = Color.White,
    )
  } else {
    lightColorScheme(
      primary = mc.accent1[500]?.let { Color(it.color) } ?: Color(0xFF6750A4),
      secondary = mc.accent2[500]?.let { Color(it.color) } ?: Color(0xFF625B71),
      tertiary = mc.accent3[500]?.let { Color(it.color) } ?: Color(0xFF7D5260),
      background = mc.neutral1[50]?.let { Color(it.color) } ?: Color(0xFFFFFBFE),
      surface = mc.neutral2[100]?.let { Color(it.color) } ?: Color(0xFFFFFFFF),
      onPrimary = Color.White,
      onSecondary = Color.White,
      onTertiary = Color.White,
      onBackground = Color(0xFF1C1B1F),
      onSurface = Color(0xFF1C1B1F),
    )
  }

  MaterialTheme(
    colorScheme = colorScheme,
    content = content,
  )
}
