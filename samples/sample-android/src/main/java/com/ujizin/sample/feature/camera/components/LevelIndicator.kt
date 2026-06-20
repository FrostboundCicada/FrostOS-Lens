package com.ujizin.sample.feature.camera.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 水平仪 / 平衡仪
 * 使用 ROTATION_VECTOR 传感器获取设备在世界坐标系中的姿态，
 * 再根据屏幕旋转方向映射到正确的轴，支持所有方向。
 * 倾斜 < 1.5° 时变绿，表示水平。
 */
@Composable
fun LevelIndicator(
  modifier: Modifier = Modifier,
  visible: Boolean,
) {
  if (!visible) return
  val context = LocalContext.current
  var rollDegrees by remember { mutableStateOf(0f) }

  DisposableEffect(context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    val rotationVectorSensor =
      sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val rotation = wm.defaultDisplay.rotation

        if (rotationVectorSensor != null && event.sensor.type == rotationVectorSensor.type) {
          // 使用旋转矢量传感器，精度高，所有方向都可靠
          val rotationMatrix = FloatArray(9)
          SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
          val orientation = FloatArray(3)
          SensorManager.getOrientation(rotationMatrix, orientation)
          // orientation[2] = roll (绕 Y 轴), orientation[1] = pitch (绕 X 轴)
          rollDegrees = Math.toDegrees(
            when (rotation) {
              Surface.ROTATION_0 -> orientation[2].toDouble()   // 竖屏：roll
              Surface.ROTATION_90 -> orientation[1].toDouble()  // 横屏左：pitch
              Surface.ROTATION_180 -> (-orientation[2]).toDouble()
              Surface.ROTATION_270 -> (-orientation[1]).toDouble()
              else -> orientation[2].toDouble()
            },
          ).toFloat()
        } else {
          // 回退：加速度计（某些设备没有旋转矢量传感器）
          val x = event.values[0]
          val y = event.values[1]
          val z = event.values[2]
          rollDegrees = Math.toDegrees(
            when (rotation) {
              Surface.ROTATION_0 -> kotlin.math.atan2(x.toDouble(), z.toDouble())
              Surface.ROTATION_90 -> kotlin.math.atan2(y.toDouble(), z.toDouble())
              Surface.ROTATION_180 -> kotlin.math.atan2((-x).toDouble(), z.toDouble())
              Surface.ROTATION_270 -> kotlin.math.atan2((-y).toDouble(), z.toDouble())
              else -> kotlin.math.atan2(x.toDouble(), z.toDouble())
            },
          ).toFloat()
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    val sensorToUse = rotationVectorSensor
      ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    sensorManager.registerListener(listener, sensorToUse, SensorManager.SENSOR_DELAY_GAME)
    onDispose { sensorManager.unregisterListener(listener) }
  }

  val isLevel = abs(rollDegrees) < 1.5f
  val lineColor = if (isLevel) Color.Green.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.55f)

  Canvas(modifier = modifier.fillMaxSize()) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val lineLength = size.width * 0.3f
    val maxAngle = 30f
    val clampedRoll = rollDegrees.coerceIn(-maxAngle, maxAngle)
    val angleRad = Math.toRadians(clampedRoll.toDouble()).toFloat()

    val dx = cos(angleRad) * lineLength / 2f
    val dy = -sin(angleRad) * lineLength / 2f

    // 参考水平线（始终水平）
    drawLine(
      color = Color.White.copy(alpha = 0.3f),
      start = Offset(cx - lineLength / 2f, cy),
      end = Offset(cx + lineLength / 2f, cy),
      strokeWidth = 1f,
    )

    // 实际倾斜线
    drawLine(
      color = lineColor,
      start = Offset(cx - dx, cy - dy),
      end = Offset(cx + dx, cy + dy),
      strokeWidth = 2.5f,
    )

    // 两端圆点
    val dotRadius = 3f
    drawCircle(color = lineColor, radius = dotRadius, center = Offset(cx - dx, cy - dy))
    drawCircle(color = lineColor, radius = dotRadius, center = Offset(cx + dx, cy + dy))

    // 水平时中间绿点
    if (isLevel) {
      drawCircle(color = Color.Green, radius = 5f, center = Offset(cx, cy))
      drawCircle(
        color = Color.Green.copy(alpha = 0.3f),
        radius = 12f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f),
      )
    }
  }
}