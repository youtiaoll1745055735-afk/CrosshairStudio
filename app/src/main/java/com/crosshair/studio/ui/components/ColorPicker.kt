package com.crosshair.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.crosshair.studio.core.argbLong
import com.crosshair.studio.core.blue
import com.crosshair.studio.core.green
import com.crosshair.studio.core.parseHex
import com.crosshair.studio.core.red
import com.crosshair.studio.core.toComposeColor
import com.crosshair.studio.core.toHexString
import com.crosshair.studio.ui.theme.StudioColors

/** 常用颜色预设 */
val PresetColors: List<Long> = listOf(
    0xFFFFFFFF, 0xFF9CA3AF, 0xFF000000, 0xFFEF4444, 0xFFF97316, 0xFFFACC15,
    0xFF4ADE80, 0xFF22D3EE, 0xFF3B82F6, 0xFF8B5CF6, 0xFFEC4899, 0xFFF43F5E,
    0xFF84CC16, 0xFF14B8A6, 0xFF0EA5E9, 0xFF6366F1, 0xFFD946EF, 0xFFFBBF24,
    0xFFFF6B00, 0xFF00E676, 0xFF40C4FF, 0xFFFFEA00, 0xFFFF4081, 0xFF651FFF
)

@Composable
fun ColorPickerCard(
    title: String,
    color: Long,
    alpha: Float,
    onColor: (Long) -> Unit,
    onAlpha: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = StudioColors.Accent,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.toComposeColor())
                        .border(1.dp, StudioColors.Outline, RoundedCornerShape(8.dp))
                )
            }
            ColorPickerBody(color = color, alpha = alpha, onColor = onColor, onAlpha = onAlpha)
        }
    }
}

/** 颜色选择主体：HEX 输入 + 预设色板 + RGB 滑块 + 透明度。 */
@Composable
fun ColorPickerBody(
    color: Long,
    alpha: Float,
    onColor: (Long) -> Unit,
    onAlpha: (Float) -> Unit,
) {
    HexField(color, onColor)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PresetColors.chunked(8).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowColors.forEach { c ->
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(c.toComposeColor())
                            .border(
                                1.dp,
                                if (c == color) StudioColors.Accent else StudioColors.Outline,
                                CircleShape
                            )
                            .clickable { onColor(c) }
                    )
                }
            }
        }
    }

    val r = color.red()
    val g = color.green()
    val b = color.blue()
    ParamSlider("R", r.toFloat(), 0f..255f, 255f, { "%.0f".format(it) }) { v ->
        onColor(argbLong(0xFF, v.toInt(), g, b))
    }
    ParamSlider("G", g.toFloat(), 0f..255f, 255f, { "%.0f".format(it) }) { v ->
        onColor(argbLong(0xFF, r, v.toInt(), b))
    }
    ParamSlider("B", b.toFloat(), 0f..255f, 255f, { "%.0f".format(it) }) { v ->
        onColor(argbLong(0xFF, r, g, v.toInt()))
    }
    ParamSlider("透明度", alpha, 0.05f..1f, 1f, { "${(it * 100).toInt()}%" }) { onAlpha(it) }
}

@Composable
fun HexField(color: Long, onColor: (Long) -> Unit) {
    var text by remember(color) { mutableStateOf(color.toHexString()) }
    var error by remember(color) { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
                .filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
                .take(6)
                .uppercase()
            error = text.length == 6 && parseHex(text) == null
            if (text.length == 6) parseHex(text)?.let(onColor)
        },
        label = { Text("HEX 色值") },
        prefix = { Text("#", color = StudioColors.TextSecondary) },
        singleLine = true,
        isError = error,
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        modifier = Modifier.fillMaxWidth()
    )
}

/** 小型颜色选择入口：点击色块弹出完整选择对话框。 */
@Composable
fun ColorSwatch(
    color: Long,
    modifier: Modifier = Modifier,
    onPick: (Long) -> Unit,
) {
    var show by remember { mutableStateOf(false) }
    Box(
        modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.toComposeColor())
            .border(1.dp, StudioColors.Outline, RoundedCornerShape(8.dp))
            .clickable { show = true }
    )
    if (show) {
        ColorPickerDialog(
            initial = color,
            onConfirm = { c -> onPick(c); show = false },
            onDismiss = { show = false }
        )
    }
}

@Composable
fun ColorPickerDialog(
    initial: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var c by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择颜色") },
        text = {
            ColorPickerBody(color = c, alpha = 1f, onColor = { c = it }, onAlpha = {})
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(c) }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
