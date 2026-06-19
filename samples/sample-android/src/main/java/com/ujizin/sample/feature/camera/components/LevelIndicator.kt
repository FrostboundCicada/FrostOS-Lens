package com.ujizin.sample.feature.camera.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
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
 * 检测手机左右倾斜角度，画一条水平线
 * 倾斜 < 1° 时变绿，表示水平
 */
@Composable
fun LevelIndicator(
  modifier: Modifier = Modifier,
  visible: Boolean,
) {
  if (!visible) return
  val context = LocalContext.current
  var rollDegrees by remember { mutableFloatStateOf(0f) }

  DisposableEffect(context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        // 计算左右倾斜角度 (roll)
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        rollDegrees = Math.toDegrees(atan2(x.toDouble(), z.toDouble())).toFloat()
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
    val maxAngle = 30f // 最大倾斜角度映射
    val clampedRoll = rollDegrees.coerceIn(-maxAngle, maxAngle)
    val angleRad = Math.toRadians(clampedRoll.toDouble()).toFloat()

    // 画水平线（根据倾斜角度旋转）
    val dx = cos(angleRad) * lineLength / 2f
    val dy = -sin(angleRad) * lineLength / 2f

    // 中间短参考线（始终水平）
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

    // 中心圆点
    if (isLevel) {
      drawCircle(color = Color.Green, radius = 5f, center = Offset(cx, cy))
      drawCircle(color = Color.Green.copy(alpha = 0.3f), radius = 12f, center = Offset(cx, cy), style = Stroke(width = 1.5f))
    }
  }
}