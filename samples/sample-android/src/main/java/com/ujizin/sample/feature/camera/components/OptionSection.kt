package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.sample.feature.camera.model.CameraOption

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
        color = Color.Black.copy(alpha = 0.45f),
        shape = RoundedCornerShape(24.dp),
      )
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    CameraOption.values().forEach { option ->
      if (!isVideoSupported && option == CameraOption.Video) return@forEach

      val isSelected = currentCameraOption == option
      Text(
        modifier = Modifier
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onCameraOptionChanged(option) },
          )
          .padding(vertical = 4.dp, horizontal = 4.dp),
        text = stringResource(id = option.titleRes).replaceFirstChar { it.uppercase() },
        fontSize = 15.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        textAlign = TextAlign.Center,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f),
      )
    }
  }
}
