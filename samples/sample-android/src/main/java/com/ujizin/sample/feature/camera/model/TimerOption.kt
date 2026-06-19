package com.ujizin.sample.feature.camera.model

import androidx.annotation.StringRes
import com.ujizin.sample.R

/**
 * 定时拍摄选项
 */
enum class TimerOption(
  @StringRes val titleRes: Int,
  val seconds: Int,
) {
  Off(R.string.timer_off, 0),
  ThreeSec(R.string.timer_3s, 3),
  FiveSec(R.string.timer_5s, 5),
  TenSec(R.string.timer_10s, 10),
  ;

  companion object {
    val Default = Off
  }
}