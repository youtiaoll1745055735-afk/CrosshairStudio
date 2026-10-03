package com.crosshair.studio.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crosshair.studio.ui.theme.StudioColors

/**
 * 参数滑块组件：拖动调节 + 数值显示 + 点击数值精确输入 + 一键恢复默认。
 */
@Composable
fun ParamSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    defaultValue: Float,
    format: (Float) -> String,
    onChange: (Float) -> Unit,
) {
    var showInput by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = StudioColors.TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = format(value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = StudioColors.Accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showInput = true }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            IconButton(
                onClick = { onChange(defaultValue) },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "恢复默认",
                    tint = StudioColors.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showInput) {
        NumberInputDialog(
            title = label,
            initial = value,
            range = range,
            format = format,
            onConfirm = { v -> onChange(v); showInput = false },
            onDismiss = { showInput = false }
        )
    }
}

@Composable
fun NumberInputDialog(
    title: String,
    initial: Float,
    range: ClosedFloatingPointRange<Float>,
    format: (Float) -> String,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(format(initial)) }
    val parsed = text.toFloatOrNull()?.takeIf { it in range }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    text = input.filter { ch -> ch.isDigit() || ch == '.' || ch == '-' }.take(8)
                },
                label = { Text("数值（${format(range.start)} ~ ${format(range.endInclusive)}）") },
                singleLine = true,
                isError = text.isNotEmpty() && parsed == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(onClick = { parsed?.let(onConfirm) }, enabled = parsed != null) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
