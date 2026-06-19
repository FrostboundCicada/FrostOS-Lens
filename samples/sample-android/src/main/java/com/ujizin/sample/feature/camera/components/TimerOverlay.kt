package com.ujizin.sample.feature.camera.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 定时拍摄倒计时
 * @param seconds 定时秒数 (0 = 不启用)
 * @param onFinished 倒计时结束回调
 */
@Composable
fun TimerOverlay(
  modifier: Modifier = Modifier,
  seconds: Int,
  onFinished: () -> Unit,
) {
  if (seconds <= 0) return
  var countdown by remember(seconds) { mutableIntStateOf(seconds) }
  var show by remember(seconds) { mutableIntStateOf(true) }

  LaunchedEffect(seconds) {
    countdown = seconds
    show = true
    while (countdown > 0) {
      delay(1000)
      countdown--
    }
    delay(100)
    show = false
    onFinished()
  }

  AnimatedVisibility(
    visible = show,
    enter = fadeIn() + scaleIn(animationSpec = tween(200)),
    exit = fadeOut() + scaleOut(animationSpec = tween(200)),
  ) {
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = if (countdown > 0) countdown.toString() else "",
        fontSize = 96.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.9f),
      )
    }
  }
}