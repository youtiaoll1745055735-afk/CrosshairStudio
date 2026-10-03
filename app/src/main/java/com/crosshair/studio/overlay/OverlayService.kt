package com.crosshair.studio.overlay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.crosshair.studio.App
import com.crosshair.studio.MainActivity
import com.crosshair.studio.R
import com.crosshair.studio.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 悬浮准心前台服务。
 * - API 34+ 使用 specialUse 前台服务类型（已在 Manifest 声明属性）
 * - 通过 WindowManager 添加 TYPE_APPLICATION_OVERLAY 全屏层，仅做视觉显示
 * - 订阅当前配置流，编辑时实时刷新，无持续高频绘制
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: OverlayView? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startAsForeground("正在启动…")

        val app = application as App
        val settingsRepo = app.settingsRepo
        val configRepo = app.configRepo

        val settings = settingsRepo.settings.value
        val config = configRepo.current.value
        OverlaySession.config = config
        OverlaySession.fracX = settings.overlayFracX
        OverlaySession.fracY = settings.overlayFracY
        OverlaySession.locked = settings.overlayLocked

        try {
            val view = OverlayView(
                context = this,
                onDrag = { dx, dy ->
                    OverlaySession.moveBy(dx, dy, overlayView?.width ?: 0, overlayView?.height ?: 0)
                    settingsRepo.update {
                        it.copy(overlayFracX = OverlaySession.fracX, overlayFracY = OverlaySession.fracY)
                    }
                },
                onToggleLock = { toggleLock(settingsRepo) },
                onOpenApp = {
                    startActivity(
                        Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            ).apply {
                this.config = OverlaySession.config
                this.fracX = OverlaySession.fracX
                this.fracY = OverlaySession.fracY
                this.locked = OverlaySession.locked
            }
            windowManager.addView(view, buildParams(OverlaySession.locked))
            overlayView = view
            OverlaySession.view = view
            OverlaySession.onLockChanged = { reAddWindow() }
            OverlaySession.apply()
            OverlayController.setRunning(true)
            updateNotification(config.name)

            // 实时同步：编辑器改动 → 悬浮层立即重绘
            scope.launch {
                configRepo.current.collect { c ->
                    OverlaySession.config = c
                    OverlaySession.apply()
                    updateNotification(c.name)
                }
            }
        } catch (t: Throwable) {
            OverlayController.setRunning(false)
            OverlayController.emit("悬浮窗创建失败：${t.message ?: "未知错误"}")
            stopSelf()
        }
    }

    private fun toggleLock(settingsRepo: SettingsRepository) {
        val locked = !OverlaySession.locked
        OverlaySession.locked = locked
        OverlaySession.apply()
        settingsRepo.update { it.copy(overlayLocked = locked) }
        vibrate()
        Toast.makeText(
            this,
            if (locked) "准心已锁定，触摸穿透" else "准心已解锁，可拖动定位（长按重新锁定）",
            Toast.LENGTH_SHORT
        ).show()
    }

    /** FLAG_NOT_TOUCHABLE 无法通过 updateViewLayout 切换，需要移除后重新添加。 */
    private fun reAddWindow() {
        val v = overlayView ?: return
        runCatching {
            windowManager.removeView(v)
            windowManager.addView(v, buildParams(OverlaySession.locked))
        }
    }

    private fun buildParams(locked: Boolean): WindowManager.LayoutParams {
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        if (locked) flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "准心悬浮窗", NotificationManager.IMPORTANCE_LOW)
        channel.description = "保持准心悬浮窗显示"
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun startAsForeground(text: String) {
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this, 1, Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("准心悬浮窗运行中")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(0, "关闭准心", stopIntent)
            .build()
        val type = if (Build.VERSION.SDK_INT >= 34) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
    }

    private fun updateNotification(text: String) {
        runCatching {
            val contentIntent = PendingIntent.getActivity(
                this, 0, Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("准心悬浮窗运行中")
                .setContentText("当前准心：$text")
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .build()
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun vibrate() {
        runCatching {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        overlayView?.let { runCatching { windowManager.removeView(it) } }
        overlayView = null
        OverlaySession.view = null
        OverlaySession.onLockChanged = null
        OverlayController.setRunning(false)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.crosshair.studio.action.STOP_OVERLAY"
        const val CHANNEL_ID = "crosshair_overlay"
        const val NOTIFICATION_ID = 1001
    }
}
