package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.sample.feature.camera.model.WatermarkConfig
import com.ujizin.sample.feature.camera.model.WatermarkPosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 水印覆盖层 — 在预览上显示自定义水印
 */
@Composable
fun WatermarkOverlay(
  modifier: Modifier = Modifier,
  config: WatermarkConfig,
) {
  if (!config.enabled) return

  val alignment = when (config.position) {
    WatermarkPosition.BottomLeft -> Alignment.BottomStart
    WatermarkPosition.BottomRight -> Alignment.BottomEnd
    WatermarkPosition.TopLeft -> Alignment.TopStart
    WatermarkPosition.TopRight -> Alignment.TopEnd
  }

  val padding = when (config.position) {
    WatermarkPosition.BottomLeft, WatermarkPosition.BottomRight ->
      Modifier.padding(bottom = 120.dp, start = 20.dp, end = 20.dp)
    WatermarkPosition.TopLeft, WatermarkPosition.TopRight ->
      Modifier.padding(top = 60.dp, start = 20.dp, end = 20.dp)
  }

  Box(
    modifier = modifier.fillMaxSize().then(padding),
    contentAlignment = alignment,
  ) {
    val dateStr = if (config.showDate) {
      SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
    } else null

    val lines = buildList {
      if (config.text.isNotBlank()) add(config.text)
      if (dateStr != null) add(dateStr)
      if (config.showLocation) add("📍 定位中...")
    }

    if (lines.isEmpty()) return@Box

    Column(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(Color.Black.copy(alpha = 0.35f))
        .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
      lines.forEachIndexed { index, line ->
        Text(
          text = line,
          fontSize = config.fontSize.sp,
          fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
          color = Color.White.copy(alpha = 0.9f),
          textAlign = TextAlign.Start,
        )
      }
    }
  }
}
