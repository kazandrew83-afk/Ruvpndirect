package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppCategory
import com.example.data.AppRepository
import com.example.data.BypassedAppEntity
import com.example.data.InstalledAppInfo
import com.example.data.NetworkDiagnostics
import com.example.data.NetworkInfoState
import com.example.data.ServiceCheckResult
import com.example.vpn.RuDirectVpnService
import com.example.vpn.VpnEngineStatus
import com.example.vpn.VpnStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val diagnostics = NetworkDiagnostics(application)

    val vpnStatus: StateFlow<VpnEngineStatus> = VpnStateHolder.status

    private val _networkInfo = MutableStateFlow(diagnostics.getNetworkInfo())
    val networkInfo: StateFlow<NetworkInfoState> = _networkInfo.asStateFlow()

    private val _diagnosticResults = MutableStateFlow<List<ServiceCheckResult>>(emptyList())
    val diagnosticResults: StateFlow<List<ServiceCheckResult>> = _diagnosticResults.asStateFlow()

    private val _isCheckingDiagnostics = MutableStateFlow(false)
    val isCheckingDiagnostics: StateFlow<Boolean> = _isCheckingDiagnostics.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory: StateFlow<AppCategory> = _selectedCategory.asStateFlow()

    private val _filterOnlyInstalled = MutableStateFlow(false)
    val filterOnlyInstalled: StateFlow<Boolean> = _filterOnlyInstalled.asStateFlow()

    private val _deviceApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val deviceApps: StateFlow<List<InstalledAppInfo>> = _deviceApps.asStateFlow()

    private val _showAddAppDialog = MutableStateFlow(false)
    val showAddAppDialog: StateFlow<Boolean> = _showAddAppDialog.asStateFlow()

    val rawApps: StateFlow<List<BypassedAppEntity>> = repository.allApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bypassedCount: StateFlow<Int> = repository.bypassedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val filteredApps: StateFlow<List<BypassedAppEntity>> = combine(
        rawApps,
        _searchQuery,
        _selectedCategory,
        _filterOnlyInstalled
    ) { apps, query, category, onlyInstalled ->
        apps.filter { app ->
            val matchesCategory = category == AppCategory.ALL || app.category == category.name
            val matchesSearch = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true) ||
                    app.directBypassReason.contains(query, ignoreCase = true)
            val matchesInstalled = !onlyInstalled || app.isInstalled

            matchesCategory && matchesSearch && matchesInstalled
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeCatalogIfNeeded()
            loadDeviceApps()
            runDiagnostics()
        }
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: AppCategory) {
        _selectedCategory.value = category
    }

    fun setFilterOnlyInstalled(onlyInstalled: Boolean) {
        _filterOnlyInstalled.value = onlyInstalled
    }

    fun setShowAddAppDialog(show: Boolean) {
        _showAddAppDialog.value = show
        if (show && _deviceApps.value.isEmpty()) {
            loadDeviceApps()
        }
    }

    private fun loadDeviceApps() {
        viewModelScope.launch {
            _deviceApps.value = repository.getInstalledDeviceApps()
        }
    }

    fun toggleAppBypass(packageName: String, isBypassed: Boolean) {
        viewModelScope.launch {
            repository.setBypassStatus(packageName, isBypassed)
        }
    }

    fun toggleCategoryBypass(category: AppCategory, isBypassed: Boolean) {
        viewModelScope.launch {
            if (category == AppCategory.ALL) {
                repository.setAllBypassStatus(isBypassed)
            } else {
                repository.setCategoryBypassStatus(category, isBypassed)
            }
        }
    }

    fun addCustomApp(packageName: String, appName: String, category: AppCategory = AppCategory.OTHER) {
        viewModelScope.launch {
            repository.addCustomApp(packageName, appName, category)
            _showAddAppDialog.value = false
        }
    }

    fun deleteApp(app: BypassedAppEntity) {
        viewModelScope.launch {
            repository.deleteApp(app)
        }
    }

    fun resetDefaults() {
        viewModelScope.launch {
            repository.resetToDefaultPresets()
        }
    }

    fun refreshNetworkInfo() {
        _networkInfo.value = diagnostics.getNetworkInfo()
    }

    fun runDiagnostics() {
        viewModelScope.launch {
            _isCheckingDiagnostics.value = true
            refreshNetworkInfo()
            _diagnosticResults.value = diagnostics.checkRussianServices()
            _isCheckingDiagnostics.value = false
        }
    }

    fun startVpnService(context: Context) {
        val intent = Intent(context, RuDirectVpnService::class.java).apply {
            action = RuDirectVpnService.ACTION_START
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopVpnService(context: Context) {
        val intent = Intent(context, RuDirectVpnService::class.java).apply {
            action = RuDirectVpnService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun isVpnPrepared(context: Context): Boolean {
        return VpnService.prepare(context) == null
    }
}
