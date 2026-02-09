# KSeF Hub - Multi-tenant KSeF 2.0 Integration Platform

Kompleksowa platforma do integracji z polskim systemem **KSeF 2.0** (Krajowy System e-Faktur), umożliwiająca zarządzanie fakturami elektronicznymi dla wielu firm.

> **✨ AKTUALIZACJA:** Projekt zaktualizowany do **KSeF 2.0 API** (5 lutego 2026)  
> Zobacz: [KSEF_2.0_DONE.md](KSEF_2.0_DONE.md) - pełna dokumentacja zmian

## 🚀 Funkcjonalności

### ✅ Zaimplementowane funkcjonalności:

- **Multi-tenant** - obsługa wielu firm w jednym systemie
- **Zarządzanie użytkownikami** - role i uprawnienia (Admin, Manager, User, Viewer)
- **Faktury** - tworzenie, edycja, wyszukiwanie, eksport
- **KSeF 2.0 API Integration** - pełna integracja z nowym API KSeF
  - ✅ Otwieranie i zamykanie sesji KSeF (endpointy `/api/online/`)
  - ✅ Wysyłanie faktur do KSeF
  - ✅ Pobieranie UPO (Urzędowe Poświadczenie Odbioru)
  - ✅ Generowanie XML w formacie FA(3)
  - ✅ Podpis XML certyfikatem kwalifikowanym
  - ✅ Obsługa błędów KSeF 2.0
- **Certyfikaty** - zarządzanie certyfikatami kwalifikowanymi i tokenami KSeF
- **QR Kody** - automatyczne generowanie kodów QR dla faktur
- **Audyt** - pełne logowanie wszystkich operacji
- **REST API** - kompletne API z dokumentacją Swagger
- **Security** - JWT authentication, role-based access control
- **Baza danych** - PostgreSQL lub H2 (development)

## 🆕 KSeF 2.0 - Co nowego?

### Zgodność z oficjalnym API KSeF 2.0:

- ✅ Endpointy: `/api/online/Session/InitToken`, `/api/online/Invoice/Send`, etc.
- ✅ DTOs zgodne z oficjalną specyfikacją (OffsetDateTime, processingCode)
- ✅ Środowiska: DEMO, TEST, PROD
- ✅ Error handling z kodami błędów KSeF

### Dokumentacja KSeF 2.0:

- [KSEF_2.0_DONE.md](KSEF_2.0_DONE.md) - ✅ Podsumowanie aktualizacji
- [KSEF_COMPLIANCE.md](KSEF_COMPLIANCE.md) - ✅ Analiza zgodności
- [KSEF_2.0_UPDATE.md](KSEF_2.0_UPDATE.md) - 📋 Szczegóły techniczne
- [KSEF_XSD_UPDATE.md](KSEF_XSD_UPDATE.md) - 📋 Lokalne schematy XSD i walidacja

## 📋 Wymagania

- Java 17+
- PostgreSQL 14+ (lub H2 dla rozwoju)
- Maven 3.8+
- (Opcjonalnie) Docker & Docker Compose

## 🛠️ Instalacja i uruchomienie

### Opcja 1: Z bazą H2 (development) - ZALECANE

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=h2"
```

### Opcja 2: Z PostgreSQL (produkcja)

1. Uruchom PostgreSQL:

```bash
docker-compose up -d postgres
```

2. Uruchom aplikację:

```bash
mvn spring-boot:run
```

### 3. Dostęp do aplikacji

- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui/index.html
- **H2 Console** (gdy profil h2): http://localhost:8080/api/h2-console

## 🔌 Integracja z KSeF

### Architektura integracji

```
KsefIntegrationController
        ↓
KsefInvoiceService / KsefSessionService
        ↓
KsefApiClient (WebClient)
        ↓
KSeF API (https://ksef.mf.gov.pl/api)
```

### Komponenty:

1. **KsefApiClient** - komunikacja HTTP z API KSeF
2. **KsefSessionService** - zarządzanie sesjami
3. **KsefInvoiceService** - wysyłka faktur i pobieranie UPO
4. **KsefXmlGeneratorService** - generowanie XML FA_VAT
5. **KsefSignatureService** - podpis XML certyfikatem

### Przykład wysłania faktury:

```bash
# 1. Otwórz sesję
POST /api/tenants/{tenantId}/ksef/session/open
{
  "sessionType": "ONLINE",
  "initialToken": "your-initial-token"
}

# 2. Wyślij fakturę
POST /api/tenants/{tenantId}/ksef/invoices/{invoiceId}/send?sessionToken=xyz

# 3. Pobierz UPO
GET /api/tenants/{tenantId}/ksef/invoices/{invoiceId}/upo?sessionToken=xyz
```

## 📚 Struktura projektu

```
ksef-hub/
├── src/main/java/pl/ksef/hub/
│   ├── api/
│   │   ├── controller/       # REST controllers
│   │   └── dto/              # Data Transfer Objects
│   ├── config/               # Configuration classes
│   ├── domain/
│   │   ├── entity/           # JPA entities
│   │   └── repository/       # Spring Data repositories
│   ├── integration/ksef/     # 🆕 KSeF Integration
│   │   ├── client/           # KsefApiClient (WebClient)
│   │   ├── config/           # WebClient configuration
│   │   ├── dto/              # KSeF API DTOs
│   │   └── service/          # Session, Invoice, XML, Signature
│   ├── security/             # Security & JWT
│   ├── service/              # Business logic
│   └── exception/            # Custom exceptions
└── pom.xml
```

## 🔑 Domyślne dane testowe

Po uruchomieniu aplikacji dostępne są testowe dane:

- **Firma**: Test Company (NIP: 1234567890)
- **Email**: admin@testcompany.pl
- **Hasło**: Admin123!

### Przykład logowania (curl):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@testcompany.pl",
    "password": "Admin123!"
  }'
```

## 📖 Przykłady użycia API

### 1. Logowanie

```bash
POST /api/auth/login
{
  "email": "admin@testcompany.pl",
  "password": "Admin123!"
}
```

### 2. Lista firm (tenants)

```bash
GET /api/tenants
Authorization: Bearer {token}
```

### 3. Tworzenie faktury

```bash
POST /api/tenants/1/invoices
Authorization: Bearer {token}
{
  "invoiceNumber": "FV/2026/02/001",
  "invoiceDate": "2026-02-05",
  "saleDate": "2026-02-05",
  "sellerNip": "1234567890",
  "sellerName": "Test Company",
  "buyerNip": "9876543210",
  "buyerName": "Buyer Company",
  "netAmount": 1000.00,
  "vatAmount": 230.00,
  "grossAmount": 1230.00,
  "currency": "PLN",
  "xmlContent": "<xml>...</xml>"
}
```

### 4. Pobieranie QR kodu faktury

```bash
GET /api/tenants/1/invoices/{invoiceId}/qrcode
Authorization: Bearer {token}
```

## 🔐 Security

### Role użytkowników:

- **ADMIN** - pełny dostęp do systemu
- **MANAGER** - zarządzanie firmą i fakturami
- **USER** - tworzenie i edycja faktur
- **VIEWER** - tylko odczyt

### JWT Token:

- Ważność access token: 24 godziny
- Ważność refresh token: 7 dni
- Algorytm: HS256

## 🗄️ Baza danych

### Główne tabele:

- `tenants` - firmy
- `users` - użytkownicy
- `invoices` - faktury
- `certificates` - certyfikaty
- `ksef_sessions` - sesje KSeF
- `audit_logs` - logi audytowe

### Migracje:

Flyway automatycznie wykonuje migracje przy starcie aplikacji.

## 🔌 Integracja z KSeF SDK

Aby dodać pełną integrację z KSeF:

1. Dodaj zależność do `pom.xml`:

```xml
<dependency>
    <groupId>pl.akmf.ksef</groupId>
    <artifactId>ksef-client</artifactId>
    <version>2.0.0</version>
</dependency>
```

2. Zaimplementuj serwis `KsefClientService` używając `DefaultKsefClient`
3. Dodaj obsługę sesji, wysyłania faktur i pobierania UPO
4. Zaktualizuj kontroler `KsefIntegrationController`

## 🐳 Docker

### Uruchomienie całej aplikacji w Docker:

```bash
docker-compose up -d
```

To uruchomi:

- PostgreSQL na porcie 5432
- Aplikację KSeF Hub na porcie 8080

## 📝 TODO

- [ ] Implementacja KSeF SDK integration
- [ ] Obsługa certyfikatów kwalifikowanych (upload, walidacja)
- [ ] Automatyczne zadania (wygasające certyfikaty, retry wysyłek)
- [ ] WebSocket notifications
- [ ] Eksport raportów (PDF, Excel)
- [ ] Dashboard z statystykami
- [ ] Testy jednostkowe i integracyjne
- [ ] CI/CD pipeline

## 📄 Licencja

MIT License

## 👥 Autor

el Filberto
