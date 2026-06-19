package com.ujizin.sample.feature.camera.model

import androidx.annotation.StringRes
import com.ujizin.sample.R

/**
 * 照片/视频比例选项
 */
enum class AspectRatioOption(
    @StringRes val titleRes: Int,
    val ratio: Float,
) {
    /** 1:1 正方形 */
    Square(R.string.aspect_ratio_1_1, 1f),

    /** 4:3 标准照片比例 */
    Standard(R.string.aspect_ratio_4_3, 4f / 3f),

    /** 16:9 宽屏比例 */
    Wide(R.string.aspect_ratio_16_9, 16f / 9f),

    /** 全屏自适应 */
    FullScreen(R.string.aspect_ratio_fullscreen, 0f), // 0 表示全屏自适应
    ;

    companion object {
        val Default = Standard
    }
}
