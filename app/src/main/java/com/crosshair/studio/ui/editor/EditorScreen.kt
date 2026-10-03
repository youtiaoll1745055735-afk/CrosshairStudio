package com.crosshair.studio.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosshair.studio.data.model.PreviewBackground
import com.crosshair.studio.ui.AppViewModel
import com.crosshair.studio.ui.components.CrosshairPreview
import com.crosshair.studio.ui.components.NameInputDialog
import com.crosshair.studio.ui.theme.StudioColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    vm: AppViewModel,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val current by vm.current.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val unsaved by vm.unsaved.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showSaveAs by remember { mutableStateOf(false) }
    val tabs = listOf("样式", "基础", "中心点", "颜色与描边", "图层")

    Column(modifier.fillMaxSize()) {
        // 顶部栏
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        current.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (unsaved) {
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(6.dp).background(StudioColors.Warning, CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text("已修改", style = MaterialTheme.typography.labelSmall, color = StudioColors.Warning)
                    }
                }
                Text(
                    if (current.presetKey != null) "内置预设 · 保存时将创建副本" else "自定义配置",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioColors.TextSecondary
                )
            }
            Button(onClick = {
                if (current.presetKey == null) {
                    vm.saveCurrent()
                    scope.launch { snackbar.showSnackbar("已保存") }
                } else {
                    showSaveAs = true
                }
            }) {
                Icon(Icons.Filled.Check, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("保存")
            }
        }

        // 实时预览
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(10.dp)) {
                CrosshairPreview(
                    config = current,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    background = settings.previewBackgroundMode,
                    zoom = settings.previewZoom
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "预览背景",
                        style = MaterialTheme.typography.labelSmall,
                        color = StudioColors.TextSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    PreviewBackground.entries.toList().forEach { bg ->
                        FilterChip(
                            selected = settings.previewBackgroundMode == bg,
                            onClick = { vm.updateSettings { it.copy(previewBackground = bg.name) } },
                            label = { Text(bg.label, style = MaterialTheme.typography.labelSmall) }
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "缩放 ${(settings.previewZoom * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = StudioColors.TextSecondary
                    )
                }
                Slider(
                    value = settings.previewZoom,
                    onValueChange = { v -> vm.updateSettings { it.copy(previewZoom = v) } },
                    valueRange = 0.5f..2f
                )
            }
        }

        // 分区标签
        ScrollableTabRow(
            selectedTabIndex = tab,
            containerColor = Color.Transparent,
            edgePadding = 12.dp,
            indicator = { positions ->
                val indicatorModifier = with(TabRowDefaults) {
                    Modifier.tabIndicatorOffset(positions[tab])
                }
                TabRowDefaults.SecondaryIndicator(
                    indicatorModifier,
                    color = StudioColors.Accent
                )
            }
        ) {
            tabs.forEachIndexed { i, t ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = { Text(t, fontSize = 13.sp) },
                    selectedContentColor = StudioColors.Accent,
                    unselectedContentColor = StudioColors.TextSecondary
                )
            }
        }

        when (tab) {
            0 -> StyleSection(vm, current)
            1 -> BaseSection(vm, current)
            2 -> CenterSection(vm, current)
            3 -> ColorSection(vm, current)
            else -> LayersSection(vm, current)
        }
    }

    if (showSaveAs) {
        NameInputDialog(
            title = "另存为新配置",
            initial = "${current.name} 副本",
            onConfirm = { name ->
                val saved = vm.saveAs(name)
                scope.launch { snackbar.showSnackbar("已保存为「${saved.name}」") }
                showSaveAs = false
            },
            onDismiss = { showSaveAs = false }
        )
    }
}
