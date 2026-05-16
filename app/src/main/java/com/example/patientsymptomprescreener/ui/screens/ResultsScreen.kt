package com.prescreener.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prescreener.data.model.AnalysisResponse
import com.prescreener.data.model.UiState
import com.prescreener.data.model.UrgencyLevel
import com.prescreener.ui.SharedViewModel
import com.prescreener.ui.components.StepHeader
import com.prescreener.ui.components.SectionCard

@Composable
fun ResultsScreen(
    viewModel: SharedViewModel,
    onNewAssessment: () -> Unit
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val patientInfo by viewModel.patientInfo.collectAsState()
    val symptomInput by viewModel.symptomInput.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        StepHeader(step = 3, total = 3, title = "AI Analysis Results")
        Spacer(modifier = Modifier.height(20.dp))

        // ── Patient Profile (Static — entered by patient) ──────────────────
        SectionCard(title = "Patient Profile  (entered by patient)") {
            InfoRow("Age", patientInfo.age.ifBlank { "Not provided" })
            InfoRow("Sex", patientInfo.biologicalSex.label)
            InfoRow("Conditions", patientInfo.knownConditions.ifBlank { "None reported" })
            InfoRow("Allergies", patientInfo.allergies.ifBlank { "None reported" })
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Reported Symptoms (Static) ─────────────────────────────────────
        SectionCard(title = "Reported Symptoms  (entered by patient)") {
            if (symptomInput.freeText.isNotBlank()) {
                Text(
                    text = symptomInput.freeText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (symptomInput.selectedChips.isNotEmpty()) {
                Text(
                    text = "Quick Select Tags:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = symptomInput.selectedChips.joinToString("  │  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── AI-Generated Section ───────────────────────────────────────────
        when (val state = analysisState) {
            is UiState.Success -> {
                AiGeneratedResults(response = state.data)
            }
            else -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Analysis data unavailable.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Action Buttons ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNewAssessment,
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("← New Assessment")
            }
            Button(
                onClick = { /* TODO: Implement print/share */ },
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("🖨 Print Summary")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AiGeneratedResults(response: AnalysisResponse) {
    val urgency = UrgencyLevel.fromString(response.urgencyLevel)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✦  AI-GENERATED ANALYSIS  ✦  Powered by Gemini",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Urgency Level Badge
            Text(
                text = "Urgency Level",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(urgency.colorHex).copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = "${urgency.display} — Requires physician evaluation",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Color(urgency.colorHex),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Possible Categories
            Text(
                text = "Possible Medical Categories  ← GEMINI-GENERATED",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            response.possibleCategories.forEachIndexed { index, category ->
                Text(
                    text = "${index + 1}. $category",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (index < 2) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Doctor Note
            Text(
                text = "Doctor Note  ← GEMINI-GENERATED",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = response.doctorNote,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚠️ AI content is for triage assistance only. Not a medical diagnosis.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.6f)
        )
    }
}


