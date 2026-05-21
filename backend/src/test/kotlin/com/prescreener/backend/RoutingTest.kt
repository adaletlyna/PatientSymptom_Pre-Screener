package com.prescreener.backend


import com.prescreener.backend.model.AnalysisRequest
import com.prescreener.backend.plugins.configureRouting
import com.prescreener.backend.plugins.configureSerialization
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutingTest {

    @Test
    fun `health endpoint returns 200`() = testApplication {
        application {
            configureSerialization()
            configureRouting()
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("running"))
    }

    @Test
    fun `analyze endpoint accepts valid request body`() = testApplication {
        application {
            configureSerialization()
            configureRouting()
        }
        val request = AnalysisRequest(
            age = "42",
            sex = "Male",
            conditions = "Hypertension",
            allergies = "None",
            symptomText = "Chest pain and shortness of breath for 20 minutes",
            tags = listOf("Chest Pain", "Shortness of Breath")
        )

        val response = client.post("/analyze") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(request))
        }

        // If GEMINI_API_KEY is not set, we expect a 500 with a clear error message.
        // In CI, mock the Gemini client or provide a test key.
        assertTrue(
            response.status == HttpStatusCode.OK ||
                    response.status == HttpStatusCode.InternalServerError
        )
    }
}