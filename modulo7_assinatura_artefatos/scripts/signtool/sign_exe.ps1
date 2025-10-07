# scripts/signtool/sign_exe.ps1
param(
  [Parameter(Mandatory=$true)][string]$ExePath,
  [Parameter(Mandatory=$true)][string]$PfxPath,
  [Parameter(Mandatory=$true)][string]$PfxPass,
  [Parameter(Mandatory=$true)][string]$TimestampUrl
)
signtool sign /f $PfxPath /p $PfxPass /fd SHA256 /tr $TimestampUrl /td SHA256 $ExePath
signtool verify /pa /v $ExePath