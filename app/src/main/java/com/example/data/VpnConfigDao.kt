package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnConfigDao {
    @Query("SELECT * FROM vpn_configs")
    fun getAllConfigs(): Flow<List<VpnConfig>>

    @Query("SELECT * FROM vpn_configs WHERE id = :id LIMIT 1")
    suspend fun getConfigById(id: Int): VpnConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: VpnConfig)

    @Update
    suspend fun updateConfig(config: VpnConfig)

    @Delete
    suspend fun deleteConfig(config: VpnConfig)
}
