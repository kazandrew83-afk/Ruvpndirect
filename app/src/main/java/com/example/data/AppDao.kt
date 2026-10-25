package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM bypassed_apps ORDER BY isBypassed DESC, isInstalled DESC, appName ASC")
    fun getAllApps(): Flow<List<BypassedAppEntity>>

    @Query("SELECT * FROM bypassed_apps WHERE isBypassed = 1")
    suspend fun getBypassedAppsDirect(): List<BypassedAppEntity>

    @Query("SELECT * FROM bypassed_apps WHERE isBypassed = 1")
    fun getBypassedAppsFlow(): Flow<List<BypassedAppEntity>>

    @Query("SELECT COUNT(*) FROM bypassed_apps WHERE isBypassed = 1")
    fun getBypassedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<BypassedAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: BypassedAppEntity)

    @Update
    suspend fun update(app: BypassedAppEntity)

    @Query("UPDATE bypassed_apps SET isBypassed = :isBypassed WHERE packageName = :packageName")
    suspend fun setBypassStatus(packageName: String, isBypassed: Boolean)

    @Query("UPDATE bypassed_apps SET isBypassed = :isBypassed WHERE category = :category")
    suspend fun setCategoryBypassStatus(category: String, isBypassed: Boolean)

    @Query("UPDATE bypassed_apps SET isBypassed = :isBypassed")
    suspend fun setAllBypassStatus(isBypassed: Boolean)

    @Query("UPDATE bypassed_apps SET isInstalled = :isInstalled WHERE packageName = :packageName")
    suspend fun updateInstalledStatus(packageName: String, isInstalled: Boolean)

    @Delete
    suspend fun delete(app: BypassedAppEntity)
}
