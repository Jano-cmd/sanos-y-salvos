$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$sourceFile = Join-Path $repoRoot 'serverless-notification-function\lambda_function.py'
$outputZip = Join-Path $repoRoot 'serverless-notification-function\sanos-report-processor.zip'
$tempRoot = Join-Path $repoRoot 'serverless-notification-function\.package-tmp'

if (-not (Test-Path $sourceFile)) {
    throw "Lambda source file was not found at $sourceFile"
}

if (Test-Path $tempRoot) {
    Remove-Item -Path $tempRoot -Recurse -Force
}

New-Item -ItemType Directory -Path $tempRoot | Out-Null
Copy-Item -Path $sourceFile -Destination (Join-Path $tempRoot 'lambda_function.py')

if (Test-Path $outputZip) {
    Remove-Item -Path $outputZip -Force
}

Compress-Archive -Path (Join-Path $tempRoot 'lambda_function.py') -DestinationPath $outputZip -Force

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zipFile = [System.IO.Compression.ZipFile]::OpenRead($outputZip)

try {
    $entryNames = $zipFile.Entries | ForEach-Object { $_.FullName }
    if ($entryNames -notcontains 'lambda_function.py') {
        throw 'The ZIP does not contain lambda_function.py at the root.'
    }

    if ($entryNames.Count -ne 1) {
        throw "The ZIP should contain only lambda_function.py at the root. Entries found: $($entryNames -join ', ')"
    }
}
finally {
    $zipFile.Dispose()
    if (Test-Path $tempRoot) {
        Remove-Item -Path $tempRoot -Recurse -Force
    }
}

Write-Host "Lambda package created at: $outputZip"
