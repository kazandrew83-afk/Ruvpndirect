package com.example

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyberNavyCardBorder
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startVpnService(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Request POST_NOTIFICATIONS permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { /* Handled gracefully */ }

                    LaunchedEffect(Unit) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                MainScreen(
                    viewModel = viewModel,
                    onRequestVpnPermission = {
                        val prepareIntent = VpnService.prepare(this)
                        if (prepareIntent != null) {
                            vpnPermissionLauncher.launch(prepareIntent)
                        } else {
                            viewModel.startVpnService(this)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestVpnPermission: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    BackHandler(enabled = selectedTab != 0) {
        viewModel.selectTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = CyberNavyDark,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .background(CyberNavySurface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                containerColor = CyberNavySurface,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    NavigationItem("Главная", Icons.Filled.Security, Icons.Outlined.Security, 0, "nav_tab_dashboard"),
                    NavigationItem("Приложения", Icons.Filled.Apps, Icons.Outlined.Apps, 1, "nav_tab_apps"),
                    NavigationItem("Диагностика", Icons.Filled.Speed, Icons.Outlined.Speed, 2, "nav_tab_diagnostics"),
                    NavigationItem("Настройки", Icons.Filled.Settings, Icons.Outlined.Settings, 3, "nav_tab_settings")
                )

                navItems.forEach { item ->
                    val isSelected = selectedTab == item.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(item.index) },
                        modifier = Modifier.testTag(item.testTag),
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> DashboardScreen(
                viewModel = viewModel,
                onRequestVpnPermission = onRequestVpnPermission,
                modifier = screenModifier
            )
            1 -> AppsScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            2 -> DiagnosticsScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            3 -> SettingsScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
        }
    }
}

private data class NavigationItem(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val index: Int,
    val testTag: String
)
