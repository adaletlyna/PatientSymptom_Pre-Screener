Param(
    [string]$Key = $null,
    [string]$Url = $null
)

# This script starts the backend in development using a GEMINI API key supplied
# either via parameter, environment variable or prompt. The key is NOT saved.

if (-not $Key) {
    # If the caller didn't pass -Key, prefer an existing environment variable.
    if ($env:GEMINI_API_KEY) {
        Write-Host "Using GEMINI_API_KEY from environment."
        # Copy it into $Key so later logic can rely on a single variable.
        $Key = $env:GEMINI_API_KEY
    } else {
        Write-Host "No GEMINI_API_KEY found in environment. You will be prompted to enter it (input hidden)."
        $secure = Read-Host -Prompt "Enter GEMINI_API_KEY (input hidden)" -AsSecureString
        $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
        try {
            $Key = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
        } finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
        }
    }
}

if ($Key) { $env:GEMINI_API_KEY = $Key }
if ($Url) { $env:GEMINI_API_URL = $Url }

Write-Host "Starting backend (GEMINI_API_KEY is set only for this session)."

# Ensure we run from the backend folder
Push-Location $PSScriptRoot
try {
    # Try to find gradlew.bat in this folder or up the tree (up to 3 parents)
    $wrapperPath = $null
    $searchDir = $PSScriptRoot
    for ($i = 0; $i -lt 4; $i++) {
        $candidate = Join-Path $searchDir 'gradlew.bat'
        if (Test-Path $candidate) { $wrapperPath = (Resolve-Path $candidate).Path; break }
        # move up one level; if we reach the root Resolve-Path will still give a path but Test-Path for parent may fail
        $parent = Join-Path $searchDir '..'
        try { $searchDir = (Resolve-Path $parent).Path } catch { break }
    }

    if (-not $wrapperPath) {
        Write-Error "gradlew.bat not found in '$PSScriptRoot' or parent folders. Please ensure the Gradle wrapper exists in the project root."
        return
    }

    # Determine which port the backend will try to use
    $preferredPort = if ($env:BACKEND_PORT) { [int]$env:BACKEND_PORT } elseif ($env:PORT) { [int]$env:PORT } else { 8080 }
    Write-Host "Checking port $preferredPort for existing listeners..."
    # Try modern cmdlet first, fall back to netstat
    $conn = $null
    try {
        $conn = Get-NetTCPConnection -LocalPort $preferredPort -ErrorAction SilentlyContinue
    } catch { $conn = $null }
    if (-not $conn) {
        $ns = netstat -ano | findstr ":$preferredPort"
        if ($ns) {
            # parse PID from netstat output
            $parts = $ns -split '\s+' | Where-Object { $_ -ne '' }
            $portPid = $parts[-1]
        } else {
            $portPid = $null
        }
    } else {
        $portPid = $conn.OwningProcess
    }

    if ($portPid) {
        Write-Host "Port $preferredPort is currently used by PID $portPid."
        try {
            $proc = Get-Process -Id $portPid -ErrorAction SilentlyContinue
            if ($proc) { Write-Host "Process name: $($proc.ProcessName) (Id: $($proc.Id))" }
        } catch { }

        $answer = Read-Host "Kill this process now? (Y/N)"
        if ($answer -match '^[Yy]') {
            try {
                Stop-Process -Id $portPid -Force -ErrorAction Stop
                Write-Host "Process $portPid killed. Proceeding to start backend."
            } catch {
                Write-Warning "Failed to kill process $portPid. You can stop it manually or choose a different port by setting BACKEND_PORT."
            }
        } else {
            Write-Host "Continuing without killing. If port remains busy the server may fail; you can set BACKEND_PORT to another port first."
        }
    } else {
        Write-Host "Port $preferredPort appears free."
    }

    # Run wrapper from its containing directory. If wrapper is in the project root, run the backend task explicitly.
    $wrapperDir = Split-Path $wrapperPath -Parent
    Push-Location $wrapperDir
    try {
        if ($wrapperDir -eq $PSScriptRoot) {
            # wrapper is inside backend
            & $wrapperPath run
        } else {
            # wrapper is in project root (or above) — run the backend module task explicitly
            & $wrapperPath ':backend:run'
        }
    } finally {
        Pop-Location
    }

} finally {
    Pop-Location
}

