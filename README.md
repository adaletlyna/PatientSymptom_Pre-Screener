# 🏥 Patient Symptom Pre-Screener

> **AI-Powered Medical Triage System** — Android app + Ktor backend middleware  
> Based on the Technical System Design Report (April 2026)

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
│       │       │   ├── MainActivity.kt     ← App Entry Point
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
