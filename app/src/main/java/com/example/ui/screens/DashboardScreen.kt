package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppCategory
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberNavyCardBorder
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberNavySurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onRequestVpnPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vpnStatus by viewModel.vpnStatus.collectAsState()
    val bypassedCount by viewModel.bypassedCount.collectAsState()
    val networkInfo by viewModel.networkInfo.collectAsState()

    val isRunning = vpnStatus.isRunning

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HeaderSection()
        }

        item {
            ShieldHeroCard(
                isRunning = isRunning,
                bypassedCount = if (isRunning) vpnStatus.bypassedAppsCount else bypassedCount,
                onToggle = {
                    if (isRunning) {
                        viewModel.stopVpnService(context)
                    } else {
                        if (viewModel.isVpnPrepared(context)) {
                            viewModel.startVpnService(context)
                        } else {
                            onRequestVpnPermission()
                        }
                    }
                }
            )
        }

        item {
            MetricsGrid(
                isRunning = isRunning,
                bypassedCount = if (isRunning) vpnStatus.bypassedAppsCount else bypassedCount,
                trafficBytes = vpnStatus.directTrafficBytes
            )
        }

        item {
            NetworkStatusCard(
                networkInfo = networkInfo,
                onRefresh = { viewModel.refreshNetworkInfo() }
            )
        }

        item {
            QuickPresetsSection(
                onToggleCategory = { category ->
                    viewModel.toggleCategoryBypass(category, true)
                },
                onOpenApps = {
                    viewModel.selectTab(1)
                }
            )
        }

        item {
            SplitTunnelExplanationCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "RuDirect",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Прямой мобильный трафик для сервисов РФ",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CyberNavySurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Split-Tunneling",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonEmerald
                )
            }
        }
    }
}

@Composable
private fun ShieldHeroCard(
    isRunning: Boolean,
    bypassedCount: Int,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val activeGlowColor by animateColorAsState(
        targetValue = if (isRunning) NeonEmerald else Color(0xFF334155),
        label = "glowColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shield_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRunning) NeonEmerald.copy(alpha = 0.5f) else CyberNavyCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    activeGlowColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Middle circle button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isRunning) {
                                    listOf(Color(0xFF005328), Color(0xFF00391A))
                                } else {
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                }
                            )
                        )
                        .border(
                            2.dp,
                            if (isRunning) NeonEmerald else Color(0xFF475569),
                            CircleShape
                        )
                        .clickable { onToggle() }
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Shield else Icons.Default.PowerSettingsNew,
                        contentDescription = "Toggle direct bypass",
                        tint = if (isRunning) NeonEmerald else Color(0xFF94A3B8),
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isRunning) "ПРЯМОЙ ОБХОД АКТИВЕН" else "ОБХОД ОТКЛЮЧЕН",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) NeonEmerald else TextPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isRunning) {
                    "Трафик $bypassedCount приложений (Озон, WB, Банки, Госуслуги) направляется напрямую через мобильный интернет"
                } else {
                    "Включите, чтобы исключить российские сервисы из VPN и избежать блокировок"
                },
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("vpn_toggle_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFB91C1C) else NeonCyan,
                    contentColor = if (isRunning) Color.White else Color(0xFF00363D)
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.PowerSettingsNew else Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "Остановить обход" else "Включить прямой доступ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun MetricsGrid(
    isRunning: Boolean,
    bypassedCount: Int,
    trafficBytes: Long
) {
    val trafficMb = (trafficBytes / (1024f * 1024f))
    val trafficStr = if (trafficMb > 1024f) {
        String.format("%.1f ГБ", trafficMb / 1024f)
    } else {
        String.format("%.1f МБ", trafficMb)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            title = "Приложений",
            value = "$bypassedCount",
            subtitle = "в прямом маршруте",
            icon = Icons.Default.Public,
            iconTint = NeonCyan,
            modifier = Modifier.weight(1f)
        )

        MetricCard(
            title = "Прямой трафик",
            value = if (isRunning) trafficStr else "0.0 МБ",
            subtitle = "мимо VPN шлюза",
            icon = Icons.Default.Speed,
            iconTint = NeonEmerald,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun NetworkStatusCard(
    networkInfo: com.example.data.NetworkInfoState,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Сетевой интерфейс",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Обновить",
                    fontSize = 12.sp,
                    color = NeonCyan,
                    modifier = Modifier.clickable { onRefresh() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            NetworkInfoRow(label = "Текущая сеть", value = networkInfo.networkType)
            NetworkInfoRow(label = "DNS сервер", value = networkInfo.dnsServer)
            NetworkInfoRow(label = "Антифрод защита", value = "Активна (прямой IP РФ)")
        }
    }
}

@Composable
private fun NetworkInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun QuickPresetsSection(
    onToggleCategory: (AppCategory) -> Unit,
    onOpenApps: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Быстрый выбор категорий",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Все правила >",
                    fontSize = 12.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onOpenApps() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    title = "Банки",
                    icon = Icons.Default.AccountBalance,
                    color = Color(0xFF21A038),
                    modifier = Modifier.weight(1f),
                    onClick = { onToggleCategory(AppCategory.BANKING) }
                )
                CategoryChip(
                    title = "Маркеты",
                    icon = Icons.Default.ShoppingBag,
                    color = Color(0xFF005BFF),
                    modifier = Modifier.weight(1f),
                    onClick = { onToggleCategory(AppCategory.MARKETPLACE) }
                )
                CategoryChip(
                    title = "Госуслуги",
                    icon = Icons.Default.Security,
                    color = Color(0xFF0D4CD3),
                    modifier = Modifier.weight(1f),
                    onClick = { onToggleCategory(AppCategory.GOSUSLUGI) }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = CyberNavySurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun SplitTunnelExplanationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A2E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E314F))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Как работает прямой обход?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Android VpnService аппаратно исключает выбранные приложения из VPN-туннеля. Банки, Озон и Госуслуги подключаются напрямую через сотовую вышку вашего мобильного оператора, поэтому они не видят VPN и не блокируют аккаунт.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
