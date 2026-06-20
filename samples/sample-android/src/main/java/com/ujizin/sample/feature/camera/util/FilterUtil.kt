package com.ujizin.sample.feature.camera.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.ujizin.sample.feature.camera.model.CameraFilter
import java.io.ByteArrayOutputStream

/**
 * 滤镜工具 — 将 ColorMatrix 滤镜应用到图片
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
}
