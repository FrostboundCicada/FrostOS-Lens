package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect

/**
 * 3x3 九宫格辅助线
 */
@Composable
fun GridOverlay(
  modifier: Modifier = Modifier,
  visible: Boolean,
) {
  if (!visible) return
  Canvas(modifier = modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    val lineColor = Color.White.copy(alpha = 0.25f)
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

    // 两条水平线 (1/3, 2/3)
    for (i in 1..2) {
      val y = h * i / 3f
      drawLine(
        color = lineColor,
        start = Offset(0f, y),
        end = Offset(w, y),
        strokeWidth = 1f,
        pathEffect = dash,
      )
    }
    // 两条垂直线 (1/3, 2/3)
    for (i in 1..2) {
      val x = w * i / 3f
      drawLine(
        color = lineColor,
        start = Offset(x, 0f),
        end = Offset(x, h),
        strokeWidth = 1f,
        pathEffect = dash,
      )
    }
  }
}