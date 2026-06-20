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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.ujizin.sample.feature.camera.model.WatermarkStyle

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
      .heightIn(max = 520.dp)
      .verticalScroll(rememberScrollState())
      .background(Color.Black.copy(alpha = 0.85f))
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // 标题栏
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
    SettingRow(
      label = "启用水印",
      checked = config.enabled,
      onCheckedChange = { onConfigChanged(config.copy(enabled = it)) },
    )

    if (config.enabled) {
      // 样式选择
      Text("样式", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        WatermarkStyle.entries.forEach { style ->
          val isSelected = config.style == style
          val label = when (style) {
            WatermarkStyle.Minimal -> "简洁"
            WatermarkStyle.Card -> "卡片"
            WatermarkStyle.FilmStrip -> "胶片"
          }
          SelectableChip(
            text = label,
            isSelected = isSelected,
            modifier = Modifier.weight(1f),
            onClick = { onConfigChanged(config.copy(style = style)) },
          )
        }
      }

      // 水印标题文字
      Text("水印标题", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
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

      // 个人签名
      Text("个人签名", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
      TextField(
        value = config.signature,
        onValueChange = { onConfigChanged(config.copy(signature = it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("输入你的签名", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) },
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
        WatermarkPosition.entries.forEach { pos ->
          val label = when (pos) {
            WatermarkPosition.BottomLeft -> "左下"
            WatermarkPosition.BottomRight -> "右下"
            WatermarkPosition.TopLeft -> "左上"
            WatermarkPosition.TopRight -> "右上"
          }
          SelectableChip(
            text = label,
            isSelected = config.position == pos,
            modifier = Modifier.weight(1f),
            onClick = { onConfigChanged(config.copy(position = pos)) },
          )
        }
      }

      // 显示选项
      SettingRow(
        label = "显示日期",
        checked = config.showDate,
        onCheckedChange = { onConfigChanged(config.copy(showDate = it)) },
      )
      SettingRow(
        label = "显示拍摄参数",
        checked = config.showCameraParams,
        onCheckedChange = { onConfigChanged(config.copy(showCameraParams = it)) },
      )
      SettingRow(
        label = "显示位置",
        checked = config.showLocation,
        onCheckedChange = { onConfigChanged(config.copy(showLocation = it)) },
      )
    }
  }
}

@Composable
private fun SettingRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, color = Color.White, fontSize = 15.sp)
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedTrackColor = MaterialTheme.colorScheme.primary,
      ),
    )
  }
}

@Composable
private fun SelectableChip(
  text: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Box(
    modifier = modifier
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
        onClick = onClick,
      )
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text, color = Color.White, fontSize = 12.sp)
  }
}
