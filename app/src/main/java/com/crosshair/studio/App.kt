package com.crosshair.studio

import android.app.Application
import com.crosshair.studio.data.ConfigRepository
import com.crosshair.studio.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var configRepo: ConfigRepository
        private set
    lateinit var settingsRepo: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        configRepo = ConfigRepository(this, appScope)
        settingsRepo = SettingsRepository(this, appScope)
    }
}
