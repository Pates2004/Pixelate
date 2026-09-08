param([string]$Sdk=$env:ANDROID_HOME,[string]$Java=$env:JAVA_HOME)
$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$output=Join-Path $project 'trash\android-fixture-build'
New-Item -ItemType Directory -Force -Path $output,(Join-Path $output 'classes'),(Join-Path $output 'dex') | Out-Null
$tools=Join-Path $Sdk 'build-tools\35.0.0'
$android=Join-Path $Sdk 'platforms\android-35\android.jar'
& "$tools\aapt2.exe" compile --dir "$PSScriptRoot\res" -o "$output\resources.zip"
if($LASTEXITCODE -ne 0) {throw 'Resource compilation failed'}
& "$tools\aapt2.exe" link -I $android --manifest "$PSScriptRoot\AndroidManifest.xml" --java $output -o "$output\unsigned.apk" "$output\resources.zip"
if($LASTEXITCODE -ne 0) {throw 'Resource linking failed'}
& "$Java\bin\javac.exe" -cp $android -d "$output\classes" "$PSScriptRoot\FixtureActivity.java" "$output\pl\pixelate\testlauncher\R.java"
if($LASTEXITCODE -ne 0) {throw 'Java compilation failed'}
$classes=@(Get-ChildItem "$output\classes" -Recurse -Filter '*.class' | ForEach-Object {$_.FullName})
& "$tools\d8.bat" --lib $android --output "$output\dex" @classes
if($LASTEXITCODE -ne 0) {throw 'DEX compilation failed'}
& "$Java\bin\jar.exe" uf "$output\unsigned.apk" -C "$output\dex" classes.dex
& "$tools\zipalign.exe" -f 4 "$output\unsigned.apk" "$output\aligned.apk"
if(-not(Test-Path "$output\fixture.jks")) {
 & "$Java\bin\keytool.exe" -genkeypair -keystore "$output\fixture.jks" -storepass android -keypass android -alias fixture -dname 'CN=Pixelate Test Fixture' -keyalg RSA -validity 3650 | Out-Null
}
& "$tools\apksigner.bat" sign --ks "$output\fixture.jks" --ks-pass pass:android --key-pass pass:android --out "$output\fixture.apk" "$output\aligned.apk"
if($LASTEXITCODE -ne 0) {throw 'Fixture signing failed'}
Write-Output "$output\fixture.apk"
