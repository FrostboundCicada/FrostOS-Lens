package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import com.ujizin.sample.feature.camera.model.CameraFilter

/**
 * 滤镜覆盖层 — 使用 BlendMode 在预览上叠加颜色实现滤镜效果
 * 叠加在相机预览上方，通过不同颜色和混合模式创建视觉滤镜
 */
@Composable
fun FilterOverlay(
  modifier: Modifier = Modifier,
  filter: CameraFilter,
) {
  if (filter == CameraFilter.None) return

  val (color, blendMode, alpha) = when (filter) {
    CameraFilter.None -> Triple(Color.Transparent, BlendMode.SrcOver, 0f)
    CameraFilter.Mono -> Triple(Color.Gray, BlendMode.Saturation, 0.7f)
    CameraFilter.Sepia -> Triple(Color(0xFF8B5E3C), BlendMode.Multiply, 0.4f)
    CameraFilter.Cool -> Triple(Color(0xFF4A90D9), BlendMode.Color, 0.25f)
    CameraFilter.Warm -> Triple(Color(0xFFFF8C42), BlendMode.Color, 0.25f)
    CameraFilter.Vivid -> Triple(Color(0xFF00CC66), BlendMode.Color, 0.15f)
    CameraFilter.Fade -> Triple(Color.White, BlendMode.SrcOver, 0.2f)
    CameraFilter.Film -> Triple(Color(0xFF2A1F1A), BlendMode.Multiply, 0.3f)
    CameraFilter.Negative -> Triple(Color.White, BlendMode.Difference, 1f)
  }

  if (alpha <= 0f) return

  Canvas(modifier = modifier.fillMaxSize()) {
    drawRect(
      color = color.copy(alpha = alpha),
      blendMode = blendMode,
    )
  }
}
