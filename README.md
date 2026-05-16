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
   - 5.1 [Get a Gemini API Key](#51-get-a-gemini-api-key--required)
   - 5.2 [Create the Android Project](#52-create-the-android-project-in-android-studio)
   - 5.3 [Copy Source Files](#53-copy-the-source-files)
   - 5.4 [Configure the Backend](#54-configure-and-run-the-backend)
   - 5.5 [Run the Android App](#55-run-the-android-app)
6. [AI Integration Deep Dive](#6-ai-integration-deep-dive)
7. [API Keys — Complete Guide](#7-api-keys--complete-guide)
8. [Testing](#8-testing)
9. [Going to Production](#9-going-to-production)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Project Overview

The Patient Symptom Pre-Screener is a 4-screen Android application designed for clinical waiting areas. A patient enters their demographic data and describes their symptoms; the app sends that data to a **Ktor backend middleware** which constructs a medically-framed prompt and calls the **Google Gemini API**. Gemini returns a structured JSON triage summary that the app renders for clinical staff.

### Key features
| Feature | Detail |
|---|---|
| Patient intake | Age, biological sex, known conditions, allergies |
| Symptom input | Free-text narrative + Quick-Select symptom chips |
| AI triage | Urgency level (Immediate / Urgent / Semi-Urgent / Non-Urgent) |
| Doctor summary | Possible medical categories + AI-generated Doctor Note |
| Security | API key never on device; TLS 1.3 enforced for production |

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

**Why a backend middleware?**  
The Gemini API key must *never* be embedded in an Android APK — it can be extracted by anyone who decompiles the app. The Ktor backend acts as a secure proxy: it holds the key as an environment variable and the Android app only ever talks to the backend.

---

## 3. Project Folder Structure

```
PatientSymptomPrescreener/
├── app/                                    ← Android module
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/prescreener/
│           ├── MainActivity.kt
│           ├── data/
│           │   ├── model/
│           │   │   └── Models.kt           ← All data classes & enums
│           │   └── repository/
│           │       └── SymptomRepository.kt
│           ├── network/
│           │   └── ApiService.kt           ← Ktor HTTP client
│           └── ui/
│               ├── SharedViewModel.kt      ← Single ViewModel for all screens
│               ├── components/
│               │   └── Components.kt       ← Reusable Compose components
│               ├── navigation/
│               │   └── Navigation.kt       ← NavHost with 4 routes
│               ├── screens/
│               │   ├── WelcomeScreen.kt    ← Screen 1: disclaimer gate
│               │   ├── PatientInfoScreen.kt ← Screen 2: demographics
│               │   ├── SymptomsScreen.kt   ← Screen 3: symptom input + AI trigger
│               │   └── ResultsScreen.kt    ← Screen 4: AI triage output
│               └── theme/
│                   └── Theme.kt            ← Material 3 color scheme
│
├── backend/                                ← Ktor JVM backend (separate process)
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/prescreener/backend/
│       │   ├── Application.kt              ← Entry point, starts Netty on :8080
│       │   ├── model/
│       │   │   └── Models.kt               ← Request/response + Gemini API wrappers
│       │   └── plugins/
│       │       ├── Routing.kt              ← /health + /analyze endpoints + Gemini call
│       │       └── Serialization.kt
│       └── test/kotlin/com/prescreener/backend/
│           └── RoutingTest.kt
│
├── build.gradle.kts                        ← Root project build file
└── gradle/
    └── libs.versions.toml                  ← Version catalog
```

---

## 4. Prerequisites

| Tool | Minimum version | Download |
|---|---|---|
| Android Studio | Hedgehog (2023.1.1) or later | https://developer.android.com/studio |
| JDK | 17+ | Bundled with Android Studio |
| Kotlin | 2.0.0 | Via Gradle |
| Android SDK | API 26 (Android 8.0) | SDK Manager in Android Studio |
| Android Emulator or device | API 26+ | AVD Manager |
| A Google account | — | To get a Gemini API key |

---

## 5. Step-by-Step Setup Guide

### 5.1 Get a Gemini API Key ← Required

The **Gemini API key** is the only external credential this project requires.

1. Go to **https://aistudio.google.com/app/apikey**
2. Sign in with your Google account
3. Click **"Create API Key"**
4. Copy the key — it looks like: `AIzaSy...`
5. **Keep this key secret.** Never paste it into your Android code or commit it to git.

> **Free tier:** Google AI Studio provides a free tier with generous limits (60 requests/minute as of 2026). No credit card required for development.

---

### 5.2 Create the Android Project in Android Studio

1. Open **Android Studio** → **New Project**
2. Choose **"Empty Activity"**
3. Set:
   - **Name:** `PatientSymptomPrescreener`
   - **Package name:** `com.prescreener`
   - **Save location:** your preferred folder
   - **Language:** Kotlin
   - **Minimum SDK:** API 26 (Android 8.0)
4. Click **Finish** and wait for Gradle sync

---

### 5.3 Copy the Source Files

Replace/create every file in `app/src/main/java/com/prescreener/` with the provided source files, maintaining the folder hierarchy shown in [Section 3](#3-project-folder-structure).

**Then update `app/build.gradle.kts`** to include the dependencies from the provided file (Ktor client, Navigation Compose, Accompanist FlowLayout, etc.).

After copying, click **"Sync Now"** in the Gradle bar at the top of Android Studio.

---

### 5.4 Configure and Run the Backend

The backend is a **standalone Kotlin JVM project** — it runs on your computer (or a server), not on the Android device.

#### Option A: Run from command line (Quickest)

```bash
# Navigate to the backend directory
cd PatientSymptomPrescreener/backend

# Set your Gemini API key as an environment variable
export GEMINI_API_KEY=AIzaSy...your_key_here...   # macOS/Linux
set GEMINI_API_KEY=AIzaSy...your_key_here...       # Windows CMD
$env:GEMINI_API_KEY="AIzaSy...your_key_here..."    # Windows PowerShell

# Run the backend
./gradlew run
```

You should see:
```
Application started: http://0.0.0.0:8080
```

Verify it works:
```bash
curl http://localhost:8080/health
# → Patient Pre-Screener backend is running ✓
```

#### Option B: Open as a separate project in IntelliJ IDEA

1. Open IntelliJ IDEA → **Open** → select the `backend/` folder
2. Go to **Run → Edit Configurations**
3. Add an **Environment Variable**: `GEMINI_API_KEY=AIzaSy...`
4. Run `Application.kt`

---

### 5.5 Run the Android App

1. In Android Studio, start an **Android Emulator** (API 26+) via AVD Manager
2. Confirm the backend is running (step 5.4)
3. In `app/src/main/java/com/prescreener/network/ApiService.kt`, verify:
   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:8080"
   // 10.0.2.2 is the Android Emulator's alias for your computer's localhost
   ```
4. Click **Run ▶** in Android Studio

The app will launch on the emulator. The full flow:

```
Welcome screen → scroll disclaimer → Get Started
    ↓
Patient Info → fill age, sex, conditions, allergies → Next
    ↓
Symptoms → type symptoms + select chips → Analyze with AI
    ↓
Results → view urgency level, categories, Doctor Note
```

---

## 6. AI Integration Deep Dive

### How the AI works — end to end

```
SymptomsScreen.kt
  └─ viewModel.analyze()
       └─ SymptomRepository.analyze(request)
            └─ ApiService.client.post("http://10.0.2.2:8080/analyze", body=AnalysisRequest)
                 └─ [Network hop to Ktor backend]
                      └─ Routing.kt: POST /analyze
                           ├─ Reads GEMINI_API_KEY from environment
                           ├─ Builds system prompt (medical framing + JSON output schema)
                           ├─ Builds user prompt (patient data template)
                           └─ httpClient.post(GEMINI_URL, body=GeminiRequest)
                                └─ [Gemini API processes the prompt]
                                     └─ Returns JSON: { possible_categories, urgency_level, doctor_note }
                                          └─ Parsed → AnalysisResponse → sent back to Android app
                                               └─ ResultsScreen renders the triage summary
```

### Prompt Engineering Strategy

**System Prompt** (instructs Gemini's behavior):
```
You are a medical triage assistant helping clinical staff prioritize patient care.

Respond ONLY in valid JSON format with exactly these keys:
- "possible_categories": array of strings (3–5 possible medical categories, most likely first)
- "urgency_level": exactly one of "Immediate", "Urgent", "Semi-Urgent", or "Non-Urgent"
- "doctor_note": string, max 100 words, a concise clinical summary with recommended actions

Do not add any text, explanation, or markdown outside the JSON object.
```

**User Prompt** (filled with patient data per request):
```
Patient: {age} year old {sex}.
Known conditions: {conditions}.
Allergies: {allergies}.
Symptoms reported: {symptomText}.
Quick-select tags: {tags}.
```

### Why `gemini-1.5-flash`?

The design doc specifies < 5 seconds p95 latency. `gemini-1.5-flash` is Google's fastest production model and consistently meets this threshold. To trade speed for higher quality output, change the constant in `Routing.kt`:

```kotlin
// In backend/src/.../plugins/Routing.kt
private const val GEMINI_MODEL = "gemini-1.5-pro"  // higher quality, slightly slower
```

### Structured JSON output

The backend uses `responseMimeType = "application/json"` in the `GenerationConfig`. This instructs Gemini to return only valid JSON — no markdown fences, no preamble — making parsing deterministic and robust.

---

## 7. API Keys — Complete Guide

### What keys does this project need?

| Key | Required? | Who uses it | Where to store it |
|---|---|---|---|
| **Gemini API Key** | ✅ Yes | Ktor backend only | Environment variable `GEMINI_API_KEY` |

That's it. There are no other API keys required.

### Why the key must NEVER be in the Android app

Android APKs can be decompiled in seconds with free tools. Any string or constant you put in Kotlin code is recoverable from the compiled APK. If your Gemini key is in the app:

- Anyone who downloads your APK can extract and use your key
- Your quota will be exhausted (or you'll be billed) for requests you didn't make
- Google may revoke the key without warning

The Ktor backend is the security boundary. The Android app only calls `POST /analyze` on your backend. The backend calls Gemini. The key stays on the server.

### Environment variable setup per platform

**macOS / Linux (temporary — current session only):**
```bash
export GEMINI_API_KEY=AIzaSy...
./gradlew run
```

**macOS / Linux (permanent — add to shell profile):**
```bash
echo 'export GEMINI_API_KEY=AIzaSy...' >> ~/.zshrc   # or ~/.bashrc
source ~/.zshrc
```

**Windows (Command Prompt):**
```cmd
set GEMINI_API_KEY=AIzaSy...
gradlew.bat run
```

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY = "AIzaSy..."
.\gradlew run
```

**IntelliJ IDEA / Android Studio Run Configuration:**
1. Run → Edit Configurations
2. Select your run config
3. Click "Modify options" → "Environment variables"
4. Add: `GEMINI_API_KEY=AIzaSy...`

**Production deployment (e.g., Google Cloud Run, AWS, Railway):**
Set the environment variable in your hosting provider's dashboard or secrets manager. Never include it in a `Dockerfile` or `docker-compose.yml` committed to git.

---

## 8. Testing

### Backend tests

```bash
cd backend
./gradlew test
```

The provided `RoutingTest.kt` tests:
- `/health` endpoint returns HTTP 200
- `/analyze` endpoint accepts a valid request body

> Note: The `/analyze` test expects either 200 (if `GEMINI_API_KEY` is set) or 500 with a clear error (if not). In CI, either provide a test key via environment variable or mock the Gemini HTTP client.

### Android UI tests

Android Studio includes Espresso and Compose test support. Run instrumented tests with:

```bash
cd app
./gradlew connectedAndroidTest
```

Recommended test cases to add:
- `WelcomeScreenTest`: verify button is disabled until disclaimer is scrolled
- `PatientInfoScreenTest`: verify "Next" navigation works
- `SymptomsScreenTest`: verify chip toggling adds/removes from selected set
- `ResultsScreenTest`: verify urgency level color mapping

### Manual end-to-end test (smoke test)

Use the cardiac emergency example from the design doc:

| Field | Value |
|---|---|
| Age | 42 |
| Sex | Male |
| Conditions | HTN |
| Allergies | None |
| Symptoms | Sudden tightness in my chest and left arm pain for the past 20 minutes. Feeling very sweaty. |
| Chips | Chest Pain, Shortness of Breath, Dizziness |

Expected: Urgency level **IMMEDIATE**, categories including Acute Coronary Syndrome / Myocardial Infarction.

---

## 9. Going to Production

Before deploying to real clinical environments:

1. **Remove `android:usesCleartextTraffic="true"`** from `AndroidManifest.xml`
2. **Change `BASE_URL`** in `ApiService.kt` to your deployed backend HTTPS URL
3. **Deploy the backend** to a cloud provider (Railway, Render, Google Cloud Run, AWS ECS, etc.) with `GEMINI_API_KEY` set as a secret environment variable
4. **Implement authentication** on the `/analyze` endpoint (e.g., a shared secret header between app and backend)
5. **Enable ProGuard/R8** in the release build (`isMinifyEnabled = true`)
6. **Review GDPR / HIPAA compliance** — the current implementation does not persist patient data; confirm this meets your regional regulations
7. **Add rate limiting** to the backend endpoint to prevent abuse

---

## 10. Troubleshooting

| Problem | Solution |
|---|---|
| `GEMINI_API_KEY environment variable is not set` | Set the env var before running the backend (see Section 7) |
| App can't connect to backend | Ensure backend is running; emulator uses `10.0.2.2`, not `localhost` |
| `Failed to connect to 10.0.2.2:8080` | Backend is not running, or the port is blocked by a firewall |
| Gemini returns 403 | API key is invalid or expired — regenerate at aistudio.google.com |
| Gemini returns 429 | Rate limit exceeded — wait 60 seconds or upgrade your plan |
| JSON parse error from Gemini | The model returned non-JSON text; check the system prompt is correctly set |
| `usesCleartextTraffic` error | Add `android:usesCleartextTraffic="true"` to the `<application>` tag in `AndroidManifest.xml` for local dev only |
| `FlowRow` not found | Ensure `accompanist-flowlayout:0.32.0` is in `app/build.gradle.kts` |
| Gradle sync fails | Check JDK version is 17+; try File → Invalidate Caches → Restart |

---

## License

This project is provided as an implementation reference based on the Patient Symptom Pre-Screener Technical Design Report (April 2026).  
**This tool is NOT approved for diagnostic use. It is a triage assistance tool for clinical staff.**
