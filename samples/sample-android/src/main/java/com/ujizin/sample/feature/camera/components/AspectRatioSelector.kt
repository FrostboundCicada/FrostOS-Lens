package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.liquidGlass
import com.ujizin.sample.feature.camera.model.AspectRatioOption

/**
 * 照片/视频比例选择器组件
 * 支持 1:1, 4:3, 16:9 和全屏模式
 */
@Composable
fun AspectRatioSelector(
    modifier: Modifier = Modifier,
    currentRatio: AspectRatioOption,
    onRatioChanged: (AspectRatioOption) -> Unit,
) {
    Row(
        modifier = modifier
            .cloudy(radius = 16)
            .liquidGlass(lensCenter = Offset(0f, 0f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AspectRatioOption.values().forEach { option ->
            val isSelected = currentRatio == option
            AspectRatioButton(
                option = option,
                isSelected = isSelected,
                onClick = { onRatioChanged(option) },
            )
        }
    }
}

@Composable
private fun AspectRatioButton(
    option: AspectRatioOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) Color.White.copy(alpha = 0.3f) else Color.Transparent
    val textColor = if (isSelected) Color.Yellow else Color.White

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = option.titleRes),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = textColor,
            maxLines = 1,
        )
    }
}
