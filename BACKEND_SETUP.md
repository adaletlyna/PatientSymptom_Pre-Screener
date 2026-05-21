# Backend Setup Guide - Patient Symptom Prescreener

## Overview
The backend is a **Ktor-based middleware** that:
1. Receives analysis requests from the Android app
2. Securely holds the Gemini API key (not transmitted to app)
3. Calls the Google Gemini API with patient symptoms
4. Returns results to the Android app

**Architecture:**
```
Android App (port 8080 via 10.0.2.2)
    ↓
Ktor Backend (port 8080 on 0.0.0.0)
    ↓
Google Gemini API (v1/beta/models/gemini-1.5-flash)
```

---

## Configuration

### 1. Set GEMINI_API_KEY
The backend loads the API key from (in order of preference):
1. **Environment variable** `GEMINI_API_KEY` (highest priority)
2. **local.properties** file (automatic, no setup needed)

#### Option A: Via local.properties (Recommended for Development)
The key should be in `${PROJECT_ROOT}/local.properties`:
```properties
GEMINI_API_KEY=YOUR_ACTUAL_KEY_HERE
```
The backend will automatically find and load it on startup.

#### Option B: Via Environment Variable (Recommended for CI/Production)
```powershell
$env:GEMINI_API_KEY='YOUR_ACTUAL_KEY_HERE'
.\run-dev.ps1
```

---

## Running the Backend

### Quick Start (from `backend/` folder)
```powershell
cd 'backend'
# Standard Gradle run command:
../gradlew :backend:run
```

The server will:
1. ✓ Start the backend on port 8080
2. ✓ Load config and display startup info

### Expected Output
```
════════════════════════════════════
Patient Pre-Screener Backend
════════════════════════════════════
Starting on port: 8080 (host: 0.0.0.0)
Android Emulator URL: http://10.0.2.2:8080
Local access URL:     http://localhost:8080
Health endpoint:      GET  http://localhost:8080/health
Analysis endpoint:    POST http://localhost:8080/analyze
════════════════════════════════════

[CONFIG] Found and loaded: .../local.properties
[CONFIG] ✓ Gemini API key loaded successfully (AIzaSyC...)
```

---

## Testing the Backend

### 1. Health Check
```powershell
curl http://localhost:8080/health
```
Expected response:
```
Patient Pre-Screener backend is running ✓
```

---

## Troubleshooting

### Issue: "Gemini API key not set"
**Check 1:** Verify local.properties exists in the project root.
**Check 2:** Verify environment variable is set.

### Issue: Android app can't reach backend
- Ensure backend is running on `0.0.0.0:8080`.
- Android Emulator uses `http://10.0.2.2:8080` (not `localhost`).

---

## Security
- ✓ API key is NOT stored in app code or APK.
- ✓ API key is held securely in backend process only.
- ⚠ **CRITICAL**: `local.properties` MUST be in your `.gitignore` to prevent leaking your key to GitHub.

---

## Next Steps
1. Change `BASE_URL` in `ApiService.kt` when deploying to production.
2. Use HTTPS instead of HTTP for production.
