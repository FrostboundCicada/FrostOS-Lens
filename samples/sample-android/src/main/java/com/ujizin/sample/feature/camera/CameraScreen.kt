package com.ujizin.sample.feature.camera

import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageProxy
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.ujizin.camposer.state.properties.ImplementationMode
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
import com.ujizin.sample.feature.camera.components.FilterBar
import com.ujizin.sample.feature.camera.components.applyFilter
import com.ujizin.sample.feature.camera.components.GridOverlay
import com.ujizin.sample.feature.camera.components.LevelIndicator
import com.ujizin.sample.feature.camera.components.SettingsBox
import com.ujizin.sample.feature.camera.components.TimerOverlay
import com.ujizin.sample.feature.camera.components.WatermarkOverlay
import com.ujizin.sample.feature.camera.components.WatermarkSettingsSheet
import com.ujizin.sample.feature.camera.mapper.toFlash
import com.ujizin.sample.feature.camera.mapper.toFlashMode
import com.ujizin.sample.feature.camera.model.AspectRatioOption
import com.ujizin.sample.feature.camera.model.CameraFilter
import com.ujizin.sample.feature.camera.model.CameraOption
import com.ujizin.sample.feature.camera.model.Flash
import com.ujizin.sample.feature.camera.model.TimerOption
import com.ujizin.sample.feature.camera.model.WatermarkConfig
import com.ujizin.sample.feature.camera.model.CameraParams
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun CameraScreen(
  viewModel: CameraViewModel = koinViewModel(),
  onGalleryClick: () -> Unit,
  onConfigurationClick: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var watermarkConfig by remember { mutableStateOf(WatermarkConfig()) }

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
        watermarkConfig = watermarkConfig,
        onWatermarkConfigChanged = { watermarkConfig = it },
        onGalleryClick = onGalleryClick,
        onConfigurationClick = onConfigurationClick,
        onRecording = {
          viewModel.toggleRecording(context.contentResolver, cameraController)
        },
        onTakePicture = { params ->
          viewModel.takePicture(cameraController, watermarkConfig, currentFilter, params)
        },
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
  watermarkConfig: WatermarkConfig,
  onWatermarkConfigChanged: (WatermarkConfig) -> Unit,
  onTakePicture: (CameraParams) -> Unit,
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
  var showFilter by rememberSaveable { mutableStateOf(false) }
  var currentFilter by rememberSaveable { mutableStateOf(CameraFilter.None) }
  var timerOption by rememberSaveable { mutableStateOf(TimerOption.Default) }
  var timerActive by remember { mutableStateOf(false) }
  var showWatermarkSheet by remember { mutableStateOf(false) }

  val flashMode by cameraSession.state.flashMode.collectAsStateWithLifecycle()
  val enableTorch by cameraSession.state.isTorchEnabled.collectAsStateWithLifecycle()
  val exposureCompensation by cameraSession.state.exposureCompensation.collectAsStateWithLifecycle()
  val evMin = cameraInfoState.minExposure
  val evMax = cameraInfoState.maxExposure
  val isExposureSupported = cameraInfoState.isExposureSupported
  val imageAnalyzer = cameraSession.rememberImageAnalyzer(analyze = onAnalyzeImage)

  // 拍照时捕获当前相机参数，用于水印
  val takePictureWithParams: () -> Unit = {
    val params = CameraParams(
      zoomRatio = zoomRatio,
      exposureCompensation = exposureCompensation,
    )
    onTakePicture(params)
  }

  LaunchedEffect(zoomRatio) { zoomHasChanged = true }
  val camDeviceState by rememberCameraDeviceState()

  LaunchedEffect(camDeviceState) {
    val camDeviceState = camDeviceState
    if (camDeviceState is CameraDeviceState.Devices) {
      Log.d("YUJI", "devices: ${camDeviceState.cameraDevices}")
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    CameraPreview(
      modifier = Modifier
        .fillMaxSize()
        .applyFilter(currentFilter),
      cameraSession = cameraSession,
      camSelector = camSelector,
      captureMode = cameraOption.toCaptureMode(),
      // 有滤镜时用 Compatible(TextureView) 让 RenderEffect 生效
      implementationMode = if (currentFilter != CameraFilter.None)
        ImplementationMode.Compatible else ImplementationMode.Performance,
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
      GridOverlay(
        visible = showGrid,
        aspectRatio = aspectRatio.ratio,
        isFullScreen = aspectRatio.isFullScreen,
      )
      LevelIndicator(visible = showLevel)
      WatermarkOverlay(config = watermarkConfig)
      TimerOverlay(
        seconds = if (timerActive) timerOption.seconds else 0,
        onFinished = {
          timerActive = false
          if (cameraOption == CameraOption.Video) onRecording()
          else takePictureWithParams()
        },
      )
      BlinkPictureBox(lastPicture, cameraOption == CameraOption.Video)

      val startRecording = onRecording
      CameraInnerContent(
        Modifier.fillMaxSize(),
        zoomHasChanged = zoomHasChanged,
        zoomRatio = zoomRatio,
        flashMode = flashMode.toFlash(enableTorch),
        isRecording = isRecording,
        cameraOption = cameraOption,
        aspectRatio = aspectRatio,
        showGrid = showGrid,
        showLevel = showLevel,
        showFilter = showFilter,
        currentFilter = currentFilter,
        timerOption = timerOption,
        watermarkEnabled = watermarkConfig.enabled,
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
        onShowLevelChanged = { showLevel = it },
        onShowFilterChanged = { showFilter = it },
        onFilterChanged = { currentFilter = it },
        onTimerChanged = { timerOption = it },
        onWatermarkClick = { showWatermarkSheet = true },
        onEvChanged = { cameraSession.controller.setExposureCompensation(it) },
        onZoomFinish = { zoomHasChanged = false },
        lastPicture = lastPicture,
        onTakePicture = {
          if (timerOption.seconds > 0) {
            timerActive = true
          } else {
            takePictureWithParams()
          }
        },
        onRecording = {
          if (timerOption.seconds > 0) {
            timerActive = true
          } else {
            startRecording()
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

    // 水印设置面板 — 从底部滑入
    AnimatedVisibility(
      modifier = Modifier.align(Alignment.BottomCenter),
      visible = showWatermarkSheet,
      enter = slideInVertically { it },
      exit = slideOutVertically { it },
    ) {
      WatermarkSettingsSheet(
        config = watermarkConfig,
        onConfigChanged = onWatermarkConfigChanged,
        onDismiss = { showWatermarkSheet = false },
      )
    }
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
  showLevel: Boolean,
  showFilter: Boolean,
  currentFilter: CameraFilter,
  timerOption: TimerOption,
  watermarkEnabled: Boolean,
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
  onShowLevelChanged: (Boolean) -> Unit,
  onShowFilterChanged: (Boolean) -> Unit,
  onFilterChanged: (CameraFilter) -> Unit,
  onTimerChanged: (TimerOption) -> Unit,
  onWatermarkClick: () -> Unit,
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
    // 顶部栏 — 使用 statusBarsPadding 避开状态栏
    SettingsBox(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(top = 4.dp, start = 12.dp, end = 12.dp),
      flashMode = flashMode,
      zoomRatio = zoomRatio,
      isVideo = cameraOption == CameraOption.Video,
      hasFlashUnit = hasFlashUnit,
      zoomHasChanged = zoomHasChanged,
      isRecording = isRecording,
      showGrid = showGrid,
      showLevel = showLevel,
      showFilter = showFilter,
      timerOption = timerOption,
      watermarkEnabled = watermarkEnabled,
      onFlashModeChanged = onFlashModeChanged,
      onShowGridChanged = onShowGridChanged,
      onShowLevelChanged = onShowLevelChanged,
      onShowFilterChanged = onShowFilterChanged,
      onTimerChanged = onTimerChanged,
      onWatermarkClick = onWatermarkClick,
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

    // 底部区域
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // 滤镜选择栏（可切换显示/隐藏）
      AnimatedVisibility(visible = showFilter) {
        FilterBar(
          modifier = Modifier.padding(bottom = 8.dp),
          currentFilter = currentFilter,
          onFilterChanged = onFilterChanged,
        )
      }

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
          .padding(bottom = 16.dp),
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
