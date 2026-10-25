package com.example.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VpnEngineStatus(
    val isRunning: Boolean = false,
    val bypassedAppsCount: Int = 0,
    val bypassedPackages: Set<String> = emptySet(),
    val directTrafficBytes: Long = 0L,
    val connectionStartTime: Long = 0L,
    val directCellularPreferred: Boolean = true,
    val dnsMode: String = "Яндекс.DNS (77.88.8.8)",
    val lastEventMessage: String = "Служба готова к запуску"
)

object VpnStateHolder {
    private val _status = MutableStateFlow(VpnEngineStatus())
    val status: StateFlow<VpnEngineStatus> = _status.asStateFlow()

    fun updateStatus(
        isRunning: Boolean? = null,
        bypassedCount: Int? = null,
        bypassedPackages: Set<String>? = null,
        directBytes: Long? = null,
        startTime: Long? = null,
        cellularPreferred: Boolean? = null,
        dnsMode: String? = null,
        eventMessage: String? = null
    ) {
        val current = _status.value
        _status.value = current.copy(
            isRunning = isRunning ?: current.isRunning,
            bypassedAppsCount = bypassedCount ?: current.bypassedAppsCount,
            bypassedPackages = bypassedPackages ?: current.bypassedPackages,
            directTrafficBytes = directBytes ?: current.directTrafficBytes,
            connectionStartTime = startTime ?: current.connectionStartTime,
            directCellularPreferred = cellularPreferred ?: current.directCellularPreferred,
            dnsMode = dnsMode ?: current.dnsMode,
            lastEventMessage = eventMessage ?: current.lastEventMessage
        )
    }

    fun incrementTraffic(bytes: Long) {
        val current = _status.value
        _status.value = current.copy(directTrafficBytes = current.directTrafficBytes + bytes)
    }
}
