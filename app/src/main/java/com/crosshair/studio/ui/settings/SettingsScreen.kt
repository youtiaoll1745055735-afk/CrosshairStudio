package com.crosshair.studio.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosshair.studio.BuildConfig
import com.crosshair.studio.data.model.ThemeMode
import com.crosshair.studio.ui.AppViewModel
import com.crosshair.studio.ui.components.ConfirmDialog
import com.crosshair.studio.ui.components.ParamSlider
import com.crosshair.studio.ui.components.SectionCard
import com.crosshair.studio.ui.components.SettingRow
import com.crosshair.studio.ui.theme.StudioColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val configs by vm.configs.collectAsStateWithLifecycle()
    val permission by vm.overlayPermission.collectAsStateWithLifecycle()
    val running by vm.overlayRunning.collectAsStateWithLifecycle()

    var showHelp by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    var defaultMenu by remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    val hasNotifPermission = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    val defaultName = configs.find { it.id == settings.defaultConfigId }?.name ?: "未设置"

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            SectionCard("悬浮窗") {
                StatusLine(permission, if (permission) "悬浮窗权限：已授予" else "悬浮窗权限：未授予")
                StatusLine(running, if (running) "前台服务：运行中" else "前台服务：未运行")
                if (!permission) {
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("授权悬浮窗权限")
                    }
                }
                if (!hasNotifPermission) {
                    OutlinedButton(
                        onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("授权通知权限（前台服务常驻通知）")
                    }
                }
                Text(
                    "悬浮层仅用于显示准心，不模拟触控、不读取游戏数据。",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioColors.TextSecondary
                )
            }
        }

        item {
            SectionCard("位置校准") {
                ParamSlider(
                    "水平位置",
                    settings.overlayFracX * 100f,
                    -100f..100f, 0f, { "${it.toInt()}%" }
                ) { v -> vm.setOverlayPosition(v / 100f, settings.overlayFracY) }
                ParamSlider(
                    "垂直位置",
                    settings.overlayFracY * 100f,
                    -100f..100f, 0f, { "${it.toInt()}%" }
                ) { v -> vm.setOverlayPosition(settings.overlayFracX, v / 100f) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { vm.setOverlayPosition(0f, 0f) },
                        enabled = settings.overlayFracX != 0f || settings.overlayFracY != 0f
                    ) { Text("重置位置") }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        if (settings.overlayLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        null,
                        tint = if (settings.overlayLocked) StudioColors.Accent else StudioColors.Warning
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("锁定位置", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = settings.overlayLocked,
                        onCheckedChange = { vm.setOverlayLocked(it) }
                    )
                }
                Text(
                    "解锁后可长按拖动悬浮准心定位，长按悬浮层可快速重新锁定；锁定后触摸穿透到游戏。",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioColors.TextSecondary
                )
            }
        }

        item {
            SectionCard("默认准心") {
                Text(
                    "应用启动时默认使用的准心",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioColors.TextSecondary
                )
                Box {
                    OutlinedButton(onClick = { defaultMenu = true }) {
                        Text(defaultName, maxLines = 1)
                    }
                    DropdownMenu(expanded = defaultMenu, onDismissRequest = { defaultMenu = false }) {
                        configs.forEach { cfg ->
                            DropdownMenuItem(
                                text = { Text(cfg.name, maxLines = 1) },
                                onClick = {
                                    vm.updateSettings { it.copy(defaultConfigId = cfg.id) }
                                    defaultMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionCard("主题") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.toList().forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { vm.updateSettings { it.copy(theme = mode.name) } },
                            label = { Text(mode.label) }
                        )
                    }
                }
            }
        }

        item {
            SectionCard("其他") {
                SettingRow("使用说明", Icons.AutoMirrored.Filled.HelpOutline, onClick = { showHelp = true })
                SettingRow("隐私说明", Icons.Filled.PrivacyTip, onClick = { showPrivacy = true })
                SettingRow("恢复默认配置", Icons.Filled.Restore, onClick = { showReset = true })
                SettingRow("版本", Icons.Filled.GpsFixed, onClick = {}) {
                    Text(BuildConfig.VERSION_NAME, color = StudioColors.TextSecondary)
                }
            }
        }
    }

    if (showHelp) {
        ConfirmDialog(
            title = "使用说明",
            message = "1. 在首页打开「悬浮准心」开关，首次使用需授予悬浮窗权限。\n" +
                "2. 在编辑器中调整样式与参数，悬浮准心实时更新。\n" +
                "3. 「设置 → 位置校准」可移动准心；解锁后可直接拖动悬浮层，长按快速锁定。\n" +
                "4. 建议在游戏设置中关闭游戏自带准心，避免重叠。",
            confirmText = "知道了",
            onConfirm = {},
            onDismiss = { showHelp = false }
        )
    }

    if (showPrivacy) {
        ConfirmDialog(
            title = "隐私说明",
            message = "· 本应用不申请网络权限，所有准心配置仅保存在本机。\n" +
                "· 悬浮层仅用于绘制准心，不读取游戏数据、不修改游戏文件、不进行自动瞄准或自动操作。\n" +
                "· 不收集任何个人信息，卸载应用即清除全部数据。",
            confirmText = "知道了",
            onConfirm = {},
            onDismiss = { showPrivacy = false }
        )
    }

    if (showReset) {
        ConfirmDialog(
            title = "恢复默认配置",
            message = "将删除所有自定义准心并恢复内置预设，确定继续吗？",
            confirmText = "恢复默认",
            danger = true,
            onConfirm = { vm.resetAll() },
            onDismiss = { showReset = false }
        )
    }
}

@Composable
private fun StatusLine(ok: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (ok) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            null,
            tint = if (ok) StudioColors.Green else StudioColors.Danger
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
