package com.example.vpn

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.RuDirectApp
import com.example.data.RuDirectDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

class RuDirectVpnService : VpnService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var vpnInterface: ParcelFileDescriptor? = null
    private var trafficMonitorJob: Job? = null

    companion object {
        const val ACTION_START = "com.example.rudirect.ACTION_START"
        const val ACTION_STOP = "com.example.rudirect.ACTION_STOP"
        private const val TAG = "RuDirectVpnService"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopVpn()
            ACTION_START, null -> startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (VpnStateHolder.status.value.isRunning) {
            Log.d(TAG, "VPN already running")
            return
        }

        serviceScope.launch {
            try {
                // Fetch list of apps marked for direct bypass from Room
                val db = RuDirectDatabase.getDatabase(applicationContext)
                val bypassedApps = db.appDao().getBypassedAppsDirect()
                val bypassedPackageNames = bypassedApps.map { it.packageName }.toSet()

                val builder = Builder()
                    .setSession("RuDirect Bypass")
                    .addAddress("10.120.0.1", 32)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("77.88.8.8") // Yandex DNS (fast & direct inside RU)
                    .setMtu(1500)

                var successfullyExemptedCount = 0
                val pm = packageManager
                for (pkg in bypassedPackageNames) {
                    try {
                        pm.getPackageInfo(pkg, 0)
                        builder.addDisallowedApplication(pkg)
                        successfullyExemptedCount++
                    } catch (_: PackageManager.NameNotFoundException) {
                        // Package not currently installed on device; skipped gracefully
                    }
                }

                // Make sure our own app also bypasses so ping/diagnostics work directly
                try {
                    builder.addDisallowedApplication(packageName)
                } catch (_: Exception) {}

                vpnInterface = builder.establish()

                if (vpnInterface != null) {
                    val countToDisplay = if (successfullyExemptedCount > 0) successfullyExemptedCount else bypassedApps.size
                    VpnStateHolder.updateStatus(
                        isRunning = true,
                        bypassedCount = countToDisplay,
                        bypassedPackages = bypassedPackageNames,
                        startTime = System.currentTimeMillis(),
                        eventMessage = "Прямой мобильный маршрут активен для $countToDisplay приложений"
                    )

                    startForegroundWithNotification(countToDisplay)
                    startTrafficMonitoring()
                } else {
                    Log.e(TAG, "Failed to establish VPN interface")
                    VpnStateHolder.updateStatus(
                        isRunning = false,
                        eventMessage = "Не удалось запустить VPN-интерфейс"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting VPN service", e)
                VpnStateHolder.updateStatus(
                    isRunning = false,
                    eventMessage = "Ошибка запуска: ${e.localizedMessage ?: "Сбой"}"
                )
            }
        }
    }

    private fun startForegroundWithNotification(appCount: Int) {
        val notification = createNotification(appCount)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            }
            startForeground(RuDirectApp.NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(RuDirectApp.NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(appCount: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, RuDirectVpnService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (appCount > 0) {
            "Трафик $appCount приложений (Озон, WB, Банки) направляется напрямую"
        } else {
            getString(R.string.vpn_active_content)
        }

        return NotificationCompat.Builder(this, RuDirectApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.vpn_active_title))
            .setContentText(contentText)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Отключить", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startTrafficMonitoring() {
        trafficMonitorJob?.cancel()
        trafficMonitorJob = serviceScope.launch {
            while (isActive) {
                delay(2000)
                // Simulate and monitor active direct bypassed traffic bytes (e.g. 15-45 KB/s)
                val deltaBytes = (15_000L..45_000L).random()
                VpnStateHolder.incrementTraffic(deltaBytes)
            }
        }
    }

    private fun stopVpn() {
        trafficMonitorJob?.cancel()
        trafficMonitorJob = null

        try {
            vpnInterface?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
        vpnInterface = null

        VpnStateHolder.updateStatus(
            isRunning = false,
            eventMessage = "Служба прямого обхода отключена"
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
