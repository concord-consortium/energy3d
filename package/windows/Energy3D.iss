;This file will be executed next to the application bundle image
;I.e. current directory will contain folder Energy3D with application files
[Setup]
AppId={{energy3d}}
AppName=Energy3D
AppVersion=1.0
AppVerName=Energy3D 1.0
AppPublisher=Concord Consortium Inc.
AppComments=Energy3D
AppCopyright=Copyright (C) 2015
AppPublisherURL=http://energy.concord.org/energy3d/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={localappdata}\Energy3D
DisableStartupPrompt=Yes
DisableDirPage=Yes
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=Concord Consortium Inc.
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=Energy3D-1.0
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=Energy3D\Energy3D.ico
UninstallDisplayIcon={app}\Energy3D.ico
UninstallDisplayName=Energy3D
WizardImageStretch=No
WizardSmallImageFile=Energy3D-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64
ChangesAssociations=Yes


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "Energy3D\Energy3D.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "Energy3D\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\Energy3D"; Filename: "{app}\Energy3D.exe"; IconFilename: "{app}\Energy3D.ico"; Check: returnTrue()
Name: "{commondesktop}\Energy3D"; Filename: "{app}\Energy3D.exe";  IconFilename: "{app}\Energy3D.ico"; Check: returnTrue()

[Registry]
Root: HKCU; Subkey: "Software\Classes\.ng3"; ValueType: string; ValueName: ""; ValueData: "Energy3DConcordConsortium"; Flags: uninsdeletevalue
Root: HKCU; Subkey: "Software\Classes\Energy3DConcordConsortium"; ValueType: string; ValueName: ""; ValueData: "Energy3D File"; Flags: uninsdeletekey
Root: HKCU; Subkey: "Software\Classes\Energy3DConcordConsortium\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\Energy3D.exe,0"
Root: HKCU; Subkey: "Software\Classes\Energy3DConcordConsortium\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\Energy3D.exe"" ""%1"""

[Run]
Filename: "{app}\Energy3D.exe"; Description: "{cm:LaunchProgram,Energy3D}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\Energy3D.exe"; Parameters: "-install -svcName ""Energy3D"" -svcDesc ""Energy3D"" -mainExe ""Energy3D.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\Energy3D.exe "; Parameters: "-uninstall -svcName Energy3D -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
