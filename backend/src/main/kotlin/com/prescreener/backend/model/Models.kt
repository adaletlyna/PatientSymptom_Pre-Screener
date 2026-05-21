package com.prescreener.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class AnalysisRequest(
    val age: String,
    val sex: String,
    val conditions: String,
    val allergies: String,
    val symptomText: String,
    val tags: List<String> = emptyList()
)

@Serializable
data class AnalysisResponse(
    val possibleCategories: List<String>,
    val urgencyLevel: String,
    val doctorNote: String
)

// Gemini API structures
@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig = GenerationConfig()
)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String)

@Serializable
data class GenerationConfig(
    val responseMimeType: String = "application/json"
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(val content: Content)
