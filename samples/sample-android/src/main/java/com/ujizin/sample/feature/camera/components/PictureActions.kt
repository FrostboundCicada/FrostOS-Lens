package com.ujizin.sample.feature.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.liquidGlass
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
    PictureButton(
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
  val animScale by animateFloatAsState(targetValue = if (shouldAnimate) 1.25F else 1F)
  AsyncImage(
    modifier = Modifier
      .scale(animScale)
      .size(44.dp)
      .clip(CircleShape)
      .background(Color.White.copy(alpha = 0.12F), CircleShape)
      .clickable(onClick = onClick)
      .then(modifier),
    contentScale = ContentScale.Crop,
    model = ImageRequest
      .Builder(LocalContext.current)
      .data(lastPicture)
      .decoderFactory(VideoFrameDecoder.Factory())
      .videoFrameMillis(1)
      .build(),
    contentDescription = stringResource(R.string.gallery),
  )

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
  )
  Button(
    modifier = Modifier
      .rotate(rotate)
      .size(44.dp)
      .background(Color.White.copy(alpha = 0.12F), CircleShape)
      .clip(CircleShape)
      .then(modifier),
    onClick = {
      clicked = !clicked
      onClick()
    },
  ) {
    Image(
      modifier = Modifier.size(22.dp),
      painter = painterResource(id = R.drawable.refresh),
      colorFilter = ColorFilter.tint(Color.White),
      contentDescription = stringResource(R.string.refresh),
    )
  }
}

/**
 * 拍照/录制按钮 - 使用液态玻璃效果
 */
@Composable
private fun PictureButton(
  modifier: Modifier = Modifier,
  isVideo: Boolean,
  isRecording: Boolean,
  onClick: () -> Unit,
) {
  val color by animateColorAsState(
    targetValue = if (isRecording) Color.Red else Color.White.copy(alpha = 0.08F),
  )
  val innerPadding by animateFloatAsState(targetValue = if (isRecording) 26F else 10F)
  val percentShape by animateIntAsState(targetValue = if (isRecording) 25 else 50)
  Box(
    modifier = Modifier
      .size(74.dp)
      .cloudy(radius = 18)
      .liquidGlass(lensCenter = Offset(0f, 0f))
      .border(BorderStroke(3.dp, Color.White), CircleShape)
      .padding(innerPadding.dp)
      .background(color, RoundedCornerShape(percent = percentShape))
      .clip(CircleShape)
      .clickable(onClick = onClick)
      .then(modifier),
  )
}
