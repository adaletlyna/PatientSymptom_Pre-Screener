# 🏥 Patient Symptom Pre-Screener

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Ktor](https://img.shields.io/badge/Backend-Ktor-000000?logo=ktor&logoColor=white)
![Gemini](https://img.shields.io/badge/AI-Google%20Gemini-4285F4?logo=google&logoColor=white)

An **AI-powered medical triage system** designed for clinical waiting areas. This project captures patient demographics and symptoms, which are then securely analyzed by Google's Gemini AI via a Ktor middleware to provide triage recommendations for clinical staff.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Project Folder Structure](#3-project-folder-structure)
4. [Prerequisites](#4-prerequisites)
5. [Step-by-Step Setup Guide](#5-step-by-step-setup-guide)
6. [AI Integration Deep Dive](#6-ai-integration-deep-dive)
7. [API Keys — Complete Guide](#7-api-keys--complete-guide)
8. [Testing](#8-testing)
9. [Going to Production](#9-going-to-production)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Project Overview

The Patient Symptom Pre-Screener is an Android application designed for clinical triage. It captures patient data and symptoms, which are then analyzed by a **Ktor backend middleware** using the **Google Gemini API**. The system provides a structured urgency assessment and a clinical summary for medical staff.

### Key features
| Feature | Detail |
|---|---|
| Patient intake | Age, biological sex, known conditions, allergies |
| Symptom input | Free-text narrative + Quick-Select symptom chips |
| AI triage | Urgency levels: Immediate, Urgent, Semi-Urgent, Non-Urgent |
| Doctor summary | AI-generated clinical summary & medical categories |
| Security | Secure API key handling; no sensitive keys in the Android APK |

---

## 2. Architecture

```
┌─────────────────────────────┐        HTTPS/TLS        ┌──────────────────────────┐
│  Android App (Jetpack Compose)│ ──────────────────────► │  Ktor Backend (JVM)      │
│                             │                          │  Port 8080               │
│  Screen 1: Welcome          │                          │  POST /analyze           │
│  Screen 2: Patient Info     │ ◄────────────────────── │                          │
│  Screen 3: Symptoms         │     AnalysisResponse     │  Builds Gemini prompt    │
│  Screen 4: Results          │                          │  Holds GEMINI_API_KEY    │
└─────────────────────────────┘                          └──────────┬───────────────┘
                                                                    │ HTTPS
                                                                    ▼
                                                         ┌──────────────────────────┐
                                                         │  Google Gemini API       │
                                                         │  gemini-1.5-flash model  │
                                                         └──────────────────────────┘
```

---

## 3. Project Folder Structure

```
PatientSymptomPrescreener/
├── app/                                    ← Android module
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/
│       │       ├── com/example/patientsymptomprescreener/
│       │       │   ├── MainActivity.kt         ← App Entry Point
│       │       │   ├── data/               ← Models & Repositories
│       │       │   ├── network/            ← ApiService (Ktor Client)
│       │       │   └── ui/screens/         ← Screen Composables
│       │       └── com/prescreener/
│       │           ├── ui/components/      ← Reusable UI Components
│       │           └── ui/navigation/      ← NavHost & Routing
│       ├── test/java/com/prescreener/      ← Local Unit Tests
│       │   ├── data/model/ModelUnitTest.kt
│       │   └── ui/SharedViewModelTest.kt
│       └── androidTest/java/com/prescreener/ ← UI & Navigation Tests
│           └── ui/navigation/NavigationTest.kt
│
├── backend/                                ← Ktor JVM backend
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/prescreener/backend/
│       │   ├── Application.kt              ← Server start-up
│       │   ├── config/ConfigManager.kt     ← Env var secret handling
│       │   ├── model/Models.kt             ← Gemini/API Models
│       │   └── plugins/
│       │       ├── Routing.kt              ← /analyze & /health routes
│       │       └── Serialization.kt
│       └── test/kotlin/com/prescreener/backend/
│           └── RoutingTest.kt              ← Server API tests
│
└── gradle/libs.versions.toml               ← Version catalog
```

---

## 4. Prerequisites

| Tool | Minimum version | Download |
|---|---|---|
| Android Studio | Hedgehog (2023.1.1) or later | [Official Site](https://developer.android.com/studio) |
| JDK | 17+ | Bundled with Android Studio |
| Kotlin | 2.0.0 | Managed via Gradle |
| Android SDK | API 26 (Android 8.0) | SDK Manager in Android Studio |
| Android Emulator | API 26+ | AVD Manager |
| Google AI Studio Account | — | To generate a Gemini API key |

---

## 5. Step-by-Step Setup Guide

### 5.1 Get a Gemini API Key
1. Go to **https://aistudio.google.com/app/apikey**
2. Click **"Create API Key"** and copy the key string.
3. Keep this key secret; do not commit it to version control.

### 5.2 Configure and Run the Backend
The backend requires the `GEMINI_API_KEY` as an environment variable.
```bash
cd backend
# Set the environment variable
export GEMINI_API_KEY=YOUR_KEY_HERE                 # macOS/Linux
set GEMINI_API_KEY=YOUR_KEY_HERE                    # Windows CMD
$env:GEMINI_API_KEY="YOUR_KEY_HERE"                 # Windows PowerShell
# Start the server
./gradlew run
```

### 5.3 Run the Android App
1. Open the project in Android Studio.
2. In `app/src/main/java/com/prescreener/network/ApiService.kt` (or your project's `ApiService.kt`), verify `BASE_URL = "http://10.0.2.2:8080"` (emulator's alias for localhost).
3. Ensure your emulator or device is connected.
4. Click **Run ▶** in the top toolbar.

---

## 6. AI Integration Deep Dive

### Prompt Engineering
The Ktor backend constructs a specific prompt for Gemini 1.5 Flash. It provides patient context (age, sex, history) and symptoms, then instructs the AI to return a structured JSON response.

### Triage Logic
The AI is instructed to categorize the patient into one of four urgency levels:
- **Immediate**: Emergency conditions requiring instant intervention.
- **Urgent**: High-priority care needed within minutes.
- **Semi-Urgent**: Standard priority, stable condition.
- **Non-Urgent**: Minor issues, routine follow-up.

---

## 7. API Keys — Complete Guide

The `GEMINI_API_KEY` is handled exclusively by the backend middleware. This ensures that the key is never bundled with the Android application, protecting it from extraction via reverse engineering.

**Backend Setup:**
- **Local Development**: Use environment variables or IDE Run Configurations.
- **Production**: Use a secrets manager (e.g., AWS Secrets Manager, Google Cloud Secret Manager).

---

## 8. Testing

The project includes a multi-tiered test suite to ensure the reliability of the triage process.

### 8.1 Backend API Tests
**File:** `backend/src/test/kotlin/com/prescreener/backend/RoutingTest.kt`
- Verifies server health and POST request handling.
- **Run command:** `./gradlew :backend:test`

### 8.2 Android Local Unit Tests
**Files:**
- `app/src/test/java/com/prescreener/data/model/ModelUnitTest.kt`
- `app/src/test/java/com/prescreener/ui/SharedViewModelTest.kt`
- Verifies medical data logic, serialization, and ViewModel state transitions.
- **Run command:** `./gradlew :app:testDebugUnitTest`

### 8.3 Android Instrumented UI Tests
**File:** `app/src/androidTest/java/com/prescreener/ui/navigation/NavigationTest.kt`
- Verifies end-to-end user navigation flows on a device/emulator.
- **Run command:** `./gradlew :app:connectedDebugAndroidTest`

---

## 9. Going to Production

1. **Enable HTTPS**: Use SSL/TLS for your backend and update the Android `BASE_URL`.
2. **Disable Cleartext**: Remove `android:usesCleartextTraffic="true"` from `AndroidManifest.xml`.
3. **Authentication**: Implement authentication (e.g., OAuth2 or API Keys) to secure the `/analyze` endpoint.
4. **Obfuscation**: Use R8/ProGuard to shrink and secure the Android code.

---

## 10. Troubleshooting

| Problem | Solution |
|---|---|
| Build error: `minus` operator | Ensure `libs` accessors in `build.gradle.kts` use dots (e.g., `libs.androidx.core.ktx`) instead of hyphens. |
| API Key not found | Verify `GEMINI_API_KEY` is exported in the terminal where the backend is run. |
| Backend connection failed | Check that the backend is running and `BASE_URL` in `ApiService.kt` is `http://10.0.2.2:8080` for emulator usage. |

---

## License

This project is a technical reference.  
**Disclaimer: This tool is NOT for diagnostic use. It is a triage assistance tool for clinical staff.**
