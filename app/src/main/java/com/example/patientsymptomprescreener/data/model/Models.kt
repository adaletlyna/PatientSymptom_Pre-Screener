package com.prescreener.data.model


import kotlinx.serialization.Serializable

// ─── Patient Input Models ───────────────────────────────────────────────────

data class PatientInfo(
    val age: String = "",
    val biologicalSex: BiologicalSex = BiologicalSex.PREFER_NOT_TO_SAY,
    val knownConditions: String = "",
    val allergies: String = ""
)

enum class BiologicalSex(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
    PREFER_NOT_TO_SAY("Prefer not to say")
}

data class SymptomInput(
    val freeText: String = "",
    val selectedChips: Set<String> = emptySet()
)

// ─── API Request / Response Models ─────────────────────────────────────────

@Serializable
data class AnalysisRequest(
    val age: String,
    val sex: String,
    val conditions: String,
    val allergies: String,
    val symptomText: String,
    val tags: List<String>
)

@Serializable
data class AnalysisResponse(
    val possibleCategories: List<String>,
    val urgencyLevel: String,
    val doctorNote: String
)

// ─── UI State ───────────────────────────────────────────────────────────────

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// ─── Quick-Select Chips ─────────────────────────────────────────────────────

val QUICK_SELECT_SYMPTOMS = listOf(
    "Fever",
    "Chest Pain",
    "Shortness of Breath",
    "Nausea",
    "Headache",
    "Dizziness",
    "Vomiting",
    "Back Pain",
    "Fatigue",
    "Abdominal Pain",
    "Swelling",
    "Palpitations"
)

// ─── Urgency Level ──────────────────────────────────────────────────────────

enum class UrgencyLevel(val display: String, val colorHex: Long) {
    IMMEDIATE("🔴 IMMEDIATE", 0xFFD32F2F),
    URGENT("🟠 URGENT", 0xFFF57C00),
    SEMI_URGENT("🟡 SEMI-URGENT", 0xFFF9A825),
    NON_URGENT("🟢 NON-URGENT", 0xFF388E3C),
    UNKNOWN("⚪ UNKNOWN", 0xFF757575);

    companion object {
        fun fromString(value: String): UrgencyLevel =
            entries.firstOrNull {
                it.display.contains(value.trim(), ignoreCase = true) ||
                        value.trim().equals(it.name.replace("_", " "), ignoreCase = true)
            } ?: UNKNOWN
    }
}

