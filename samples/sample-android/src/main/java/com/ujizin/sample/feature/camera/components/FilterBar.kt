package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.sample.feature.camera.model.CameraFilter

/**
 * 滤镜选择栏 — 底部横向滚动
 * 每个滤镜项显示名称，选中项高亮
 */
@Composable
fun FilterBar(
  modifier: Modifier = Modifier,
  currentFilter: CameraFilter,
  onFilterChanged: (CameraFilter) -> Unit,
) {
  LazyRow(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 4.dp),
  ) {
    items(CameraFilter.values()) { filter ->
      FilterChip(
        filter = filter,
        isSelected = currentFilter == filter,
        onClick = { onFilterChanged(filter) },
      )
    }
  }
}

@Composable
private fun FilterChip(
  filter: CameraFilter,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val bgColor = if (isSelected) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
  } else {
    Color.White.copy(alpha = 0.12f)
  }
  val borderColor = if (isSelected) {
    MaterialTheme.colorScheme.primary
  } else {
    Color.White.copy(alpha = 0.2f)
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(20.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
      )
      .padding(horizontal = 14.dp, vertical = 7.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = filter.displayName,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = Color.White,
    )
  }
}
