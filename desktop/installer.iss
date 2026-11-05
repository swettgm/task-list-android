#define AppVersion "0.65.1"

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
Source: "..\dist\Tasks-PC.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "USING-TASKS-PC.txt"; DestDir: "{app}"; DestName: "START-HERE.txt"; Flags: ignoreversion

[Icons]
Name: "{group}\Tasks"; Filename: "{app}\Tasks-PC.exe"; WorkingDir: "{app}"
Name: "{autodesktop}\Tasks"; Filename: "{app}\Tasks-PC.exe"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "{app}\Tasks-PC.exe"; Description: "Open Tasks"; Flags: nowait postinstall skipifsilent

[UninstallRun]
Filename: "{app}\Tasks-PC.exe"; Parameters: "--shutdown"; Flags: runhidden waituntilterminated; RunOnceId: "StopTasks"; Check: FileExists(ExpandConstant('{app}\Tasks-PC.exe'))

[Code]
function PrepareToInstall(var NeedsRestart: Boolean): String;
var
  ExitCode: Integer;
begin
  Result := '';
  { Use the incoming executable so a portable or older installed copy can stop too. }
  ExtractTemporaryFile('Tasks-PC.exe');
  if not Exec(ExpandConstant('{tmp}\Tasks-PC.exe'), '--shutdown', '', SW_HIDE,
              ewWaitUntilTerminated, ExitCode) then
    Result := 'Tasks could not close its running companion. Close Tasks and try again.'
  else if ExitCode <> 0 then
    Result := 'Tasks could not close its running companion. Close Tasks and try again.';
end;
