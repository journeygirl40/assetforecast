package com.journeygirl.assetforecast.data.repo

import com.journeygirl.assetforecast.data.db.AssetDao
import com.journeygirl.assetforecast.data.db.AssetRecord
import kotlinx.coroutines.flow.Flow

class AssetRepository(private val dao: AssetDao) {
    fun observeAll(): Flow<List<AssetRecord>> = dao.observeAllAsc()
    suspend fun insert(record: AssetRecord) = dao.insert(record)
    suspend fun update(record: AssetRecord) = dao.update(record)
    suspend fun delete(record: AssetRecord) = dao.delete(record)
}
