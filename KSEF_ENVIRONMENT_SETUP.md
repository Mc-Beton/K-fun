# KSeF Hub - Konfiguracja środowisk i certyfikatów

## 📋 Spis treści

1. [Środowiska KSeF](#środowiska-ksef)
2. [Konfiguracja DEMO (bez certyfikatu)](#konfiguracja-demo)
3. [Konfiguracja TEST (wymaga rejestracji)](#konfiguracja-test)
4. [Konfiguracja PROD (produkcja)](#konfiguracja-prod)
5. [Certyfikat kwalifikowany](#certyfikat-kwalifikowany)
6. [Zmienne środowiskowe](#zmienne-środowiskowe)
7. [Troubleshooting](#troubleshooting)

---

## Środowiska KSeF

KSeF (Krajowy System e-Faktur) udostępnia 3 środowiska:

| Środowisko | URL                           | Wymaga rejestracji | Certyfikat             | Przeznaczenie           |
| ---------- | ----------------------------- | ------------------ | ---------------------- | ----------------------- |
| **DEMO**   | `https://ksef-demo.mf.gov.pl` | ❌ Nie             | ❌ Opcjonalny (test)   | Testy integracji, nauka |
| **TEST**   | `https://ksef-test.mf.gov.pl` | ✅ Tak             | ✅ Tak (testowy)       | Testy przed wdrożeniem  |
| **PROD**   | `https://ksef.mf.gov.pl`      | ✅ Tak             | ✅ Tak (kwalifikowany) | **Prawdziwe faktury!**  |

⚠️ **UWAGA:** Środowisko PROD wysyła faktury do Ministerstwa Finansów! Używaj tylko dla prawdziwych dokumentów.

---

## Konfiguracja DEMO

### Charakterystyka środowiska DEMO:

- ✅ Publicznie dostępne, bez rejestracji
- ✅ Brak ograniczeń na liczbę requestów
- ✅ Dane są mockowane (niezależne od prawdziwej bazy MF)
- ❌ Certyfikat kwalifikowany **nie jest wymagany**
- ❌ Wysłane faktury **nie trafiają** do MF

### Kroki konfiguracji:

#### 1. Wybierz profil Spring Boot: `dev`

Profil `dev` jest już skonfigurowany dla DEMO w pliku `application-dev.yml`:

```yaml
ksef:
  api:
    base-url: https://ksef-demo.mf.gov.pl/api
    environment: DEMO
```

#### 2. Uruchom aplikację z profilem `dev`:

**Windows PowerShell:**

```powershell
$env:SPRING_PROFILES_ACTIVE='dev'
mvn spring-boot:run
```

**Linux/macOS:**

```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

#### 3. Certyfikat (opcjonalny)

Dla DEMO możesz **pominąć** certyfikat lub użyć samopodpisanego do testów podpisywania XML:

```yaml
ksef:
  signature:
    enabled: false # Zostaw wyłączone dla DEMO
```

Jeśli chcesz testować podpisywanie, wygeneruj testowy certyfikat:

**PowerShell:**

```powershell
$cert = New-SelfSignedCertificate -Subject "CN=Test DEMO, O=Test Company, C=PL" `
    -CertStoreLocation "Cert:\CurrentUser\My" `
    -KeyExportPolicy Exportable -KeySpec Signature -KeyLength 2048 `
    -NotAfter (Get-Date).AddYears(2)

$password = ConvertTo-SecureString -String "demo123" -Force -AsPlainText
Export-PfxCertificate -Cert $cert -FilePath "ksef-demo.p12" -Password $password
```

Następnie ustaw zmienne:

```powershell
$env:KSEF_KEYSTORE_PATH='ksef-demo.p12'
$env:KSEF_KEYSTORE_PASSWORD='demo123'
$env:KSEF_KEY_ALIAS='1'  # Domyślny alias dla samopodpisanego
```

#### 4. Testowanie

Dashboard:

```
http://localhost:3000
```

API:

```
http://localhost:8080/api/swagger-ui/index.html
```

Status KSeF:

```
GET http://localhost:8080/api/status
```

---

## Konfiguracja TEST

### Charakterystyka środowiska TEST:

- ✅ Wymaga rejestracji w Ministerstwie Finansów
- ✅ Wymaga testowego certyfikatu kwalifikowanego
- ✅ Faktury są zapisywane w systemie testowym MF
- ⚠️ Dane są **prawdziwe** ale oznaczone jako testowe
- ⚠️ Regularnie czyszczone przez MF

### Kroki konfiguracji:

#### 1. Rejestracja w KSeF TEST

1. Przejdź do portalu: https://ksef-test.mf.gov.pl
2. Zarejestruj firmę/podmiot do testów
3. Uzyskaj dostęp do panelu testowego

#### 2. Certyfikat testowy

**Opcja A: Certyfikat testowy od CA**

- Certum wydaje bezpłatne certyfikaty testowe
- Mają pełną strukturę certyfikatu kwalifikowanego
- Są ważne tylko w środowisku TEST

**Opcja B: Samopodpisany certyfikat (może nie działać)**

- Niektóre środowiska TEST akceptują samopodpisane
- Sprawdź dokumentację MF

#### 3. Konfiguracja application.yml

Utwórz plik `application-test.yml` (lub edytuj istniejący):

```yaml
spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

ksef:
  api:
    base-url: https://ksef-test.mf.gov.pl/api
    environment: TEST
    timeout: 30000

  signature:
    enabled: true # WŁĄCZ podpisywanie dla TEST

logging:
  level:
    root: INFO
    pl.ksef.hub: DEBUG
```

#### 4. Ustaw zmienne środowiskowe:

```powershell
$env:SPRING_PROFILES_ACTIVE='test'
$env:KSEF_KEYSTORE_PATH='D:\certificates\ksef-test-cert.p12'
$env:KSEF_KEYSTORE_PASSWORD='haslo_do_certyfikatu'
$env:KSEF_KEY_ALIAS='certum_test_alias'
$env:KSEF_KEY_PASSWORD='haslo_do_klucza'
```

#### 5. Uruchom aplikację:

```powershell
mvn spring-boot:run
```

---

## Konfiguracja PROD

⚠️ **UWAGA: ŚRODOWISKO PRODUKCYJNE - PRAWDZIWE FAKTURY DO MF!**

### Charakterystyka środowiska PROD:

- ✅ Oficjalne środowisko Ministerstwa Finansów
- ✅ **WYMAGA** certyfikatu kwalifikowanego
- ✅ Faktury są **prawdziwe** i trafiają do systemu MF
- ✅ Pełna integracja z systemami MF
- ⚠️ **Błędnie wysłane faktury mogą mieć konsekwencje prawne!**

### Kroki konfiguracji:

#### 1. Zdobądź certyfikat kwalifikowany

**Wymagania:**

- Certyfikat zgodny z eIDAS (Rozporządzenie UE)
- Wydany przez zaufane CA w Polsce:
  - **Certum** (Asseco Data Systems) - https://www.certum.pl
  - **Szafir** (Krajowa Izba Rozliczeniowa) - https://www.elektronicznypodpis.pl
  - **mSignature** (mBank) - https://www.mbank.pl
  - **Sigillum** (PWPW) - https://sigillum.pl

**Koszt:** 100-400 PLN/rok (zależnie od dostawcy)

**Format:** PKCS#12 (.p12 lub .pfx)

**Zawartość certyfikatu musi zawierać:**

- Klucz prywatny (do podpisywania)
- Certyfikat publiczny
- Łańcuch certyfikatów CA

#### 2. Przygotowanie certyfikatu

Po otrzymaniu certyfikatu od CA:

1. **Sprawdź poprawność certyfikatu:**

**PowerShell:**

```powershell
# Wyświetl szczegóły certyfikatu
$cert = Get-PfxCertificate -FilePath "twoj-certyfikat.p12"
$cert | Format-List Subject, Issuer, NotBefore, NotAfter, Thumbprint
```

**Linux:**

```bash
# Wyświetl szczegóły
openssl pkcs12 -in twoj-certyfikat.p12 -nokeys -info
```

2. **Znajdź alias certyfikatu:**

```bash
keytool -list -v -keystore twoj-certyfikat.p12 -storetype PKCS12
```

Szukaj linii: `Alias name: xxxxxx`

#### 3. Umieść certyfikat w bezpiecznej lokalizacji

**Windows:**

```
D:\secure\certificates\prod\ksef-production.p12
```

**Linux:**

```
/opt/ksef-hub/certificates/ksef-production.p12
```

**Zabezpiecz plik:**

```powershell
# Windows - tylko administrator
icacls "D:\secure\certificates\prod\ksef-production.p12" /inheritance:r /grant:r "Administrators:F"
```

```bash
# Linux - tylko właściciel
chmod 600 /opt/ksef-hub/certificates/ksef-production.p12
chown ksef-app:ksef-app /opt/ksef-hub/certificates/ksef-production.p12
```

#### 4. Profil produkcyjny

Plik `application-prod.yml` już istnieje:

```yaml
spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

ksef:
  api:
    base-url: https://ksef.mf.gov.pl/api
    environment: PROD
    timeout: 30000

  signature:
    enabled: true # MUSI być włączone dla PROD!

logging:
  level:
    root: WARN
    pl.ksef.hub: INFO
```

#### 5. Zmienne środowiskowe dla PROD

**NIE przechowuj haseł w kodzie!** Użyj zmiennych środowiskowych:

**Windows (PowerShell):**

```powershell
# Ustawienia jednorazowe (sesja)
$env:SPRING_PROFILES_ACTIVE='prod'
$env:KSEF_KEYSTORE_PATH='D:\secure\certificates\prod\ksef-production.p12'
$env:KSEF_KEYSTORE_PASSWORD='TWOJE_HASLO_KEYSTORE'
$env:KSEF_KEY_ALIAS='certum_production_2024'
$env:KSEF_KEY_PASSWORD='TWOJE_HASLO_KLUCZA'
$env:JWT_SECRET='generuj-losowy-ciag-256-bitow-minimum'
```

**Windows (systemowe - persystentne):**

```powershell
# Dodaj do zmiennych systemowych (Panel Sterowania > System > Zmienne środowiskowe)
# LUB użyj PowerShell jako Administrator:
[System.Environment]::SetEnvironmentVariable('KSEF_KEYSTORE_PATH', 'D:\secure\certificates\prod\ksef-production.p12', 'Machine')
[System.Environment]::SetEnvironmentVariable('KSEF_KEY_ALIAS', 'certum_production_2024', 'Machine')
# UWAGA: Nie zapisuj haseł jako zmienne systemowe! Użyj Azure Key Vault / HashiCorp Vault
```

**Linux (.env file dla systemd service):**

```bash
# /etc/ksef-hub/.env
SPRING_PROFILES_ACTIVE=prod
KSEF_KEYSTORE_PATH=/opt/ksef-hub/certificates/ksef-production.p12
KSEF_KEYSTORE_PASSWORD=TWOJE_HASLO_KEYSTORE
KSEF_KEY_ALIAS=certum_production_2024
KSEF_KEY_PASSWORD=TWOJE_HASLO_KLUCZA
JWT_SECRET=generuj-losowy-ciag-256-bitow-minimum
```

Zabezpiecz plik `.env`:

```bash
chmod 600 /etc/ksef-hub/.env
chown ksef-app:ksef-app /etc/ksef-hub/.env
```

#### 6. Baza danych produkcyjna

Upewnij się że używasz PostgreSQL (nie H2!):

**application-prod.yml:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://prod-db-server:5432/ksef_hub_prod
    username: ksef_prod_user
    password: ${DB_PASSWORD} # Ze zmiennej środowiskowej!
```

#### 7. Uruchom aplikację:

```powershell
# Windows
mvn spring-boot:run

# Lub JAR
java -jar target/ksef-hub-1.0.0-SNAPSHOT.jar
```

#### 8. Weryfikacja produkcyjna

**PRZED wysłaniem pierwszej prawdziwej faktury:**

1. Sprawdź status połączenia:

```
GET http://twoja-domena:8080/api/status
```

Oczekiwany wynik:

```json
{
  "online": true,
  "ksefConnected": true, // ✅ MUSI być true!
  "receivedMessagesCount": 0,
  "sentToKsefCount": 0
}
```

2. Przetestuj sesję KSeF:

```
POST http://twoja-domena:8080/api/tenants/{tenantId}/ksef/session/open
```

3. Wyślij **fakturę testową** z małą kwotą i sprawdź czy została przyjęta

4. Pobierz UPO (Urzędowe Poświadczenie Odbioru) i zweryfikuj

---

## Certyfikat kwalifikowany

### Gdzie kupić certyfikat?

| Dostawca       | URL                                | Cena/rok | Uwagi                  |
| -------------- | ---------------------------------- | -------- | ---------------------- |
| **Certum**     | https://www.certum.pl              | ~200 PLN | Najpopularniejszy w PL |
| **Szafir**     | https://www.elektronicznypodpis.pl | ~150 PLN | KIR, dobre wsparcie    |
| **mSignature** | https://www.mbank.pl               | ~250 PLN | Dla klientów mBanku    |
| **Sigillum**   | https://sigillum.pl                | ~300 PLN | PWPW, wysoka jakość    |

### Co sprawdzić przed zakupem?

✅ **Certyfikat musi być typu:**

- "Certyfikat kwalifikowany" (zgodny z eIDAS)
- Typ: Osoby fizycznej prowadzącej działalność lub Podmiotu prawnego
- Format: PKCS#12 (.p12 / .pfx)

✅ **Wymagane pola w certyfikacie:**

```
Subject: CN=Jan Kowalski, O=Twoja Firma Sp. z o.o., C=PL
KeyUsage: digitalSignature, nonRepudiation
ExtendedKeyUsage: emailProtection, codeSigning (opcjonalnie)
```

### Struktura pliku .p12

Plik PKCS#12 zawiera:

```
twoj-certyfikat.p12
├── Klucz prywatny (chroniony hasłem)
├── Certyfikat publiczny (twój)
├── Certyfikat pośredni CA
└── Certyfikat główny CA
```

### Jak uzyskać alias certyfikatu?

**Windows (PowerShell):**

```powershell
# Zainstaluj Java keytool (JDK)
keytool -list -v -keystore "twoj-certyfikat.p12" -storetype PKCS12

# Wpisz hasło keystore
# Szukaj: "Alias name: xxxxxx"
```

**Przykładowy output:**

```
Keystore type: PKCS12
Keystore provider: SUN

Your keystore contains 1 entry

Alias name: certum production jan kowalski 2024  ← TO JEST ALIAS!
Creation date: Jan 15, 2024
Entry type: PrivateKeyEntry
Certificate chain length: 3
```

Użyj tej wartości jako `KSEF_KEY_ALIAS`

---

## Zmienne środowiskowe

### Pełna lista zmiennych:

| Zmienna                  | Przykład                | Wymagana       | Opis                     |
| ------------------------ | ----------------------- | -------------- | ------------------------ |
| `SPRING_PROFILES_ACTIVE` | `prod`                  | ✅             | Profil: dev, test, prod  |
| `KSEF_KEYSTORE_PATH`     | `/path/cert.p12`        | ✅ (TEST/PROD) | Ścieżka do certyfikatu   |
| `KSEF_KEYSTORE_PASSWORD` | `SecurePass123!`        | ✅ (TEST/PROD) | Hasło do keystore        |
| `KSEF_KEY_ALIAS`         | `certum_prod_2024`      | ✅ (TEST/PROD) | Alias certyfikatu        |
| `KSEF_KEY_PASSWORD`      | `KeyPass456!`           | ✅ (TEST/PROD) | Hasło klucza prywatnego  |
| `JWT_SECRET`             | `random-256-bit-string` | ✅             | Klucz JWT (min 256 bit)  |
| `DB_PASSWORD`            | `postgres_password`     | ✅             | Hasło do bazy PostgreSQL |

### Bezpieczne zarządzanie hasłami:

**❌ NIE rób tego:**

```yaml
# application.yml - NIE!
ksef:
  signature:
    keystore:
      password: "moje-haslo-w-kodzie" # ❌ NIGDY!
```

**✅ Zrób to:**

**Opcja 1: Zmienne środowiskowe (proste)**

```bash
export KSEF_KEYSTORE_PASSWORD='...'
```

**Opcja 2: HashiCorp Vault (zaawansowane)**

```bash
vault kv get secret/ksef-hub/prod/keystore-password
```

**Opcja 3: Azure Key Vault (Azure)**

```bash
az keyvault secret show --vault-name ksef-hub-vault --name keystore-password
```

**Opcja 4: AWS Secrets Manager (AWS)**

```bash
aws secretsmanager get-secret-value --secret-id ksef/prod/keystore-password
```

---

## Troubleshooting

### Problem: "ksefConnected: false" w dashboardzie

**Przyczyny:**

1. Serwer KSeF jest niedostępny (konserwacja)
2. Błędny URL w konfiguracji
3. Problem z firewallem/proxy
4. Aplikacja nie ma dostępu do internetu

**Rozwiązanie:**

```powershell
# Sprawdź dostęp do KSeF
curl https://ksef-demo.mf.gov.pl/api/common/Status

# Sprawdź logi aplikacji
# Szukaj: "KSeF API is not available"
```

### Problem: "Failed to load keystore"

**Przyczyny:**

1. Błędna ścieżka do pliku .p12
2. Błędne hasło keystore
3. Nieprawidłowy format pliku

**Rozwiązanie:**

```powershell
# Sprawdź czy plik istnieje
Test-Path "D:\certificates\cert.p12"

# Sprawdź certyfikat
keytool -list -v -keystore cert.p12 -storetype PKCS12
```

### Problem: "Certificate has expired"

**Przyczyny:**

- Certyfikat kwalifikowany wygasł
- Certyfikat nie jest jeszcze ważny (NotBefore)

**Rozwiązanie:**

```powershell
# Sprawdź daty ważności
$cert = Get-PfxCertificate -FilePath cert.p12
$cert.NotBefore
$cert.NotAfter

# Kup nowy certyfikat przed wygaśnięciem starego!
```

### Problem: "Invalid signature"

**Przyczyny:**

1. Błędny alias certyfikatu
2. Błędne hasło klucza prywatnego
3. Certyfikat nie ma uprawnień do podpisywania

**Rozwiązanie:**

```bash
# Sprawdź KeyUsage
openssl pkcs12 -in cert.p12 -nokeys -info | grep -A 5 "Key Usage"

# Powinno zawierać: digitalSignature, nonRepudiation
```

### Problem: "403 Forbidden" z KSeF API

**Przyczyny:**

1. Brak rejestracji w środowisku TEST/PROD
2. Certyfikat nie jest zaufany przez MF
3. Błędny NIP w requestie

**Rozwiązanie:**

- Sprawdź czy firma jest zarejestrowana w KSeF
- Zweryfikuj certyfikat u dostawcy CA
- Sprawdź logi KSeF API

---

## Szybki start - Przełączanie środowisk

### DEMO → PROD

```powershell
# 1. Zatrzymaj aplikację
# 2. Kup certyfikat kwalifikowany
# 3. Ustaw zmienne:
$env:SPRING_PROFILES_ACTIVE='prod'
$env:KSEF_KEYSTORE_PATH='D:\secure\certificates\prod\ksef-production.p12'
$env:KSEF_KEYSTORE_PASSWORD='***'
$env:KSEF_KEY_ALIAS='certum_prod_2024'
$env:KSEF_KEY_PASSWORD='***'

# 4. Uruchom z profilem prod
mvn spring-boot:run
```

### PROD → DEMO (na czas testów)

```powershell
# 1. Zatrzymaj aplikację
# 2. Zmień profil:
$env:SPRING_PROFILES_ACTIVE='dev'

# 3. Uruchom
mvn spring-boot:run
```

---

## Podsumowanie

| Środowisko | Certyfikat       | Rejestracja | Profil | URL                 |
| ---------- | ---------------- | ----------- | ------ | ------------------- |
| **DEMO**   | ❌ Nie           | ❌ Nie      | `dev`  | ksef-demo.mf.gov.pl |
| **TEST**   | ⚠️ Testowy       | ✅ Tak      | `test` | ksef-test.mf.gov.pl |
| **PROD**   | ✅ Kwalifikowany | ✅ Tak      | `prod` | ksef.mf.gov.pl      |

**Zalecana ścieżka wdrożenia:**

1. Zacznij od **DEMO** - nauka, prototypy, testy integracji
2. Przejdź na **TEST** - testy przed wdrożeniem z prawdziwymi procesami
3. Wdróż na **PROD** - produkcja z certyfikatem kwalifikowanym

---

📞 **Wsparcie:**

- Dokumentacja KSeF: https://www.podatki.gov.pl/ksef
- API Docs: https://ksef-demo.mf.gov.pl/web/
- Helpdesk MF: helpdesk.ksef@mf.gov.pl
