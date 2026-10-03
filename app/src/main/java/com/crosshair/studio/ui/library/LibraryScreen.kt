package com.crosshair.studio.ui.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosshair.studio.data.model.CrosshairConfig
import com.crosshair.studio.data.model.PreviewBackground
import com.crosshair.studio.ui.AppViewModel
import com.crosshair.studio.ui.components.ConfirmDialog
import com.crosshair.studio.ui.components.CrosshairPreview
import com.crosshair.studio.ui.components.NameInputDialog
import com.crosshair.studio.ui.theme.StudioColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    vm: AppViewModel,
    snackbar: SnackbarHostState,
    onOpenEditor: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configs by vm.configs.collectAsStateWithLifecycle()
    val current by vm.current.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var renameTarget by remember { mutableStateOf<CrosshairConfig?>(null) }
    var deleteTarget by remember { mutableStateOf<CrosshairConfig?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val json = vm.exportJson()
                val result = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write((json ?: "").toByteArray())
                    }
                }
                withContext(Dispatchers.Main) {
                    snackbar.showSnackbar(
                        if (result.isSuccess) "已导出配置" else "导出失败：${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val text = runCatching {
                    context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes().decodeToString()
                    }
                }.getOrNull()
                val msg = if (text == null) "读取文件失败" else vm.import(text)
                withContext(Dispatchers.Main) { snackbar.showSnackbar(msg) }
            }
        }
    }

    val filtered = when (filter) {
        0 -> configs
        1 -> configs.filter { it.presetKey != null }
        2 -> configs.filter { it.presetKey == null }
        else -> configs.filter { it.favorite }
    }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.CollectionsBookmark, null,
                Modifier.size(20.dp), tint = StudioColors.Accent
            )
            Spacer(Modifier.width(8.dp))
            Text("准心库", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = {
                if (vm.exportJson() == null) {
                    scope.launch { snackbar.showSnackbar("没有可导出的自定义配置") }
                } else {
                    exportLauncher.launch("crosshair_configs.json")
                }
            }) {
                Icon(Icons.Filled.FileDownload, "导出", tint = StudioColors.TextSecondary)
            }
            IconButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }) {
                Icon(Icons.Filled.FileUpload, "导入", tint = StudioColors.TextSecondary)
            }
        }

        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("全部", "预设", "我的", "收藏").forEachIndexed { i, label ->
                FilterChip(
                    selected = filter == i,
                    onClick = { filter = i },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.id }) { cfg ->
                ConfigCard(
                    config = cfg,
                    isCurrent = cfg.id == current.id,
                    onUse = {
                        vm.setCurrent(cfg.id)
                        scope.launch { snackbar.showSnackbar("已切换到「${cfg.name}」") }
                    },
                    onEdit = { onOpenEditor(cfg.id) },
                    onToggleFavorite = { vm.toggleFavorite(cfg.id) },
                    onDuplicate = {
                        vm.duplicate(cfg.id)
                        scope.launch { snackbar.showSnackbar("已复制「${cfg.name}」") }
                    },
                    onRename = { renameTarget = cfg },
                    onDelete = { deleteTarget = cfg }
                )
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        "这里空空如也",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioColors.TextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
        }
    }

    renameTarget?.let { target ->
        NameInputDialog(
            title = "重命名",
            initial = target.name,
            onConfirm = { name ->
                vm.rename(target.id, name)
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "删除配置",
            message = "确定删除「${target.name}」吗？此操作不可恢复。",
            confirmText = "删除",
            danger = true,
            onConfirm = { vm.delete(target.id) },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun ConfigCard(
    config: CrosshairConfig,
    isCurrent: Boolean,
    onUse: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val isPreset = config.presetKey != null

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.border(1.5.dp, StudioColors.Accent, RoundedCornerShape(14.dp))
                else Modifier
            )
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioColors.Surface2)
            ) {
                CrosshairPreview(config, Modifier.fillMaxSize(), PreviewBackground.DARK, autoFit = true)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    config.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.size(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isPreset) Badge("预设", StudioColors.Blue)
                    if (config.favorite) Badge("收藏", StudioColors.Warning)
                    if (isCurrent) Badge("使用中", StudioColors.Green)
                }
            }
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (config.favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    "收藏",
                    Modifier.size(17.dp),
                    tint = if (config.favorite) StudioColors.Warning else StudioColors.TextSecondary
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.MoreVert, "更多", tint = StudioColors.TextSecondary)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (!isCurrent) {
                        DropdownMenuItem(text = { Text("使用") }, onClick = { menuOpen = false; onUse() })
                    }
                    DropdownMenuItem(text = { Text("编辑") }, onClick = { menuOpen = false; onEdit() })
                    DropdownMenuItem(
                        text = { Text(if (config.favorite) "取消收藏" else "收藏") },
                        onClick = { menuOpen = false; onToggleFavorite() }
                    )
                    DropdownMenuItem(text = { Text("复制") }, onClick = { menuOpen = false; onDuplicate() })
                    if (!isPreset) {
                        DropdownMenuItem(text = { Text("重命名") }, onClick = { menuOpen = false; onRename() })
                        DropdownMenuItem(
                            text = { Text("删除", color = StudioColors.Danger) },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
