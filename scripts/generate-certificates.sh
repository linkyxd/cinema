#!/bin/bash
# Генерация цепочки сертификатов (Root CA -> Intermediate CA -> Server)
# Замени YOUR_STUDENT_ID на номер студенческого билета!
set -e

STUDENT_ID="${STUDENT_ID:-1bks22149}"
OUT_DIR="${OUT_DIR:-./certs}"
mkdir -p "$OUT_DIR"
cd "$OUT_DIR"

ROOT_CN="cinema-root-ca"
INT_CN="cinema-intermediate-ca"
SERVER_CN="cinema-server"
VALIDITY_DAYS=365

echo "Student ID in certs: $STUDENT_ID"
echo "Output dir: $OUT_DIR"

# 1. Root CA (самоподписанный)
openssl genrsa -out "${ROOT_CN}.key" 4096
openssl req -new -x509 -days $VALIDITY_DAYS -key "${ROOT_CN}.key" -out "${ROOT_CN}.crt" \
  -subj "/CN=${ROOT_CN}/O=Cinema Lab/OU=StudentID:${STUDENT_ID}/C=RU"

# 2. Intermediate CA (подписан Root)
openssl genrsa -out "${INT_CN}.key" 4096
openssl req -new -key "${INT_CN}.key" -out "${INT_CN}.csr" \
  -subj "/CN=${INT_CN}/O=Cinema Lab/OU=StudentID:${STUDENT_ID}/C=RU"
openssl x509 -req -days $VALIDITY_DAYS -in "${INT_CN}.csr" -CA "${ROOT_CN}.crt" -CAkey "${ROOT_CN}.key" \
  -CAcreateserial -out "${INT_CN}.crt"

# 3. Server cert (подписан Intermediate)
openssl genrsa -out "${SERVER_CN}.key" 2048
openssl req -new -key "${SERVER_CN}.key" -out "${SERVER_CN}.csr" \
  -subj "/CN=${SERVER_CN}/O=Cinema Lab/OU=StudentID:${STUDENT_ID}/C=RU" \
  -addext "subjectAltName=DNS:localhost,DNS:*.localhost,IP:127.0.0.1"
openssl x509 -req -days $VALIDITY_DAYS -in "${SERVER_CN}.csr" -CA "${INT_CN}.crt" -CAkey "${INT_CN}.key" \
  -CAcreateserial -out "${SERVER_CN}.crt"

# Keystore для Spring Boot (server cert + цепочка)
KEYSTORE_PASS="${KEYSTORE_PASSWORD:-changeit}"
openssl pkcs12 -export -in "${SERVER_CN}.crt" -inkey "${SERVER_CN}.key" \
  -certfile "${INT_CN}.crt" -CAfile "${ROOT_CN}.crt" -caname root \
  -out cinema-keystore.p12 -passout "pass:${KEYSTORE_PASS}" -name cinema-server

# Truststore (Root + Intermediate) — для добавления в доверенные
cat "${ROOT_CN}.crt" "${INT_CN}.crt" > cinema-trustchain.crt

echo "Done. Keystore: cinema-keystore.p12, trust chain: cinema-trustchain.crt"
