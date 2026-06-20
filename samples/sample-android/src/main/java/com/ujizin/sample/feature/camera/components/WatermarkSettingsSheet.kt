package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.sample.feature.camera.model.WatermarkConfig
import com.ujizin.sample.feature.camera.model.WatermarkPosition

/**
 * 水印设置面板 — 从底部弹出
 */
@Composable
fun WatermarkSettingsSheet(
  modifier: Modifier = Modifier,
  config: WatermarkConfig,
  onConfigChanged: (WatermarkConfig) -> Unit,
  onDismiss: () -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color.Black.copy(alpha = 0.7f))
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "水印设置",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
      )
      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.15f))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onDismiss,
          )
          .padding(horizontal = 12.dp, vertical = 6.dp),
      ) {
        Text("完成", color = Color.White, fontSize = 14.sp)
      }
    }

    // 启用开关
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("启用水印", color = Color.White, fontSize = 15.sp)
      Switch(
        checked = config.enabled,
        onCheckedChange = { onConfigChanged(config.copy(enabled = it)) },
        colors = SwitchDefaults.colors(
          checkedTrackColor = MaterialTheme.colorScheme.primary,
        ),
      )
    }

    if (config.enabled) {
      // 水印文字
      Text("水印文字", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
      TextField(
        value = config.text,
        onValueChange = { onConfigChanged(config.copy(text = it)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = Color.White.copy(alpha = 0.1f),
          unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          cursorColor = MaterialTheme.colorScheme.primary,
        ),
        singleLine = true,
      )

      // 位置选择
      Text("位置", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        WatermarkPosition.values().forEach { pos ->
          val isSelected = config.position == pos
          val label = when (pos) {
            WatermarkPosition.BottomLeft -> "左下"
            WatermarkPosition.BottomRight -> "右下"
            WatermarkPosition.TopLeft -> "左上"
            WatermarkPosition.TopRight -> "右上"
          }
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else Color.White.copy(alpha = 0.1f),
              )
              .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(8.dp),
              )
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onConfigChanged(config.copy(position = pos)) },
              )
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(label, color = Color.White, fontSize = 12.sp)
          }
        }
      }

      // 显示日期
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("显示日期", color = Color.White, fontSize = 15.sp)
        Switch(
          checked = config.showDate,
          onCheckedChange = { onConfigChanged(config.copy(showDate = it)) },
          colors = SwitchDefaults.colors(
            checkedTrackColor = MaterialTheme.colorScheme.primary,
          ),
        )
      }

      // 显示位置
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("显示位置", color = Color.White, fontSize = 15.sp)
        Switch(
          checked = config.showLocation,
          onCheckedChange = { onConfigChanged(config.copy(showLocation = it)) },
          colors = SwitchDefaults.colors(
            checkedTrackColor = MaterialTheme.colorScheme.primary,
          ),
        )
      }
    }
  }
}
