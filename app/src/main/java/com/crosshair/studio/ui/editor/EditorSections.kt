package com.crosshair.studio.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crosshair.studio.data.Presets
import com.crosshair.studio.data.model.CenterStyle
import com.crosshair.studio.data.model.CrosshairConfig
import com.crosshair.studio.data.model.Layer
import com.crosshair.studio.data.model.LayerType
import com.crosshair.studio.data.model.PreviewBackground
import com.crosshair.studio.ui.AppViewModel
import com.crosshair.studio.ui.components.ColorPickerCard
import com.crosshair.studio.ui.components.ColorSwatch
import com.crosshair.studio.ui.components.CrosshairPreview
import com.crosshair.studio.ui.components.ParamSlider
import com.crosshair.studio.ui.components.SectionCard
import com.crosshair.studio.ui.theme.StudioColors
import com.crosshair.studio.core.toComposeColor

// ---------------- 样式 ----------------

@Composable
fun StyleSection(vm: AppViewModel, current: CrosshairConfig) {
    val presets = Presets.all()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            BlankPresetCard(
                selected = current.layers.isEmpty() && !current.center.visible,
                onClick = { vm.clearAllLayers() }
            )
        }
        items(presets) { p ->
            val key = p.presetKey
            PresetCard(
                preset = p,
                selected = current.presetKey == key,
                onClick = { if (key != null) vm.applyPreset(key) }
            )
        }
    }
}

@Composable
private fun PresetCard(preset: CrosshairConfig, selected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.border(1.5.dp, StudioColors.Accent, RoundedCornerShape(14.dp))
                else Modifier
            )
    ) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box {
                CrosshairPreview(
                    config = preset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    background = PreviewBackground.DARK,
                    autoFit = true
                )
                if (selected) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(8.dp)
                            .background(StudioColors.Accent, CircleShape)
                    )
                }
            }
            Text(preset.name, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun BlankPresetCard(selected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.border(1.5.dp, StudioColors.Accent, RoundedCornerShape(14.dp))
                else Modifier
            )
    ) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioColors.Surface2),
                contentAlignment = Alignment.Center
            ) {
                Text("∅", style = MaterialTheme.typography.titleLarge, color = StudioColors.TextSecondary)
            }
            Text("空白准心", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ---------------- 基础参数 ----------------

@Composable
fun BaseSection(vm: AppViewModel, c: CrosshairConfig) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionCard("基础参数") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ParamSlider("整体缩放", c.baseScale, 0.2f..3f, 1f, { "%.2f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(baseScale = v) }
                    }
                    ParamSlider("线条长度", c.lineLength, 2f..120f, 24f, { "%.0f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(
                            lineLength = v,
                            layers = it.layers.map { l ->
                                if (l.layerType == LayerType.LINES) l.copy(length = v) else l
                            }
                        ) }
                    }
                    ParamSlider("线条粗细", c.lineThickness, 0.5f..30f, 3f, { "%.1f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(
                            lineThickness = v,
                            layers = it.layers.map { l ->
                                if (l.layerType?.isStrokeType == true) l.copy(thickness = v) else l
                            }
                        ) }
                    }
                    ParamSlider("中心间距", c.gap, 0f..60f, 6f, { "%.0f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(
                            gap = v,
                            layers = it.layers.map { l ->
                                if (l.layerType == LayerType.LINES) l.copy(gap = v) else l
                            }
                        ) }
                    }
                    ParamSlider("整体透明度", c.baseAlpha, 0.05f..1f, 1f, { "${(it * 100).toInt()}%" }) { v ->
                        vm.updateCurrent { it.copy(baseAlpha = v) }
                    }
                    ParamSlider("旋转角度", c.baseRotation, -180f..180f, 0f, { "${it.toInt()}°" }) { v ->
                        vm.updateCurrent { it.copy(baseRotation = v) }
                    }
                    ParamSlider("水平偏移", c.baseOffsetX, -100f..100f, 0f, { "%.0f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(baseOffsetX = v) }
                    }
                    ParamSlider("垂直偏移", c.baseOffsetY, -100f..100f, 0f, { "%.0f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(baseOffsetY = v) }
                    }
                }
            }
        }
    }
}

// ---------------- 中心点 ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterSection(vm: AppViewModel, c: CrosshairConfig) {
    val center = c.center
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionCard("中心点") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("显示中心点", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "中心点独立于线条图层",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioColors.TextSecondary
                        )
                    }
                    Switch(
                        checked = center.visible,
                        onCheckedChange = { v -> vm.updateCurrent { it.copy(center = it.center.copy(visible = v)) } }
                    )
                }
                if (center.visible) {
                    CenterStyle.entries.toList().chunked(3).forEach { rowStyles ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowStyles.forEach { st ->
                                val sel = center.centerStyle == st
                                FilterChip(
                                    selected = sel,
                                    onClick = { vm.updateCurrent { it.copy(center = it.center.copy(style = st.name)) } },
                                    label = { Text(st.label, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    ParamSlider("大小", center.size, 2f..40f, 8f, { "%.0f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(center = it.center.copy(size = v)) }
                    }
                }
            }
        }
        if (center.visible) {
            item {
                ColorPickerCard(
                    title = "中心点颜色",
                    color = center.color,
                    alpha = center.alpha,
                    onColor = { col -> vm.updateCurrent { it.copy(center = it.center.copy(color = col)) } },
                    onAlpha = { a -> vm.updateCurrent { it.copy(center = it.center.copy(alpha = a)) } }
                )
            }
            item {
                SectionCard("中心点描边") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("启用描边", style = MaterialTheme.typography.titleSmall)
                        }
                        Switch(
                            checked = center.strokeEnabled,
                            onCheckedChange = { v ->
                                vm.updateCurrent { it.copy(center = it.center.copy(strokeEnabled = v)) }
                            }
                        )
                    }
                    if (center.strokeEnabled) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "描边颜色",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StudioColors.TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            ColorSwatch(center.strokeColor) { col ->
                                vm.updateCurrent { it.copy(center = it.center.copy(strokeColor = col)) }
                            }
                        }
                        ParamSlider("描边宽度", center.strokeWidth, 0.5f..10f, 2f, { "%.1f".format(it) }) { v ->
                            vm.updateCurrent { it.copy(center = it.center.copy(strokeWidth = v)) }
                        }
                    }
                }
            }
            item {
                SectionCard("中心点偏移") {
                    ParamSlider("水平偏移", center.offsetX, -40f..40f, 0f, { "%.0f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(center = it.center.copy(offsetX = v)) }
                    }
                    ParamSlider("垂直偏移", center.offsetY, -40f..40f, 0f, { "%.0f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(center = it.center.copy(offsetY = v)) }
                    }
                }
            }
        }
    }
}

// ---------------- 颜色与描边 ----------------

@Composable
fun ColorSection(vm: AppViewModel, c: CrosshairConfig) {
    val strokeLayers = c.layers.filter { it.layerType?.isStrokeType == true }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionCard("线条颜色") {
                Text(
                    "应用到所有线条类图层",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioColors.TextSecondary
                )
                if (strokeLayers.isEmpty()) {
                    Text(
                        "当前没有线条类图层，请到「图层」页添加",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioColors.Warning
                    )
                }
            }
        }
        if (strokeLayers.isNotEmpty()) {
            item {
                ColorPickerCard(
                    title = "线条颜色",
                    color = strokeLayers.first().color,
                    alpha = strokeLayers.first().alpha,
                    onColor = { col ->
                        vm.updateCurrent { it.copy(
                            layers = it.layers.map { l ->
                                if (l.layerType?.isStrokeType == true) l.copy(color = col) else l
                            }
                        ) }
                    },
                    onAlpha = { a ->
                        vm.updateCurrent { it.copy(
                            layers = it.layers.map { l ->
                                if (l.layerType?.isStrokeType == true) l.copy(alpha = a) else l
                            }
                        ) }
                    }
                )
            }
        }
        if (strokeLayers.isNotEmpty()) {
            item {
                SectionCard("描边") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("启用描边", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "为线条增加对比描边，提高可见度",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioColors.TextSecondary
                            )
                        }
                        Switch(
                            checked = strokeLayers.all { it.strokeEnabled },
                            onCheckedChange = { v ->
                                vm.updateCurrent { it.copy(
                                    layers = it.layers.map { l ->
                                        if (l.layerType?.isStrokeType == true) l.copy(strokeEnabled = v) else l
                                    }
                                ) }
                            }
                        )
                    }
                    if (strokeLayers.any { it.strokeEnabled }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "描边颜色",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StudioColors.TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            ColorSwatch(strokeLayers.first { it.strokeEnabled }.strokeColor) { col ->
                                vm.updateCurrent { it.copy(
                                    layers = it.layers.map { l ->
                                        if (l.layerType?.isStrokeType == true) l.copy(strokeColor = col) else l
                                    }
                                ) }
                            }
                        }
                        ParamSlider(
                            "描边宽度",
                            strokeLayers.first { it.strokeEnabled }.strokeWidth,
                            0.5f..12f, 2f, { "%.1f".format(it) }
                        ) { v ->
                            vm.updateCurrent { it.copy(
                                layers = it.layers.map { l ->
                                    if (l.layerType?.isStrokeType == true) l.copy(strokeWidth = v) else l
                                }
                            ) }
                        }
                    }
                }
            }
        }
        item {
            SectionCard("阴影") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("启用阴影", style = MaterialTheme.typography.titleSmall)
                    }
                    Switch(
                        checked = c.shadowEnabled,
                        onCheckedChange = { v -> vm.updateCurrent { it.copy(shadowEnabled = v) } }
                    )
                }
                if (c.shadowEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "阴影颜色",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StudioColors.TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        ColorSwatch(c.shadowColor) { col ->
                            vm.updateCurrent { it.copy(shadowColor = col) }
                        }
                    }
                    ParamSlider("模糊半径", c.shadowRadius, 0f..40f, 6f, { "%.0f".format(it) }) { v ->
                        vm.updateCurrent { it.copy(shadowRadius = v) }
                    }
                }
            }
        }
    }
}

// ---------------- 图层 ----------------

private fun layerTypeIcon(type: LayerType): ImageVector = when (type) {
    LayerType.LINES -> Icons.Filled.Tune
    LayerType.CROSS -> Icons.Filled.Add
    LayerType.DOT, LayerType.CIRCLE_FILLED -> Icons.Filled.Circle
    LayerType.RING -> Icons.Filled.RadioButtonUnchecked
    LayerType.DIAMOND, LayerType.TRIANGLE -> Icons.Filled.ChangeHistory
    LayerType.RECT -> Icons.Filled.CropSquare
    LayerType.BRACKET -> Icons.Filled.CropFree
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayersSection(vm: AppViewModel, c: CrosshairConfig) {
    var selectedId by rememberSaveable { mutableStateOf(c.layers.firstOrNull()?.id) }
    val selected = c.layers.find { it.id == selectedId }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionCard("添加图层") {
                Text(
                    "选择图层类型添加到当前准心",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioColors.TextSecondary
                )
                LayerType.entries.toList().chunked(3).forEach { rowTypes ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowTypes.forEach { t ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val layer = Layer(
                                        name = t.label,
                                        type = t.name,
                                        size = when (t) {
                                            LayerType.RING -> 30f
                                            LayerType.BRACKET -> 40f
                                            else -> 20f
                                        },
                                        length = c.lineLength,
                                        thickness = c.lineThickness,
                                        gap = c.gap
                                    )
                                    vm.updateCurrent { it.copy(layers = it.layers + layer) }
                                    selectedId = layer.id
                                },
                                label = { Text(t.label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        items(c.layers, key = { it.id }) { layer ->
            LayerRow(
                layer = layer,
                selected = layer.id == selectedId,
                onSelect = { selectedId = layer.id },
                onToggleVisible = {
                    vm.updateCurrent { cur ->
                        cur.copy(layers = cur.layers.map { if (it.id == layer.id) it.copy(visible = !it.visible) else it })
                    }
                },
                onDuplicate = {
                    vm.updateCurrent { cur ->
                        cur.copy(layers = cur.layers + layer.copy(id = java.util.UUID.randomUUID().toString(), name = layer.name + " 副本"))
                    }
                },
                onDelete = {
                    vm.updateCurrent { cur -> cur.copy(layers = cur.layers.filter { it.id != layer.id }) }
                    if (selectedId == layer.id) selectedId = null
                }
            )
        }
        if (selected != null) {
            item {
                LayerParamsCard(vm, selected)
            }
        } else if (c.layers.isEmpty()) {
            item {
                Text(
                    "暂无图层。点击上方按钮添加，或到「样式」页选择预设。",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LayerRow(
    layer: Layer,
    selected: Boolean,
    onSelect: () -> Unit,
    onToggleVisible: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .then(
                if (selected) Modifier.border(1.5.dp, StudioColors.Accent, RoundedCornerShape(12.dp))
                else Modifier
            )
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleVisible, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (layer.visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    if (layer.visible) "隐藏" else "显示",
                    Modifier.size(17.dp),
                    tint = if (layer.visible) StudioColors.Accent else StudioColors.TextSecondary
                )
            }
            layer.layerType?.let {
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(StudioColors.Surface2),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(layerTypeIcon(it), null, Modifier.size(14.dp), tint = StudioColors.TextSecondary)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    layer.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (layer.visible) "透明度 ${(layer.alpha * 100).toInt()}%" else "已隐藏",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioColors.TextSecondary
                )
            }
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(layer.color.toComposeColor())
                    .border(1.dp, StudioColors.Outline, CircleShape)
            )
            IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.ContentCopy, "复制", Modifier.size(16.dp), tint = StudioColors.TextSecondary)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, "删除", Modifier.size(16.dp), tint = StudioColors.Danger)
            }
        }
    }
}

@Composable
private fun LayerParamsCard(vm: AppViewModel, layer: Layer) {
    val t = layer.layerType ?: return

    fun updateLayer(transform: (Layer) -> Layer) {
        vm.updateCurrent { c ->
            c.copy(layers = c.layers.map { if (it.id == layer.id) transform(it) else it })
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionCard("图层参数 · ${t.label}") {
            ParamSlider("尺寸", layer.size, 2f..120f, 20f, { "%.0f".format(it) }) { v ->
                updateLayer { it.copy(size = v) }
            }
            if (t.isStrokeType) {
                ParamSlider("粗细", layer.thickness, 0.5f..30f, 3f, { "%.1f".format(it) }) { v ->
                    updateLayer { it.copy(thickness = v) }
                }
            }
            if (t == LayerType.LINES || t == LayerType.CROSS || t == LayerType.BRACKET) {
                ParamSlider("长度", layer.length, 2f..120f, 24f, { "%.0f".format(it) }) { v ->
                    updateLayer { it.copy(length = v) }
                }
            }
            if (t == LayerType.LINES) {
                ParamSlider("中心间距", layer.gap, 0f..80f, 6f, { "%.0f".format(it) }) { v ->
                    updateLayer { it.copy(gap = v) }
                }
            }
            ParamSlider("旋转", layer.rotation, -180f..180f, 0f, { "${it.toInt()}°" }) { v ->
                updateLayer { it.copy(rotation = v) }
            }
            ParamSlider("水平偏移", layer.offsetX, -60f..60f, 0f, { "%.0f".format(it) }) { v ->
                updateLayer { it.copy(offsetX = v) }
            }
            ParamSlider("垂直偏移", layer.offsetY, -60f..60f, 0f, { "%.0f".format(it) }) { v ->
                updateLayer { it.copy(offsetY = v) }
            }
            ParamSlider("透明度", layer.alpha, 0.05f..1f, 1f, { "${(it * 100).toInt()}%" }) { v ->
                updateLayer { it.copy(alpha = v) }
            }
        }

        ColorPickerCard(
            title = "图层颜色",
            color = layer.color,
            alpha = layer.alpha,
            onColor = { col -> updateLayer { it.copy(color = col) } },
            onAlpha = { a -> updateLayer { it.copy(alpha = a) } }
        )

        SectionCard("图层描边") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("启用描边", style = MaterialTheme.typography.titleSmall)
                }
                Switch(
                    checked = layer.strokeEnabled,
                    onCheckedChange = { v -> updateLayer { it.copy(strokeEnabled = v) } }
                )
            }
            if (layer.strokeEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "描边颜色",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioColors.TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    ColorSwatch(layer.strokeColor) { col -> updateLayer { it.copy(strokeColor = col) } }
                }
                ParamSlider("描边宽度", layer.strokeWidth, 0.5f..12f, 2f, { "%.1f".format(it) }) { v ->
                    updateLayer { it.copy(strokeWidth = v) }
                }
            }
        }
    }
}
