package com.crosshair.studio.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.crosshair.studio.core.AppJson
import com.crosshair.studio.data.model.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

private val Context.dataStore by preferencesDataStore(name = "crosshair_settings")

class SettingsRepository(private val context: Context, private val scope: CoroutineScope) {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings

    private var persistJob: Job? = null

    init {
        scope.launch { load() }
    }

    private suspend fun load() {
        val raw = context.dataStore.data.first()[KEY_SETTINGS]
        _settings.value = raw
            ?.let { runCatching { AppJson.decodeFromString<AppSettings>(it) }.getOrNull() }
            ?: AppSettings()
    }

    fun update(transform: (AppSettings) -> AppSettings) {
        _settings.value = transform(_settings.value)
        persistJob?.cancel()
        persistJob = scope.launch {
            delay(300)
            context.dataStore.edit { it[KEY_SETTINGS] = AppJson.encodeToString(_settings.value) }
        }
    }

    companion object {
        private val KEY_SETTINGS = stringPreferencesKey("settings")
    }
}
