package pl.ksef.hub.integration.ksef.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ksef.hub.domain.entity.Invoice;
import pl.ksef.hub.domain.entity.Tenant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla KsefXmlGeneratorService
 */
@ExtendWith(MockitoExtension.class)
class KsefXmlGeneratorServiceTest {

    @InjectMocks
    private KsefXmlGeneratorService xmlGeneratorService;

    private Invoice testInvoice;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        // Przygotuj testowe dane
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setNip("1234567890");
        testTenant.setFullName("Test Company Sp. z o.o.");
        testTenant.setName("Test Company");
        testTenant.setAddress("ul. Testowa 10, 00-001 Warszawa");
        testTenant.setEmail("test@company.pl");
        testTenant.setPhone("+48123456789");

        testInvoice = new Invoice();
        testInvoice.setId(1L);
        testInvoice.setTenant(testTenant);
        testInvoice.setInvoiceNumber("FV/2026/02/001");
        testInvoice.setInvoiceDate(LocalDate.of(2026, 2, 5));
        testInvoice.setSaleDate(LocalDate.of(2026, 2, 5));
        testInvoice.setSellerNip("1234567890");
        testInvoice.setSellerName("Test Sp. z o.o.");
        testInvoice.setBuyerNip("9876543210");
        testInvoice.setBuyerName("Klient Testowy");
        testInvoice.setCreatedAt(LocalDateTime.of(2026, 2, 5, 10, 0));
        testInvoice.setNetAmount(new BigDecimal("1000.00"));
        testInvoice.setVatAmount(new BigDecimal("230.00"));
        testInvoice.setGrossAmount(new BigDecimal("1230.00"));
        testInvoice.setNotes("Usługa konsultingowa");
    }

    @Test
    void shouldGenerateValidXmlStructure() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertNotNull(xml);
        assertFalse(xml.isEmpty());

        // Sprawdź podstawową strukturę
        assertTrue(xml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(xml.contains("<Faktura xmlns=\"http://crd.gov.pl/wzor/2023/06/29/12648/\""));
        assertTrue(xml.contains("</Faktura>"));
    }

    @Test
    void shouldIncludeHeader() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xml.contains("<Naglowek>"));
        assertTrue(xml.contains("<KodFormularza kodSystemowy=\"FA(3)\" wersjaSchemy=\"1-0E\">FA</KodFormularza>"));
        assertTrue(xml.contains("<WariantFormularza>3</WariantFormularza>"));
        assertTrue(xml.contains("<SystemInfo>KSeF Hub v2.0</SystemInfo>"));
    }

    @Test
    void shouldIncludePodmiot1Seller() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xml.contains("<Podmiot1>"));
        assertTrue(xml.contains("<NIP>1234567890</NIP>"));
        assertTrue(xml.contains("<Nazwa>Test Company Sp. z o.o.</Nazwa>"));
        assertTrue(xml.contains("<KodKraju>PL</KodKraju>"));
        assertTrue(xml.contains("<AdresL1>ul. Testowa 10</AdresL1>"));
        assertTrue(xml.contains("<KodPocztowy>00-001</KodPocztowy>"));
        assertTrue(xml.contains("<Miejscowosc>Warszawa</Miejscowosc>"));
    }

    @Test
    void shouldIncludePodmiot2Buyer() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xml.contains("<Podmiot2>"));
        assertTrue(xml.contains("</Podmiot2>"));
        // Podmiot2 powinien zawierać dane nabywcy (mock lub z notes)
    }

    @Test
    void shouldIncludeFaInvoiceData() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xml.contains("<Fa>"));
        assertTrue(xml.contains("<KodWaluty>PLN</KodWaluty>"));
        assertTrue(xml.contains("<P_1>2026-02-05</P_1>")); // Data wystawienia
        assertTrue(xml.contains("<P_2>FV/2026/02/001</P_2>")); // Numer faktury
        assertTrue(xml.contains("<P_13_1>1000.00</P_13_1>")); // Netto
        assertTrue(xml.contains("<P_14_1>230.00</P_14_1>")); // VAT
        assertTrue(xml.contains("<P_15>1230.00</P_15>")); // Brutto
        assertTrue(xml.contains("<RodzajFaktury>VAT</RodzajFaktury>"));
    }

    @Test
    void shouldIncludeFaWierszLineItems() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xml.contains("<FaWiersz>"));
        assertTrue(xml.contains("<NrWierszaFa>1</NrWierszaFa>"));
        assertTrue(xml.contains("<P_12>23</P_12>")); // Stawka VAT 23%
    }

    @Test
    void shouldEscapeSpecialCharacters() {
        // Given
        testTenant.setFullName("Test & Company <XML>");
        testInvoice.setNotes("Test > description with & special < characters");

        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xml.contains("Test &amp; Company &lt;XML&gt;"));
        assertFalse(xml.contains("Test & Company <XML>")); // Raw chars should be escaped
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        testInvoice.setNetAmount(null);
        testInvoice.setVatAmount(null);
        testInvoice.setGrossAmount(null);
        testInvoice.setNotes(null);
        testTenant.setEmail(null);
        testTenant.setPhone(null);

        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<P_13_1>0.00</P_13_1>")); // Should default to 0
        assertTrue(xml.contains("<P_14_1>0.00</P_14_1>"));
        assertTrue(xml.contains("<P_15>0.00</P_15>"));
    }

    @Test
    void shouldFormatAmountsCorrectly() {
        // Given
        testInvoice.setNetAmount(new BigDecimal("1234.567")); // 3 decimal places
        testInvoice.setVatAmount(new BigDecimal("283.95041")); // 5 decimal places

        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xml.contains("<P_13_1>1234.57</P_13_1>")); // Rounded to 2 decimals
        assertTrue(xml.contains("<P_14_1>283.95</P_14_1>")); // Rounded to 2 decimals
    }

    @Test
    void shouldIncludePaymentTerms() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xml.contains("<TerminPlatnosci>"));
        assertTrue(xml.contains("<FormaPlatnosci>6</FormaPlatnosci>")); // 6 = Przelew
    }

    @Test
    void shouldGenerateValidXmlForDifferentAddressFormats() {
        // Given - format bez kodu pocztowego
        testTenant.setAddress("ul. Krótka 1");

        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<AdresL1>ul. Krótka 1</AdresL1>"));
        assertTrue(xml.contains("<KodPocztowy>00-000</KodPocztowy>")); // Default
    }

    @Test
    void shouldIncludeAdnotacje() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xml.contains("<Adnotacje>"));
        assertTrue(xml.contains("<P_16>2</P_16>")); // Metoda kasowa = NIE
        assertTrue(xml.contains("<P_17>2</P_17>")); // Samofakturowanie = NIE
        assertTrue(xml.contains("<P_18>2</P_18>")); // Odwrotne obciążenie = NIE
        assertTrue(xml.contains("<P_19>2</P_19>")); // MPP = NIE
    }

    @Test
    void shouldGenerateCompleteXmlDocument() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertThat(xml)
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .contains("<Faktura xmlns=")
                .contains("<Naglowek>")
                .contains("<Podmiot1>")
                .contains("<Podmiot2>")
                .contains("<Fa>")
                .contains("<FaWiersz>")
                .endsWith("</Faktura>");
    }
}
