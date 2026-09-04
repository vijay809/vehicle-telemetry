@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo          AntiGravity Android App Auto-Installer
echo ========================================================
echo.

cd /d "%~dp0"

:: 1. Locate adb.exe
where adb >nul 2>&1
if %errorlevel% neq 0 (
    echo [*] Checking common ADB installation paths...
    if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;%PATH%"
        echo [+] Found ADB in %LOCALAPPDATA%\Android\Sdk\platform-tools
    ) else if exist "C:\platform-tools\adb.exe" (
        set "PATH=C:\platform-tools;%PATH%"
        echo [+] Found ADB in C:\platform-tools
    ) else if exist "C:\Android\platform-tools\adb.exe" (
        set "PATH=C:\Android\platform-tools;%PATH%"
        echo [+] Found ADB in C:\Android\platform-tools
    ) else (
        echo [!] ERROR: 'adb' executable not found in PATH or standard directories.
        echo     Please ensure platform-tools is installed or added to PATH.
        pause
        exit /b 1
    )
) else (
    echo [+] ADB found in system PATH.
)

:: 2. Check connected devices
echo.
echo [*] Checking connected Android devices via ADB...
adb devices
echo.

:: 3. Locate Java / JDK if not configured
where java >nul 2>&1
if %errorlevel% neq 0 (
    if not defined JAVA_HOME (
        echo [*] Searching for Android Studio bundled JBR/JDK...
        if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
            set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
            set "PATH=%JAVA_HOME%\bin;%PATH%"
            echo [+] Using Android Studio JBR: %JAVA_HOME%
        ) else if exist "%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\java.exe" (
            set "JAVA_HOME=%LOCALAPPDATA%\Programs\Android Studio\jbr"
            set "PATH=%JAVA_HOME%\bin;%PATH%"
            echo [+] Using Android Studio JBR: %JAVA_HOME%
        ) else (
            echo [!] WARNING: JAVA_HOME is not set and java.exe is not in PATH.
            echo     If build fails, please set JAVA_HOME to your JDK directory.
        )
    )
)

:: 4. Ensure gradle-wrapper.jar is present
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [*] gradle-wrapper.jar missing. Downloading Gradle wrapper jar...
    curl.exe -fSL -o "gradle\wrapper\gradle-wrapper.jar" "https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradle/wrapper/gradle-wrapper.jar" 2>nul
    if not exist "gradle\wrapper\gradle-wrapper.jar" (
        curl.exe -fSL -o "gradle\wrapper\gradle-wrapper.jar" "https://github.com/nicoulaj/gradle-wrapper-jar/raw/master/gradle-wrapper.jar" 2>nul
    )
)

:: 5. Build and Install via Gradle
echo [*] Building and installing AntiGravity debug APK...
if exist "gradle\wrapper\gradle-wrapper.jar" (
    call gradlew.bat installDebug
) else (
    echo [*] Attempting direct gradle build...
    call gradle installDebug
)

if %errorlevel% neq 0 (
    echo.
    echo [!] Build or installation failed. Check the error log above.
    echo     Tip: Ensure your device has USB Debugging enabled and 'Install via USB' permitted.
    pause
    exit /b %errorlevel%
)

:: 5. Launch the App
echo.
echo [+] Installation successful!
echo [*] Launching AntiGravity on connected device...
adb shell am start -n com.antigravity.telemetry/.MainActivity

echo.
echo ========================================================
echo  AntiGravity is now running on your device!
echo  Telemetry Profile: Victoris CNG (42,850 km active)
echo ========================================================
echo.
pause
