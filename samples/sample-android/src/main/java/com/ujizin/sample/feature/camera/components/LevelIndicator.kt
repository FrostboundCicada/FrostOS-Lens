package com.ujizin.sample.feature.camera.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * 水平仪 / 平衡仪
 * 根据屏幕旋转方向自动适配，支持竖屏、横屏、反向竖屏、反向横屏
 * 倾斜 < 1.5° 时变绿，表示水平
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
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // 根据屏幕旋转方向选择正确的轴来计算倾斜角
        val displayRotation = context.display?.rotation ?: Surface.ROTATION_0
        rollDegrees = when (displayRotation) {
          // 竖屏 (正常): 绕 Y 轴旋转，使用 atan2(x, z)
          Surface.ROTATION_0 -> Math.toDegrees(atan2(x.toDouble(), z.toDouble())).toFloat()
          // 横屏 (左转90°): 绕 X 轴旋转，使用 atan2(y, z)
          Surface.ROTATION_90 -> Math.toDegrees(atan2(y.toDouble(), z.toDouble())).toFloat()
          // 反向竖屏 (180°): 绕 Y 轴旋转，但 X 轴反向
          Surface.ROTATION_180 -> Math.toDegrees(atan2((-x).toDouble(), z.toDouble())).toFloat()
          // 反向横屏 (右转90°): 绕 X 轴旋转，但 Y 轴反向
          Surface.ROTATION_270 -> Math.toDegrees(atan2((-y).toDouble(), z.toDouble())).toFloat()
          else -> Math.toDegrees(atan2(x.toDouble(), z.toDouble())).toFloat()
        }
      }
      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    onDispose { sensorManager.unregisterListener(listener) }
  }

  val isLevel = abs(rollDegrees) < 1.5f
  val lineColor = if (isLevel) Color.Green.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f)

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

    // 两端小圆点
    val dotRadius = 3f
    drawCircle(color = lineColor, radius = dotRadius, center = Offset(cx - dx, cy - dy))
    drawCircle(color = lineColor, radius = dotRadius, center = Offset(cx + dx, cy + dy))

    // 中心圆点 - 水平时显示
    if (isLevel) {
      drawCircle(color = Color.Green, radius = 5f, center = Offset(cx, cy))
      drawCircle(color = Color.Green.copy(alpha = 0.3f), radius = 12f, center = Offset(cx, cy), style = Stroke(width = 1.5f))
    }
  }
}