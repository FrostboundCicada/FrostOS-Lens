package com.ujizin.sample.feature.camera.model

/**
 * 水印位置
 */
enum class WatermarkPosition {
  BottomLeft,
  BottomRight,
  TopLeft,
  TopRight,
}

/**
 * 水印配置
 */
data class WatermarkConfig(
  val enabled: Boolean = false,
  val text: String = "FrostOS Lens",
  val showDate: Boolean = true,
  val showLocation: Boolean = false,
  val position: WatermarkPosition = WatermarkPosition.BottomRight,
  val fontSize: Int = 14,
)
