package com.journeygirl.assetforecast.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM asset_records ORDER BY dateEpochDays ASC")
    fun observeAllAsc(): Flow<List<AssetRecord>>

    @Insert
    suspend fun insert(record: AssetRecord): Long

    @Update
    suspend fun update(record: AssetRecord)

    @Delete
    suspend fun delete(record: AssetRecord)
}
