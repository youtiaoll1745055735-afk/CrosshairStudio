package com.crosshair.studio.data

import com.crosshair.studio.core.AppJson
import com.crosshair.studio.data.model.CenterStyle
import com.crosshair.studio.data.model.CrosshairConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.util.UUID

@Serializable
data class ExportEnvelope(
    val format: String = "crosshair-studio-config",
    val version: Int = 1,
    val configs: List<CrosshairConfig> = emptyList(),
)

/**
 * 导入校验：解析带版本号的 JSON 文件，对每个字段做范围收敛，防止无效参数导致崩溃。
 */
object ConfigValidator {

    private const val MAX_LAYERS = 32

    fun parse(raw: String): List<CrosshairConfig>? {
        val text = raw.trim()
        if (text.isEmpty()) return null
        val envelope = runCatching { AppJson.decodeFromString<ExportEnvelope>(text) }.getOrNull()
        val list = when {
            envelope != null && envelope.version <= 1 -> envelope.configs
            else -> runCatching { listOf(AppJson.decodeFromString<CrosshairConfig>(text)) }.getOrNull()
        } ?: return null
        return list.mapNotNull { sanitize(it) }.ifEmpty { null }
    }

    fun sanitize(src: CrosshairConfig): CrosshairConfig {
        val layers = src.layers
            .filter { it.layerType != null }
            .take(MAX_LAYERS)
            .map { l ->
                l.copy(
                    id = UUID.randomUUID().toString(),
                    color = l.color and 0xFFFFFFFFL,
                    alpha = l.alpha.coerceIn(0.02f, 1f),
                    size = l.size.coerceIn(1f, 400f),
                    thickness = l.thickness.coerceIn(0.3f, 80f),
                    length = l.length.coerceIn(0f, 400f),
                    gap = l.gap.coerceIn(0f, 200f),
                    rotation = l.rotation.coerceIn(-360f, 360f),
                    offsetX = l.offsetX.coerceIn(-500f, 500f),
                    offsetY = l.offsetY.coerceIn(-500f, 500f),
                    strokeColor = l.strokeColor and 0xFFFFFFFFL,
                    strokeWidth = l.strokeWidth.coerceIn(0f, 40f)
                )
            }
        return CrosshairConfig(
            id = UUID.randomUUID().toString(),
            name = src.name.trim().ifBlank { "导入的准心" }.take(40),
            presetKey = null,
            favorite = false,
            baseScale = src.baseScale.coerceIn(0.1f, 6f),
            baseRotation = src.baseRotation.coerceIn(-360f, 360f),
            baseOffsetX = src.baseOffsetX.coerceIn(-1000f, 1000f),
            baseOffsetY = src.baseOffsetY.coerceIn(-1000f, 1000f),
            baseAlpha = src.baseAlpha.coerceIn(0.02f, 1f),
            lineLength = src.lineLength.coerceIn(1f, 400f),
            lineThickness = src.lineThickness.coerceIn(0.3f, 80f),
            gap = src.gap.coerceIn(0f, 200f),
            shadowEnabled = src.shadowEnabled,
            shadowRadius = src.shadowRadius.coerceIn(0f, 60f),
            shadowColor = src.shadowColor and 0xFFFFFFFFL,
            shadowDx = src.shadowDx.coerceIn(-60f, 60f),
            shadowDy = src.shadowDy.coerceIn(-60f, 60f),
            center = src.center.copy(
                visible = src.center.visible,
                style = if (runCatching { CenterStyle.valueOf(src.center.style) }.isSuccess) {
                    src.center.style
                } else CenterStyle.DOT.name,
                size = src.center.size.coerceIn(1f, 200f),
                color = src.center.color and 0xFFFFFFFFL,
                alpha = src.center.alpha.coerceIn(0.02f, 1f),
                strokeColor = src.center.strokeColor and 0xFFFFFFFFL,
                strokeWidth = src.center.strokeWidth.coerceIn(0f, 40f),
                offsetX = src.center.offsetX.coerceIn(-500f, 500f),
                offsetY = src.center.offsetY.coerceIn(-500f, 500f)
            ),
            layers = layers,
            updatedAt = System.currentTimeMillis()
        )
    }
}
