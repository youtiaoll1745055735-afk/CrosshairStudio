package com.crosshair.studio.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.crosshair.studio.App
import com.crosshair.studio.core.AppJson
import com.crosshair.studio.data.ConfigValidator
import com.crosshair.studio.data.ExportEnvelope
import com.crosshair.studio.data.Presets
import com.crosshair.studio.data.model.AppSettings
import com.crosshair.studio.data.model.CrosshairConfig
import com.crosshair.studio.overlay.OverlayController
import com.crosshair.studio.overlay.OverlaySession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString

/**
 * 全局共享 ViewModel：连接配置仓库、设置仓库与悬浮窗控制器。
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as App
    val configRepo = app.configRepo
    val settingsRepo = app.settingsRepo

    val configs = configRepo.configs
    val current = configRepo.current
    val settings = settingsRepo.settings

    val overlayRunning = OverlayController.running
    val overlayPermission = OverlayController.permission
    val overlayEvents = OverlayController.events

    private val _unsaved = MutableStateFlow(false)
    val unsaved: StateFlow<Boolean> = _unsaved

    // ---- 配置操作 ----

    fun setCurrent(id: String) {
        configRepo.setCurrent(id)
        _unsaved.value = false
    }

    /** 实时编辑（内存态）：预览与悬浮窗立即生效。 */
    fun updateCurrent(transform: (CrosshairConfig) -> CrosshairConfig) {
        configRepo.updateCurrent(transform)
        _unsaved.value = true
    }

    fun saveCurrent(): CrosshairConfig {
        val c = configRepo.persistCurrent()
        _unsaved.value = false
        return c
    }

    fun saveAs(name: String): CrosshairConfig {
        val c = configRepo.saveAs(name)
        _unsaved.value = false
        return c
    }

    fun applyPreset(presetKey: String) {
        val p = Presets.byKey(presetKey) ?: return
        updateCurrent { cur ->
            p.copy(id = cur.id, name = cur.name, favorite = cur.favorite, updatedAt = cur.updatedAt)
        }
    }

    fun clearAllLayers() {
        updateCurrent { it.copy(layers = emptyList(), center = it.center.copy(visible = false)) }
    }

    fun duplicate(id: String): CrosshairConfig? = configRepo.duplicate(id)
    fun rename(id: String, name: String) = configRepo.rename(id, name)
    fun delete(id: String) = configRepo.delete(id)
    fun toggleFavorite(id: String) = configRepo.toggleFavorite(id)

    fun import(raw: String): String {
        val list = ConfigValidator.parse(raw) ?: return "导入失败：文件格式无效"
        configRepo.importConfigs(list)
        return "成功导入 ${list.size} 个配置"
    }

    fun exportJson(): String? {
        val list = configRepo.exportUserConfigs()
        if (list.isEmpty()) return null
        return AppJson.encodeToString(ExportEnvelope(configs = list))
    }

    fun resetAll() {
        configRepo.resetAll()
        _unsaved.value = false
    }

    // ---- 设置与悬浮窗 ----

    fun updateSettings(transform: (AppSettings) -> AppSettings) = settingsRepo.update(transform)

    fun setOverlayPosition(fx: Float, fy: Float) {
        settingsRepo.update { it.copy(overlayFracX = fx, overlayFracY = fy) }
        OverlaySession.fracX = fx
        OverlaySession.fracY = fy
        OverlaySession.apply()
    }

    fun setOverlayLocked(locked: Boolean) {
        settingsRepo.update { it.copy(overlayLocked = locked) }
        OverlaySession.locked = locked
        OverlaySession.apply()
    }

    fun toggleOverlay(context: Context, enable: Boolean) {
        if (enable) OverlayController.start(context) else OverlayController.stop(context)
    }

    fun refreshPermission(context: Context) = OverlayController.refreshPermission(context)

    /** 离开编辑器：自定义配置自动保存，预设配置保留会话内修改（需另存为）。 */
    fun onEditorExit() {
        if (_unsaved.value && current.value.presetKey == null) {
            configRepo.persistCurrent()
            _unsaved.value = false
        }
    }
}
