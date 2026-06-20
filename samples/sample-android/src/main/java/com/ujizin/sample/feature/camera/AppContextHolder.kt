package com.ujizin.sample.feature.camera

import android.content.Context

/**
 * 全局 Context 持有者 — 用于 ViewModel 中访问 ContentResolver
 */
object AppContextHolder {
  lateinit var contentResolver: android.content.ContentResolver
    private set

  fun init(context: Context) {
    contentResolver = context.contentResolver
  }
}
