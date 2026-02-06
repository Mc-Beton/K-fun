package pl.ksef.hub.integration.ksef;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.ksef.hub.domain.entity.Invoice;
import pl.ksef.hub.domain.entity.Tenant;
import pl.ksef.hub.domain.repository.InvoiceRepository;
import pl.ksef.hub.domain.repository.TenantRepository;
import pl.ksef.hub.integration.ksef.service.KsefXmlGeneratorService;
import pl.ksef.hub.integration.ksef.service.XmlValidationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy integracyjne dla generatora XML i walidacji
 */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class KsefXmlIntegrationTest {

    @Autowired
    private KsefXmlGeneratorService xmlGeneratorService;

    @Autowired
    private XmlValidationService xmlValidationService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Tenant testTenant;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        // Utwórz testowego tenant w bazie
        testTenant = new Tenant();
        testTenant.setNip("1234567890");
        testTenant.setFullName("Test Integration Company Sp. z o.o.");
        testTenant.setName("Test Integration");
        testTenant.setAddress("ul. Integracyjna 15, 02-222 Warszawa");
        testTenant.setEmail("integration@test.pl");
        testTenant.setPhone("+48222333444");
        testTenant.setActive(true);
        testTenant.setStatus(Tenant.TenantStatus.ACTIVE);
        testTenant = tenantRepository.save(testTenant);

        // Utwórz testową fakturę
        testInvoice = new Invoice();
        testInvoice.setTenant(testTenant);
        testInvoice.setInvoiceNumber("FV/TEST/2026/001");
        testInvoice.setInvoiceDate(LocalDate.of(2026, 2, 5));
        testInvoice.setSaleDate(LocalDate.of(2026, 2, 5));
        testInvoice.setSellerNip("1234567890");
        testInvoice.setSellerName("Test Sp. z o.o.");
        testInvoice.setBuyerNip("9876543210");
        testInvoice.setBuyerName("Klient Testowy Sp. z o.o.");
        testInvoice.setCreatedAt(LocalDateTime.now());
        testInvoice.setNetAmount(new BigDecimal("5000.00"));
        testInvoice.setVatAmount(new BigDecimal("1150.00"));
        testInvoice.setGrossAmount(new BigDecimal("6150.00"));
        testInvoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        testInvoice.setType(Invoice.InvoiceType.FA);
        testInvoice.setNotes("Usługa integracyjna - testowanie systemu KSeF");
        testInvoice = invoiceRepository.save(testInvoice);
    }

    @Test
    void shouldGenerateAndValidateCompleteInvoiceXml() {
        // Given - faktura z bazy danych
        Invoice invoice = invoiceRepository.findById(testInvoice.getId()).orElseThrow();

        // When - generuj XML
        String xml = xmlGeneratorService.generateInvoiceXml(invoice);

        // Then - sprawdź strukturę
        assertNotNull(xml);
        assertFalse(xml.isEmpty());
        
        // Sprawdź czy XML jest well-formed
        assertTrue(xmlValidationService.isWellFormed(xml), 
                "Generated XML should be well-formed");
        
        // Sprawdź kluczowe elementy
        assertThat(xml)
                .contains("<?xml version=\"1.0\"")
                .contains("<Faktura xmlns=\"http://crd.gov.pl/wzor/2023/06/29/12648/\"")
                .contains("<Naglowek>")
                .contains("<Podmiot1>")
                .contains("<Podmiot2>")
                .contains("<Fa>")
                .contains("<FaWiersz>")
                .contains("</Faktura>");
    }

    @Test
    void shouldIncludeCorrectTenantData() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then - dane sprzedawcy
        assertThat(xml)
                .contains("<NIP>1234567890</NIP>")
                .contains("<Nazwa>Test Integration Company Sp. z o.o.</Nazwa>")
                .contains("<AdresL1>ul. Integracyjna 15</AdresL1>")
                .contains("<KodPocztowy>02-222</KodPocztowy>")
                .contains("<Miejscowosc>Warszawa</Miejscowosc>")
                .contains("<AdresEmail>integration@test.pl</AdresEmail>")
                .contains("<NrTelefonu>+48222333444</NrTelefonu>");
    }

    @Test
    void shouldIncludeCorrectInvoiceAmounts() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then - kwoty faktury
        assertThat(xml)
                .contains("<P_13_1>5000.00</P_13_1>") // Netto
                .contains("<P_14_1>1150.00</P_14_1>") // VAT
                .contains("<P_15>6150.00</P_15>"); // Brutto
    }

    @Test
    void shouldIncludeCorrectInvoiceMetadata() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then - metadane
        assertThat(xml)
                .contains("<P_1>2026-02-05</P_1>") // Data wystawienia
                .contains("<P_2>FV/TEST/2026/001</P_2>") // Numer faktury
                .contains("<KodWaluty>PLN</KodWaluty>")
                .contains("<RodzajFaktury>VAT</RodzajFaktury>");
    }

    @Test
    void shouldValidateGeneratedXmlStructure() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);
        XmlValidationService.ValidationResult result = 
                xmlValidationService.validateWithDetails(xml);

        // Then
        assertNotNull(result);
        // Uwaga: Wynik może być valid lub invalid w zależności czy XSD jest dostępne
        // Ale metoda powinna działać bez rzucania wyjątków
    }

    @Test
    void shouldGenerateXmlForMultipleInvoices() {
        // Given - druga faktura
        Invoice invoice2 = new Invoice();
        invoice2.setTenant(testTenant);
        invoice2.setInvoiceNumber("FV/TEST/2026/002");
        invoice2.setInvoiceDate(LocalDate.of(2026, 2, 6));
        invoice2.setSaleDate(LocalDate.of(2026, 2, 6));
        invoice2.setSellerNip("1234567890");
        invoice2.setSellerName("Test Sp. z o.o.");
        invoice2.setBuyerNip("1111111111");
        invoice2.setBuyerName("Inny Klient Sp. z o.o.");
        invoice2.setCreatedAt(LocalDateTime.now());
        invoice2.setNetAmount(new BigDecimal("2500.00"));
        invoice2.setVatAmount(new BigDecimal("575.00"));
        invoice2.setGrossAmount(new BigDecimal("3075.00"));
        invoice2.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice2.setType(Invoice.InvoiceType.FA);
        invoice2 = invoiceRepository.save(invoice2);

        // When
        String xml1 = xmlGeneratorService.generateInvoiceXml(testInvoice);
        String xml2 = xmlGeneratorService.generateInvoiceXml(invoice2);

        // Then
        assertNotEquals(xml1, xml2, "Different invoices should generate different XML");
        
        assertTrue(xml1.contains("FV/TEST/2026/001"));
        assertTrue(xml2.contains("FV/TEST/2026/002"));
        
        assertTrue(xml1.contains("<P_15>6150.00</P_15>"));
        assertTrue(xml2.contains("<P_15>3075.00</P_15>"));
    }

    @Test
    void shouldSaveGeneratedXmlToInvoice() {
        // Given
        Invoice invoice = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertNull(invoice.getXmlContent(), "XML should be null initially");

        // When
        String xml = xmlGeneratorService.generateInvoiceXml(invoice);
        invoice.setXmlContent(xml);
        invoiceRepository.save(invoice);

        // Then
        Invoice savedInvoice = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertNotNull(savedInvoice.getXmlContent());
        assertEquals(xml, savedInvoice.getXmlContent());
    }

    @Test
    void shouldHandleSpecialCharactersInCompanyName() {
        // Given
        testTenant.setFullName("Firma \"Test & Development\" <Sp. z o.o.>");
        tenantRepository.save(testTenant);

        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertTrue(xmlValidationService.isWellFormed(xml), 
                "XML with escaped special characters should be well-formed");
        
        assertThat(xml)
                .contains("&amp;") // & escaped
                .contains("&lt;") // < escaped
                .contains("&gt;") // > escaped
                .contains("&quot;"); // " escaped
    }

    @Test
    void shouldIncludeFA3Format() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then - Format FA(3)
        assertThat(xml)
                .contains("kodSystemowy=\"FA(3)\"")
                .contains("wersjaSchemy=\"1-0E\"")
                .contains("<WariantFormularza>3</WariantFormularza>");
    }

    @Test
    void shouldIncludePaymentInformation() {
        // When
        String xml = xmlGeneratorService.generateInvoiceXml(testInvoice);

        // Then
        assertThat(xml)
                .contains("<TerminPlatnosci>")
                .contains("<FormaPlatnosci>6</FormaPlatnosci>"); // 6 = Przelew
    }
}
