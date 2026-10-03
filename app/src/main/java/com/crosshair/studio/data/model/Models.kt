package com.crosshair.studio.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 准心配置（带版本号的 JSON 结构）。
 * 渲染引擎只读取 [layers]、[center] 与基础变换参数，见 CrosshairRenderer。
 */
@Serializable
data class CrosshairConfig(
    val version: Int = 1,
    val id: String = UUID.randomUUID().toString(),
    val name: String = "未命名准心",
    val presetKey: String? = null,
    val favorite: Boolean = false,
    // 基础参数
    val baseScale: Float = 1f,
    val baseRotation: Float = 0f,
    val baseOffsetX: Float = 0f,
    val baseOffsetY: Float = 0f,
    val baseAlpha: Float = 1f,
    // 线条类基础参数（编辑器全局滑块作用于对应图层）
    val lineLength: Float = 24f,
    val lineThickness: Float = 3f,
    val gap: Float = 6f,
    // 全局阴影
    val shadowEnabled: Boolean = false,
    val shadowRadius: Float = 6f,
    val shadowColor: Long = 0x80000000,
    val shadowDx: Float = 0f,
    val shadowDy: Float = 2f,
    // 中心点（独立组件）
    val center: CenterPoint = CenterPoint(),
    // 图层系统
    val layers: List<Layer> = emptyList(),
    val updatedAt: Long = 0L,
)

/**
 * 通用图层：type 为 LayerType 名称；params 使用显式字段，便于编辑器与导入校验。
 */
@Serializable
data class Layer(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "图层",
    val type: String = LayerType.LINES.name,
    val visible: Boolean = true,
    val color: Long = 0xFFFFFFFF,
    val alpha: Float = 1f,
    val size: Float = 20f,
    val thickness: Float = 3f,
    val length: Float = 24f,
    val gap: Float = 6f,
    val rotation: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val strokeEnabled: Boolean = false,
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 2f,
) {
    val layerType: LayerType?
        get() = runCatching { LayerType.valueOf(type) }.getOrNull()

    val displayName: String
        get() = layerType?.label ?: name
}

enum class LayerType(val label: String, val isStrokeType: Boolean) {
    LINES("线条", true),
    CROSS("十字线", true),
    DOT("圆点", false),
    RING("圆环", true),
    CIRCLE_FILLED("实心圆", false),
    DIAMOND("菱形", true),
    RECT("方形", true),
    BRACKET("四角括号", true),
    TRIANGLE("三角形", true),
}

@Serializable
data class CenterPoint(
    val visible: Boolean = true,
    val style: String = CenterStyle.DOT.name,
    val size: Float = 8f,
    val color: Long = 0xFF4ADE80,
    val alpha: Float = 1f,
    val strokeEnabled: Boolean = false,
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 2f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
) {
    val centerStyle: CenterStyle
        get() = runCatching { CenterStyle.valueOf(style) }.getOrDefault(CenterStyle.DOT)
}

enum class CenterStyle(val label: String) {
    DOT("圆点"),
    HOLLOW_CIRCLE("空心圆"),
    SQUARE("方点"),
    DIAMOND("菱点"),
    CROSS("十字点"),
    SMALL_RING("小圆环"),
}
