package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var cellularPreferred by remember { mutableStateOf(true) }
    var persistentNotification by remember { mutableStateOf(true) }
    var selectedDns by remember { mutableStateOf("yandex_fast") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Настройки и маршрутизация",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Тонкая настройка прямого мобильного трафика и DNS",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        item {
            SettingsGroupCard(title = "Параметры сетевого туннеля") {
                SettingToggleRow(
                    icon = Icons.Default.SignalCellularAlt,
                    title = "Приоритет мобильного интернета",
                    subtitle = "Направлять исключённые приложения строго через сотовую сеть LTE/5G",
                    isChecked = cellularPreferred,
                    onCheckedChange = { cellularPreferred = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Индикатор активности в шторке",
                    subtitle = "Показывать количество защищённых приложений и прямой трафик",
                    isChecked = persistentNotification,
                    onCheckedChange = { persistentNotification = it }
                )
            }
        }

        item {
            SettingsGroupCard(title = "Выбор DNS-сервера для РФ") {
                DnsOptionRow(
                    title = "Яндекс.DNS Быстрый (Рекомендуется)",
                    subtitle = "77.88.8.8 — минимальный пинг до Озона, Сбера и Госуслуг",
                    isSelected = selectedDns == "yandex_fast",
                    onClick = { selectedDns = "yandex_fast" }
                )
                DnsOptionRow(
                    title = "Яндекс.DNS Безопасный",
                    subtitle = "77.88.8.88 — защита от вредоносных сайтов и фишинга",
                    isSelected = selectedDns == "yandex_safe",
                    onClick = { selectedDns = "yandex_safe" }
                )
                DnsOptionRow(
                    title = "Системный DNS оператора",
                    subtitle = "Использовать DNS-серверы вашей SIM-карты",
                    isSelected = selectedDns == "system",
                    onClick = { selectedDns = "system" }
                )
            }
        }

        item {
            SettingsGroupCard(title = "База правил и предустановки") {
                SettingActionRow(
                    icon = Icons.Default.Refresh,
                    title = "Пересканировать установленные приложения",
                    subtitle = "Обновить статус приложений Ozon, Wildberries, банков и др.",
                    onClick = {
                        viewModel.refreshNetworkInfo()
                        Toast.makeText(context, "Список приложений устройства обновлён", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingActionRow(
                    icon = Icons.Default.RestartAlt,
                    title = "Сбросить к заводскому каталогу РФ",
                    subtitle = "Восстановить исходный список правил (20+ сервисов)",
                    iconTint = DangerRed,
                    onClick = {
                        viewModel.resetDefaults()
                        Toast.makeText(context, "Предустановки РФ восстановлены", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        item {
            SettingsGroupCard(title = "О программе RuDirect") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "RuDirect v1.0",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Раздельное туннелирование (Split Tunneling) для Android",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Все правила и настройки хранятся локально в базе данных устройства. Персональные данные никуда не передаются.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00391A),
                checkedTrackColor = NeonEmerald
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = NeonCyan,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun DnsOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = NeonEmerald,
                unselectedColor = Color(0xFF64748B)
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
