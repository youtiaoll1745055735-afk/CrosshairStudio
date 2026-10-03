package com.crosshair.studio.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.crosshair.studio.ui.editor.EditorScreen
import com.crosshair.studio.ui.home.HomeScreen
import com.crosshair.studio.ui.library.LibraryScreen
import com.crosshair.studio.ui.settings.SettingsScreen
import com.crosshair.studio.ui.theme.StudioColors

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("首页", Icons.Filled.Home),
    TabItem("编辑器", Icons.Filled.Edit),
    TabItem("准心库", Icons.Filled.CollectionsBookmark),
    TabItem("设置", Icons.Filled.Settings),
)

@Composable
fun MainScreen(vm: AppViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.overlayEvents.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = {
                            if (i != tab) {
                                if (tab == 1) vm.onEditorExit()
                                tab = i
                            }
                        },
                        icon = { Icon(t.icon, null) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StudioColors.Accent,
                            selectedTextColor = StudioColors.Accent,
                            indicatorColor = StudioColors.AccentDim,
                            unselectedIconColor = StudioColors.TextSecondary,
                            unselectedTextColor = StudioColors.TextSecondary
                        )
                    )
                }
            }
        }
    ) { inner ->
        val padded = Modifier
            .fillMaxSize()
            .padding(inner)
        when (tab) {
            0 -> HomeScreen(vm, snackbar, onNavigate = { tab = it }, modifier = padded)
            1 -> EditorScreen(vm, snackbar, modifier = padded)
            2 -> LibraryScreen(
                vm = vm,
                snackbar = snackbar,
                onOpenEditor = { id -> vm.setCurrent(id); tab = 1 },
                modifier = padded
            )
            else -> SettingsScreen(vm, modifier = padded)
        }
    }
}
