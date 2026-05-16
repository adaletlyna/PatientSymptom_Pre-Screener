package com.prescreener.network

import com.prescreener.data.model.AnalysisRequest
import com.prescreener.data.model.AnalysisResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiService {

    private const val BASE_URL = "http://10.0.2.2:8080"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.BODY
        }
    }

    suspend fun analyze(request: AnalysisRequest): AnalysisResponse {
        val response: HttpResponse = client.post("$BASE_URL/analyze") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body()
        } else {
            // Read the error message from the backend
            val errorBody = response.bodyAsText()
            throw Exception("Backend Error (${response.status}): $errorBody")
        }
    }
}
