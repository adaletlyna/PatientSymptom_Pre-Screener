package com.prescreener.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.prescreener.data.model.QUICK_SELECT_SYMPTOMS
import com.prescreener.data.model.UiState
import com.prescreener.ui.SharedViewModel
import com.prescreener.ui.components.StepHeader

@Composable
fun SymptomsScreen(
    viewModel: SharedViewModel,
    onAnalyzeComplete: () -> Unit
) {
    val symptomInput by viewModel.symptomInput.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()

    // Navigate to results when analysis succeeds
    LaunchedEffect(analysisState) {
        if (analysisState is UiState.Success) {
            onAnalyzeComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        StepHeader(step = 2, total = 3, title = "Describe Your Symptoms")
        Spacer(modifier = Modifier.height(24.dp))

        // Free-Text Area
        Text(
            text = "Tell us how you're feeling",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = symptomInput.freeText,
            onValueChange = viewModel::updateSymptomText,
            placeholder = {
                Text("Describe your symptoms in your own words. When did they start? How severe?")
            },
            minLines = 5,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick-Select Chips
        Text(
            text = "Quick Select — Common Symptoms",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            QUICK_SELECT_SYMPTOMS.forEach { chip ->
                FilterChip(
                    selected = symptomInput.selectedChips.contains(chip),
                    onClick = { viewModel.toggleChip(chip) },
                    label = { Text(chip) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        if (analysisState is UiState.Error) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ ${(analysisState as UiState.Error).message}",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Analyze Button
        Button(
            onClick = { viewModel.analyze() },
            enabled = analysisState !is UiState.Loading &&
                    (symptomInput.freeText.isNotBlank() || symptomInput.selectedChips.isNotEmpty()),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (analysisState is UiState.Loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Text("Analyzing...")
                }
            } else {
                Text("🔍  Analyze with AI")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}


