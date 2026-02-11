# Generate RSA key pair for Remember-Me feature
$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Security

# Generate 2048-bit RSA key pair
$rsa = [System.Security.Cryptography.RSA]::Create(2048)

# Export private key
$privateKeyBytes = $rsa.ExportRSAPrivateKey()
$privateKeyBase64 = [Convert]::ToBase64String($privateKeyBytes, [System.Base64FormattingOptions]::InsertLineBreaks)
$privateKeyPem = "-----BEGIN PRIVATE KEY-----`n$privateKeyBase64`n-----END PRIVATE KEY-----"

# Export public key
$publicKeyBytes = $rsa.ExportSubjectPublicKeyInfo()
$publicKeyBase64 = [Convert]::ToBase64String($publicKeyBytes, [System.Base64FormattingOptions]::InsertLineBreaks)
$publicKeyPem = "-----BEGIN PUBLIC KEY-----`n$publicKeyBase64`n-----END PUBLIC KEY-----"

# Save to files
$keysPath = "src/main/resources/keys"
$privateKeyPem | Out-File -FilePath "$keysPath/remember_private_key.pem" -Encoding ASCII -NoNewline
$publicKeyPem | Out-File -FilePath "$keysPath/remember_public_key.pem" -Encoding ASCII -NoNewline

Write-Host "Keys generated successfully!" -ForegroundColor Green
Write-Host "Location: $keysPath" -ForegroundColor Cyan
Write-Host "remember_private_key.pem" -ForegroundColor Yellow
Write-Host "remember_public_key.pem" -ForegroundColor Yellow
