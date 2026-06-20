package com.ujizin.sample.feature.camera

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.graphics.ImageFormat.YUV_420_888
import android.graphics.ImageFormat.YUV_422_888
import android.graphics.ImageFormat.YUV_444_888
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.sample.data.local.datasource.FileDataSource
import com.ujizin.sample.data.local.datasource.UserDataSource
import com.ujizin.sample.domain.User
import com.ujizin.sample.extensions.getQRCodeResult
import com.ujizin.sample.feature.camera.model.WatermarkConfig
import com.ujizin.sample.feature.camera.model.CameraFilter
import com.ujizin.sample.feature.camera.model.CameraParams
import com.ujizin.sample.feature.camera.util.WatermarkUtil
import com.ujizin.sample.feature.camera.util.FilterUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CameraViewModel(
  private val fileDataSource: FileDataSource,
  private val userDataSource: UserDataSource,
) : ViewModel() {
  private val _uiState: MutableStateFlow<CameraUiState> = MutableStateFlow(CameraUiState.Initial)
  val uiState: StateFlow<CameraUiState> get() = _uiState

  private val reader = MultiFormatReader().apply {
    val map = mapOf(DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE))
    setHints(map)
  }

  private lateinit var user: User

  init {
    initCamera()
  }

  private fun initCamera() {
    viewModelScope.launch {
      userDataSource
        .getUser()
        .onStart { CameraUiState.Initial }
        .collect { user ->
          this@CameraViewModel.user = user
          _uiState.update {
            CameraUiState.Ready(user, fileDataSource.lastPicture)
          }
        }
    }
  }

  /**
   * 拍照 — 支持裁剪、滤镜和水印合成
   * 如果裁剪/滤镜/水印任一启用，使用 ByteArray 重载获取图像数据，处理后保存
   * 否则走原有逻辑直接保存
   */
  fun takePicture(
    cameraController: CameraController,
    watermarkConfig: WatermarkConfig = WatermarkConfig(),
    filter: CameraFilter = CameraFilter.None,
    cameraParams: CameraParams = CameraParams(),
    cropRatio: Float = 0f,
  ) = with(cameraController) {
    viewModelScope.launch {
      if (watermarkConfig.enabled || filter != CameraFilter.None || cropRatio > 0f) {
        // 使用 ByteArray 重载，获取图像数据后处理
        takePicture { result ->
          when (result) {
            is CaptureResult.Error -> onError(result.throwable)
            is CaptureResult.Success -> {
              viewModelScope.launch {
                processAndSaveImage(result.data, filter, watermarkConfig, cameraParams, cropRatio)
              }
            }
          }
        }
      } else {
        // 无滤镜无水印 — 走原有逻辑
        when {
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> takePicture(
            fileDataSource.imageContentValues,
            onResult = ::onImageResult,
          )
          else -> takePicture(
            fileDataSource.getFile("jpg"),
            ::onImageResult,
          )
        }
      }
    }
  }

  /**
   * 处理图片：裁剪 → 应用滤镜 → 合成水印 → 保存
   */
  private suspend fun processAndSaveImage(
    jpegBytes: ByteArray,
    filter: CameraFilter,
    config: WatermarkConfig,
    cameraParams: CameraParams,
    cropRatio: Float,
  ) = withContext(Dispatchers.IO) {
    try {
      var processedBytes = jpegBytes
      // 0. 裁剪到目标比例
      if (cropRatio > 0f) {
        processedBytes = FilterUtil.cropToAspectRatio(processedBytes, cropRatio)
      }
      // 1. 应用滤镜
      if (filter != CameraFilter.None) {
        processedBytes = FilterUtil.applyFilter(processedBytes, filter)
      }
      // 2. 合成水印（传入拍摄参数）
      if (config.enabled) {
        processedBytes = WatermarkUtil.applyWatermark(processedBytes, config, cameraParams)
      }
      // 3. 保存
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = AppContextHolder.contentResolver
        val uri = resolver.insert(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          fileDataSource.imageContentValues,
        )
        uri?.let {
          resolver.openOutputStream(it)?.use { os ->
            os.write(processedBytes)
            os.flush()
          }
        }
      } else {
        val file = fileDataSource.getFile("jpg")
        FileOutputStream(file).use { fos ->
          fos.write(processedBytes)
          fos.flush()
        }
      }
      captureSuccess()
    } catch (e: Exception) {
      onError(e)
    }
  }

  @SuppressLint("MissingPermission")
  fun toggleRecording(
    contentResolver: ContentResolver,
    cameraController: CameraController,
  ) = with(cameraController) {
    viewModelScope.launch {
      when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
          toggleRecording(
            MediaStoreOutputOptions
              .Builder(
                contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
              ).setContentValues(fileDataSource.videoContentValues)
              .build(),
            onResult = ::onVideoResult,
          )
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
          toggleRecording(
            FileOutputOptions.Builder(fileDataSource.getFile("mp4")).build(),
            onResult = ::onVideoResult,
          )
        }
      }
    }
  }

  fun analyzeImage(image: ImageProxy) {
    viewModelScope.launch {
      if (image.format !in listOf(YUV_420_888, YUV_422_888, YUV_444_888)) {
        Log.e("QRCodeAnalyzer", "Expected YUV, now = ${image.format}")
      }
      val qrCodeResult = reader.getQRCodeResult(image)
      _uiState.update {
        CameraUiState.Ready(
          user = user,
          lastPicture = fileDataSource.lastPicture,
          qrCodeText = qrCodeResult?.text,
        )
      }
      image.close()
    }
  }

  private fun captureSuccess() {
    viewModelScope.launch {
      _uiState.update {
        CameraUiState.Ready(user = user, lastPicture = fileDataSource.lastPicture)
      }
    }
  }

  private fun onVideoResult(videoResult: CaptureResult<Uri?>) {
    when (videoResult) {
      is CaptureResult.Error -> onError(videoResult.throwable)
      is CaptureResult.Success -> captureSuccess()
    }
  }

  private fun onImageResult(imageResult: CaptureResult<Uri?>) {
    when (imageResult) {
      is CaptureResult.Error -> onError(imageResult.throwable)
      is CaptureResult.Success -> captureSuccess()
    }
  }

  private fun onError(throwable: Throwable?) {
    _uiState.update { CameraUiState.Ready(user, fileDataSource.lastPicture, throwable) }
  }
}

sealed interface CameraUiState {
  data object Initial : CameraUiState

  data class Ready(
    val user: User,
    val lastPicture: File?,
    val throwable: Throwable? = null,
    val qrCodeText: String? = null,
  ) : CameraUiState
}