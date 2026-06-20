package com.ujizin.sample.feature.camera.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.ujizin.sample.feature.camera.model.WatermarkConfig
import com.ujizin.sample.feature.camera.model.WatermarkPosition
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 水印合成工具 — 将水印绘制到 Bitmap 上
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
    val baseFontSize = config.fontSize * (w / 1080f).coerceIn(0.6f, 2f)
    val padding = w * 0.03f

    val lines = buildList {
      if (config.text.isNotBlank()) add(config.text)
      if (config.showDate) {
        add(SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()))
      }
      if (config.showLocation) add("📍 定位中...")
    }
    if (lines.isEmpty()) return result

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = baseFontSize
      typeface = Typeface.DEFAULT
      isFakeBoldText = true
      setShadowLayer(baseFontSize * 0.15f, 1f, 1f, Color.BLACK)
    }

    // 测量文字宽高
    var maxTextWidth = 0f
    var totalHeight = 0f
    val lineHeights = mutableListOf<Float>()
    lines.forEachIndexed { index, line ->
      val textWidth = textPaint.measureText(line)
      maxTextWidth = maxOf(maxTextWidth, textWidth)
      val fm = textPaint.fontMetrics
      val lineHeight = fm.descent - fm.ascent
      lineHeights.add(lineHeight)
      totalHeight += lineHeight
    }

    val bgPaddingH = baseFontSize * 0.5f
    val bgPaddingV = baseFontSize * 0.3f
    val bgWidth = maxTextWidth + bgPaddingH * 2
    val bgHeight = totalHeight + bgPaddingV * 2

    // 计算水印位置
    val (bgLeft, bgTop) = when (config.position) {
      WatermarkPosition.BottomLeft -> padding to (h - padding - bgHeight)
      WatermarkPosition.BottomRight -> (w - padding - bgWidth) to (h - padding - bgHeight)
      WatermarkPosition.TopLeft -> padding to padding
      WatermarkPosition.TopRight -> (w - padding - bgWidth) to padding
    }

    // 绘制半透明背景
    val bgPaint = Paint().apply {
      color = Color.BLACK
      alpha = 90
    }
    val radius = baseFontSize * 0.3f
    canvas.drawRoundRect(
      bgLeft, bgTop, bgLeft + bgWidth, bgTop + bgHeight,
      radius, radius, bgPaint,
    )

    // 绘制文字
    var textY = bgTop + bgPaddingV - textPaint.fontMetrics.ascent
    lines.forEachIndexed { index, line ->
      textPaint.typeface = if (index == 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
      canvas.drawText(line, bgLeft + bgPaddingH, textY, textPaint)
      textY += lineHeights[index]
    }

    return result
  }
}
