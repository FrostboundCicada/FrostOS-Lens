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
import com.ujizin.sample.feature.camera.model.CameraOption

/**
 * OPPO 风格模式选择器
 * 药丸形状，选中项使用莫奈主色高亮填充
 */
@Composable
fun OptionSection(
  modifier: Modifier = Modifier,
  currentCameraOption: CameraOption,
  isVideoSupported: Boolean,
  onCameraOptionChanged: (CameraOption) -> Unit,
) {
  Row(
    modifier = modifier
      .background(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(50),
      )
      .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(0.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    CameraOption.values().forEach { option ->
      if (!isVideoSupported && option == CameraOption.Video) return@forEach

      val isSelected = currentCameraOption == option
      val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
      } else {
        Color.Transparent
      }
      val textColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(50))
          .background(bgColor)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onCameraOptionChanged(option) },
          )
          .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(id = option.titleRes),
          fontSize = 14.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
          textAlign = TextAlign.Center,
          color = textColor,
          maxLines = 1,
        )
      }
    }
  }
}