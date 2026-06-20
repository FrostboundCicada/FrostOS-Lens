package com.ujizin.sample.feature.camera.components

import android.graphics.RenderEffect
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.ujizin.sample.feature.camera.model.CameraFilter

/**
 * 滤镜 modifier — 使用 RenderEffect 将 ColorMatrix 直接应用到预览 View
 *
 * API 31+ (Android 12) 使用 RenderEffect.createColorFilterEffect
 * 低版本不应用滤镜（RenderEffect 不可用）
 */
fun Modifier.applyFilter(filter: CameraFilter): Modifier {
  if (filter == CameraFilter.None) return this
  val matrix = filter.matrix ?: return this

  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    this.graphicsLayer {
      renderEffect = RenderEffect.createColorFilterEffect(
        android.graphics.ColorMatrixColorFilter(matrix),
      ).asComposeRenderEffect()
    }
  } else {
    this
  }
}
