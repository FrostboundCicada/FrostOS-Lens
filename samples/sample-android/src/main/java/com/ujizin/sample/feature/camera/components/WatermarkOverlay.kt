package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.sample.feature.camera.model.WatermarkConfig
import com.ujizin.sample.feature.camera.model.WatermarkPosition
import com.ujizin.sample.feature.camera.model.WatermarkStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 水印覆盖层 — 在预览上显示自定义水印
 * 预览样式与 WatermarkUtil 保存到图片中的水印外观对应
 */
@Composable
fun WatermarkOverlay(
  modifier: Modifier = Modifier,
  config: WatermarkConfig,
) {
  if (!config.enabled) return

  val lines = buildList {
    if (config.text.isNotBlank()) add(config.text to true)
    if (config.signature.isNotBlank()) add("by ${config.signature}" to false)
    if (config.showDate) {
      add(SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()) to false)
    }
    if (config.showCameraParams) add("ISO --  f/--  --mm" to false)
    if (config.showLocation) add("📍 定位中..." to false)
  }
  if (lines.isEmpty()) return

  when (config.style) {
    WatermarkStyle.Minimal -> MinimalPreview(modifier, config, lines)
    WatermarkStyle.Card -> CardPreview(modifier, config, lines)
    WatermarkStyle.FilmStrip -> FilmStripPreview(modifier, config, lines)
  }
}

@Composable
private fun MinimalPreview(
  modifier: Modifier,
  config: WatermarkConfig,
  lines: List<Pair<String, Boolean>>,
) {
  val alignment = when (config.position) {
    WatermarkPosition.BottomLeft, WatermarkPosition.BottomRight -> Alignment.BottomStart
    WatermarkPosition.TopLeft, WatermarkPosition.TopRight -> Alignment.TopStart
  }
  val padBottom = if (config.position == WatermarkPosition.BottomLeft || config.position == WatermarkPosition.BottomRight) 120.dp else 0.dp
  val padTop = if (config.position == WatermarkPosition.TopLeft || config.position == WatermarkPosition.TopRight) 60.dp else 0.dp

  Box(
    modifier = modifier.fillMaxSize().padding(bottom = padBottom, top = padTop, start = 16.dp, end = 16.dp),
    contentAlignment = alignment,
  ) {
    Column {
      lines.forEachIndexed { index, (text, isTitle) ->
        Text(
          text = text,
          fontSize = if (isTitle) config.fontSize.sp else (config.fontSize * 0.7f).sp,
          fontWeight = if (isTitle) FontWeight.Bold else FontWeight.Normal,
          color = Color.White.copy(alpha = if (isTitle) 0.95f else 0.7f),
        )
      }
    }
  }
}

@Composable
private fun CardPreview(
  modifier: Modifier,
  config: WatermarkConfig,
  lines: List<Pair<String, Boolean>>,
) {
  val alignment = when (config.position) {
    WatermarkPosition.BottomLeft -> Alignment.BottomStart
    WatermarkPosition.BottomRight -> Alignment.BottomEnd
    WatermarkPosition.TopLeft -> Alignment.TopStart
    WatermarkPosition.TopRight -> Alignment.TopEnd
  }
  val padBottom = if (config.position == WatermarkPosition.BottomLeft || config.position == WatermarkPosition.BottomRight) 120.dp else 0.dp
  val padTop = if (config.position == WatermarkPosition.TopLeft || config.position == WatermarkPosition.TopRight) 60.dp else 0.dp

  Box(
    modifier = modifier.fillMaxSize().padding(bottom = padBottom, top = padTop, start = 16.dp, end = 16.dp),
    contentAlignment = alignment,
  ) {
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(6.dp))
        .background(
          Brush.verticalGradient(
            listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.55f)),
          ),
        )
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .width(2.dp)
          .height(32.dp)
          .clip(RoundedCornerShape(1.dp))
          .background(Color.White.copy(alpha = 0.8f)),
      )
      Column(modifier = Modifier.padding(start = 8.dp)) {
        lines.forEachIndexed { index, (text, isTitle) ->
          Text(
            text = text,
            fontSize = if (isTitle) config.fontSize.sp else (config.fontSize * 0.65f).sp,
            fontWeight = if (isTitle) FontWeight.Bold else FontWeight.Normal,
            color = Color.White.copy(alpha = if (isTitle) 0.95f else 0.75f),
            textAlign = TextAlign.Start,
          )
        }
      }
    }
  }
}

@Composable
private fun FilmStripPreview(
  modifier: Modifier,
  config: WatermarkConfig,
  lines: List<Pair<String, Boolean>>,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.BottomStart,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 110.dp)
        .background(
          Brush.verticalGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
          ),
        )
        .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      Column {
        lines.forEachIndexed { index, (text, isTitle) ->
          Text(
            text = text,
            fontSize = if (isTitle) config.fontSize.sp else (config.fontSize * 0.6f).sp,
            fontWeight = if (isTitle) FontWeight.Bold else FontWeight.Normal,
            color = Color.White.copy(alpha = if (isTitle) 0.95f else 0.7f),
          )
        }
      }
    }
  }
}
