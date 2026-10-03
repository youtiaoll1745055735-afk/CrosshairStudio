package com.crosshair.studio.overlay

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 悬浮窗控制器：UI 与服务的唯一交互入口。
 */
object OverlayController {

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _permission = MutableStateFlow(false)
    val permission: StateFlow<Boolean> = _permission.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun refreshPermission(context: Context) {
        _permission.value = Settings.canDrawOverlays(context)
    }

    fun start(context: Context) {
        if (!Settings.canDrawOverlays(context)) {
            _events.tryEmit("请先授予悬浮窗权限")
            return
        }
        try {
            ContextCompat.startForegroundService(context, Intent(context, OverlayService::class.java))
            _running.value = true
        } catch (t: Throwable) {
            _running.value = false
            _events.tryEmit("无法启动悬浮服务：${t.message ?: "未知错误"}")
        }
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, OverlayService::class.java))
        _running.value = false
    }

    fun setRunning(value: Boolean) {
        _running.value = value
    }

    fun emit(message: String) {
        _events.tryEmit(message)
    }
}
