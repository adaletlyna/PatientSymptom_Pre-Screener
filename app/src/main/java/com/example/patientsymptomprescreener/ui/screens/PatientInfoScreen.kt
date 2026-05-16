package com.prescreener.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.prescreener.data.model.BiologicalSex
import com.prescreener.ui.SharedViewModel
import com.prescreener.ui.components.StepHeader

@Composable
fun PatientInfoScreen(
    viewModel: SharedViewModel,
    onNext: () -> Unit
) {
    val patientInfo by viewModel.patientInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        StepHeader(step = 1, total = 3, title = "Your Information")
        Spacer(modifier = Modifier.height(24.dp))

        // Age Field
        OutlinedTextField(
            value = patientInfo.age,
            onValueChange = viewModel::updateAge,
            label = { Text("Age") },
            placeholder = { Text("Enter your age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Biological Sex Selector
        Text(
            text = "Biological Sex",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BiologicalSex.entries.forEach { sex ->
                FilterChip(
                    selected = patientInfo.biologicalSex == sex,
                    onClick = { viewModel.updateSex(sex) },
                    label = { Text(sex.label, style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Known Conditions
        OutlinedTextField(
            value = patientInfo.knownConditions,
            onValueChange = viewModel::updateConditions,
            label = { Text("Known Medical Conditions (optional)") },
            placeholder = { Text("e.g., Diabetes, Hypertension, Asthma...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Allergies
        OutlinedTextField(
            value = patientInfo.allergies,
            onValueChange = viewModel::updateAllergies,
            label = { Text("Known Allergies (optional)") },
            placeholder = { Text("e.g., Penicillin, Sulfa drugs, Aspirin...") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Next: Describe Symptoms →")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}


