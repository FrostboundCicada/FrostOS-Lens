package com.ujizin.sample.feature.camera.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.ujizin.sample.feature.camera.model.CameraFilter
import java.io.ByteArrayOutputStream
import kotlin.math.abs

/**
 * 图片处理工具 — 滤镜应用 + 比例裁剪
 */
object FilterUtil {

  /**
   * 将 JPEG 字节数据解码为 Bitmap，应用滤镜后返回新的 JPEG 字节
   */
  fun applyFilter(jpegBytes: ByteArray, filter: CameraFilter): ByteArray {
    val matrix = filter.matrix ?: return jpegBytes
    val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
      ?: return jpegBytes

    val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      colorFilter = ColorMatrixColorFilter(matrix)
    }
    canvas.drawBitmap(result, 0f, 0f, paint)

    val output = ByteArrayOutputStream()
    result.compress(Bitmap.CompressFormat.JPEG, 95, output)
    if (result != bitmap) bitmap.recycle()
    result.recycle()
    return output.toByteArray()
  }

  /**
   * 将图片裁剪到目标宽高比（居中裁剪）
   *
   * @param jpegBytes 原始 JPEG 数据
   * @param targetRatio 目标宽高比（width / height），如 1.0=正方形, 1.333=4:3, 1.778=16:9
   * @return 裁剪后的 JPEG 数据
   */
  fun cropToAspectRatio(jpegBytes: ByteArray, targetRatio: Float): ByteArray {
    if (targetRatio <= 0f) return jpegBytes
    val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
      ?: return jpegBytes

    val w = bitmap.width
    val h = bitmap.height
    val currentRatio = w.toFloat() / h.toFloat()

    // 比例已接近目标，无需裁剪
    if (abs(currentRatio - targetRatio) < 0.01f) {
      val output = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
      bitmap.recycle()
      return output.toByteArray()
    }

    val cropped: Bitmap = if (currentRatio > targetRatio) {
      // 太宽，左右裁剪
      val newW = (h * targetRatio).toInt().coerceAtLeast(1)
      val x = (w - newW) / 2
      Bitmap.createBitmap(bitmap, x, 0, newW, h)
    } else {
      // 太高，上下裁剪
      val newH = (w / targetRatio).toInt().coerceAtLeast(1)
      val y = (h - newH) / 2
      Bitmap.createBitmap(bitmap, 0, y, w, newH)
    }

    val output = ByteArrayOutputStream()
    cropped.compress(Bitmap.CompressFormat.JPEG, 95, output)
    if (cropped != bitmap) bitmap.recycle()
    cropped.recycle()
    return output.toByteArray()
  }
}
