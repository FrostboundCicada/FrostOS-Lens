package com.ujizin.sample.feature.camera.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import com.ujizin.sample.feature.camera.model.WatermarkConfig
import com.ujizin.sample.feature.camera.model.WatermarkPosition
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 水印合成工具 — 将水印绘制到 Bitmap 上
 *
 * 设计风格：底部渐变背景 + 左侧强调条 + 白色文字
 */
object WatermarkUtil {

  /**
   * 将 JPEG 字节数据解码为 Bitmap，绘制水印后返回新的 JPEG 字节
   */
  fun applyWatermark(jpegBytes: ByteArray, config: WatermarkConfig): ByteArray {
    if (!config.enabled) return jpegBytes

    val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
      ?: return jpegBytes

    val watermarked = applyWatermarkToBitmap(bitmap, config)

    val output = ByteArrayOutputStream()
    watermarked.compress(Bitmap.CompressFormat.JPEG, 95, output)
    if (watermarked != bitmap) {
      bitmap.recycle()
    }
    return output.toByteArray()
  }

  /**
   * 在 Bitmap 上绘制水印
   */
  private fun applyWatermarkToBitmap(bitmap: Bitmap, config: WatermarkConfig): Bitmap {
    // 创建可变副本
    val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)

    val w = result.width.toFloat()
    val h = result.height.toFloat()

    // 根据图片尺寸缩放字体
    val scale = (w / 1080f).coerceIn(0.6f, 2.5f)
    val baseFontSize = config.fontSize * scale * 1.8f
    val padding = w * 0.035f
    val accentBarWidth = baseFontSize * 0.12f

    val lines = buildList {
      if (config.text.isNotBlank()) add(config.text)
      if (config.showDate) {
        add(SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()))
      }
      if (config.showLocation) add("📍 定位中...")
    }
    if (lines.isEmpty()) return result

    // 标题画笔（第一行）
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = baseFontSize
      typeface = Typeface.DEFAULT_BOLD
      isFakeBoldText = true
      setShadowLayer(baseFontSize * 0.12f, 1f, 1f, Color.argb(180, 0, 0, 0))
    }

    // 副标题画笔（后续行）
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.argb(220, 255, 255, 255)
      textSize = baseFontSize * 0.65f
      typeface = Typeface.DEFAULT
      setShadowLayer(baseFontSize * 0.08f, 1f, 1f, Color.argb(160, 0, 0, 0))
    }

    // 测量文字宽高
    var maxTextWidth = 0f
    val lineHeights = mutableListOf<Float>()
    lines.forEachIndexed { index, line ->
      val paint = if (index == 0) titlePaint else subPaint
      val textWidth = paint.measureText(line)
      maxTextWidth = maxOf(maxTextWidth, textWidth)
      val fm = paint.fontMetrics
      lineHeights.add(fm.descent - fm.ascent)
    }

    val textBlockHeight = lineHeights.sum()
    val bgPaddingH = baseFontSize * 0.4f
    val bgPaddingV = baseFontSize * 0.25f
    val bgWidth = maxTextWidth + bgPaddingH * 2 + accentBarWidth + baseFontSize * 0.2f
    val bgHeight = textBlockHeight + bgPaddingV * 2

    // 计算水印位置
    val (bgLeft, bgTop) = when (config.position) {
      WatermarkPosition.BottomLeft -> padding to (h - padding - bgHeight)
      WatermarkPosition.BottomRight -> (w - padding - bgWidth) to (h - padding - bgHeight)
      WatermarkPosition.TopLeft -> padding to padding
      WatermarkPosition.TopRight -> (w - padding - bgWidth) to padding
    }

    // 绘制半透明渐变背景
    val bgPaint = Paint().apply {
      isAntiAlias = true
    }
    val gradient = LinearGradient(
      bgLeft, bgTop, bgLeft, bgTop + bgHeight,
      intArrayOf(
        Color.argb(100, 0, 0, 0),
        Color.argb(140, 0, 0, 0),
      ),
      null, Shader.TileMode.CLAMP,
    )
    bgPaint.shader = gradient

    val radius = baseFontSize * 0.15f
    canvas.drawRoundRect(
      bgLeft, bgTop, bgLeft + bgWidth, bgTop + bgHeight,
      radius, radius, bgPaint,
    )

    // 左侧强调条（accent bar）
    val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.argb(200, 255, 255, 255)
    }
    canvas.drawRoundRect(
      bgLeft + baseFontSize * 0.15f,
      bgTop + bgPaddingV,
      bgLeft + baseFontSize * 0.15f + accentBarWidth,
      bgTop + bgHeight - bgPaddingV,
      accentBarWidth / 2f, accentBarWidth / 2f,
      accentPaint,
    )

    // 绘制文字
    val textStartX = bgLeft + bgPaddingH + accentBarWidth + baseFontSize * 0.2f
    var textY = bgTop + bgPaddingV - titlePaint.fontMetrics.ascent
    lines.forEachIndexed { index, line ->
      val paint = if (index == 0) titlePaint else subPaint
      canvas.drawText(line, textStartX, textY, paint)
      textY += lineHeights[index]
    }

    return result
  }
}
