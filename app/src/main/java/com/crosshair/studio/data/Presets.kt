package com.crosshair.studio.data

import com.crosshair.studio.data.model.CenterPoint
import com.crosshair.studio.data.model.CenterStyle
import com.crosshair.studio.data.model.CrosshairConfig
import com.crosshair.studio.data.model.Layer
import com.crosshair.studio.data.model.LayerType

/**
 * 内置准心预设。预设为只读模板：编辑后通过「另存为」生成用户配置。
 */
object Presets {

    const val KEY_CLASSIC = "p_classic"
    const val KEY_LINES_DOT = "p_lines_dot"
    const val KEY_DOT = "p_dot"
    const val KEY_RING = "p_ring"
    const val KEY_RING_DOT = "p_ring_dot"
    const val KEY_CROSS = "p_cross"
    const val KEY_BRACKET = "p_bracket"
    const val KEY_DIAMOND = "p_diamond"
    const val KEY_COMBO = "p_combo"

    fun all(): List<CrosshairConfig> = listOf(
        classic(), linesDot(), dotOnly(), hollowCircle(), ringDot(),
        cross(), bracket(), diamond(), combo()
    )

    fun byKey(key: String): CrosshairConfig? = all().find { it.presetKey == key }

    private fun base(presetKey: String, name: String, layers: List<Layer>, center: CenterPoint = CenterPoint()) =
        CrosshairConfig(id = presetKey, name = name, presetKey = presetKey, layers = layers, center = center)

    private fun lines(len: Float = 24f, th: Float = 3f, gap: Float = 6f, color: Long = 0xFFFFFFFF) =
        Layer(name = "四段线条", type = LayerType.LINES.name, length = len, thickness = th, gap = gap, color = color)

    private fun ring(sz: Float, th: Float, color: Long = 0xFFFFFFFF) =
        Layer(name = "圆环", type = LayerType.RING.name, size = sz, thickness = th, color = color)

    private fun cross(len: Float, th: Float, color: Long = 0xFFFFFFFF) =
        Layer(name = "十字线", type = LayerType.CROSS.name, length = len, thickness = th, color = color)

    private fun bracket(sz: Float, len: Float, th: Float, color: Long = 0xFFFFFFFF) =
        Layer(name = "四角括号", type = LayerType.BRACKET.name, size = sz, length = len, thickness = th, color = color)

    private fun diamond(sz: Float, th: Float, color: Long = 0xFFFFFFFF) =
        Layer(name = "菱形", type = LayerType.DIAMOND.name, size = sz, thickness = th, color = color)

    private fun dot(sz: Float, color: Long) =
        CenterPoint(visible = true, style = CenterStyle.DOT.name, size = sz, color = color)

    fun classic() = base(KEY_CLASSIC, "经典四段式", listOf(lines()), CenterPoint(visible = false))
    fun linesDot() = base(KEY_LINES_DOT, "四段式+中心点", listOf(lines()), dot(7f, 0xFF4ADE80))
    fun dotOnly() = base(KEY_DOT, "中心圆点", emptyList(), dot(10f, 0xFF22D3EE))
    fun hollowCircle() = base(KEY_RING, "空心圆", listOf(ring(30f, 3f)), CenterPoint(visible = false))
    fun ringDot() = base(KEY_RING_DOT, "圆环+中心点", listOf(ring(30f, 3f)), dot(6f, 0xFF22D3EE))
    fun cross() = base(KEY_CROSS, "十字线", listOf(cross(22f, 3f)), CenterPoint(visible = false))
    fun bracket() = base(KEY_BRACKET, "四角括号", listOf(bracket(40f, 10f, 3f)), CenterPoint(visible = false))
    fun diamond() = base(KEY_DIAMOND, "菱形准心", listOf(diamond(30f, 3f, 0xFF22D3EE)), CenterPoint(visible = false))
    fun combo() = base(KEY_COMBO, "几何组合", listOf(ring(34f, 2.5f), cross(14f, 2f, 0xFF3B82F6)), dot(6f, 0xFF4ADE80))
}
