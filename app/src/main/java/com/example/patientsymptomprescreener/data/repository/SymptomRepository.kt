package com.prescreener.data.repository

import com.prescreener.data.model.AnalysisRequest
import com.prescreener.data.model.AnalysisResponse
import com.prescreener.network.ApiService

class SymptomRepository {

    suspend fun analyze(request: AnalysisRequest): Result<AnalysisResponse> {
        return try {
            val response = ApiService.analyze(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


