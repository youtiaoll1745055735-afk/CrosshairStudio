package com.crosshair.studio.overlay

import com.crosshair.studio.data.model.CrosshairConfig

/**
 * 悬浮窗会话状态（同进程共享）。
 * 服务持有视图引用；编辑器/设置的实时更新通过 [apply] 推送到悬浮层。
 */
object OverlaySession {

    var view: OverlayView? = null
    var config: CrosshairConfig? = null

    /** 准心中心相对屏幕中心的位置比例（-1..1），由拖动/位置校准更新。 */
    var fracX = 0f
    var fracY = 0f

    var locked = true

    /** 锁定状态变化时由服务回调（需要重建窗口参数以切换触摸穿透）。 */
    var onLockChanged: (() -> Unit)? = null

    fun apply() {
        val v = view ?: return
        val lockChanged = v.locked != locked
        v.locked = locked
        v.config = config
        v.fracX = fracX
        v.fracY = fracY
        v.invalidate()
        if (lockChanged) onLockChanged?.invoke()
    }

    fun moveBy(dx: Float, dy: Float, width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        fracX = (fracX + dx / (width / 2f)).coerceIn(-1f, 1f)
        fracY = (fracY + dy / (height / 2f)).coerceIn(-1f, 1f)
        apply()
    }
}
