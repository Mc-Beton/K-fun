# Integracja Frontend-Backend - Podsumowanie Zmian

**Data:** 9 lutego 2026  
**Status:** ✅ Skonfigurowane i gotowe do testowania

## 🎯 Co zostało zrobione?

### 1. Backend - Nowe Endpointy Dashboard API

#### Dodane pliki:

**DTOs:**

- `HubStatusDTO.java` - Status systemu (online, ksefConnected, counts, lastUpdate)
- `MessageDTO.java` - Wiadomości XML jako faktury

**Kontroler:**

- `DashboardController.java` - Obsługuje `/api/status` i `/api/messages`

**Endpointy:**

```
GET /api/status          - Status systemu
GET /api/messages?limit  - Ostatnie wiadomości/faktury XML
```

#### Zmiany w istniejących plikach:

- `InvoiceRepository.java` - dodano `countByStatus(InvoiceStatus)` dla statystyk

#### Struktury danych backendu:

**HubStatusDTO:**

```json
{
  "online": true,
  "ksefConnected": true,
  "receivedMessagesCount": 42,
  "sentToKsefCount": 15,
  "lastUpdate": "2026-02-09T15:00:00+01:00"
}
```

**MessageDTO[]:**

```json
[
  {
    "id": "1",
    "timestamp": "2026-02-09T14:30:00+01:00",
    "direction": "outgoing",
    "source": "KSeF Hub",
    "destination": "KSeF API",
    "status": "success",
    "xmlContent": "<?xml version='1.0'...",
    "response": "ref-12345",
    "errorMessage": null
  }
]
```

### 2. Frontend - Połączenie z Backendem

#### Zmiany w plikach:

**Environment:**

- `.env.local` ✨ NOWY - konfiguracja URL backendu
  ```
  NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
  ```

**API Routes:**

- `app/api/status/route.ts` - zmienione z mockowych danych na połączenie z backendem
- `app/api/messages/route.ts` - zmienione z mockowych danych na połączenie z backendem

#### Jak działa integracja:

1. **Frontend** (localhost:3000) wywołuje własne API routes `/api/status` i `/api/messages`
2. **Next.js API Routes** przekierowują zapytania do backendu (localhost:8080)
3. **Backend** zwraca prawdziwe dane z bazy
4. **Frontend** wyświetla dane w dashboardzie

### 3. Mapowanie Danych

**Invoice → Message:**

- `id` → invoice.id
- `timestamp` → invoice.createdAt (LocalDateTime → OffsetDateTime)
- `direction` → "outgoing" jeśli SENT, "incoming" w przeciwnym razie
- `source` → invoice.sellerName lub "KSeF Hub"
- `destination` → "KSeF API" lub "KSeF Hub"
- `status` → "success" (SENT), "error" (ERROR), "pending" (inne)
- `xmlContent` → invoice.xmlContent
- `response` → invoice.referenceNumber
- `errorMessage` → invoice.errorMessage

### 4. CORS Configuration

Backend automatycznie akceptuje requesty z frontendu:

```java
@CrossOrigin(origins = "http://localhost:3000")
```

## 🔄 Przepływ Danych

```
┌──────────────┐      GET /api/status       ┌──────────────────┐
│   Frontend   │ ───────────────────────▶   │  Next.js Route   │
│ localhost:   │                             │   /api/status    │
│    3000      │ ◀───────────────────────   │                  │
└──────────────┘      JSON Response          └──────────────────┘
                                                     │
                                                     │ HTTP Request
                                                     ▼
                                              ┌──────────────────┐
                                              │    Backend       │
                                              │ DashboardController
                                              │ localhost:8080   │
                                              │ GET /api/status  │
                                              └──────────────────┘
                                                     │
                                                     │ Query
                                                     ▼
                                              ┌──────────────────┐
                                              │    Database      │
                                              │ InvoiceRepository│
                                              │      H2/PG       │
                                              └──────────────────┘
```

## ✅ Status Integracji

| Funkcjonalność    | Status            | Notatki                            |
| ----------------- | ----------------- | ---------------------------------- |
| Backend endpointy | ✅ Gotowe         | /api/status, /api/messages         |
| DTOs              | ✅ Gotowe         | HubStatusDTO, MessageDTO           |
| Repository        | ✅ Gotowe         | countByStatus() dodane             |
| CORS              | ✅ Skonfigurowane | localhost:3000 dozwolone           |
| Frontend .env     | ✅ Gotowe         | NEXT_PUBLIC_BACKEND_URL            |
| API Routes        | ✅ Gotowe         | Połączenie z backendem             |
| Mapowanie danych  | ✅ Gotowe         | Invoice → Message                  |
| Fallback          | ✅ Gotowe         | Mockowe dane jeśli backend offline |

## 🚀 Jak uruchomić

### 1. Backend (Terminal 1):

```bash
cd D:\Apps\KSeF-A\KSeF
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"
```

Lub użyj istniejącego skryptu:

```bash
.\start-backend-h2.bat
```

Backend będzie dostępny na: **http://localhost:8080**

### 2. Frontend (Terminal 2):

```bash
cd D:\Apps\KSeF-A\ksef-hub-frontend
npm run dev
```

Frontend będzie dostępny na: **http://localhost:3000**

### 3. Testowanie:

1. Otwórz http://localhost:3000
2. Zobacz prawdziwe dane z backendu
3. Dane odświeżają się automatycznie co 5 sekund
4. Możesz ręcznie odświeżyć przyciskiem "Odśwież"

## 🔍 Weryfikacja Połączenia

### Backend:

```bash
# Test endpoint status
curl http://localhost:8080/api/status

# Test endpoint messages
curl http://localhost:8080/api/messages?limit=10
```

### Frontend:

Otwórz Developer Console (F12) i sprawdź:

- Network tab - powinny być requesty do `/api/status` i `/api/messages`
- Console - nie powinno być błędów "Error fetching..."

## ⚠️ Potencjalne Problemy

### Problem: Backend zwraca pustą listę wiadomości

**Przyczyna:** Baza danych jest pusta (brak faktur)

**Rozwiązanie:**

1. Użyj seed data z `V2__Seed_data.sql` (Flyway)
2. Lub stwórz testową fakturę przez Swagger UI:
   - http://localhost:8080/api/swagger-ui/index.html
   - POST /tenants/1/invoices

### Problem: CORS error

**Przyczyna:** Frontend działa na innym porcie niż localhost:3000

**Rozwiązanie:**
Zaktualizuj `@CrossOrigin` w `DashboardController.java`:

```java
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:TWOJ_PORT"})
```

### Problem: "Failed to fetch from backend"

**Przyczyna:** Backend nie jest uruchomiony

**Rozwiązanie:**

1. Sprawdź czy backend działa: `curl http://localhost:8080/actuator/health`
2. Uruchom backend: `mvn spring-boot:run`

**Fallback:** Frontend automatycznie przełączy się na tryb offline (mockowe dane)

## 📊 Co dalej?

### Zrealizowane:

- ✅ Backend endpointy dla dashboardu
- ✅ Frontend połączony z backendem
- ✅ CORS skonfigurowany
- ✅ Mapowanie danych Invoice → Message
- ✅ Fallback jeśli backend offline

### Do zrobienia (opcjonalnie):

- [ ] WebSocket dla real-time updates (zamiast polling co 5s)
- [ ] Paginacja dla /api/messages
- [ ] Filtrowanie wiadomości (po statusie, dacie, kierunku)
- [ ] Więcej statystyk w /api/status (błędy, pending, etc.)
- [ ] Authentication dla API endpoints (JWT)

## 🎉 Podsumowanie

**Frontend i Backend są teraz w pełni zintegrowane!**

- ✅ Backend udostępnia endpointy `/api/status` i `/api/messages`
- ✅ Frontend pobiera prawdziwe dane z bazy danych
- ✅ Wszystkie struktury danych są zgodne
- ✅ CORS poprawnie skonfigurowany
- ✅ Fallback działa jeśli backend jest offline
- ✅ Auto-refresh co 5 sekund

**Kompilacja:** BUILD SUCCESS ✅  
**Gotowe do testowania:** TAK ✅
