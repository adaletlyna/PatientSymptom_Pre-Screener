package com.prescreener.backend.plugins

import com.prescreener.backend.config.ConfigManager
import com.prescreener.backend.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    routing {
        get("/health") {
            val key = ConfigManager.getString("GEMINI_API_KEY")
            val status = if (key.isNullOrBlank()) "MISSING ✗" else "LOADED ✓"
            call.respondText("Backend: RUNNING ✓\nAPI Key: $status\nVersion: 4.0 (Gemini 2.5 Update)")
        }

        // Discovery: Check current models
        get("/models") {
            val key = ConfigManager.getString("GEMINI_API_KEY")
            if (key.isNullOrBlank()) return@get call.respondText("API Key Missing")
            val url = "https://generativelanguage.googleapis.com/v1/models?key=$key"
            try {
                call.respondText(client.get(url).bodyAsText())
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}")
            }
        }

        get("/test-key") {
            val key = ConfigManager.getString("GEMINI_API_KEY")
            if (key.isNullOrBlank()) return@get call.respondText("API Key Missing")
            
            // Updated to Gemini 2.5 Flash as per your discovery!
            val testUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=$key"
            val testRequest = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = "Hello, testing Gemini 2.5")))))
            
            try {
                val response: HttpResponse = client.post(testUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(testRequest)
                }
                call.respondText("Gemini Response (${response.status}):\n${response.bodyAsText()}")
            } catch (e: Exception) {
                call.respondText("Connection Error: ${e.message}")
            }
        }

        post("/analyze") {
            val key = ConfigManager.getString("GEMINI_API_KEY") ?: return@post call.respond(HttpStatusCode.InternalServerError, "Key Missing")
            val req = call.receive<AnalysisRequest>()
            
            // Updated to Gemini 2.5 Flash
            val geminiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=$key"
            
            val prompt = """
                You are a medical triage assistant. Analyze:
                Age: ${req.age}, Sex: ${req.sex}, Conditions: ${req.conditions}, Symptoms: ${req.symptomText}.
                Return ONLY valid JSON with keys:
                "possibleCategories" (list of strings), 
                "urgencyLevel" (exactly one of: IMMEDIATE, URGENT, SEMI-URGENT, NON-URGENT), 
                "doctorNote" (string).
            """.trimIndent()

            try {
                val response: HttpResponse = client.post(geminiUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = prompt))))))
                }
                
                if (response.status != HttpStatusCode.OK) {
                    return@post call.respond(HttpStatusCode.InternalServerError, response.bodyAsText())
                }

                val gResp = response.body<GeminiResponse>()
                val jsonText = gResp.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "{}"
                val clean = jsonText.trim().removeSurrounding("```json", "```").trim()
                call.respond(Json.decodeFromString<AnalysisResponse>(clean))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown Error")
            }
        }
    }
}
