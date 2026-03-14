# TLS и сертификаты

## 1. Генерация цепочки сертификатов

Цепочка: **Root CA** → **Intermediate CA** → **Server certificate** (3 звена).

### Укажи номер студенческого билета

Перед генерацией задай переменную `STUDENT_ID`:

**Windows (PowerShell):**
```powershell
$env:STUDENT_ID = "12345678"
$env:KEYSTORE_PASSWORD = "changeit"
.\scripts\generate-certificates.ps1
```

**Linux / Git Bash:**
```bash
export STUDENT_ID=12345678
export KEYSTORE_PASSWORD=changeit
chmod +x scripts/generate-certificates.sh
./scripts/generate-certificates.sh
```

По умолчанию файлы создаются в `./certs/`:
- `cinema-keystore.p12` — keystore для Spring Boot
- `cinema-trustchain.crt` — Root + Intermediate (для добавления в доверенные)

---

## 2. Запуск с TLS

```powershell
$env:KEYSTORE_PATH = "C:\javap\cinema\certs\cinema-keystore.p12"
$env:KEYSTORE_PASSWORD = "changeit"
mvn spring-boot:run -Dspring-boot.run.profiles=tls
```

Приложение будет доступно по **https://localhost:8443** (если порт не переопределён).

---

## 3. Добавление в доверенные (браузер)

### Windows
1. Дважды кликни по `cinema-trustchain.crt`
2. «Установить сертификат» → «Локальный компьютер»
3. «Поместить все сертификаты в следующее хранилище» → «Доверенные корневые центры сертификации»

### macOS
```bash
sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain certs/cinema-trustchain.crt
```

### Linux (Chrome/Firefox)
Импорт `cinema-trustchain.crt` в настройки браузера (сертификаты).

---

## 4. CI и секреты

Keystore и пароль **не хранить в репозитории**.

### GitHub Secrets
- `KEYSTORE_BASE64` — keystore, закодированный в Base64  
  `[System.Convert]::ToBase64String([IO.File]::ReadAllBytes("certs/cinema-keystore.p12"))`
- `KEYSTORE_PASSWORD` — пароль keystore

При деплое: декодировать keystore из secret и задать `KEYSTORE_PATH` и `KEYSTORE_PASSWORD` перед запуском приложения.

### GitLab Variables
- `KEYSTORE_BASE64` (Masked)
- `KEYSTORE_PASSWORD` (Masked)
