# 📚 KSeF Hub API - Kompletny przewodnik dla klienta

## Wprowadzenie

KSeF Hub dostarcza pełne REST API do zarządzania fakturami i integracją z polskim systemem KSeF (Krajowy System e-Faktur). Wszystkie endpointy zwracają dane w **jednolitym formacie JSON**.

---

## 📋 Format odpowiedzi (wszystkie endpointy)

```json
{
  "success": true/false,
  "message": "Opis operacji",
  "data": { /* dane */ },
  "error": { /* szczegóły błędu (jeśli wystąpił) */ }
}
```

---

## 1️⃣ AUTENTYKACJA (`/api/auth/*`)

### 🔑 Logowanie

**Request:**

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@testcompany.pl",
  "password": "Admin123!"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresIn": 86400000
  }
}
```

**Co otrzymujesz:**

- JWT token ważny 24 godziny
- Token używany we wszystkich dalszych requestach jako nagłówek:
  ```
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```

### 🚪 Wylogowanie

```http
POST /api/auth/logout
Authorization: Bearer {token}
```

**Uwaga:** JWT jest stateless - wylogowanie obsługiwane po stronie klienta (usunięcie tokenu).

---

## 2️⃣ ZARZĄDZANIE FIRMAMI (`/api/tenants/*`)

### 📊 Lista wszystkich firm

```http
GET /api/tenants?page=0&size=20
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "nip": "1234567890",
        "name": "Test Company",
        "fullName": "Test Company Sp. z o.o.",
        "email": "contact@testcompany.pl",
        "phone": "+48123456789",
        "address": "ul. Testowa 1, 00-000 Warszawa",
        "status": "ACTIVE",
        "active": true,
        "createdAt": "2026-02-05T10:00:00",
        "updatedAt": "2026-02-05T10:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 🔍 Pobranie firmy po ID

```http
GET /api/tenants/{id}
Authorization: Bearer {token}
```

### 🔍 Pobranie firmy po NIP

```http
GET /api/tenants/nip/1234567890
Authorization: Bearer {token}
```

### ➕ Dodanie nowej firmy

```http
POST /api/tenants
Authorization: Bearer {token}
Content-Type: application/json

{
  "nip": "9876543210",
  "name": "Nova Firma",
  "fullName": "Nova Firma Sp. z o.o.",
  "email": "kontakt@novafirma.pl",
  "phone": "+48987654321",
  "address": "ul. Nowa 10, 01-000 Warszawa"
}
```

### ✏️ Aktualizacja firmy

```http
PUT /api/tenants/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Nova Firma Updated",
  "email": "new@novafirma.pl",
  "phone": "+48111222333"
}
```

### ✅ Aktywacja firmy

```http
POST /api/tenants/{id}/activate
Authorization: Bearer {token}
```

### ❌ Dezaktywacja firmy

```http
POST /api/tenants/{id}/deactivate
Authorization: Bearer {token}
```

### 🗑️ Usunięcie firmy

```http
DELETE /api/tenants/{id}
Authorization: Bearer {token}
```

---

## 3️⃣ FAKTURY (`/api/tenants/{tenantId}/invoices/*`)

### 📄 Lista faktur firmy

```http
GET /api/tenants/1/invoices?page=0&size=20
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "invoiceNumber": "FV/2026/01/001",
        "ksefNumber": "1234567890123456789012",
        "type": "FA_VAT",
        "status": "SENT",
        "invoiceDate": "2026-02-01",
        "saleDate": "2026-02-01",
        "sellerNip": "1234567890",
        "sellerName": "Test Company Sp. z o.o.",
        "buyerNip": "9876543210",
        "buyerName": "Klient ABC Sp. z o.o.",
        "netAmount": 1000.0,
        "vatAmount": 230.0,
        "grossAmount": 1230.0,
        "currency": "PLN",
        "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
        "sentToKsefAt": "2026-02-05T12:30:00",
        "createdAt": "2026-02-05T10:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**Statusy faktury:**

- `DRAFT` - szkic
- `PENDING` - oczekująca
- `SENT` - wysłana do KSeF
- `ACCEPTED` - zaakceptowana przez KSeF
- `REJECTED` - odrzucona
- `ERROR` - błąd

### 🔍 Wyszukiwanie faktur

```http
GET /api/tenants/1/invoices/search?status=SENT&invoiceNumber=FV/2026
Authorization: Bearer {token}
```

**Parametry:**

- `status` - status faktury (DRAFT, SENT, ACCEPTED, etc.)
- `invoiceNumber` - numer faktury (częściowe dopasowanie)

### 📅 Faktury z zakresu dat

```http
GET /api/tenants/1/invoices/date-range?startDate=2026-02-01&endDate=2026-02-28
Authorization: Bearer {token}
```

**Parametry:**

- `startDate` - data początkowa (format: yyyy-MM-dd)
- `endDate` - data końcowa (format: yyyy-MM-dd)

### 🔍 Pobranie faktury po ID

```http
GET /api/tenants/1/invoices/{id}
Authorization: Bearer {token}
```

### 🔍 Pobranie faktury po numerze KSeF

```http
GET /api/tenants/1/invoices/ksef/{ksefNumber}
Authorization: Bearer {token}
```

### ➕ Utworzenie nowej faktury

```http
POST /api/tenants/1/invoices
Authorization: Bearer {token}
Content-Type: application/json

{
  "invoiceNumber": "FV/2026/02/001",
  "invoiceDate": "2026-02-05",
  "saleDate": "2026-02-05",
  "sellerNip": "1234567890",
  "sellerName": "Test Company Sp. z o.o.",
  "buyerNip": "9876543210",
  "buyerName": "Klient XYZ Sp. z o.o.",
  "netAmount": 5000.00,
  "vatAmount": 1150.00,
  "grossAmount": 6150.00,
  "currency": "PLN"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Invoice created successfully",
  "data": {
    "id": 2,
    "invoiceNumber": "FV/2026/02/001",
    "status": "DRAFT",
    "netAmount": 5000.0,
    "vatAmount": 1150.0,
    "grossAmount": 6150.0,
    "createdAt": "2026-02-05T13:00:00"
  }
}
```

### ✏️ Aktualizacja faktury

```http
PUT /api/tenants/1/invoices/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "sellerName": "Updated Company Name",
  "netAmount": 5500.00,
  "vatAmount": 1265.00,
  "grossAmount": 6765.00
}
```

### 🗑️ Usunięcie faktury

```http
DELETE /api/tenants/1/invoices/{id}
Authorization: Bearer {token}
```

### 🎯 Pobranie kodu QR faktury

```http
GET /api/tenants/1/invoices/{id}/qrcode
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "data": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

**Co otrzymujesz:**

- QR kod w formacie Base64 PNG
- Gotowy do wyświetlenia w HTML: `<img src="data:image/png;base64,..." />`

---

## 4️⃣ INTEGRACJA KSEF (`/api/tenants/{tenantId}/ksef/*`) ⭐

### 🔓 Otwarcie sesji KSeF

```http
POST /api/tenants/1/ksef/session/open?sessionType=ONLINE&initialToken=abc123xyz
Authorization: Bearer {token}
```

**Parametry:**

- `sessionType` - typ sesji: `ONLINE` lub `BATCH`
- `initialToken` - token autoryzacyjny KSeF (wydany przez Ministerstwo Finansów)

**Response:**

```json
{
  "success": true,
  "message": "KSeF session opened successfully",
  "data": {
    "sessionId": 1,
    "referenceNumber": "20260205-SE-1234567890AB-CD",
    "status": "OPENED",
    "tokenExpiresAt": "2026-02-05T14:30:00"
  }
}
```

**Co otrzymujesz:**

- `sessionId` - identyfikator sesji w systemie KSeF Hub
- `referenceNumber` - numer referencyjny sesji w KSeF
- `status` - status sesji (OPENED, ACTIVE, CLOSED, ERROR, EXPIRED)
- `tokenExpiresAt` - kiedy token wygaśnie (trzeba otworzyć nową sesję)

### 📤 Wysłanie faktury do KSeF

```http
POST /api/tenants/1/ksef/invoices/{invoiceId}/send?sessionToken=abc123xyz
Authorization: Bearer {token}
```

**Co się dzieje:**

1. System automatycznie generuje XML FA_VAT z danych faktury
2. Oblicza hash SHA-256 z zawartości XML
3. Koduje XML do Base64
4. Wysyła do API KSeF
5. Zapisuje numer KSeF w bazie danych

**Response:**

```json
{
  "success": true,
  "message": "Invoice sent to KSeF successfully",
  "data": {
    "invoiceId": 2,
    "ksefNumber": "1234567890123456789022",
    "status": "SENT",
    "sentAt": "2026-02-05T13:45:00"
  }
}
```

**Co otrzymujesz:**

- `ksefNumber` - unikalny 22-cyfrowy numer faktury w systemie KSeF
- `status` - `SENT` (faktura wysłana pomyślnie)
- `sentAt` - timestamp wysłania

### 📥 Pobranie UPO (Urzędowe Poświadczenie Odbioru)

```http
GET /api/tenants/1/ksef/invoices/{invoiceId}/upo?sessionToken=abc123xyz
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "message": "UPO retrieved successfully",
  "data": "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPFVQT..."
}
```

**Co otrzymujesz:**

- UPO w formacie Base64 XML
- Oficjalne potwierdzenie przyjęcia faktury przez KSeF
- Można zdekodować i zapisać jako plik XML lub wydrukować

**Dekodowanie UPO:**

```javascript
const upoXml = atob(response.data); // dekoduj Base64
// lub zapisz jako plik
const blob = new Blob([upoXml], { type: "application/xml" });
```

### ✅ Pobranie aktywnej sesji

```http
GET /api/tenants/1/ksef/session/active
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "sessionId": 1,
    "referenceNumber": "20260205-SE-1234567890AB-CD",
    "status": "OPENED",
    "tokenExpiresAt": "2026-02-05T14:30:00",
    "sessionType": "ONLINE"
  }
}
```

### 🔒 Zamknięcie sesji KSeF

```http
POST /api/tenants/1/ksef/session/{sessionId}/close
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "message": "KSeF session closed successfully",
  "data": null
}
```

---

## 5️⃣ CERTYFIKATY (`/api/tenants/{tenantId}/certificates/*`)

### 📜 Lista certyfikatów

```http
GET /api/tenants/1/certificates
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "alias": "cert-2026",
      "subjectDn": "CN=Test Company, O=Test Org, C=PL",
      "issuerDn": "CN=Certum CA, O=Asseco, C=PL",
      "serialNumber": "ABC123456789",
      "validFrom": "2026-01-01T00:00:00",
      "expiresAt": "2027-01-01T00:00:00",
      "status": "ACTIVE",
      "createdAt": "2026-01-15T10:00:00"
    }
  ]
}
```

**Statusy certyfikatu:**

- `ACTIVE` - aktywny
- `EXPIRED` - wygasły
- `REVOKED` - odwołany

### 📜 Aktywne certyfikaty

```http
GET /api/tenants/1/certificates/active
Authorization: Bearer {token}
```

### 🔍 Pobranie certyfikatu po ID

```http
GET /api/tenants/1/certificates/{id}
Authorization: Bearer {token}
```

### 📤 Upload certyfikatu

```http
POST /api/tenants/1/certificates/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [certyfikat.p12]
password: haslo123
alias: cert-prod
```

**Parametry:**

- `file` - plik certyfikatu (.p12, .pfx)
- `password` - hasło do certyfikatu
- `alias` - alias certyfikatu

### ✅ Aktywacja certyfikatu

```http
POST /api/tenants/1/certificates/{id}/activate
Authorization: Bearer {token}
```

### ❌ Odwołanie certyfikatu

```http
POST /api/tenants/1/certificates/{id}/revoke
Authorization: Bearer {token}
```

### 🗑️ Usunięcie certyfikatu

```http
DELETE /api/tenants/1/certificates/{id}
Authorization: Bearer {token}
```

---

## 🎯 Typowy workflow - Wysłanie faktury do KSeF

### Przykład w JavaScript/TypeScript

```javascript
// Krok 1: Logowanie
const loginResponse = await fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    email: "admin@firma.pl",
    password: "haslo123",
  }),
});
const {
  data: { token },
} = await loginResponse.json();

// Krok 2: Utworzenie faktury
const invoiceResponse = await fetch(
  "http://localhost:8080/api/tenants/1/invoices",
  {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      invoiceNumber: "FV/2026/02/100",
      invoiceDate: "2026-02-05",
      saleDate: "2026-02-05",
      sellerNip: "1234567890",
      sellerName: "Moja Firma Sp. z o.o.",
      buyerNip: "9876543210",
      buyerName: "Klient ABC",
      netAmount: 10000.0,
      vatAmount: 2300.0,
      grossAmount: 12300.0,
      currency: "PLN",
    }),
  },
);
const { data: invoice } = await invoiceResponse.json();
console.log("Invoice ID:", invoice.id); // 100

// Krok 3: Otwarcie sesji KSeF
const sessionResponse = await fetch(
  "http://localhost:8080/api/tenants/1/ksef/session/open?sessionType=ONLINE&initialToken=moj-token-ksef",
  {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  },
);
const { data: session } = await sessionResponse.json();
console.log("Session opened:", session.referenceNumber);

// Krok 4: Wysłanie faktury do KSeF
const sendResponse = await fetch(
  `http://localhost:8080/api/tenants/1/ksef/invoices/${invoice.id}/send?sessionToken=moj-token-ksef`,
  {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  },
);
const { data: result } = await sendResponse.json();
console.log("KSeF Number:", result.ksefNumber); // "1234567890123456789100"

// Krok 5: Pobranie UPO
const upoResponse = await fetch(
  `http://localhost:8080/api/tenants/1/ksef/invoices/${invoice.id}/upo?sessionToken=moj-token-ksef`,
  {
    headers: { Authorization: `Bearer ${token}` },
  },
);
const { data: upoXml } = await upoResponse.json();

// Dekoduj Base64 i zapisz XML
const decodedUpo = atob(upoXml);
console.log("UPO XML:", decodedUpo);

// Krok 6: Zamknięcie sesji
await fetch(
  `http://localhost:8080/api/tenants/1/ksef/session/${session.sessionId}/close`,
  {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  },
);
console.log("Session closed");
```

### Przykład w Python

```python
import requests
import base64
from datetime import date

API_URL = "http://localhost:8080/api"

# 1. Logowanie
login_response = requests.post(f"{API_URL}/auth/login", json={
    "email": "admin@firma.pl",
    "password": "haslo123"
})
token = login_response.json()["data"]["token"]
headers = {"Authorization": f"Bearer {token}"}

# 2. Utworzenie faktury
invoice_response = requests.post(
    f"{API_URL}/tenants/1/invoices",
    headers={**headers, "Content-Type": "application/json"},
    json={
        "invoiceNumber": "FV/2026/02/100",
        "invoiceDate": str(date.today()),
        "saleDate": str(date.today()),
        "sellerNip": "1234567890",
        "sellerName": "Moja Firma Sp. z o.o.",
        "buyerNip": "9876543210",
        "buyerName": "Klient ABC",
        "netAmount": 10000.00,
        "vatAmount": 2300.00,
        "grossAmount": 12300.00,
        "currency": "PLN"
    }
)
invoice_id = invoice_response.json()["data"]["id"]

# 3. Otwarcie sesji KSeF
session_response = requests.post(
    f"{API_URL}/tenants/1/ksef/session/open",
    headers=headers,
    params={"sessionType": "ONLINE", "initialToken": "moj-token-ksef"}
)
session_id = session_response.json()["data"]["sessionId"]

# 4. Wysłanie faktury do KSeF
send_response = requests.post(
    f"{API_URL}/tenants/1/ksef/invoices/{invoice_id}/send",
    headers=headers,
    params={"sessionToken": "moj-token-ksef"}
)
ksef_number = send_response.json()["data"]["ksefNumber"]
print(f"KSeF Number: {ksef_number}")

# 5. Pobranie UPO
upo_response = requests.get(
    f"{API_URL}/tenants/1/ksef/invoices/{invoice_id}/upo",
    headers=headers,
    params={"sessionToken": "moj-token-ksef"}
)
upo_base64 = upo_response.json()["data"]
upo_xml = base64.b64decode(upo_base64).decode('utf-8')

# Zapisz UPO do pliku
with open(f"upo_{ksef_number}.xml", "w", encoding="utf-8") as f:
    f.write(upo_xml)

# 6. Zamknięcie sesji
requests.post(
    f"{API_URL}/tenants/1/ksef/session/{session_id}/close",
    headers=headers
)
```

---

## ✨ Podsumowanie - Co klient otrzymuje

| Funkcja          | Format danych          | Zastosowanie                                      |
| ---------------- | ---------------------- | ------------------------------------------------- |
| **Token JWT**    | String                 | Autoryzacja wszystkich requestów (24h ważności)   |
| **Lista faktur** | JSON Array + paginacja | Wyświetlenie listy, eksport, statystyki           |
| **Faktura**      | JSON Object            | Szczegóły, edycja, wydruk                         |
| **QR Kod**       | Base64 PNG             | Wyświetlenie na stronie/fakturze, wydruk          |
| **Numer KSeF**   | String (22 cyfry)      | Unikalny identyfikator w systemie KSeF            |
| **UPO**          | Base64 XML             | Oficjalne potwierdzenie - do przechowania/wydruku |
| **Sesja KSeF**   | JSON Object            | Zarządzanie komunikacją z KSeF                    |
| **XML FA_VAT**   | String (XML)           | Automatycznie generowany, zgodny ze schematem MF  |
| **Certyfikaty**  | JSON Array             | Lista certyfikatów kwalifikowanych                |

---

## 🔒 Bezpieczeństwo

### Autoryzacja

Każdy request (poza logowaniem) wymaga JWT tokenu:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Role użytkowników

- `ADMIN` - pełen dostęp
- `MANAGER` - zarządzanie firmą i fakturami
- `USER` - tworzenie i edycja faktur
- `VIEWER` - tylko odczyt

### HTTPS

W środowisku produkcyjnym zawsze używaj HTTPS!

```
https://your-domain.com/api/...
```

---

## 🌐 Środowiska KSeF

Konfiguracja w `application.yml`:

```yaml
ksef:
  api:
    base-url: https://ksef-test.mf.gov.pl/api # Testowe
    # base-url: https://ksef-demo.mf.gov.pl/api  # Demo
    # base-url: https://ksef.mf.gov.pl/api       # Produkcja
    environment: TEST
```

---

## 📞 Wsparcie

- **Swagger UI**: http://localhost:8080/api/swagger-ui/index.html
- **API Docs**: http://localhost:8080/api/v3/api-docs
- **H2 Console** (dev): http://localhost:8080/api/h2-console

---

## 🚀 Quick Start

1. **Uruchom aplikację:**

   ```bash
   mvn spring-boot:run '-Dspring-boot.run.profiles=h2'
   ```

2. **Zaloguj się:**

   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@testcompany.pl","password":"Admin123!"}'
   ```

3. **Użyj tokenu:**

   ```bash
   curl http://localhost:8080/api/tenants \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
   ```

4. **Otwórz Swagger UI:**
   ```
   http://localhost:8080/api/swagger-ui/index.html
   ```

---

**Wszystko przez REST API, wszystkie dane w JSON!** 🎉
