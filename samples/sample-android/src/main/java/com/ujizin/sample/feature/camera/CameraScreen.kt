package com.ujizin.sample.feature.camera

import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.lifecycle.compose.collectStateWithLifecycle
import com.ujizin.camposer.manager.CameraDeviceState
import com.ujizin.camposer.manager.rememberCameraDeviceState
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.session.rememberCameraSession
import com.ujizin.camposer.session.rememberImageAnalyzer
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.properties.selector.Saver
import com.ujizin.camposer.state.properties.selector.inverse
import com.ujizin.sample.extensions.noClickable
import com.ujizin.sample.feature.camera.components.ActionBox
import com.ujizin.sample.feature.camera.components.AspectRatioSelector
import com.ujizin.sample.feature.camera.components.BlinkPictureBox
import com.ujizin.sample.feature.camera.components.ExposureSlider
import com.ujizin.sample.feature.camera.components.GridOverlay
import com.ujizin.sample.feature.camera.components.LevelIndicator
import com.ujizin.sample.feature.camera.components.SettingsBox
import com.ujizin.sample.feature.camera.components.TimerOverlay
import com.ujizin.sample.feature.camera.mapper.toFlash
import com.ujizin.sample.feature.camera.mapper.toFlashMode
import com.ujizin.sample.feature.camera.model.CameraOption
import com.ujizin.sample.feature.camera.model.AspectRatioOption
import com.ujizin.sample.feature.camera.model.Flash
import com.ujizin.sample.feature.camera.model.TimerOption
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun CameraScreen(
  viewModel: CameraViewModel = koinViewModel(),
  onGalleryClick: () -> Unit,
  onConfigurationClick: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  when (val result: CameraUiState = uiState) {
    is CameraUiState.Ready -> {
      val cameraController = remember { CameraController() }
      val cameraSession = rememberCameraSession(cameraController)
      val isRecording by cameraController.isRecording.collectAsStateWithLifecycle()
      val context = LocalContext.current
      CameraSection(
        cameraSession = cameraSession,
        useFrontCamera = result.user.useCamFront,
        usePinchToZoom = result.user.usePinchToZoom,
        useTapToFocus = result.user.useTapToFocus,
        lastPicture = result.lastPicture,
        qrCodeText = result.qrCodeText,
        onGalleryClick = onGalleryClick,
        onConfigurationClick = onConfigurationClick,
        onRecording = {
          viewModel.toggleRecording(context.contentResolver, cameraController)
        },
        onTakePicture = { viewModel.takePicture(cameraController) },
        isRecording = isRecording,
        onAnalyzeImage = viewModel::analyzeImage,
      )

      LaunchedEffect(result.throwable) {
        if (result.throwable != null) {
          Toast.makeText(context, result.throwable.message, Toast.LENGTH_SHORT).show()
        }
      }
    }
    CameraUiState.Initial -> Unit
  }
}

@Composable
fun CameraSection(
  cameraSession: CameraSession,
  isRecording: Boolean,
  useFrontCamera: Boolean,
  usePinchToZoom: Boolean,
  useTapToFocus: Boolean,
  qrCodeText: String?,
  lastPicture: File?,
  onTakePicture: () -> Unit,
  onRecording: () -> Unit,
  onGalleryClick: () -> Unit,
  onAnalyzeImage: (ImageProxy) -> Unit,
  onConfigurationClick: () -> Unit,
) {
  var camSelector by rememberSaveable(stateSaver = CamSelector.Saver) {
    mutableStateOf(
      CamSelector(
        camPosition = if (useFrontCamera) CamSelector.Front.camPosition else CamSelector.Back.camPosition,
        camLensTypes = listOf(CamLensType.Wide),
      ),
    )
  }
  val zoomRatio by cameraSession.state.zoomRatio.collectAsStateWithLifecycle()
  var zoomHasChanged by remember { mutableStateOf(false) }
  val cameraInfoState by cameraSession.info.collectStateWithLifecycle()
  val hasFlashUnit = cameraInfoState.isFlashSupported
  var cameraOption by rememberSaveable { mutableStateOf(CameraOption.Photo) }
  var aspectRatio by rememberSaveable { mutableStateOf(AspectRatioOption.Default) }
  var showGrid by rememberSaveable { mutableStateOf(false) }
  var showLevel by rememberSaveable { mutableStateOf(true) }
  var timerOption by rememberSaveable { mutableStateOf(TimerOption.Default) }
  var timerActive by remember { mutableStateOf(false) }

  val flashMode by cameraSession.state.flashMode.collectAsStateWithLifecycle()
  val enableTorch by cameraSession.state.isTorchEnabled.collectAsStateWithLifecycle()
  val exposureCompensation by cameraSession.state.exposureCompensation.collectAsStateWithLifecycle()
  val evMin = cameraInfoState.minExposure
  val evMax = cameraInfoState.maxExposure
  val isExposureSupported = cameraInfoState.isExposureSupported
  val imageAnalyzer = cameraSession.rememberImageAnalyzer(analyze = onAnalyzeImage)

  LaunchedEffect(zoomRatio) { zoomHasChanged = true }
  val camDeviceState by rememberCameraDeviceState()

  LaunchedEffect(camDeviceState) {
    if (camDeviceState is CameraDeviceState.Devices) {
      Log.d("YUJI", "devices: ${camDeviceState.cameraDevices}")
    }
  }

  CameraPreview(
    modifier = Modifier.fillMaxSize(),
    cameraSession = cameraSession,
    camSelector = camSelector,
    captureMode = cameraOption.toCaptureMode(),
    camFormat = remember(aspectRatio) {
      CamFormat(
        AspectRatioConfig(aspectRatio.ratio),
        ResolutionConfig.UltraHigh,
        FrameRateConfig(60),
        VideoStabilizationConfig(VideoStabilizationMode.Standard),
      )
    },
    scaleType = if (aspectRatio.isFullScreen) ScaleType.FillCenter else ScaleType.FitCenter,
    previewBackgroundColor = Color.Black,
    imageAnalyzer = imageAnalyzer,
    isImageAnalysisEnabled = cameraOption == CameraOption.QRCode,
    isPinchToZoomEnabled = usePinchToZoom,
    isFocusOnTapEnabled = useTapToFocus,
  ) {
    GridOverlay(visible = showGrid)
    LevelIndicator(visible = showLevel)
    TimerOverlay(
      seconds = if (timerActive) timerOption.seconds else 0,
      onFinished = {
        timerActive = false
        if (cameraOption == CameraOption.Video) onRecording()
        else onTakePicture()
      },
    )
    BlinkPictureBox(lastPicture, cameraOption == CameraOption.Video)
    CameraInnerContent(
      Modifier.fillMaxSize(),
      zoomHasChanged = zoomHasChanged,
      zoomRatio = zoomRatio,
      flashMode = flashMode.toFlash(enableTorch),
      isRecording = isRecording,
      cameraOption = cameraOption,
      aspectRatio = aspectRatio,
      showGrid = showGrid,
      timerOption = timerOption,
      exposureCompensation = exposureCompensation,
      evMin = evMin,
      evMax = evMax,
      isExposureSupported = isExposureSupported,
      hasFlashUnit = hasFlashUnit,
      qrCodeText = qrCodeText,
      isVideoSupported = true,
      onFlashModeChanged = { flash ->
        with(cameraSession.controller) {
          setTorchEnabled(flash == Flash.Always)
          setFlashMode(flash.toFlashMode())
        }
      },
      onShowGridChanged = { showGrid = it },
      onTimerChanged = { timerOption = it },
      onEvChanged = { cameraSession.controller.setExposureCompensation(it) },
      onZoomFinish = { zoomHasChanged = false },
      lastPicture = lastPicture,
      onTakePicture = {
        if (timerOption.seconds > 0) {
          timerActive = true
        } else {
          onTakePicture()
        }
      },
      onRecording = {
        if (timerOption.seconds > 0) {
          timerActive = true
        } else {
          onRecording()
        }
      },
      onSwitchCamera = {
        if (cameraSession.isStreaming) camSelector = camSelector.inverse
      },
      onCameraOptionChanged = { cameraOption = it },
      onAspectRatioChanged = { aspectRatio = it },
      onGalleryClick = onGalleryClick,
      onConfigurationClick = onConfigurationClick,
    )
  }
}

@Composable
fun CameraInnerContent(
  modifier: Modifier = Modifier,
  zoomHasChanged: Boolean,
  zoomRatio: Float,
  flashMode: Flash,
  isRecording: Boolean,
  cameraOption: CameraOption,
  aspectRatio: AspectRatioOption,
  showGrid: Boolean,
  timerOption: TimerOption,
  exposureCompensation: Float,
  evMin: Float,
  evMax: Float,
  isExposureSupported: Boolean,
  hasFlashUnit: Boolean,
  qrCodeText: String?,
  lastPicture: File?,
  isVideoSupported: Boolean,
  onGalleryClick: () -> Unit,
  onFlashModeChanged: (Flash) -> Unit,
  onShowGridChanged: (Boolean) -> Unit,
  onTimerChanged: (TimerOption) -> Unit,
  onEvChanged: (Float) -> Unit,
  onZoomFinish: () -> Unit,
  onRecording: () -> Unit,
  onTakePicture: () -> Unit,
  onConfigurationClick: () -> Unit,
  onSwitchCamera: () -> Unit,
  onCameraOptionChanged: (CameraOption) -> Unit,
  onAspectRatioChanged: (AspectRatioOption) -> Unit,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    SettingsBox(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, start = 16.dp, end = 16.dp),
      flashMode = flashMode,
      zoomRatio = zoomRatio,
      isVideo = cameraOption == CameraOption.Video,
      hasFlashUnit = hasFlashUnit,
      zoomHasChanged = zoomHasChanged,
      isRecording = isRecording,
      showGrid = showGrid,
      timerOption = timerOption,
      onFlashModeChanged = onFlashModeChanged,
      onShowGridChanged = onShowGridChanged,
      onTimerChanged = onTimerChanged,
      onConfigurationClick = onConfigurationClick,
      onZoomFinish = onZoomFinish,
    )
    // 中间区域: 曝光补偿 (右侧竖排)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
    ) {
      if (cameraOption != CameraOption.QRCode && isExposureSupported) {
        ExposureSlider(
          modifier = Modifier.padding(end = 12.dp),
          currentEv = exposureCompensation,
          minEv = evMin,
          maxEv = evMax,
          onEvChanged = onEvChanged,
        )
      }
    }
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      if (cameraOption == CameraOption.Photo) {
        AspectRatioSelector(
          modifier = Modifier.padding(bottom = 12.dp),
          currentRatio = aspectRatio,
          onRatioChanged = onAspectRatioChanged,
        )
      }
      ActionBox(
        modifier = Modifier
          .fillMaxWidth()
          .noClickable()
          .padding(bottom = 32.dp),
        lastPicture = lastPicture,
        onGalleryClick = onGalleryClick,
        cameraOption = cameraOption,
        qrCodeText = qrCodeText,
        onTakePicture = onTakePicture,
        isRecording = isRecording,
        isVideoSupported = isVideoSupported,
        onRecording = onRecording,
        onSwitchCamera = onSwitchCamera,
        onCameraOptionChanged = onCameraOptionChanged,
      )
    }
  }
}