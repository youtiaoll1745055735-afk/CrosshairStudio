package com.crosshair.studio.overlay

import android.content.Context
import android.graphics.Canvas
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.crosshair.studio.data.model.CrosshairConfig
import com.crosshair.studio.render.CrosshairRenderer
import kotlin.math.abs

/**
 * 全屏悬浮视图：仅负责准心矢量绘制与解锁状态下的拖动定位。
 * 锁定后窗口带 FLAG_NOT_TOUCHABLE，所有触摸事件穿透到下层应用。
 */
class OverlayView(
    context: Context,
    private val onDrag: (Float, Float) -> Unit,
    private val onToggleLock: () -> Unit,
    private val onOpenApp: () -> Unit,
) : View(context) {

    var config: CrosshairConfig? = null
    var fracX = 0f
    var fracY = 0f
    var locked = true

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var lastX = 0f
    private var lastY = 0f
    private var moved = false

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                onToggleLock()
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                onOpenApp()
                return true
            }
        }
    )

    init {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val c = config ?: return
        CrosshairRenderer.draw(
            canvas = canvas,
            widthPx = width.toFloat(),
            heightPx = height.toFloat(),
            density = resources.displayMetrics.density,
            config = c,
            cx = width / 2f + fracX * width / 2f,
            cy = height / 2f + fracY * height / 2f
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (locked) return false
        gestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
                moved = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastX
                val dy = event.rawY - lastY
                if (!moved && (abs(dx) > touchSlop || abs(dy) > touchSlop)) moved = true
                if (moved) {
                    onDrag(dx, dy)
                    lastX = event.rawX
                    lastY = event.rawY
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
