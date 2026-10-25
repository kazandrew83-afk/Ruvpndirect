package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bypassed_apps")
data class BypassedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val category: String, // from AppCategory.name
    val isBypassed: Boolean = true,
    val trackingRisk: String = TrackingRisk.HIGH.name, // from TrackingRisk.name
    val directBypassReason: String,
    val isInstalled: Boolean = false,
    val isCustomUserAdded: Boolean = false,
    val iconFallbackColor: Long = 0xFF00E5FF
)
