package com.ujizin.sample.feature.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.rotate
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
  cameraMode: com.ujizin.camposer.state.CameraMode,
  isRecording: Boolean,
  lastPicture: File?,
  onCapture: () -> Unit,
  onSwitchCamera: () -> Unit,
  onGalleryClick: () -> Unit,
) {
  Row(
    modifier = modifier.padding(horizontal = 24.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    GalleryButton(lastPicture, onClick = onGalleryClick)
    PictureButton(
      isVideo = cameraMode == com.ujizin.camposer.state.CameraMode.Video,
      isRecording = isRecording,
      onClick = onCapture,
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
  val borderColor by animateColorAsState(
    targetValue = if (shouldAnimate) Color.White else Color.White.copy(alpha = 0.5F),
  )
  Button(
    modifier = modifier
      .size(52.dp)
      .background(Color.White.copy(alpha = 0.18F), CircleShape)
      .border(BorderStroke(2.dp, borderColor), CircleShape)
      .clip(CircleShape),
    contentPaddingValues = androidx.compose.foundation.layout.PaddingValues(4.dp),
    onClick = onClick,
  ) {
    val context = LocalContext.current
    val request = remember(lastPicture) {
      ImageRequest.Builder(context)
        .data(lastPicture)
        .decoderFactory(VideoFrameDecoder.Factory())
        .videoFrameMillis(1000)
        .build()
    }
    AsyncImage(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape),
      model = request,
      contentScale = ContentScale.Crop,
      contentDescription = stringResource(R.string.gallery),
      error = painterResource(R.drawable.gallery),
      placeholder = painterResource(R.drawable.gallery),
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
    animationSpec = tween(durationMillis = 500),
  )
  Button(
    modifier = Modifier
      .rotate(rotate)
      .size(52.dp)
      .background(Color.White.copy(alpha = 0.18F), CircleShape)
      .clip(CircleShape)
      .then(modifier),
    contentPaddingValues = androidx.compose.foundation.layout.PaddingValues(12.dp),
    onClick = {
      clicked = !clicked
      onClick()
    },
  ) {
    Image(
      modifier = Modifier.size(26.dp),
      painter = painterResource(id = R.drawable.refresh),
      colorFilter = ColorFilter.tint(Color.White),
      contentDescription = stringResource(R.string.refresh),
    )
  }
}

/**
 * 拍照/录制按钮 - 应用液态玻璃效果
 */
@Composable
private fun PictureButton(
  modifier: Modifier = Modifier,
  isVideo: Boolean,
  isRecording: Boolean,
  onClick: () -> Unit,
) {
  val color by animateColorAsState(
    targetValue = if (isVideo) Color.Red else Color.Transparent,
    animationSpec = tween(durationMillis = 250),
  )

  val innerPadding by animateDpAsState(targetValue = if (isRecording) 24.dp else 10.dp)
  val percentShape by animateIntAsState(targetValue = if (isRecording) 25 else 50)
  Button(
    modifier = Modifier
      .size(84.dp)
      .cloudy(radius = 25)
      .liquidGlass(lensCenter = Offset(0f, 0f))
      .border(BorderStroke(4.dp, Color.White), CircleShape)
      .padding(innerPadding)
      .background(color, RoundedCornerShape(percent = percentShape))
      .clip(CircleShape)
      .then(modifier),
    onClick = onClick,
  )
}
