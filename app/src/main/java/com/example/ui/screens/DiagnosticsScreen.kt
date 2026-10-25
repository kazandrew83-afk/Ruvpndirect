package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ServiceCheckResult
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
fun DiagnosticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val results by viewModel.diagnosticResults.collectAsState()
    val isChecking by viewModel.isCheckingDiagnostics.collectAsState()
    val networkInfo by viewModel.networkInfo.collectAsState()

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
                text = "Диагностика прямого доступа",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Проверка доступности российских сервисов напрямую без блокировок VPN",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        item {
            ConnectionSummaryCard(networkInfo = networkInfo)
        }

        item {
            Button(
                onClick = { viewModel.runDiagnostics() },
                enabled = !isChecking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("run_diagnostics_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color(0xFF00363D)
                )
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF00363D),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Тестирование прямого маршрута...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Проверить пинг и доступность РФ", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(
                text = "Результаты прямого подключения к сервисам РФ",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        items(results) { result ->
            DiagnosticResultCard(result = result)
        }

        item {
            VpnDetectionMechanismCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConnectionSummaryCard(networkInfo: com.example.data.NetworkInfoState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Прямой интерфейс связи",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = networkInfo.networkType,
                        fontSize = 12.sp,
                        color = NeonEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusPill(label = "Сокеты", value = "Direct LTE", isGood = true)
                StatusPill(label = "DNS", value = "Яндекс 77.88.8.8", isGood = true)
                StatusPill(label = "Гео-IP", value = "Россия (Прямой)", isGood = true)
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, value: String, isGood: Boolean) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CyberNavySurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = TextMuted)
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isGood) NeonEmerald else WarningAmber
            )
        }
    }
}

@Composable
private fun DiagnosticResultCard(result: ServiceCheckResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (result.isReachable) CyberNavyCardBorder else DangerRed.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (result.isReachable) NeonEmerald.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (result.isReachable) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (result.isReachable) NeonEmerald else DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = result.serviceName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${result.host} • ${result.note}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CyberNavySurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${result.latencyMs} мс",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun VpnDetectionMechanismCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A2E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E314F))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Как банки и маркетплейсы вычисляют VPN?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1. Репутационные базы IP (Datacenter / Hosting IP вместо домашнего или мобильного провайдера).\n" +
                        "2. Разница между геолокацией SIM-карты/GPS и IP-адресом шлюза.\n" +
                        "3. Задержка пинга (RTT) и проверка DNS-серверов.\n\n" +
                        "RuDirect исключает приложение на уровне системного сетевого стека Android. Приложение связывается с сервером банка или Озона напрямую через вышку сотовой связи МегаФон, МТС, Билайн, Т2 с вашим легитимным российским IP.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
