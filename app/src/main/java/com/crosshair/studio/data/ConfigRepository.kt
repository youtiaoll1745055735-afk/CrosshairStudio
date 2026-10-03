package com.crosshair.studio.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.crosshair.studio.App
import com.crosshair.studio.core.AppJson
import com.crosshair.studio.data.model.CrosshairConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "crosshair_configs")

/**
 * 配置仓库：
 * - [stored]：已持久化的配置（含内置预设）
 * - [current]：当前使用/编辑中的配置（可能包含未保存的修改）
 * - [configs]：stored 与 current 合并后的展示列表
 */
class ConfigRepository(private val context: Context, private val scope: CoroutineScope) {

    private val _stored = MutableStateFlow<Map<String, CrosshairConfig>>(emptyMap())
    private val _current = MutableStateFlow(Presets.all().first())

    val current: StateFlow<CrosshairConfig> = _current

    val configs: StateFlow<List<CrosshairConfig>> =
        combine(_stored, _current) { stored, cur ->
            stored.values
                .map { if (it.id == cur.id) cur else it }
                .sortedWith(
                    compareByDescending<CrosshairConfig> { it.presetKey != null }
                        .thenByDescending { it.updatedAt }
                )
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        scope.launch { load() }
    }

    private suspend fun load() {
        val prefs = context.dataStore.data.first()
        val map = prefs[KEY_CONFIGS]
            ?.let { raw -> runCatching { AppJson.decodeFromString<Map<String, CrosshairConfig>>(raw) }.getOrNull() }
            ?.takeIf { it.isNotEmpty() }
            ?: Presets.all().associateBy { it.id }
        _stored.value = map
        val storedId = prefs[KEY_CURRENT_ID]
        val settingsDefault = runCatching {
            App.instance.settingsRepo.settings.value.defaultConfigId
        }.getOrNull()
        _current.value = map[storedId] ?: map[settingsDefault] ?: map.values.firstOrNull() ?: Presets.all().first()
    }

    fun setCurrent(id: String) {
        val c = _stored.value[id] ?: return
        _current.value = c
        scope.launch { context.dataStore.edit { it[KEY_CURRENT_ID] = id } }
    }

    /** 实时编辑：只更新内存中的 current，不持久化。 */
    fun updateCurrent(transform: (CrosshairConfig) -> CrosshairConfig) {
        _current.value = transform(_current.value).copy(updatedAt = System.currentTimeMillis())
    }

    /** 保存当前配置（覆盖存储）。 */
    fun persistCurrent(): CrosshairConfig {
        val c = _current.value.copy(updatedAt = System.currentTimeMillis())
        _stored.update { it + (c.id to c) }
        persistStored()
        return c
    }

    /** 另存为新配置（用于预设编辑后保存）。 */
    fun saveAs(name: String): CrosshairConfig {
        val c = _current.value.copy(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifBlank { "未命名准心" },
            presetKey = null,
            favorite = false,
            updatedAt = System.currentTimeMillis()
        )
        _stored.update { it + (c.id to c) }
        _current.value = c
        persistStored()
        scope.launch { context.dataStore.edit { it[KEY_CURRENT_ID] = c.id } }
        return c
    }

    fun duplicate(id: String): CrosshairConfig? {
        val src = _stored.value[id] ?: return null
        val c = src.copy(
            id = UUID.randomUUID().toString(),
            name = src.name + " 副本",
            presetKey = null,
            favorite = false,
            updatedAt = System.currentTimeMillis()
        )
        _stored.update { it + (c.id to c) }
        persistStored()
        return c
    }

    fun rename(id: String, name: String) {
        val clean = name.trim().ifBlank { return }
        _stored.update { map ->
            map[id]?.let { map + (id to it.copy(name = clean, updatedAt = System.currentTimeMillis())) } ?: map
        }
        if (_current.value.id == id) _current.value = _current.value.copy(name = clean)
        persistStored()
    }

    fun delete(id: String) {
        val target = _stored.value[id] ?: return
        if (target.presetKey != null) return // 内置预设不可删除
        _stored.update { it - id }
        persistStored()
        if (_current.value.id == id) {
            val next = configs.value.firstOrNull { it.id != id } ?: Presets.all().first()
            _current.value = next
            scope.launch { context.dataStore.edit { it[KEY_CURRENT_ID] = next.id } }
        }
    }

    fun toggleFavorite(id: String) {
        _stored.update { map -> map[id]?.let { map + (id to it.copy(favorite = !it.favorite)) } ?: map }
        if (_current.value.id == id) _current.value = _current.value.copy(favorite = !_current.value.favorite)
        persistStored()
    }

    fun importConfigs(list: List<CrosshairConfig>) {
        val now = System.currentTimeMillis()
        _stored.update { map -> map + list.associateBy { it.id }.mapValues { it.value.copy(updatedAt = now) } }
        persistStored()
    }

    fun exportUserConfigs(): List<CrosshairConfig> =
        _stored.value.values.filter { it.presetKey == null }.sortedByDescending { it.updatedAt }

    fun resetAll() {
        val presets = Presets.all().associateBy { it.id }
        _stored.value = presets
        _current.value = presets.values.first()
        persistStored()
        scope.launch { context.dataStore.edit { it[KEY_CURRENT_ID] = presets.keys.first() } }
    }

    private fun persistStored() {
        scope.launch { context.dataStore.edit { it[KEY_CONFIGS] = AppJson.encodeToString(_stored.value) } }
    }

    companion object {
        private val KEY_CONFIGS = stringPreferencesKey("configs")
        private val KEY_CURRENT_ID = stringPreferencesKey("current_config_id")
    }
}
