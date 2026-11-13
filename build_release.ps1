param(
 [string]$Sdk=$env:ANDROID_HOME,
 [string]$Java=$env:JAVA_HOME,
 [string]$GradleExecutable=(Join-Path $PSScriptRoot 'gradlew.bat')
)
$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot 'project_materials.ps1')
if(-not(Test-Path -LiteralPath (Join-Path $Java 'bin\java.exe'))){throw 'A JDK 17 directory is required'}
if(-not(Test-Path -LiteralPath (Join-Path $Sdk 'platforms\android-35\android.jar'))){throw 'Android SDK platform 35 is required'}
if(-not(Test-Path -LiteralPath $GradleExecutable -PathType Leaf)){throw 'Gradle executable not found'}
$workspace=Join-ProjectMaterialPath $PSScriptRoot 'Workspace\build'
New-Item -ItemType Directory -Force -Path $workspace | Out-Null
$savedJava=$env:JAVA_HOME
$savedSdk=$env:ANDROID_HOME
$savedOutput=$env:PIXELATE_BUILD_ROOT
Push-Location $PSScriptRoot
try {
 $env:JAVA_HOME=$Java
 $env:ANDROID_HOME=$Sdk
 $env:PIXELATE_BUILD_ROOT=Join-Path $workspace 'outputs'
 $ErrorActionPreference='Continue'
 & $GradleExecutable --project-cache-dir (Join-Path $workspace 'gradle-cache') --init-script (Join-Path $PSScriptRoot 'gradle\central-build.gradle') testDebugUnitTest lintDebug assembleRelease
 $code=$LASTEXITCODE
 $ErrorActionPreference='Stop'
 if($code -ne 0){throw 'Build or validation failed'}
 Write-Output ('Unsigned APK (sign before distribution): '+(Join-Path $env:PIXELATE_BUILD_ROOT 'app\outputs\apk\release\app-release-unsigned.apk'))
}finally {
 Pop-Location
 $env:JAVA_HOME=$savedJava
 $env:ANDROID_HOME=$savedSdk
 $env:PIXELATE_BUILD_ROOT=$savedOutput
}
