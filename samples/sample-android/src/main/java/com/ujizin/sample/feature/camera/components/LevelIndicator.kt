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
import kotlin.math.sqrt

/**
 * 水平仪 / 平衡仪
 *
 * 直接使用重力分量计算倾斜角，避免 getOrientation() 的万向锁问题。
 * TYPE_GRAVITY 传感器只测量重力，不受手部运动干扰。
 *
 * 坐标系推导（设备自然方向为竖屏）：
 * - 传感器 X 轴：设备右边 → 传感器 Y 轴：设备上边
 * - 竖屏时重力在 -Y，横屏时重力在 -X 或 +X
 *
 * 对每个屏幕旋转方向，将"屏幕侧向"和"屏幕垂直"映射到传感器轴：
 * - lateral > 0 = 屏幕向右倾斜
 * - vertical > 0 = 设备直立（水平时 vertical 最大）
 * - roll = atan2(lateral, vertical)
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

        // 根据屏幕旋转方向，将传感器轴映射到屏幕的"侧向"和"垂直"方向
        // lateral: 屏幕左右方向的重力分量（正=右倾）
        // vertical: 屏幕上下方向的重力分量（正=直立）
        val (lateral, vertical) = when (rotation) {
          // 竖屏：设备 +X = 屏幕右，设备 -Y = 屏幕上
          // 重力在 -Y，直立时 vertical = -gy > 0
          Surface.ROTATION_0 -> gx to -gy

          // 横屏（设备逆时针转 90°）：设备 +X = 屏幕上，设备 -Y = 屏幕右
          // 重力在 -X，直立时 vertical = -gx > 0
          // 右倾时重力偏向 -Y，lateral = -gy > 0
          Surface.ROTATION_90 -> -gy to -gx

          // 反向竖屏：设备 +X = 屏幕左，设备 -Y = 屏幕下
          // 重力在 +Y，直立时 vertical = gy > 0
          Surface.ROTATION_180 -> -gx to gy

          // 反向横屏（设备顺时针转 90°）：设备 +X = 屏幕下，设备 +Y = 屏幕右
          // 重力在 +X，直立时 vertical = gx > 0
          // 右倾时重力偏向 +Y，lateral = gy > 0
          Surface.ROTATION_270 -> gy to gx

          else -> gx to -gy
        }

        // 只在垂直分量足够大时才计算（避免设备平放时分母为零）
        val magnitude = sqrt(lateral * lateral + vertical * vertical)
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
  val lineColor = if (isLevel) Color(0xFF00E676) else Color.White.copy(alpha = 0.7f)

  Canvas(modifier = modifier.fillMaxSize()) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val lineLength = size.minDimension * 0.22f
    val maxAngle = 45f
    val clampedRoll = rollDegrees.coerceIn(-maxAngle, maxAngle)
    val angleRad = Math.toRadians(clampedRoll.toDouble()).toFloat()

    val dx = cos(angleRad) * lineLength / 2f
    val dy = -sin(angleRad) * lineLength / 2f

    // 参考水平线（始终水平）— 两侧带刻度
    val refAlpha = 0.25f
    drawLine(
      color = Color.White.copy(alpha = refAlpha),
      start = Offset(cx - lineLength / 2f, cy),
      end = Offset(cx + lineLength / 2f, cy),
      strokeWidth = 1f,
    )

    // 刻度标记（±15°, ±30°, ±45°）
    for (tickAngle in listOf(-45f, -30f, -15f, 15f, 30f, 45f)) {
      val tickRad = Math.toRadians(tickAngle.toDouble()).toFloat()
      val tickR = lineLength / 2f + 6f
      val tickX = cx + cos(tickRad) * tickR
      val tickY = cy - sin(tickRad) * tickR
      drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = 2f,
        center = Offset(tickX, tickY),
      )
    }

    // 实际倾斜线
    drawLine(
      color = lineColor,
      start = Offset(cx - dx, cy - dy),
      end = Offset(cx + dx, cy + dy),
      strokeWidth = 3f,
    )

    // 两端圆点
    val dotRadius = 4f
    drawCircle(color = lineColor, radius = dotRadius, center = Offset(cx - dx, cy - dy))
    drawCircle(color = lineColor, radius = dotRadius, center = Offset(cx + dx, cy + dy))

    // 中心点
    drawCircle(
      color = Color.White.copy(alpha = 0.5f),
      radius = 2f,
      center = Offset(cx, cy),
    )

    // 水平时中间绿点 + 光环
    if (isLevel) {
      drawCircle(color = Color(0xFF00E676), radius = 6f, center = Offset(cx, cy))
      drawCircle(
        color = Color(0xFF00E676).copy(alpha = 0.3f),
        radius = 14f,
        center = Offset(cx, cy),
        style = Stroke(width = 2f),
      )
    }
  }
}
