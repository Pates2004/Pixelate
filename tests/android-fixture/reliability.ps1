param(
 [Parameter(Mandatory=$true)][string]$Serial,
 [ValidateSet('none','talkback','jieshuo')][string]$Reader='none',
 [string]$Sdk=$env:ANDROID_HOME,
 [int]$Cycles=10
)
$ErrorActionPreference='Stop'
if($Serial -notmatch '^emulator-[0-9]+$'){throw 'This destructive test workflow is restricted to emulators.'}
$adb=Join-Path $Sdk 'platform-tools\adb.exe'
$project=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
. (Join-Path $project 'project_materials.ps1')
$fixture=Join-ProjectMaterialPath $project 'Workspace\android-fixture-build'

function Invoke-Device {
 param([string[]]$Arguments)
 $saved=$ErrorActionPreference
 try {
  $ErrorActionPreference='Continue'
  $output=& $adb -s $Serial @Arguments 2>&1
  $code=$LASTEXITCODE
 } finally {$ErrorActionPreference=$saved}
 if($code -ne 0){throw ($output -join "`n")}
 return $output
}
function Invoke-Probe {
 param([string]$Mode)
 $output=Invoke-Device @('shell',"CLASSPATH=/data/local/tmp/pixelate-probe.dex app_process /system/bin ServiceProbe $Mode")
 Write-Output ($output -join "`n")
}
function Open-Fixture {
 param([string]$Mode)
 $null=Invoke-Device @('shell','input','keyevent','KEYCODE_HOME')
 $null=Invoke-Device @('shell','am','force-stop','pl.pixelate.testlauncher')
 $null=Invoke-Device @('shell','am','start','-n','pl.pixelate.testlauncher/.FixtureActivity','--es','mode',$Mode)
}

$originalServices=(Invoke-Device @('shell','settings','get','secure','enabled_accessibility_services') -join '').Trim()
$services='pl.pixelate/pl.pixelate.PixelateAccessibilityService'
if($Reader -eq 'talkback'){$services+=':com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService'}
if($Reader -eq 'jieshuo'){$services+=':com.nirenr.talkman/com.nirenr.talkman.TalkManAccessibilityService'}
try {
 $null=Invoke-Device @('push',"$fixture\dex\classes.dex",'/data/local/tmp/pixelate-probe.dex')
 $null=Invoke-Device @('shell','settings','put','secure','enabled_accessibility_services',$services)
 $deadline=(Get-Date).AddSeconds(8)
 do {
  Start-Sleep -Milliseconds 250
  $state=Invoke-Device @('shell','dumpsys','accessibility')
  $bound=[regex]::Match(($state -join "`n"),'(?s)Bound services:.*?Enabled services:').Value
  if(-not $bound){$bound=[regex]::Match(($state -join "`n"),'(?s)services:\{.*?\}\]').Value}
  $ready=$bound -match 'label=(Pixelate|pl\.pixelate\.)' -and ($Reader -eq 'none' -or
      ($Reader -eq 'talkback' -and $bound -match 'label=TalkBack') -or
      ($Reader -eq 'jieshuo' -and $bound -match 'label=Jieshuo'))
 } while(-not $ready -and (Get-Date) -lt $deadline)
 if(-not $ready){throw 'Requested accessibility services did not bind'}
 Write-Output 'PASS requested services bound'
 Write-Output "READER=$Reader"
 Open-Fixture delayed
 Start-Sleep -Seconds 3
 Invoke-Probe await
 Write-Output 'PASS delayed tree recovered without opening Pixelate'
 Open-Fixture interrupt
 Start-Sleep -Seconds 3
 Invoke-Probe await
 Write-Output 'PASS feedback interruption'
 foreach($mode in @('success','reject','noop','unsupported','legacy-title')){
  Open-Fixture $mode
  Start-Sleep -Milliseconds 600
  Invoke-Probe clear
  Write-Output "PASS clear mode=$mode"
 }
 for($i=0;$i -lt $Cycles;$i++){
  $null=Invoke-Device @('shell','input','keyevent','KEYCODE_HOME')
  $null=Invoke-Device @('shell','am','start','-W','-a','android.settings.SETTINGS')
  Start-Sleep -Milliseconds 700
  $null=Invoke-Device @('shell','input','keyevent','KEYCODE_APP_SWITCH')
  Invoke-Probe recents
  Invoke-Probe await
  $null=Invoke-Device @('shell','input','keyevent','KEYCODE_BACK')
 }
 Write-Output "PASS recents cycles=$Cycles"
 $null=Invoke-Device @('shell','input','keyevent','KEYCODE_HOME')
 Start-Sleep -Milliseconds 700
 Invoke-Probe empty
 Write-Output 'PASS no overlay on Home'
}catch{
 $failureRoot=Join-ProjectMaterialPath $project 'Workspace\android-fixture-failures'
 $failureRoot=Join-Path $failureRoot (Get-Date -Format 'yyyyMMdd-HHmmss')
 New-Item -ItemType Directory -Force -Path $failureRoot | Out-Null
 Invoke-Device @('shell','dumpsys','accessibility') | Out-File -LiteralPath (Join-Path $failureRoot 'accessibility.txt') -Encoding utf8
 Invoke-Device @('shell','dumpsys','activity','activities') | Out-File -LiteralPath (Join-Path $failureRoot 'activities.txt') -Encoding utf8
 Invoke-Device @('shell','dumpsys','window') | Out-File -LiteralPath (Join-Path $failureRoot 'window.txt') -Encoding utf8
 Write-Output "Failure evidence: $failureRoot"
 throw
}finally{
 $null=Invoke-Device @('shell','input','keyevent','KEYCODE_HOME')
 $null=Invoke-Device @('shell','settings','put','secure','enabled_accessibility_services',$originalServices)
}
