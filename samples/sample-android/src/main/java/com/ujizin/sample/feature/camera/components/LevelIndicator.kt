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
 *
 * 使用 ROTATION_VECTOR 传感器 + remapCoordinateSystem 正确映射屏幕方向。
 * remapCoordinateSystem 会将设备坐标系转换到屏幕坐标系，
 * 这样 getOrientation()[2] 始终表示屏幕绕"指向用户"轴的旋转（即左右倾斜），
 * 无论设备是竖屏、横屏还是反向，都能正确测量。
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

    val rotationVectorSensor =
      sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val rotation = wm.defaultDisplay.rotation

        if (rotationVectorSensor != null && event.sensor.type == rotationVectorSensor.type) {
          // 1. 从旋转向量获取旋转矩阵（设备坐标系 → 世界坐标系）
          val rotationMatrix = FloatArray(9)
          SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

          // 2. 根据屏幕旋转方向重映射坐标轴
          //    remapCoordinateSystem 将设备坐标系转换到屏幕坐标系
          //    这样 getOrientation() 返回的角度就是相对于屏幕的
          val remappedMatrix = FloatArray(9)
          val (outX, outY) = when (rotation) {
            Surface.ROTATION_0 ->
              SensorManager.AXIS_X to SensorManager.AXIS_Y
            Surface.ROTATION_90 ->
              SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 ->
              SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 ->
              SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else ->
              SensorManager.AXIS_X to SensorManager.AXIS_Y
          }
          SensorManager.remapCoordinateSystem(
            rotationMatrix, outX, outY, remappedMatrix,
          )

          // 3. 获取方向角 — orientation[2] 现在始终是屏幕的 roll（左右倾斜）
          val orientation = FloatArray(3)
          SensorManager.getOrientation(remappedMatrix, orientation)
          rollDegrees = Math.toDegrees(orientation[2].toDouble()).toFloat()
        } else {
          // 回退：加速度计
          val x = event.values[0]
          val y = event.values[1]
          rollDegrees = when (rotation) {
            Surface.ROTATION_0 ->
              Math.toDegrees(kotlin.math.atan2(x.toDouble(), y.toDouble())).toFloat()
            Surface.ROTATION_90 ->
              Math.toDegrees(kotlin.math.atan2(y.toDouble(), (-x).toDouble())).toFloat()
            Surface.ROTATION_180 ->
              Math.toDegrees(kotlin.math.atan2((-x).toDouble(), (-y).toDouble())).toFloat()
            Surface.ROTATION_270 ->
              Math.toDegrees(kotlin.math.atan2((-y).toDouble(), x.toDouble())).toFloat()
            else ->
              Math.toDegrees(kotlin.math.atan2(x.toDouble(), y.toDouble())).toFloat()
          }
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