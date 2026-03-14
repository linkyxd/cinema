# PowerShell: генерация цепочки сертификатов (Root -> Intermediate -> Server)
# $env:STUDENT_ID = "12345678"  # Номер студенческого билета
# $env:KEYSTORE_PASSWORD = "changeit"

# OpenSSL: ищем в PATH, Git, Chocolatey
if (-not (Get-Command openssl -ErrorAction SilentlyContinue)) {
    $paths = @(
        "${env:ProgramFiles}\Git\usr\bin",
        "${env:ProgramFiles(x86)}\Git\usr\bin",
        "C:\Program Files\OpenSSL-Win64\bin",
        "$env:ChocolateyInstall\bin"
    )
    foreach ($p in $paths) {
        if (Test-Path "$p\openssl.exe") {
            $env:PATH = "$p;$env:PATH"
            break
        }
    }
    if (-not (Get-Command openssl -ErrorAction SilentlyContinue)) {
        Write-Error "OpenSSL not found. Install Git for Windows or OpenSSL, or run this script from Git Bash."
        exit 1
    }
}

$StudentId = if ($env:STUDENT_ID) { $env:STUDENT_ID } else { "1bks22149" }
$OutDir = if ($env:OUT_DIR) { $env:OUT_DIR } else { ".\certs" }
$KeystorePass = if ($env:KEYSTORE_PASSWORD) { $env:KEYSTORE_PASSWORD } else { "changeit" }

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
Push-Location $OutDir

$ROOT_CN = "cinema-root-ca"
$INT_CN = "cinema-intermediate-ca"
$SERVER_CN = "cinema-server"
$DAYS = 365

Write-Host "Student ID: $StudentId"

# 1. Root CA
openssl genrsa -out "$ROOT_CN.key" 4096
openssl req -new -x509 -days $DAYS -key "$ROOT_CN.key" -out "$ROOT_CN.crt" `
  -subj "/CN=$ROOT_CN/O=Cinema Lab/OU=StudentID:$StudentId/C=RU"

# 2. Intermediate CA
openssl genrsa -out "$INT_CN.key" 4096
openssl req -new -key "$INT_CN.key" -out "$INT_CN.csr" `
  -subj "/CN=$INT_CN/O=Cinema Lab/OU=StudentID:$StudentId/C=RU"
openssl x509 -req -days $DAYS -in "$INT_CN.csr" -CA "$ROOT_CN.crt" -CAkey "$ROOT_CN.key" -CAcreateserial -out "$INT_CN.crt"

# 3. Server cert
openssl genrsa -out "$SERVER_CN.key" 2048
# Config only for subjectAltName (DN via -subj to avoid Cyrillic/encoding issues)
$config = @"
[req]
distinguished_name = dn
req_extensions = v3_req
prompt = no
[dn]
CN = cinema-server
O = Cinema Lab
OU = StudentID
C = RU
[v3_req]
subjectAltName = @alt_names
[alt_names]
DNS.1 = localhost
DNS.2 = *.localhost
IP.1 = 127.0.0.1
"@
$config | Out-File -FilePath "san.cnf" -Encoding ASCII
openssl req -new -key "$SERVER_CN.key" -out "$SERVER_CN.csr" -config san.cnf
openssl x509 -req -days $DAYS -in "$SERVER_CN.csr" -CA "$INT_CN.crt" -CAkey "$INT_CN.key" -CAcreateserial -out "$SERVER_CN.crt" -extfile san.cnf -extensions v3_req

# Keystore
openssl pkcs12 -export -in "$SERVER_CN.crt" -inkey "$SERVER_CN.key" -certfile "$INT_CN.crt" -CAfile "$ROOT_CN.crt" -caname root -out cinema-keystore.p12 -passout "pass:$KeystorePass" -name cinema-server

# Trust chain
Get-Content "$ROOT_CN.crt", "$INT_CN.crt" | Set-Content cinema-trustchain.crt

Pop-Location
Write-Host "Done. Keystore: $OutDir\cinema-keystore.p12"
