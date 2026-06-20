package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.sample.feature.camera.model.AspectRatioOption

/**
 * OPPO 风格比例选择器 — 莫奈取色
 */
@Composable
fun AspectRatioSelector(
  modifier: Modifier = Modifier,
  currentRatio: AspectRatioOption,
  onRatioChanged: (AspectRatioOption) -> Unit,
) {
  Row(
    modifier = modifier
      .background(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(50),
      )
      .padding(3.dp),
    horizontalArrangement = Arrangement.spacedBy(0.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    AspectRatioOption.values().forEach { option ->
      val isSelected = currentRatio == option
      val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
      } else {
        Color.Transparent
      }
      val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(50))
          .background(bgColor)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onRatioChanged(option) },
          )
          .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(id = option.titleRes),
          fontSize = 12.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
          textAlign = TextAlign.Center,
          color = textColor,
          maxLines = 1,
        )
      }
    }
  }
}