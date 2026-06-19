package com.ujizin.sample

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * 使用 Material You 动态颜色的相机主题
 * - Android 12+ (API 31+): 从壁纸提取动态颜色 (莫奈取色)
 * - 旧版 Android: 回退到静态颜色方案
 */
@Composable
fun CamposerTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val colorScheme = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> darkColorScheme(
      primary = Color(0xFF90CAF9),
      secondary = Color(0xFF80DEEA),
      tertiary = Color(0xFFA5D6A7),
      background = Color(0xFF121212),
      surface = Color(0xFF1E1E1E),
    )
    else -> lightColorScheme(
      primary = Color(0xFF1976D2),
      secondary = Color(0xFF00BCD4),
      tertiary = Color(0xFF4CAF50),
      background = Color(0xFFF5F5F5),
      surface = Color(0xFFFFFFFF),
    )
  }

  MaterialTheme(
    colorScheme = colorScheme,
    content = content,
  )
}
