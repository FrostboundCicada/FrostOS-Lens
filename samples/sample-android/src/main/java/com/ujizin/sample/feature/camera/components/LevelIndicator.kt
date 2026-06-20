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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * 水平仪 / 平衡仪
 *
 * 直接使用重力分量计算倾斜角，避免 getOrientation() 的万向锁问题。
 * TYPE_GRAVITY 传感器只测量重力，不受手部运动干扰。
 *
 * 原理：重力向量在屏幕坐标系中的分量
 * - 竖屏时重力在 -Y 方向，左右倾斜时 X 分量增大
 * - 横屏时重力在 -X 方向，左右倾斜时 Y 分量增大
 *
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

    // 优先使用 TYPE_GRAVITY（不受运动影响），回退到加速度计
    val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
      ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val gx = event.values[0]
        val gy = event.values[1]
        val rotation = wm.defaultDisplay.rotation

        // 根据屏幕旋转方向，选择正确的"侧向"和"垂直"分量
        // 侧向 = 屏幕左右方向的重力分量
        // 垂直 = 屏幕上下方向的重力分量
        // roll = atan2(侧向, 垂直)
        val (lateral, vertical) = when (rotation) {
          Surface.ROTATION_0 -> gx to -gy      // 竖屏：X 是左右，-Y 是上下
          Surface.ROTATION_90 -> gy to gx      // 横屏左：Y 是左右，X 是上下
          Surface.ROTATION_180 -> -gx to gy    // 反向竖屏：-X 是左右，Y 是上下
          Surface.ROTATION_270 -> -gy to -gx   // 反向横屏：-Y 是左右，-X 是上下
          else -> gx to -gy
        }

        // 只在垂直分量足够大时才计算（避免设备平放时分母为零）
        val magnitude = kotlin.math.sqrt(lateral * lateral + vertical * vertical)
        if (magnitude > 0.5f) {
          rollDegrees = Math.toDegrees(
            atan2(lateral.toDouble(), vertical.toDouble()),
          ).toFloat()
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(listener, gravitySensor, SensorManager.SENSOR_DELAY_GAME)
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