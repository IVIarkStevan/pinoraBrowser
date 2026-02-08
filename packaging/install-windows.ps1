# PowerShell installer for Pinora Browser (current user)
param()

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$Launcher = Join-Path $ScriptDir '..\pinora-browser.bat' | Resolve-Path -Relative
$StartMenu = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Pinora Browser"
if (-not (Test-Path $StartMenu)) { New-Item -ItemType Directory -Path $StartMenu | Out-Null }

$ShortcutPath = Join-Path $StartMenu 'Pinora Browser.lnk'
$WScriptShell = New-Object -ComObject WScript.Shell
$Shortcut = $WScriptShell.CreateShortcut($ShortcutPath)
$Shortcut.TargetPath = (Resolve-Path "$ScriptDir\..\pinora-browser.bat").Path
$Shortcut.WorkingDirectory = (Resolve-Path "$ScriptDir\..").Path
$Icon = Get-ChildItem -Path "$ScriptDir\..\src\main\resources\icons" -Include *.ico,*.png -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
if ($Icon) { $Shortcut.IconLocation = $Icon.FullName }
$Shortcut.Save()

Write-Output "Installed Start Menu shortcut at: $ShortcutPath"
Write-Output "You can also run pinora-browser.bat from the project directory or use the launcher."