package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * 3x3 九宫格辅助线
 * 按图片比例居中绘制，线条更明显
 *
 * @param aspectRatio 图片宽高比（如 4f/3f, 1f, 16f/9f）
 * @param isFullScreen 是否全屏模式（全屏时网格填满整个预览区域）
 */
@Composable
fun GridOverlay(
  modifier: Modifier = Modifier,
  visible: Boolean,
  aspectRatio: Float = 0f,
  isFullScreen: Boolean = false,
) {
  if (!visible) return

  Box(modifier = modifier.fillMaxSize()) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasW = size.width
      val canvasH = size.height

      // 计算网格绘制区域 — 按图片比例居中
      val (gridW, gridH, offsetX, offsetY) = if (isFullScreen || aspectRatio <= 0f) {
        // 全屏模式或无比例：填满整个区域
        listOf(canvasW, canvasH, 0f, 0f)
      } else {
        // 按比例居中
        val canvasRatio = canvasW / canvasH
        if (aspectRatio > canvasRatio) {
          // 图片比画布更宽 — 以宽度为准
          val w = canvasW
          val h = canvasW / aspectRatio
          listOf(w, h, 0f, (canvasH - h) / 2f)
        } else {
          // 图片比画布更高 — 以高度为准
          val h = canvasH
          val w = canvasH * aspectRatio
          listOf(w, h, (canvasW - w) / 2f, 0f)
        }
      }

      val lineColor = Color.White.copy(alpha = 0.65f)
      val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
      val strokeWidth = 2.5f

      // 两条水平线 (1/3, 2/3)
      for (i in 1..2) {
        val y = offsetY + gridH * i / 3f
        drawLine(
          color = lineColor,
          start = Offset(offsetX, y),
          end = Offset(offsetX + gridW, y),
          strokeWidth = strokeWidth,
          pathEffect = dash,
        )
      }
      // 两条垂直线 (1/3, 2/3)
      for (i in 1..2) {
        val x = offsetX + gridW * i / 3f
        drawLine(
          color = lineColor,
          start = Offset(x, offsetY),
          end = Offset(x, offsetY + gridH),
          strokeWidth = strokeWidth,
          pathEffect = dash,
        )
      }

      // 绘制边框，标示图片区域
      drawRect(
        color = Color.White.copy(alpha = 0.25f),
        topLeft = Offset(offsetX, offsetY),
        size = androidx.compose.ui.geometry.Size(gridW, gridH),
        style = Stroke(width = 1.5f),
      )
    }
  }
}
