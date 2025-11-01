package com.journeygirl.assetforecast.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asset_records")
data class AssetRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDays: Int,  // LocalDate.toEpochDay() で保存
    val amount: Long,        // 円
    val note: String? = null
)
