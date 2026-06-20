package com.ujizin.sample.feature.camera.model

import androidx.compose.ui.graphics.ColorMatrix

/**
 * 相机滤镜定义
 * 使用 ColorMatrix 实现各种滤镜效果
 */
enum class CameraFilter(
  val displayName: String,
  val matrix: ColorMatrix?,
) {
  None("原图", null),
  Mono("黑白", ColorMatrix(floatArrayOf(
    0.299f, 0.587f, 0.114f, 0f, 0f,
    0.299f, 0.587f, 0.114f, 0f, 0f,
    0.299f, 0.587f, 0.114f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
  ))),
  Sepia("复古", ColorMatrix(floatArrayOf(
    0.393f, 0.769f, 0.189f, 0f, 0f,
    0.349f, 0.686f, 0.168f, 0f, 0f,
    0.272f, 0.534f, 0.131f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
  ))),
  Cool("冷色", ColorMatrix(floatArrayOf(
    0.8f, 0f, 0f, 0f, 0f,
    0f, 0.9f, 0f, 0f, 0f,
    0f, 0f, 1.2f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
  ))),
  Warm("暖色", ColorMatrix(floatArrayOf(
    1.2f, 0f, 0f, 0f, 0f,
    0f, 1.05f, 0f, 0f, 0f,
    0f, 0f, 0.8f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
  ))),
  Vivid("鲜艳", ColorMatrix(floatArrayOf(
    1.4f, 0f, 0f, 0f, -20f,
    0f, 1.4f, 0f, 0f, -20f,
    0f, 0f, 1.4f, 0f, -20f,
    0f, 0f, 0f, 1f, 0f,
  ))),
  Fade("淡雅", ColorMatrix(floatArrayOf(
    0.8f, 0f, 0f, 0f, 40f,
    0f, 0.8f, 0f, 0f, 40f,
    0f, 0f, 0.8f, 0f, 40f,
    0f, 0f, 0f, 1f, 0f,
  ))),
  Film("胶片", ColorMatrix(floatArrayOf(
    1.1f, 0.05f, 0f, 0f, -10f,
    0f, 1.05f, 0.05f, 0f, -10f,
    0.05f, 0f, 0.95f, 0f, -5f,
    0f, 0f, 0f, 1f, 0f,
  ))),
  Negative("负片", ColorMatrix(floatArrayOf(
    -1f, 0f, 0f, 0f, 255f,
    0f, -1f, 0f, 0f, 255f,
    0f, 0f, -1f, 0f, 255f,
    0f, 0f, 0f, 1f, 0f,
  ))),
}
