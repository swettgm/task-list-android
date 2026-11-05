#define AppVersion "0.65.2"

[Setup]
AppId={{D242BDF2-B929-4327-B6A9-ABF045127A35}
AppName=Tasks
AppVersion={#AppVersion}
AppPublisher=Tasks
AppSupportURL=https://github.com/swettgm/task-list-android
DefaultDirName={localappdata}\Programs\Tasks
DefaultGroupName=Tasks
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64
MinVersion=10.0
OutputDir=..\dist
OutputBaseFilename=Tasks-Setup-v{#AppVersion}
SetupIconFile=tasks.ico
UninstallDisplayName=Tasks
UninstallDisplayIcon={app}\Tasks-PC.exe
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
CloseApplications=yes
RestartApplications=no
SetupLogging=yes

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Shortcuts:"

[Files]
Source: "webview2-bootstrapper.exe"; Flags: dontcopy
Source: "..\dist\Tasks-PC.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "USING-TASKS-PC.txt"; DestDir: "{app}"; DestName: "START-HERE.txt"; Flags: ignoreversion

[Icons]
Name: "{group}\Tasks"; Filename: "{app}\Tasks-PC.exe"; WorkingDir: "{app}"; AppUserModelID: "Personal.Tasks.PC"
Name: "{autodesktop}\Tasks"; Filename: "{app}\Tasks-PC.exe"; WorkingDir: "{app}"; AppUserModelID: "Personal.Tasks.PC"; Tasks: desktopicon

[Run]
Filename: "{app}\Tasks-PC.exe"; Description: "Open Tasks"; Flags: nowait postinstall skipifsilent

[UninstallRun]
Filename: "{app}\Tasks-PC.exe"; Parameters: "--shutdown"; Flags: runhidden waituntilterminated; RunOnceId: "StopTasks"; Check: FileExists(ExpandConstant('{app}\Tasks-PC.exe'))

[Code]
function HasWebView2(): Boolean;
var
  Version: String;
begin
  Result := (RegQueryStringValue(HKCU, 'SOFTWARE\Microsoft\EdgeUpdate\Clients\{F3017226-FE2A-4295-8BDF-00C3A9A7E4C5}', 'pv', Version) and (Version <> '') and (Version <> '0.0.0.0'))
    or (RegQueryStringValue(HKLM32, 'SOFTWARE\Microsoft\EdgeUpdate\Clients\{F3017226-FE2A-4295-8BDF-00C3A9A7E4C5}', 'pv', Version) and (Version <> '') and (Version <> '0.0.0.0'));
end;

function PrepareToInstall(var NeedsRestart: Boolean): String;
var
  ExitCode: Integer;
begin
  Result := '';
  if not HasWebView2() then begin
    ExtractTemporaryFile('webview2-bootstrapper.exe');
    if not Exec(ExpandConstant('{tmp}\webview2-bootstrapper.exe'), '/silent /install', '', SW_HIDE,
                ewWaitUntilTerminated, ExitCode) then begin
      Result := 'Tasks could not install the required Microsoft WebView2 runtime. Connect to the internet and run Setup again.';
      Exit;
    end;
    if not HasWebView2() then begin
      Result := 'Tasks needs the Microsoft WebView2 runtime. Connect to the internet and run Setup again.';
      Exit;
    end;
  end;
  { Use the incoming executable so a portable or older installed copy can stop too. }
  ExtractTemporaryFile('Tasks-PC.exe');
  if not Exec(ExpandConstant('{tmp}\Tasks-PC.exe'), '--shutdown', '', SW_HIDE,
              ewWaitUntilTerminated, ExitCode) then
    Result := 'Tasks could not close its running companion. Close Tasks and try again.'
  else if ExitCode <> 0 then
    Result := 'Tasks could not close its running companion. Close Tasks and try again.';
end;
