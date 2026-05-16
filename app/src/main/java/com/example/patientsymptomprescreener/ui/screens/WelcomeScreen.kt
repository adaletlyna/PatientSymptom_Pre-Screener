package com.prescreener.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val scrollState = rememberScrollState()
    // Button is only enabled once the user has scrolled to the bottom of the disclaimer
    val isScrolledToBottom by remember {
        derivedStateOf { scrollState.value >= scrollState.maxValue - 10 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // App Icon & Title
        Text(text = "🏥", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "PATIENT SYMPTOM\nPRE-SCREENER",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI-assisted triage to help clinical staff\nprioritize your care",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Scrollable Disclaimer Card — user must scroll to enable the button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "⚠️ Important Medical Disclaimer",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = """
This tool does NOT replace a doctor's diagnosis or professional medical advice.

It is designed solely to assist healthcare staff in prioritizing patient assessments and preliminary triage.

• Do not rely on this app to make any medical decisions.
• Information entered is used only for this session and is not stored.
• AI-generated content may contain errors and must be reviewed by a qualified clinician.
• This app is not approved for diagnostic purposes.

IN CASE OF EMERGENCY, STOP USING THIS APP AND DIAL EMERGENCY SERVICES (e.g., 190, 911, or your local number) IMMEDIATELY.

By proceeding, you confirm you have read and understood this disclaimer.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGetStarted,
            enabled = isScrolledToBottom,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (isScrolledToBottom) "I Understand — Get Started" else "↓ Scroll to read disclaimer",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

