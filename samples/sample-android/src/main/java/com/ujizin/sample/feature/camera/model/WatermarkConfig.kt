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
 * 水印样式
 */
enum class WatermarkStyle {
  /** 简洁 — 单行文字 */
  Minimal,
  /** 卡片 — 渐变背景 + 强调条 */
  Card,
  /** 胶片 — 底部全宽渐变条 */
  FilmStrip,
}

/**
 * 拍摄参数快照 — 拍照时捕获
 */
data class CameraParams(
  val zoomRatio: Float = 1f,
  val exposureCompensation: Float = 0f,
  val iso: Int = 0,
  val exposureTime: Long = 0L,
  val focalLength: Float = 0f,
  val fNumber: Float = 0f,
  val deviceModel: String = "",
) {
  /**
   * 格式化曝光时间（如 1/120s）
   */
  fun exposureTimeString(): String {
    if (exposureTime <= 0) return ""
    return if (exposureTime < 1_000_000) {
      // 小于1秒，显示分数
      val denominator = (1_000_000.0 / exposureTime).toInt()
      "1/${denominator}s"
    } else {
      "${exposureTime / 1_000_000.0}s"
    }
  }

  /**
   * 格式化为可读字符串列表
   */
  fun toParamLines(): List<String> {
    val lines = mutableListOf<String>()
    val parts = mutableListOf<String>()
    if (iso > 0) parts.add("ISO $iso")
    if (exposureTime > 0) parts.add(exposureTimeString())
    if (fNumber > 0) parts.add("f/$fNumber")
    if (focalLength > 0) parts.add("${focalLength}mm")
    if (zoomRatio > 1.01f) parts.add("${zoomRatio}x")
    if (parts.isNotEmpty()) lines.add(parts.joinToString("  "))
    if (deviceModel.isNotBlank()) lines.add(deviceModel)
    return lines
  }
}

/**
 * 水印配置
 */
data class WatermarkConfig(
  val enabled: Boolean = false,
  val text: String = "FrostOS Lens",
  val signature: String = "",
  val showDate: Boolean = true,
  val showLocation: Boolean = false,
  val showCameraParams: Boolean = false,
  val style: WatermarkStyle = WatermarkStyle.Card,
  val position: WatermarkPosition = WatermarkPosition.BottomRight,
  val fontSize: Int = 14,
)
