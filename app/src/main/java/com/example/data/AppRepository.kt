package com.example.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

class AppRepository(private val context: Context) {
    private val appDao = RuDirectDatabase.getDatabase(context).appDao()

    val allApps: Flow<List<BypassedAppEntity>> = appDao.getAllApps()
    val bypassedAppsFlow: Flow<List<BypassedAppEntity>> = appDao.getBypassedAppsFlow()
    val bypassedCount: Flow<Int> = appDao.getBypassedCount()

    suspend fun initializeCatalogIfNeeded() = withContext(Dispatchers.IO) {
        val existing = appDao.getBypassedAppsDirect()
        if (existing.isEmpty()) {
            appDao.insertAll(DefaultCatalog.INITIAL_APPS)
        }
        scanInstalledStatus()
    }

    suspend fun scanInstalledStatus() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installedPackages = try {
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
                .map { it.packageName }
                .toSet()
        } catch (_: Exception) {
            emptySet()
        }

        val allInDb = appDao.getBypassedAppsDirect()
        allInDb.forEach { app ->
            val isInstalled = installedPackages.contains(app.packageName)
            if (app.isInstalled != isInstalled) {
                appDao.updateInstalledStatus(app.packageName, isInstalled)
            }
        }
    }

    suspend fun getInstalledDeviceApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        try {
            val packages = pm.getInstalledPackages(0)
            packages.mapNotNull { pkg ->
                val appInfo = pkg.applicationInfo ?: return@mapNotNull null
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = pm.getApplicationLabel(appInfo).toString()
                InstalledAppInfo(
                    packageName = pkg.packageName,
                    appName = label,
                    isSystemApp = isSystem
                )
            }.sortedBy { it.appName.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun setBypassStatus(packageName: String, isBypassed: Boolean) = withContext(Dispatchers.IO) {
        appDao.setBypassStatus(packageName, isBypassed)
    }

    suspend fun setCategoryBypassStatus(category: AppCategory, isBypassed: Boolean) = withContext(Dispatchers.IO) {
        appDao.setCategoryBypassStatus(category.name, isBypassed)
    }

    suspend fun setAllBypassStatus(isBypassed: Boolean) = withContext(Dispatchers.IO) {
        appDao.setAllBypassStatus(isBypassed)
    }

    suspend fun addCustomApp(packageName: String, appName: String, category: AppCategory = AppCategory.OTHER) = withContext(Dispatchers.IO) {
        val newApp = BypassedAppEntity(
            packageName = packageName,
            appName = appName,
            category = category.name,
            isBypassed = true,
            trackingRisk = TrackingRisk.HIGH.name,
            directBypassReason = "Пользовательское исключение: прямой мобильный интернет без VPN",
            isInstalled = true,
            isCustomUserAdded = true,
            iconFallbackColor = 0xFF00E5FF
        )
        appDao.insert(newApp)
    }

    suspend fun deleteApp(app: BypassedAppEntity) = withContext(Dispatchers.IO) {
        appDao.delete(app)
    }

    suspend fun resetToDefaultPresets() = withContext(Dispatchers.IO) {
        appDao.insertAll(DefaultCatalog.INITIAL_APPS)
        scanInstalledStatus()
    }
}
