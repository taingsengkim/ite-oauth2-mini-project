param(
    [ValidatePattern('^\d{4}-\d{2}$')][string]$Release = (Get-Date).ToString('yyyy-MM'),
    [ValidatePattern('^(|[a-fA-F0-9]{40})$')][string]$ExpectedSha1 = ''
)
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$geoDirectory = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../data/geoip'))
New-Item -ItemType Directory -Path $geoDirectory -Force | Out-Null
$geoArchive = Join-Path $geoDirectory 'dbip-city-lite.mmdb.gz'
$geoTemporary = Join-Path $geoDirectory 'dbip-city-lite.mmdb.tmp'
$geoDatabase = Join-Path $geoDirectory 'dbip-city-lite.mmdb'
Write-Output "Downloading DB-IP City Lite $Release (CC BY 4.0; attribution is displayed in the app)."
Invoke-WebRequest -Uri "https://download.db-ip.com/free/dbip-city-lite-$Release.mmdb.gz" -OutFile $geoArchive -UseBasicParsing
$geoInput = [System.IO.File]::OpenRead($geoArchive)
try {
    $geoGzip = New-Object System.IO.Compression.GZipStream($geoInput, [System.IO.Compression.CompressionMode]::Decompress)
    try {
        $geoOutput = [System.IO.File]::Create($geoTemporary)
        try { $geoGzip.CopyTo($geoOutput) } finally { $geoOutput.Dispose() }
    } finally { $geoGzip.Dispose() }
} finally { $geoInput.Dispose() }
if ($ExpectedSha1 -and (Get-FileHash -LiteralPath $geoTemporary -Algorithm SHA1).Hash -ne $ExpectedSha1) {
    throw 'Database checksum mismatch. The existing database was not replaced.'
}
Move-Item -LiteralPath $geoTemporary -Destination $geoDatabase -Force
Remove-Item -LiteralPath $geoArchive
Write-Output "Installed $geoDatabase. Restart IAM to load the database."
