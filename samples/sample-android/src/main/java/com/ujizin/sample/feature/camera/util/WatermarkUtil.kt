package com.ujizin.sample.feature.camera.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import com.ujizin.sample.feature.camera.model.CameraParams
import com.ujizin.sample.feature.camera.model.WatermarkConfig
import com.ujizin.sample.feature.camera.model.WatermarkPosition
import com.ujizin.sample.feature.camera.model.WatermarkStyle
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 水印合成工具 — 将水印绘制到 Bitmap 上
 *
 * 支持三种样式：Minimal / Card / FilmStrip
 * 支持内容：标题、签名、日期、位置、拍摄参数
 */
object WatermarkUtil {

  /**
   * 将 JPEG 字节数据解码为 Bitmap，绘制水印后返回新的 JPEG 字节
   *
   * @param jpegBytes 原始 JPEG 数据
   * @param config 水印配置
   * @param cameraParams 拍摄参数（从 EXIF 或相机状态获取）
   */
  fun applyWatermark(
    jpegBytes: ByteArray,
    config: WatermarkConfig,
    cameraParams: CameraParams = CameraParams(),
  ): ByteArray {
    if (!config.enabled) return jpegBytes

    val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
      ?: return jpegBytes

    // 从 EXIF 读取拍摄参数
    val enrichedParams = enrichParamsFromExif(jpegBytes, cameraParams)

    val watermarked = when (config.style) {
      WatermarkStyle.Minimal -> drawMinimalStyle(bitmap, config, enrichedParams)
      WatermarkStyle.Card -> drawCardStyle(bitmap, config, enrichedParams)
      WatermarkStyle.FilmStrip -> drawFilmStripStyle(bitmap, config, enrichedParams)
    }

    val output = ByteArrayOutputStream()
    watermarked.compress(Bitmap.CompressFormat.JPEG, 95, output)
    if (watermarked != bitmap) bitmap.recycle()
    return output.toByteArray()
  }

  /**
   * 从 EXIF 数据补充拍摄参数
   */
  private fun enrichParamsFromExif(jpegBytes: ByteArray, existing: CameraParams): CameraParams {
    return try {
      val exif = ExifInterface(ByteArrayInputStream(jpegBytes))
      CameraParams(
        zoomRatio = existing.zoomRatio,
        exposureCompensation = existing.exposureCompensation,
        iso = existing.iso.takeIf { it > 0 }
          ?: exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0),
        exposureTime = existing.exposureTime.takeIf { it > 0 }
          ?: exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.toLongOrNull() ?: 0L,
        focalLength = existing.focalLength.takeIf { it > 0 }
          ?: exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0).toFloat(),
        fNumber = existing.fNumber.takeIf { it > 0 }
          ?: exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, 0.0).toFloat(),
        deviceModel = existing.deviceModel.ifBlank {
          Build.MODEL ?: ""
        },
      )
    } catch (e: Exception) {
      existing
    }
  }

  /**
   * 构建水印内容行
   */
  private fun buildContentLines(config: WatermarkConfig, params: CameraParams): List<Pair<String, Boolean>> {
    // Pair: (text, isTitle) — isTitle=true 用粗体大字
    val lines = mutableListOf<Pair<String, Boolean>>()
    if (config.text.isNotBlank()) lines.add(config.text to true)
    if (config.signature.isNotBlank()) lines.add("by ${config.signature}" to false)
    if (config.showDate) {
      lines.add(SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()) to false)
    }
    if (config.showCameraParams) {
      params.toParamLines().forEach { lines.add(it to false) }
    }
    if (config.showLocation) lines.add("📍 定位中..." to false)
    return lines
  }

  // ==================== Minimal 样式 ====================

  private fun drawMinimalStyle(bitmap: Bitmap, config: WatermarkConfig, params: CameraParams): Bitmap {
    val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val w = result.width.toFloat()
    val h = result.height.toFloat()
    val scale = (w / 1080f).coerceIn(0.6f, 2.5f)
    val fontSize = config.fontSize * scale * 1.6f
    val padding = w * 0.03f

    val lines = buildContentLines(config, params)
    if (lines.isEmpty()) return result

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = fontSize
      setShadowLayer(fontSize * 0.1f, 1f, 1f, Color.argb(180, 0, 0, 0))
    }

    // 计算位置
    val lineHeight = fontSize * 1.3f
    val totalHeight = lineHeight * lines.size
    val (startX, startY) = when (config.position) {
      WatermarkPosition.BottomLeft -> padding to (h - padding - totalHeight + fontSize)
      WatermarkPosition.BottomRight -> padding to (h - padding - totalHeight + fontSize)
      WatermarkPosition.TopLeft -> padding to (padding + fontSize)
      WatermarkPosition.TopRight -> padding to (padding + fontSize)
    }

    lines.forEachIndexed { index, (text, isTitle) ->
      paint.typeface = if (isTitle) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
      paint.textSize = if (isTitle) fontSize else fontSize * 0.7f
      paint.alpha = if (isTitle) 255 else 200
      canvas.drawText(text, startX, startY + index * lineHeight, paint)
    }

    return result
  }

  // ==================== Card 样式 ====================

  private fun drawCardStyle(bitmap: Bitmap, config: WatermarkConfig, params: CameraParams): Bitmap {
    val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val w = result.width.toFloat()
    val h = result.height.toFloat()
    val scale = (w / 1080f).coerceIn(0.6f, 2.5f)
    val baseFontSize = config.fontSize * scale * 1.8f
    val padding = w * 0.035f
    val accentBarWidth = baseFontSize * 0.1f

    val lines = buildContentLines(config, params)
    if (lines.isEmpty()) return result

    // 标题画笔
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = baseFontSize
      typeface = Typeface.DEFAULT_BOLD
      isFakeBoldText = true
      setShadowLayer(baseFontSize * 0.12f, 1f, 1f, Color.argb(180, 0, 0, 0))
    }
    // 副标题画笔
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.argb(220, 255, 255, 255)
      textSize = baseFontSize * 0.6f
      typeface = Typeface.DEFAULT
      setShadowLayer(baseFontSize * 0.08f, 1f, 1f, Color.argb(160, 0, 0, 0))
    }

    // 测量
    var maxTextWidth = 0f
    val lineHeights = mutableListOf<Float>()
    lines.forEach { (text, isTitle) ->
      val paint = if (isTitle) titlePaint else subPaint
      maxTextWidth = maxOf(maxTextWidth, paint.measureText(text))
      val fm = paint.fontMetrics
      lineHeights.add(fm.descent - fm.ascent)
    }

    val textBlockHeight = lineHeights.sum()
    val bgPaddingH = baseFontSize * 0.4f
    val bgPaddingV = baseFontSize * 0.25f
    val bgWidth = maxTextWidth + bgPaddingH * 2 + accentBarWidth + baseFontSize * 0.2f
    val bgHeight = textBlockHeight + bgPaddingV * 2

    val (bgLeft, bgTop) = when (config.position) {
      WatermarkPosition.BottomLeft -> padding to (h - padding - bgHeight)
      WatermarkPosition.BottomRight -> (w - padding - bgWidth) to (h - padding - bgHeight)
      WatermarkPosition.TopLeft -> padding to padding
      WatermarkPosition.TopRight -> (w - padding - bgWidth) to padding
    }

    // 渐变背景
    val bgPaint = Paint().apply { isAntiAlias = true }
    bgPaint.shader = LinearGradient(
      bgLeft, bgTop, bgLeft, bgTop + bgHeight,
      intArrayOf(Color.argb(100, 0, 0, 0), Color.argb(150, 0, 0, 0)),
      null, Shader.TileMode.CLAMP,
    )
    val radius = baseFontSize * 0.15f
    canvas.drawRoundRect(bgLeft, bgTop, bgLeft + bgWidth, bgTop + bgHeight, radius, radius, bgPaint)

    // 左侧强调条
    val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.argb(220, 255, 255, 255)
    }
    canvas.drawRoundRect(
      bgLeft + baseFontSize * 0.15f, bgTop + bgPaddingV,
      bgLeft + baseFontSize * 0.15f + accentBarWidth, bgTop + bgHeight - bgPaddingV,
      accentBarWidth / 2f, accentBarWidth / 2f, accentPaint,
    )

    // 绘制文字
    val textStartX = bgLeft + bgPaddingH + accentBarWidth + baseFontSize * 0.2f
    var textY = bgTop + bgPaddingV - titlePaint.fontMetrics.ascent
    lines.forEachIndexed { index, (text, isTitle) ->
      val paint = if (isTitle) titlePaint else subPaint
      canvas.drawText(text, textStartX, textY, paint)
      textY += lineHeights[index]
    }

    return result
  }

  // ==================== FilmStrip 样式 ====================

  private fun drawFilmStripStyle(bitmap: Bitmap, config: WatermarkConfig, params: CameraParams): Bitmap {
    val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val w = result.width.toFloat()
    val h = result.height.toFloat()
    val scale = (w / 1080f).coerceIn(0.6f, 2.5f)
    val baseFontSize = config.fontSize * scale * 1.6f
    val padding = w * 0.04f

    val lines = buildContentLines(config, params)
    if (lines.isEmpty()) return result

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = baseFontSize
      typeface = Typeface.DEFAULT_BOLD
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.argb(200, 255, 255, 255)
      textSize = baseFontSize * 0.55f
      typeface = Typeface.DEFAULT
    }

    // 测量
    var maxTextWidth = 0f
    val lineHeights = mutableListOf<Float>()
    lines.forEach { (text, isTitle) ->
      val paint = if (isTitle) titlePaint else subPaint
      maxTextWidth = maxOf(maxTextWidth, paint.measureText(text))
      val fm = paint.fontMetrics
      lineHeights.add(fm.descent - fm.ascent)
    }

    val textBlockHeight = lineHeights.sum()
    val bgPaddingH = w * 0.04f
    val bgPaddingV = baseFontSize * 0.4f
    val bgHeight = textBlockHeight + bgPaddingV * 2

    // 底部全宽渐变条
    val bgTop = h - bgHeight - padding * 0.5f
    val bgPaint = Paint().apply { isAntiAlias = true }
    bgPaint.shader = LinearGradient(
      0f, bgTop, 0f, bgTop + bgHeight,
      intArrayOf(Color.argb(0, 0, 0, 0), Color.argb(160, 0, 0, 0)),
      null, Shader.TileMode.CLAMP,
    )
    canvas.drawRect(0f, bgTop, w, bgTop + bgHeight, bgPaint)

    // 左对齐文字
    var textY = bgTop + bgPaddingV - titlePaint.fontMetrics.ascent
    lines.forEachIndexed { index, (text, isTitle) ->
      val paint = if (isTitle) titlePaint else subPaint
      canvas.drawText(text, bgPaddingH, textY, paint)
      textY += lineHeights[index]
    }

    return result
  }
}
