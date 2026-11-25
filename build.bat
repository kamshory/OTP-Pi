@echo off
setlocal
cd /d "%~dp0"

set "OUTPUT_JAR=OTP-Pi.jar"

where javac >nul 2>nul
if errorlevel 1 (
	echo ERROR: javac was not found. Install a JDK and add it to PATH.
	exit /b 1
)

where jar >nul 2>nul
if errorlevel 1 (
	echo ERROR: jar was not found. Install a JDK and add it to PATH.
	exit /b 1
)

if not exist "jar\sqlite-jdbc-3.42.0.0.jar" (
	echo ERROR: SQLite JDBC dependency is missing from the jar directory.
	exit /b 1
)

for /f "tokens=2" %%V in ('javac -version 2^>^&1') do set "JAVAC_VERSION=%%V"
for /f "tokens=1 delims=." %%V in ("%JAVAC_VERSION%") do set "JAVA_MAJOR=%%V"

if exist "build\classes" rmdir /s /q "build\classes"
mkdir "build\classes"
if errorlevel 1 goto build_failed

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$sources = Get-ChildItem -Path 'src' -Recurse -Filter '*.java' | ForEach-Object { [char]34 + $_.FullName.Replace('\', '\\') + [char]34 }; [System.IO.File]::WriteAllLines('build\sources.txt', [string[]]$sources, [System.Text.Encoding]::ASCII)"
if errorlevel 1 goto build_failed

if "%JAVA_MAJOR%"=="1" (
	javac -source 1.8 -target 1.8 -encoding UTF-8 -cp "jar\*" -d "build\classes" @"build\sources.txt"
) else (
	javac --add-exports java.base/sun.net.www.protocol.http=ALL-UNNAMED -encoding UTF-8 -cp "jar\*" -d "build\classes" @"build\sources.txt"
)
if errorlevel 1 goto build_failed

copy /y "src\config.ini" "build\classes\config.ini" >nul
if errorlevel 1 goto build_failed
copy /y "src\log4j.properties" "build\classes\log4j.properties" >nul
if errorlevel 1 goto build_failed

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$jars = Get-ChildItem -Path 'jar' -Filter '*.jar' | Sort-Object Name; $lines = @('Manifest-Version: 1.0', 'Main-Class: com.planetbiru.App'); $line = 'Class-Path:'; foreach ($dependency in $jars) { $entry = ' jar/' + $dependency.Name; if ([System.Text.Encoding]::ASCII.GetByteCount($line + $entry) -gt 70) { $lines += ($line + ' '); $line = ' ' + $entry.TrimStart() } else { $line += $entry } }; $lines += $line; [System.IO.File]::WriteAllText('build\MANIFEST.MF', ($lines -join [Environment]::NewLine) + [Environment]::NewLine + [Environment]::NewLine, [System.Text.Encoding]::ASCII)"
if errorlevel 1 goto build_failed

jar cfm "%OUTPUT_JAR%" "build\MANIFEST.MF" -C "build\classes" .
if errorlevel 1 goto build_failed

echo Build complete: %OUTPUT_JAR%
exit /b 0

:build_failed
echo ERROR: Build failed.
exit /b 1