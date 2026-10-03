package com.crosshair.studio.ui.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosshair.studio.data.model.CrosshairConfig
import com.crosshair.studio.ui.AppViewModel
import com.crosshair.studio.ui.components.CrosshairPreview
import com.crosshair.studio.ui.theme.StudioColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    vm: AppViewModel,
    snackbar: SnackbarHostState,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val current by vm.current.collectAsStateWithLifecycle()
    val configs by vm.configs.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val running by vm.overlayRunning.collectAsStateWithLifecycle()
    val permission by vm.overlayPermission.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioColors.AccentDim)
                ) {
                    Icon(
                        Icons.Filled.GpsFixed,
                        "logo",
                        Modifier.padding(9.dp),
                        tint = StudioColors.Accent
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Crosshair Studio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "外置游戏准心 · 悬浮显示",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioColors.TextSecondary
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CrosshairPreview(
                        config = current,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        background = settings.previewBackgroundMode,
                        zoom = settings.previewZoom
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                current.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                if (current.presetKey != null) "内置预设" else "自定义配置",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioColors.TextSecondary
                            )
                        }
                        FilledTonalButton(onClick = { onNavigate(1) }) {
                            Icon(Icons.Filled.Edit, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("编辑")
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("悬浮准心", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (running) "服务运行中，准心已显示在其他应用上方" else "未开启",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioColors.TextSecondary
                            )
                        }
                        Switch(
                            checked = running,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (!Settings.canDrawOverlays(context)) {
                                        scope.launch { snackbar.showSnackbar("请先授予悬浮窗权限") }
                                        context.startActivity(
                                            Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                        )
                                    } else {
                                        vm.toggleOverlay(context, true)
                                        scope.launch {
                                            delay(900)
                                            if (!vm.overlayRunning.value) {
                                                snackbar.showSnackbar("悬浮窗启动失败，请检查系统是否限制了悬浮显示")
                                            }
                                        }
                                    }
                                } else {
                                    vm.toggleOverlay(context, false)
                                }
                            }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(
                            ok = permission,
                            text = if (permission) "悬浮窗权限已授予" else "缺少悬浮窗权限",
                            icon = if (permission) Icons.Filled.CheckCircle else Icons.Filled.Warning
                        )
                        StatusChip(
                            ok = running,
                            text = if (running) "服务运行中" else "服务未运行",
                            icon = Icons.Filled.GpsFixed
                        )
                    }
                    if (!permission) {
                        OutlinedButton(
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
                            Icon(Icons.Filled.LockOpen, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("前往授予悬浮窗权限")
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(vertical = 12.dp)) {
                    Text(
                        "快速切换",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(configs, key = { it.id }) { c ->
                            QuickConfigChip(
                                config = c,
                                active = c.id == current.id,
                                onClick = {
                                    vm.setCurrent(c.id)
                                    scope.launch { snackbar.showSnackbar("已切换到「${c.name}」") }
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { onNavigate(2) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.CollectionsBookmark, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("准心库")
                }
                OutlinedButton(onClick = { onNavigate(3) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Tune, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("位置校准")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(ok: Boolean, text: String, icon: ImageVector) {
    val color = if (ok) StudioColors.Green else StudioColors.Danger
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Icon(icon, null, Modifier.size(13.dp), tint = color)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun QuickConfigChip(config: CrosshairConfig, active: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (active) StudioColors.AccentDim else StudioColors.Surface2)
                .then(
                    if (active) {
                        Modifier.border(1.5.dp, StudioColors.Accent, RoundedCornerShape(10.dp))
                    } else Modifier
                )
        ) {
            CrosshairPreview(config, Modifier.fillMaxSize(), autoFit = true)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            config.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
