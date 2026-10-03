package com.crosshair.studio.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val defaultConfigId: String? = null,
    val theme: String = ThemeMode.DARK.name,
    val overlayFracX: Float = 0f,
    val overlayFracY: Float = 0f,
    val overlayLocked: Boolean = true,
    val previewBackground: String = PreviewBackground.DARK.name,
    val previewZoom: Float = 1f,
) {
    val themeMode: ThemeMode
        get() = runCatching { ThemeMode.valueOf(theme) }.getOrDefault(ThemeMode.DARK)

    val previewBackgroundMode: PreviewBackground
        get() = runCatching { PreviewBackground.valueOf(previewBackground) }.getOrDefault(PreviewBackground.DARK)
}

enum class ThemeMode(val label: String) {
    DARK("深色"),
    AMOLED("AMOLED 纯黑"),
}

enum class PreviewBackground(val label: String) {
    DARK("深色"),
    LIGHT("浅色"),
    CHECKER("棋盘"),
}
