package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppCategory
import com.example.data.BypassedAppEntity
import com.example.data.InstalledAppInfo
import com.example.data.TrackingRisk
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberNavyCardBorder
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberNavySurfaceVariant
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun AppsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val filteredApps by viewModel.filteredApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filterOnlyInstalled by viewModel.filterOnlyInstalled.collectAsState()
    val showAddDialog by viewModel.showAddAppDialog.collectAsState()
    val deviceApps by viewModel.deviceApps.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_search_field"),
                placeholder = { Text("Поиск: Озон, Сбер, ВБ, Госуслуги...", color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NeonCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberNavyCardBorder,
                    focusedContainerColor = CyberNavySurface,
                    unfocusedContainerColor = CyberNavySurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Categories horizontal scroll
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AppCategory.entries.toTypedArray()) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(cat) },
                        label = {
                            Text(
                                text = cat.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan,
                            containerColor = CyberNavySurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = NeonCyan,
                            borderColor = CyberNavyCardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row: count and bulk toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Найдено: ${filteredApps.size}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { viewModel.toggleCategoryBypass(selectedCategory, true) }
                    ) {
                        Text("Включить все", fontSize = 11.sp, color = NeonEmerald)
                    }
                    TextButton(
                        onClick = { viewModel.toggleCategoryBypass(selectedCategory, false) }
                    ) {
                        Text("Выключить", fontSize = 11.sp, color = DangerRed)
                    }
                }
            }

            // Apps list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppItemCard(
                        app = app,
                        onToggleBypass = { isBypassed ->
                            viewModel.toggleAppBypass(app.packageName, isBypassed)
                        },
                        onDelete = {
                            viewModel.deleteApp(app)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // FAB to add any device app
        FloatingActionButton(
            onClick = { viewModel.setShowAddAppDialog(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_custom_app_fab"),
            containerColor = NeonCyan,
            contentColor = Color(0xFF00363D)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add custom application")
        }

        if (showAddDialog) {
            AddAppFromDeviceDialog(
                deviceApps = deviceApps,
                existingPackages = filteredApps.map { it.packageName }.toSet(),
                onDismiss = { viewModel.setShowAddAppDialog(false) },
                onAddApp = { pkg, name ->
                    viewModel.addCustomApp(pkg, name)
                }
            )
        }
    }
}

@Composable
private fun AppItemCard(
    app: BypassedAppEntity,
    onToggleBypass: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_item_${app.packageName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (app.isBypassed) CyberNavyCardBorder else Color(0xFF1E293B)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored icon fallback badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(app.iconFallbackColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.appName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (app.isInstalled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = app.isBypassed,
                    onCheckedChange = onToggleBypass,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00391A),
                        checkedTrackColor = NeonEmerald,
                        uncheckedThumbColor = Color(0xFF475569),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reason and risk badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = app.directBypassReason,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                if (app.isCustomUserAdded) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete custom app",
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Risk indicator badge
            val riskEnum = try {
                TrackingRisk.valueOf(app.trackingRisk)
            } catch (_: Exception) {
                TrackingRisk.HIGH
            }
            val (badgeBg, badgeText, badgeColor) = when (riskEnum) {
                TrackingRisk.CRITICAL -> Triple(Color(0xFF3B1219), "Блокирует при VPN", DangerRed)
                TrackingRisk.HIGH -> Triple(Color(0xFF382306), "Детектирует VPN", WarningAmber)
                TrackingRisk.MODERATE -> Triple(Color(0xFF0B2533), "Региональный доступ", NeonCyan)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = badgeColor
                    )
                }
            }
        }
    }
}

@Composable
private fun AddAppFromDeviceDialog(
    deviceApps: List<InstalledAppInfo>,
    existingPackages: Set<String>,
    onDismiss: () -> Unit,
    onAddApp: (packageName: String, appName: String) -> Unit
) {
    var dialogSearch by remember { mutableStateOf("") }

    val filteredDeviceApps = deviceApps.filter { app ->
        !existingPackages.contains(app.packageName) &&
                (dialogSearch.isBlank() ||
                        app.appName.contains(dialogSearch, ignoreCase = true) ||
                        app.packageName.contains(dialogSearch, ignoreCase = true))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberNavySurface,
        title = {
            Text(
                text = "Добавить приложение с телефона",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                Text(
                    text = "Выберите установленное приложение, чтобы направить его трафик напрямую в мобильную сеть мимо VPN.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dialogSearch,
                    onValueChange = { dialogSearch = it },
                    placeholder = { Text("Поиск в приложениях устройства...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberNavySurfaceVariant,
                        unfocusedContainerColor = CyberNavySurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberNavyCardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (filteredDeviceApps.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (deviceApps.isEmpty()) "Загрузка списка приложений..." else "Приложения не найдены",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    items(filteredDeviceApps) { deviceApp ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddApp(deviceApp.packageName, deviceApp.appName)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = CyberNavySurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = deviceApp.appName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = deviceApp.packageName,
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = NeonCyan)
            }
        }
    )
}
