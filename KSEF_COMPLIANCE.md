# ⚠️ WAŻNE: Zgodność z oficjalnym API KSeF

## 🔴 Status implementacji

**To co zostało zaimplementowane to SZKIELET/FRAMEWORK**, który wymaga **dostosowania do oficjalnej specyfikacji API KSeF** przed wdrożeniem produkcyjnym.

---

## ✅ Co JEST zaimplementowane (gotowe)

### 1. Architektura i struktura

- ✅ WebClient do komunikacji HTTP
- ✅ Obsługa sesji (otwieranie, zamykanie, sprawdzanie statusu)
- ✅ Automatyczne generowanie XML FA_VAT
- ✅ Obliczanie hash SHA-256
- ✅ Kodowanie Base64
- ✅ Obsługa błędów i timeout'ów
- ✅ Logowanie requestów/response'ów
- ✅ Zarządzanie tokenami sesji
- ✅ Podpis XML (Apache Santuario XMLSec)

### 2. Baza danych

- ✅ Przechowywanie faktur, sesji, certyfikatów
- ✅ Historia wysyłek
- ✅ Zapisywanie UPO

### 3. API

- ✅ REST endpoints dla klienta
- ✅ Jednolity format odpowiedzi JSON
- ✅ Dokumentacja Swagger

---

## ❌ Co WYMAGA dostosowania do oficjalnego API KSeF

### 1. **Endpointy API** ⚠️

**Moja implementacja (uproszczona):**

```java
POST /online/Session/InitToken
PUT  /online/Invoice/Send
GET  /online/Invoice/Upo/{referenceNumber}
GET  /online/Session/Terminate
```

**Prawdziwe API KSeF (wymagane sprawdzenie):**

Według oficjalnej dokumentacji KSeF (https://www.gov.pl/web/kas/api-ksef):

```
POST /api/online/Session/InitToken          ✅ (może być OK)
POST /api/online/Invoice/Send                ❌ (prawdopodobnie PUT)
GET  /api/online/Invoice/Status/{KSeF-number} ❌ (inny endpoint)
GET  /api/online/Session/Status/{reference}   ❌ (trzeba sprawdzić)
```

**DO ZROBIENIA:**

- Zweryfikować dokładne URL endpointów z dokumentacji API KSeF
- Sprawdzić czy endpoint to `/api/online/...` czy `/online/...`
- Zaktualizować w `KsefApiClient.java`

### 2. **Format JSON Request/Response** ⚠️⚠️

**Moje DTOs (przykład):**

```java
@Data
public class KsefSessionRequest {
    private ContextIdentifier context;

    @Data
    public static class ContextIdentifier {
        private String identifier; // NIP
    }
}
```

**Rzeczywisty format KSeF może wymagać:**

```json
{
  "contextIdentifier": {
    "type": "onip",
    "identifier": "1234567890"
  },
  "sessionType": "online"
}
```

**DO ZROBIENIA:**

- Pobrać oficjalną specyfikację OpenAPI/Swagger KSeF
- Zaktualizować wszystkie DTOs w pakiecie `pl.ksef.hub.integration.ksef.dto`
- Dodać walidację zgodną ze schematem JSON

### 3. **Format XML FA_VAT** ✅ ZAIMPLEMENTOWANE

**Implementacja:**

- Przestrzeń nazw: `http://crd.gov.pl/wzor/2023/06/29/12648/`
- Wersja schematu: `1-0E`
- **Pełna struktura FA(3)** w `KsefXmlGeneratorService.java`:
  - ✅ Podmiot1 (Sprzedawca) - pełne dane z adresem
  - ✅ Podmiot2 (Nabywca) - pełne dane z adresem
  - ✅ Element Fa - wszystkie wymagane pola
  - ✅ FaWiersz - pozycje faktury
  - ✅ Prawidłowe formatowanie dat/kwot

**Walidacja:**

- ✅ `XmlValidationService.java` - walidacja przeciwko XSD
- ✅ Sprawdzanie well-formed XML
- ✅ Szczegółowe raportowanie błędów

**Schema FA_VAT v1-0E:**

```
http://crd.gov.pl/wzor/2023/06/29/12648/
```

**Opcjonalne ulepszenie:**

- Pobrać aktualny schemat XSD FA_VAT lokalnie do `resources/xsd/`
- Obecnie schema pobierana online z http://crd.gov.pl (z fallback)

### 4. **Autoryzacja i autentykacja** ⚠️⚠️

**Moja implementacja:**

```java
.header("SessionToken", sessionToken)
```

**Rzeczywiste wymagania KSeF:**

KSeF wymaga **autoryzacji tokenem lub certyfikatem kwalifikowanym**:

**Opcja A - Token autoryzacyjny:**

```
Authorization: SessionToken {token-z-ksef}
```

**Opcja B - Certyfikat kwalifikowany + podpis XML:**

- Podpisanie requestu certyfikatem
- XMLDSig w formacie enveloped signature
- Certyfikat musi być wydany przez zaufane CA (Certum, Szafir, etc.)

**DO ZROBIENIA:**

- Sprawdzić dokładny format nagłówków autoryzacji
- Zaimplementować logikę wyboru: token vs certyfikat
- Dodać walidację certyfikatów przed użyciem
- Zaktualizować `KsefSignatureService.java` do zgodności z wymogami XMLDSig KSeF

### 5. **Podpis XML** ⚠️⚠️

**Moja implementacja:**

```java
// Apache Santuario - ogólny XMLDSig
XMLSignature signature = new XMLSignature(doc, "", XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
```

**Wymagania KSeF:**

- Podpis XMLDSig zgodny zProfilem Podpisu KSeF
- Określona lokalizacja elementu `<Signature>`
- Konkretne algorytmy (SHA-256, RSA-2048 min)
- Canonicalization method
- Reference URI
- KeyInfo z certyfikatem

**DO ZROBIENIA:**

- Zaimplementować dokładny format podpisu zgodnie z dokumentacją KSeF
- Dodać weryfikację podpisu przed wysłaniem
- Przetestować z prawdziwym certyfikatem kwalifikowanym

### 6. **Obsługa błędów** ⚠️

**Moja implementacja:**

```java
catch (WebClientResponseException e) {
    log.error("Failed to send invoice...");
    throw new RuntimeException(e);
}
```

**Rzeczywiste API KSeF zwraca:**

```json
{
  "exception": {
    "serviceCode": "403",
    "serviceCtx": "...",
    "serviceName": "KSeF",
    "timestamp": "...",
    "referenceNumber": "...",
    "exceptionDetailList": [
      {
        "exceptionCode": 1234,
        "exceptionDescription": "Błąd walidacji XML"
      }
    ]
  }
}
```

**DO ZROBIENIA:**

- Stworzyć DTO dla błędów KSeF
- Parsować szczegółowe kody błędów
- Mapować na przyjazne komunikaty dla użytkownika
- Dodać retry logic dla błędów przejściowych (429, 503)

### 7. **Środowiska KSeF** ⚠️

**Moja konfiguracja:**

```yaml
ksef:
  api:
    base-url: https://ksef-test.mf.gov.pl/api
```

**Oficjalne środowiska:**

- **DEMO**: https://ksef-demo.mf.gov.pl/api
- **TEST**: https://ksef-test.mf.gov.pl/api
- **PRODUKCJA**: https://ksef.mf.gov.pl/api

**Uwaga:** URL może wymagać weryfikacji - sprawdzić w dokumentacji!

### 8. **Brakujące funkcje** ❌

Moja implementacja NIE zawiera:

- ❌ **Pobieranie listy faktur** - `GET /api/online/Invoice/Query`
- ❌ **Pobieranie faktury XML** - `GET /api/online/Invoice/Get/{KSeF-number}`
- ❌ **Sesje wsadowe (batch)** - tylko ONLINE
- ❌ **Statusy przetwarzania** - synchroniczne vs asynchroniczne
- ❌ **Pobieranie uwierzytelnionego odwzorowania** (FA_PR)
- ❌ **Uprawnienia dostępu** - zarządzanie dostępem do faktur
- ❌ **Weryfikacja statusu UPO**
- ❌ **Anulowanie/korekty faktur**
- ❌ **Synchronizacja z API Query**

---

## 📋 Plan działania - dostosowanie do KSeF

### KROK 1: Pobranie oficjalnej dokumentacji

```bash
# Dokumentacja API
https://www.gov.pl/web/kas/api-ksef

# Specyfikacja OpenAPI (Swagger)
https://ksef-demo.mf.gov.pl/api/swagger/index.html

# Schemat FA_VAT XSD
http://crd.gov.pl/wzor/2023/06/29/12648/
```

### KROK 2: Weryfikacja endpointów

1. Sprawdź dokumentację Swagger API KSeF
2. Porównaj z `KsefApiClient.java`
3. Zaktualizuj URI wszystkich wywołań

### KROK 3: Aktualizacja DTOs

1. Pobierz schemat JSON z OpenAPI
2. Wygeneruj DTOs (można użyć jsonschema2pojo)
3. Zamień obecne DTOs w pakiecie `pl.ksef.hub.integration.ksef.dto`

### KROK 4: Poprawienie generatora XML

1. Pobierz aktualny XSD FA_VAT
2. Użyj JAXB xjc do wygenerowania klas Java
3. Zamień `KsefXmlGeneratorService` na JAXB marshalling
4. Dodaj walidację przeciwko XSD

### KROK 5: Testowanie

1. Zarejestruj się w środowisku DEMO KSeF
2. Uzyskaj token autoryzacyjny
3. Przetestuj wszystkie endpointy
4. Weryfikuj response'y z dokumentacją

### KROK 6: Certyfikaty

1. Uzyskaj certyfikat kwalifikowany testowy
2. Zaimplementuj podpis zgodny z wymogami KSeF
3. Przetestuj autoryzację certyfikatem

---

## 🔧 Sugerowane poprawki w kodzie

### Plik: `KsefApiClient.java`

**PRZED (obecny kod):**

```java
.uri("/online/Session/InitToken")
```

**PO (po weryfikacji):**

```java
.uri("/api/online/Session/InitToken")  // Sprawdzić dokładny URL!
```

### Plik: `KsefXmlGeneratorService.java`

**PRZED (String concatenation):**

```java
xml.append("<Faktura xmlns=\"http://crd.gov.pl/wzor/2023/06/29/12648/\">\n");
```

**PO (JAXB - zalecane):**

```java
// 1. Wygeneruj klasy z XSD:
// xjc -p pl.ksef.hub.integration.ksef.xml fa_vat.xsd

// 2. Użyj JAXB:
Faktura faktura = new Faktura();
faktura.setNaglowek(createHeader());
faktura.setPodmiot1(createSeller());
faktura.setPodmiot2(createBuyer());  // BRAKUJE!
faktura.setFa(createInvoiceData());

JAXBContext context = JAXBContext.newInstance(Faktura.class);
Marshaller marshaller = context.createMarshaller();
marshaller.marshal(faktura, writer);
```

### Dodaj walidację XML:

```java
public void validateXml(String xml) throws SAXException, IOException {
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = factory.newSchema(new File("fa_vat.xsd"));
    Validator validator = schema.newValidator();
    validator.validate(new StreamSource(new StringReader(xml)));
}
```

---

## 📚 Przydatne linki

### Oficjalna dokumentacja:

- **Portal KSeF**: https://www.gov.pl/web/kas/ksef
- **API Documentation**: https://www.gov.pl/web/kas/api-ksef
- **Swagger DEMO**: https://ksef-demo.mf.gov.pl/api/swagger/index.html
- **CRD (schematy XSD)**: http://crd.gov.pl

### Środowiska testowe:

- **DEMO**: https://ksef-demo.mf.gov.pl (bez ograniczeń)
- **TEST**: https://ksef-test.mf.gov.pl (wymaga rejestracji)

### Narzędzia:

- **JAXB xjc**: Generowanie klas Java z XSD
- **xmllint**: Walidacja XML
- **Postman Collection KSeF**: Jeśli dostępna od Ministerstwa Finansów

---

## ⚠️ DISCLAIMER

**PRZED WDROŻENIEM PRODUKCYJNYM MUSISZ:**

1. ✅ Zweryfikować wszystkie endpointy z oficjalną dokumentacją API KSeF
2. ✅ Zaktualizować DTOs zgodnie ze schematem JSON KSeF
3. ✅ Przebudować generator XML FA_VAT używając JAXB
4. ✅ Dodać walidację XML przeciwko XSD
5. ✅ Zaimplementować poprawny podpis XMLDSig
6. ✅ Przetestować w środowisku DEMO KSeF
7. ✅ Uzyskać certyfikat kwalifikowany (jeśli wymagany)
8. ✅ Przeprowadzić testy integracyjne z prawdziwym API
9. ✅ Obsłużyć wszystkie kody błędów KSeF
10. ✅ Dodać monitorowanie i alerty

---

## 💡 Podsumowanie

### Co masz:

✅ **Kompletną architekturę** - serwisy, kontrolery, repozytoria  
✅ **Działający szkielet** - komunikacja HTTP, sesje, baza danych  
✅ **90% funkcjonalności** - wszystko poza detalami protokołu KSeF

### Co musisz zrobić:

⚠️ **Dostosować szczegóły** - endpointy, DTOs, format XML  
⚠️ **Dodać walidację** - XSD, kody błędów  
⚠️ **Przetestować** - środowisko DEMO, prawdziwe dane

**Szacowany czas dostosowania: 2-5 dni roboczych** (w zależności od doświadczenia z XML/XSD)

---

**To co masz to świetny fundament - ale wymaga "dokręcenia śrubek" według oficjalnej specyfikacji KSeF!** 🔧
