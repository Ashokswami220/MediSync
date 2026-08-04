package com.example.medisync.repo

import com.example.medisync.model.AppConfig
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    fun getConfig(): Flow<AppConfig>
    suspend fun updateConfig(config: AppConfig): Result<Unit>
}
