package com.ujizin.sample.feature.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.ujizin.sample.R
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun PictureActions(
  modifier: Modifier = Modifier,
  isVideo: Boolean,
  isRecording: Boolean,
  lastPicture: File?,
  onGalleryClick: () -> Unit,
  onRecording: () -> Unit,
  onTakePicture: () -> Unit,
  onSwitchCamera: () -> Unit,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    GalleryButton(lastPicture, onClick = onGalleryClick)
    ShutterButton(
      isVideo = isVideo,
      isRecording = isRecording,
      onClick = { if (isVideo) onRecording() else onTakePicture() },
    )
    SwitchButton(onClick = onSwitchCamera)
  }
}

@Composable
private fun GalleryButton(
  lastPicture: File?,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  var shouldAnimate by remember { mutableStateOf(false) }
  val animScale by animateFloatAsState(targetValue = if (shouldAnimate) 1.2F else 1F)
  val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
  Box(
    modifier = Modifier
      .scale(animScale)
      .size(48.dp)
      .clip(CircleShape)
      .background(surfaceColor, CircleShape)
      .clickable(onClick = onClick)
      .then(modifier),
    contentAlignment = Alignment.Center,
  ) {
    AsyncImage(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape),
      contentScale = ContentScale.Crop,
      model = ImageRequest
        .Builder(LocalContext.current)
        .data(lastPicture)
        .decoderFactory(VideoFrameDecoder.Factory())
        .videoFrameMillis(1)
        .build(),
      contentDescription = stringResource(R.string.gallery),
    )
  }
  LaunchedEffect(lastPicture) {
    shouldAnimate = true
    delay(50)
    shouldAnimate = false
  }
}

@Composable
private fun SwitchButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  var clicked by remember { mutableStateOf(false) }
  val rotate by animateFloatAsState(
    targetValue = if (clicked) 360F else 1F,
    animationSpec = tween(durationMillis = 400),
  )
  val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
  Box(
    modifier = Modifier
      .rotate(rotate)
      .size(48.dp)
      .clip(CircleShape)
      .background(surfaceColor, CircleShape)
      .clickable(
        onClick = {
          clicked = !clicked
          onClick()
        },
      )
      .then(modifier),
    contentAlignment = Alignment.Center,
  ) {
    Image(
      modifier = Modifier.size(24.dp),
      painter = painterResource(id = R.drawable.refresh),
      colorFilter = ColorFilter.tint(Color.White),
      contentDescription = stringResource(R.string.refresh),
    )
  }
}

/**
 * 快门按钮 — 手绘玻璃质感圆环
 * 外圈：半透明背景 + 顶部高光弧线 + 白色边框
 * 内圈：实心圆，录像时缩小变红，使用莫奈主色
 */
@Composable
private fun ShutterButton(
  modifier: Modifier = Modifier,
  isVideo: Boolean,
  isRecording: Boolean,
  onClick: () -> Unit,
) {
  val outerSize = 80.dp
  val innerSize = if (isRecording) 32.dp else 56.dp

  val innerColor by animateColorAsState(
    targetValue = if (isRecording) Color.Red
    else MaterialTheme.colorScheme.primaryContainer,
    animationSpec = tween(durationMillis = 250),
  )

  Box(
    modifier = modifier.size(outerSize),
    contentAlignment = Alignment.Center,
  ) {
    // 外圈 — 玻璃质感
    Box(
      modifier = Modifier
        .size(outerSize)
        .clip(CircleShape)
        .background(Color.White.copy(alpha = 0.16f), CircleShape)
        .drawWithContent {
          drawContent()
          // 顶部高光弧线 — 模拟玻璃反光
          val strokeW = size.width * 0.045f
          val arcR = size.width / 2f - strokeW / 2f
          val margin = strokeW / 2f
          drawArc(
            color = Color.White.copy(alpha = 0.5f),
            startAngle = 210f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(margin, margin),
            size = Size(arcR * 2f, arcR * 2f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
          )
        }
        .border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape)
        .clickable(onClick = onClick),
    )
    // 内圈实心圆
    Box(
      modifier = Modifier
        .size(innerSize)
        .clip(CircleShape)
        .background(innerColor, CircleShape),
    )
  }
}