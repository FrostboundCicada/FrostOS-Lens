package com.ujizin.sample.feature.camera.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.ujizin.sample.R
import com.ujizin.sample.extensions.roundTo
import com.ujizin.sample.feature.camera.model.Flash
import com.ujizin.sample.feature.camera.model.TimerOption
import kotlinx.coroutines.delay

@Composable
fun SettingsBox(
  modifier: Modifier = Modifier,
  zoomRatio: Float,
  zoomHasChanged: Boolean,
  flashMode: Flash,
  isRecording: Boolean,
  isVideo: Boolean,
  hasFlashUnit: Boolean,
  showGrid: Boolean,
  timerOption: TimerOption,
  onFlashModeChanged: (Flash) -> Unit,
  onShowGridChanged: (Boolean) -> Unit,
  onTimerChanged: (TimerOption) -> Unit,
  onConfigurationClick: () -> Unit,
  onZoomFinish: () -> Unit,
) {
  Box(
    modifier = modifier.fillMaxWidth(),
  ) {
    // 左侧: 闪光灯
    FlashBox(
      modifier = Modifier.align(Alignment.TopStart),
      hasFlashUnit = hasFlashUnit,
      flashMode = flashMode,
      isVideo = isVideo,
      onFlashModeChanged = onFlashModeChanged,
    )
    // 中间: 录制指示器 + 变焦
    Column(
      modifier = Modifier.align(Alignment.TopCenter),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      VideoBox(
        modifier = Modifier.padding(top = 2.dp),
        isRecording = isRecording,
      )
      AnimatedVisibility(
        modifier = Modifier.padding(top = 8.dp),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        visible = zoomHasChanged,
      ) {
        Text(
          text = "${zoomRatio.roundTo(1)}X",
          fontSize = 18.sp,
          textAlign = TextAlign.Center,
          color = Color.White,
        )
      }
    }
    // 右侧: 定时 + 网格 + 设置
    Row(
      modifier = Modifier.align(Alignment.TopEnd),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TimerButton(timerOption = timerOption, onClick = { onTimerChanged(nextTimer(timerOption)) })
      TopBarButton(
        label = stringResource(R.string.grid),
        isActive = showGrid,
        onClick = { onShowGridChanged(!showGrid) },
      )
      ConfigurationBox(onConfigurationClick = onConfigurationClick)
    }
  }
  LaunchedEffect(zoomRatio, zoomHasChanged) {
    delay(1_000)
    onZoomFinish()
  }
}

private fun nextTimer(current: TimerOption): TimerOption {
  val values = TimerOption.values()
  val nextIdx = (values.indexOf(current) + 1) % values.size
  return values[nextIdx]
}

@Composable
private fun TimerButton(
  timerOption: TimerOption,
  onClick: () -> Unit,
) {
  val isActive = timerOption != TimerOption.Off
  TopBarButton(
    label = if (isActive) stringResource(timerOption.titleRes) else stringResource(R.string.timer),
    isActive = isActive,
    onClick = onClick,
  )
}

@Composable
private fun TopBarButton(
  label: String,
  isActive: Boolean,
  onClick: () -> Unit,
) {
  val bgColor = if (isActive) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
  } else {
    Color.Transparent
  }
  Box(
    modifier = Modifier
      .clip(CircleShape)
      .background(bgColor)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
      )
      .padding(horizontal = 8.dp, vertical = 5.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
      color = if (isActive) Color.White else Color.White.copy(alpha = 0.7f),
      maxLines = 1,
    )
  }
}