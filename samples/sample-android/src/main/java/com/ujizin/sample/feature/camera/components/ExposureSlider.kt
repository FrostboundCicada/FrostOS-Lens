package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * 曝光补偿垂直滑块
 */
@Composable
fun ExposureSlider(
  modifier: Modifier = Modifier,
  currentEv: Float,
  minEv: Float,
  maxEv: Float,
  onEvChanged: (Float) -> Unit,
) {
  if (minEv >= maxEv) return

  val sliderHeight = 140.dp
  var sliderHeightPx by remember { mutableStateOf(0f) }
  val range = maxEv - minEv
  val fraction = ((currentEv - minEv) / range).coerceIn(0f, 1f)

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(text = "+", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
    Box(
      modifier = Modifier
        .width(30.dp)
        .height(sliderHeight)
        .padding(vertical = 4.dp)
        .clip(RoundedCornerShape(15.dp))
        .background(Color.Black.copy(alpha = 0.35f))
        .onGloballyPositioned { sliderHeightPx = it.size.height.toFloat() }
        .pointerInput(minEv, maxEv) {
          detectVerticalDragGestures { change, _ ->
            change.consume()
            val rawFraction = 1f - (change.position.y / sliderHeightPx).coerceIn(0f, 1f)
            val newEv = minEv + rawFraction * range
            onEvChanged(newEv)
          }
        },
      contentAlignment = Alignment.Center,
    ) {
      // 轨道
      Box(
        modifier = Modifier
          .width(3.dp)
          .fillMaxHeight()
          .padding(vertical = 14.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(Color.White.copy(alpha = 0.25f)),
      )
      // 指示器（根据fraction定位）
      val indicatorOffsetPx = (sliderHeightPx - 28f) * (1f - fraction)
      Box(
        modifier = Modifier
          .width(24.dp)
          .height(4.dp)
          .offset { IntOffset(0, indicatorOffsetPx.roundToInt()) }
          .clip(RoundedCornerShape(2.dp))
          .background(Color.White.copy(alpha = 0.85f)),
      )
    }
    Text(text = "-", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
    Text(
      text = "${if (currentEv >= 0) "+" else ""}${"%.1f".format(currentEv)}",
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = Color.White.copy(alpha = 0.8f),
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 2.dp),
    )
  }
}