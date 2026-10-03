package com.crosshair.studio.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.crosshair.studio.data.model.CenterPoint
import com.crosshair.studio.data.model.CenterStyle
import com.crosshair.studio.data.model.CrosshairConfig
import com.crosshair.studio.data.model.Layer
import com.crosshair.studio.data.model.LayerType
import kotlin.math.abs
import kotlin.math.max

/**
 * 矢量准心绘制引擎。
 * 预览（Compose Canvas）与悬浮窗（OverlayView）共用同一套绘制逻辑，保证效果一致。
 * 设计坐标系：中心为原点，单位为 dp，先应用基础变换（偏移/旋转/缩放），再逐图层绘制。
 */
object CrosshairRenderer {

    /** 估算准心在 baseScale=1 时占据的最大半径（dp），用于缩略图自动适配。 */
    fun estimateExtentDp(config: CrosshairConfig): Float {
        var ext = 0f
        fun add(v: Float) { if (v > ext) ext = v }
        for (l in config.layers) {
            if (!l.visible) continue
            val t = l.layerType ?: continue
            val r = when (t) {
                LayerType.LINES -> l.gap + l.length
                LayerType.CROSS -> l.length
                LayerType.DOT, LayerType.RING, LayerType.CIRCLE_FILLED,
                LayerType.DIAMOND, LayerType.RECT, LayerType.BRACKET,
                LayerType.TRIANGLE -> l.size / 2f
            }
            add(r + l.strokeWidth + abs(l.offsetX) + abs(l.offsetY))
        }
        if (config.center.visible) {
            add(config.center.size / 2f + abs(config.center.offsetX) + abs(config.center.offsetY))
        }
        add(abs(config.baseOffsetX) + abs(config.baseOffsetY))
        return (ext + 2f).coerceAtLeast(6f)
    }

    fun draw(
        canvas: Canvas,
        widthPx: Float,
        heightPx: Float,
        density: Float,
        config: CrosshairConfig,
        cx: Float,
        cy: Float,
    ) {
        val save = canvas.save()
        canvas.translate(cx + config.baseOffsetX * density, cy + config.baseOffsetY * density)
        canvas.rotate(config.baseRotation)
        canvas.scale(config.baseScale * density, config.baseScale * density)
        for (layer in config.layers) {
            if (layer.visible && layer.layerType != null) drawLayer(canvas, layer, config)
        }
        if (config.center.visible) drawCenter(canvas, config.center, config)
        canvas.restoreToCount(save)
    }

    private fun newPaint(color: Long, alpha: Int, width: Float, style: Paint.Style): Paint =
        Paint().apply {
            isAntiAlias = true
            this.style = style
            strokeWidth = width.coerceAtLeast(1f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.color = (color and 0xFFFFFFFFL).toInt()
            this.alpha = alpha.coerceIn(0, 255)
        }

    private fun applyShadow(p: Paint, config: CrosshairConfig) {
        if (config.shadowEnabled) {
            p.setShadowLayer(
                config.shadowRadius,
                config.shadowDx,
                config.shadowDy,
                (config.shadowColor and 0xFFFFFFFFL).toInt()
            )
        }
    }

    private fun drawLayer(canvas: Canvas, layer: Layer, config: CrosshairConfig) {
        val t = layer.layerType ?: return
        val alpha = (255f * config.baseAlpha * layer.alpha).toInt()
        canvas.save()
        canvas.translate(layer.offsetX, layer.offsetY)
        canvas.rotate(layer.rotation)

        fun strokeWith(draw: (Paint) -> Unit) {
            if (layer.strokeEnabled) {
                val u = newPaint(layer.strokeColor, alpha, layer.thickness + layer.strokeWidth * 2f, Paint.Style.STROKE)
                applyShadow(u, config)
                draw(u)
            }
            val p = newPaint(layer.color, alpha, layer.thickness, Paint.Style.STROKE)
            applyShadow(p, config)
            draw(p)
        }

        when (t) {
            LayerType.LINES -> strokeWith { p ->
                for (a in intArrayOf(0, 90, 180, 270)) {
                    canvas.save()
                    canvas.rotate(a.toFloat())
                    canvas.drawLine(layer.gap, 0f, layer.gap + layer.length, 0f, p)
                    canvas.restore()
                }
            }

            LayerType.CROSS -> strokeWith { p ->
                canvas.drawLine(-layer.length, 0f, layer.length, 0f, p)
                canvas.drawLine(0f, -layer.length, 0f, layer.length, p)
            }

            LayerType.DOT -> {
                if (layer.strokeEnabled) {
                    canvas.drawCircle(
                        0f, 0f, layer.size / 2f + layer.strokeWidth,
                        newPaint(layer.strokeColor, alpha, 0f, Paint.Style.FILL)
                    )
                }
                canvas.drawCircle(0f, 0f, layer.size / 2f, newPaint(layer.color, alpha, 0f, Paint.Style.FILL))
            }

            LayerType.CIRCLE_FILLED -> {
                if (layer.strokeEnabled) {
                    canvas.drawCircle(
                        0f, 0f, layer.size / 2f + layer.strokeWidth,
                        newPaint(layer.strokeColor, alpha, 0f, Paint.Style.FILL)
                    )
                }
                canvas.drawCircle(0f, 0f, layer.size / 2f, newPaint(layer.color, alpha, 0f, Paint.Style.FILL))
            }

            LayerType.RING -> strokeWith { p ->
                canvas.drawCircle(0f, 0f, layer.size / 2f, p)
            }

            LayerType.DIAMOND -> strokeWith { p ->
                canvas.drawPath(diamondPath(layer.size), p)
            }

            LayerType.RECT -> strokeWith { p ->
                canvas.drawRect(-layer.size / 2f, -layer.size / 2f, layer.size / 2f, layer.size / 2f, p)
            }

            LayerType.TRIANGLE -> strokeWith { p ->
                val path = Path().apply {
                    moveTo(0f, -layer.size / 2f)
                    lineTo(layer.size / 2f, layer.size / 2f)
                    lineTo(-layer.size / 2f, layer.size / 2f)
                    close()
                }
                canvas.drawPath(path, p)
            }

            LayerType.BRACKET -> {
                val s = layer.size / 2f
                val len = layer.length
                strokeWith { p ->
                    for (sx in intArrayOf(1, -1)) {
                        for (sy in intArrayOf(1, -1)) {
                            canvas.drawLine(sx * (s - len), sy * s, sx * s, sy * s, p)
                            canvas.drawLine(sx * s, sy * (s - len), sx * s, sy * s, p)
                        }
                    }
                }
            }
        }
        canvas.restore()
    }

    private fun diamondPath(size: Float): Path = Path().apply {
        moveTo(0f, -size / 2f)
        lineTo(size / 2f, 0f)
        lineTo(0f, size / 2f)
        lineTo(-size / 2f, 0f)
        close()
    }

    private fun drawCenter(canvas: Canvas, c: CenterPoint, config: CrosshairConfig) {
        val alpha = (255f * config.baseAlpha * c.alpha).toInt()
        val s = c.size / 2f
        canvas.save()
        canvas.translate(c.offsetX, c.offsetY)

        fun paint(color: Long, width: Float, style: Paint.Style): Paint =
            newPaint(color, alpha, width, style).also { applyShadow(it, config) }

        fun ringWidth() = max(1.2f, c.size * 0.16f)

        fun strokeUnder(draw: (Paint) -> Unit, mainWidth: Float) {
            if (c.strokeEnabled) {
                draw(paint(c.strokeColor, mainWidth + c.strokeWidth * 2f, Paint.Style.STROKE))
            }
            draw(paint(c.color, mainWidth, Paint.Style.STROKE))
        }

        when (c.centerStyle) {
            CenterStyle.DOT -> {
                if (c.strokeEnabled) {
                    canvas.drawCircle(0f, 0f, s + c.strokeWidth, paint(c.strokeColor, 0f, Paint.Style.FILL))
                }
                canvas.drawCircle(0f, 0f, s, paint(c.color, 0f, Paint.Style.FILL))
            }

            CenterStyle.HOLLOW_CIRCLE -> strokeUnder({ p -> canvas.drawCircle(0f, 0f, s, p) }, ringWidth())

            CenterStyle.SQUARE -> {
                if (c.strokeEnabled) {
                    canvas.drawRect(
                        -s - c.strokeWidth, -s - c.strokeWidth, s + c.strokeWidth, s + c.strokeWidth,
                        paint(c.strokeColor, 0f, Paint.Style.FILL)
                    )
                }
                canvas.drawRect(-s, -s, s, s, paint(c.color, 0f, Paint.Style.FILL))
            }

            CenterStyle.DIAMOND -> {
                val path = diamondPath(c.size)
                if (c.strokeEnabled) {
                    canvas.save()
                    canvas.scale(1f + c.strokeWidth / max(s, 1f), 1f + c.strokeWidth / max(s, 1f))
                    canvas.drawPath(path, paint(c.strokeColor, 0f, Paint.Style.FILL))
                    canvas.restore()
                }
                canvas.drawPath(path, paint(c.color, 0f, Paint.Style.FILL))
            }

            CenterStyle.CROSS -> strokeUnder({ p ->
                canvas.drawLine(-s, 0f, s, 0f, p)
                canvas.drawLine(0f, -s, 0f, s, p)
            }, ringWidth() * 0.9f)

            CenterStyle.SMALL_RING -> strokeUnder({ p ->
                canvas.drawCircle(0f, 0f, s, p)
            }, max(1f, c.size * 0.12f))
        }
        canvas.restore()
    }
}
