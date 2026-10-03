package com.crosshair.studio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.crosshair.studio.data.model.CrosshairConfig
import com.crosshair.studio.data.model.PreviewBackground
import com.crosshair.studio.render.CrosshairRenderer
import com.crosshair.studio.ui.theme.StudioColors
import kotlin.math.min

/**
 * 准心实时预览（Compose Canvas）。
 * 与悬浮窗共用 CrosshairRenderer，保证预览效果与实际显示一致。
 * 支持深色/浅色/棋盘背景、预览缩放与缩略图自动适配。
 */
@Composable
fun CrosshairPreview(
    config: CrosshairConfig,
    modifier: Modifier = Modifier,
    background: PreviewBackground = PreviewBackground.DARK,
    zoom: Float = 1f,
    autoFit: Boolean = false,
) {
    Canvas(modifier = modifier) {
        when (background) {
            PreviewBackground.DARK -> drawRect(StudioColors.BgPreviewDark)
            PreviewBackground.LIGHT -> drawRect(StudioColors.BgPreviewLight)
            PreviewBackground.CHECKER -> {
                drawRect(StudioColors.BgPreviewDark)
                val cell = 10f * density
                var y = 0f
                var row = 0
                while (y < size.height) {
                    var x = 0f
                    var col = 0
                    while (x < size.width) {
                        if ((row + col) % 2 == 0) {
                            drawRect(
                                color = Color(0xFF161E27),
                                topLeft = Offset(x, y),
                                size = Size(cell, cell)
                            )
                        }
                        x += cell
                        col++
                    }
                    y += cell
                    row++
                }
            }
        }

        val extDp = CrosshairRenderer.estimateExtentDp(config)
        val pxExtent = extDp * config.baseScale * density
        val fit = if (pxExtent > 0f) {
            (min(size.width, size.height) / 2f - 6f * density) / pxExtent
        } else 1f
        val effective = (if (autoFit) fit else zoom).coerceIn(0.05f, 12f)

        drawIntoCanvas { canvas ->
            val save = canvas.save()
            canvas.clipRect(0f, 0f, size.width, size.height)
            CrosshairRenderer.draw(
                canvas = canvas.nativeCanvas,
                widthPx = size.width,
                heightPx = size.height,
                density = density,
                config = config.copy(baseScale = config.baseScale * effective),
                cx = size.width / 2f,
                cy = size.height / 2f
            )
            canvas.restore()
        }
    }
}
