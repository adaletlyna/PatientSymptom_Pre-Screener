package com.prescreener.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prescreener.data.model.*
import com.prescreener.data.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel(
    private val repository: SymptomRepository = SymptomRepository()
) : ViewModel() {

    // ─── Patient Info State ─────────────────────────────────────────────────
    private val _patientInfo = MutableStateFlow(PatientInfo())
    val patientInfo: StateFlow<PatientInfo> = _patientInfo.asStateFlow()

    // ─── Symptom State ──────────────────────────────────────────────────────
    private val _symptomInput = MutableStateFlow(SymptomInput())
    val symptomInput: StateFlow<SymptomInput> = _symptomInput.asStateFlow()

    // ─── Analysis Result State ──────────────────────────────────────────────
    private val _analysisState = MutableStateFlow<UiState<AnalysisResponse>>(UiState.Idle)
    val analysisState: StateFlow<UiState<AnalysisResponse>> = _analysisState.asStateFlow()

    // ─── Patient Info Mutations ─────────────────────────────────────────────
    fun updateAge(age: String) {
        _patientInfo.value = _patientInfo.value.copy(age = age)
    }

    fun updateSex(sex: BiologicalSex) {
        _patientInfo.value = _patientInfo.value.copy(biologicalSex = sex)
    }

    fun updateConditions(conditions: String) {
        _patientInfo.value = _patientInfo.value.copy(knownConditions = conditions)
    }

    fun updateAllergies(allergies: String) {
        _patientInfo.value = _patientInfo.value.copy(allergies = allergies)
    }

    // ─── Symptom Input Mutations ────────────────────────────────────────────
    fun updateSymptomText(text: String) {
        _symptomInput.value = _symptomInput.value.copy(freeText = text)
    }

    fun toggleChip(chip: String) {
        val current = _symptomInput.value.selectedChips.toMutableSet()
        if (current.contains(chip)) current.remove(chip) else current.add(chip)
        _symptomInput.value = _symptomInput.value.copy(selectedChips = current)
    }

    // ─── AI Analysis ────────────────────────────────────────────────────────
    fun analyze() {
        val info = _patientInfo.value
        val symptoms = _symptomInput.value

        viewModelScope.launch {
            _analysisState.value = UiState.Loading

            val request = AnalysisRequest(
                age = info.age.ifBlank { "Unknown" },
                sex = info.biologicalSex.label,
                conditions = info.knownConditions.ifBlank { "None reported" },
                allergies = info.allergies.ifBlank { "None reported" },
                symptomText = symptoms.freeText,
                tags = symptoms.selectedChips.toList()
            )

            val result = repository.analyze(request)

            _analysisState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "An unknown error occurred") }
            )
        }
    }

    fun resetAnalysis() {
        _analysisState.value = UiState.Idle
    }

    fun resetAll() {
        _patientInfo.value = PatientInfo()
        _symptomInput.value = SymptomInput()
        _analysisState.value = UiState.Idle
    }
}


