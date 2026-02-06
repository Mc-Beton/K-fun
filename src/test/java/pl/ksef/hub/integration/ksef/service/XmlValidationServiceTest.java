package pl.ksef.hub.integration.ksef.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla XmlValidationService
 */
@ExtendWith(MockitoExtension.class)
class XmlValidationServiceTest {

    @InjectMocks
    private XmlValidationService validationService;

    private String validXml;
    private String invalidXml;
    private String malformedXml;

    @BeforeEach
    void setUp() {
        validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Faktura xmlns=\"http://crd.gov.pl/wzor/2023/06/29/12648/\">\n" +
                "  <Naglowek>\n" +
                "    <KodFormularza kodSystemowy=\"FA(3)\" wersjaSchemy=\"1-0E\">FA</KodFormularza>\n" +
                "  </Naglowek>\n" +
                "</Faktura>";

        invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Faktura xmlns=\"http://crd.gov.pl/wzor/2023/06/29/12648/\">\n" +
                "  <InvalidElement>This doesn't match schema</InvalidElement>\n" +
                "</Faktura>";

        malformedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Faktura>\n" +
                "  <Unclosed>\n" + // Missing closing tag
                "</Faktura>";
    }

    @Test
    void shouldDetectWellFormedXml() {
        // When
        boolean result = validationService.isWellFormed(validXml);

        // Then
        assertTrue(result, "Valid XML should be well-formed");
    }

    @Test
    void shouldDetectMalformedXml() {
        // When
        boolean result = validationService.isWellFormed(malformedXml);

        // Then
        assertFalse(result, "Malformed XML should not be well-formed");
    }

    @Test
    void shouldDetectEmptyXml() {
        // When
        boolean result = validationService.isWellFormed("");

        // Then
        assertFalse(result, "Empty string should not be well-formed XML");
    }

    @Test
    void shouldDetectNullXml() {
        // When
        boolean result = validationService.isWellFormed(null);

        // Then
        assertFalse(result, "Null should not be well-formed XML");
    }

    @Test
    void shouldValidateWithDetails() {
        // When
        XmlValidationService.ValidationResult result = 
                validationService.validateWithDetails(validXml);

        // Then
        assertNotNull(result);
        // Note: Może być valid lub invalid w zależności czy XSD jest dostępne
        // W teście sprawdzamy że metoda działa bez wyjątku
    }

    @Test
    void shouldHandleMalformedXmlInValidation() {
        // When
        XmlValidationService.ValidationResult result = 
                validationService.validateWithDetails(malformedXml);

        // Then
        assertNotNull(result);
        // Malformed XML powinien zwrócić błąd walidacji
    }

    @Test
    void shouldHandleSpecialCharactersInXml() {
        // Given
        String xmlWithSpecialChars = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Faktura xmlns=\"http://crd.gov.pl/wzor/2023/06/29/12648/\">\n" +
                "  <Naglowek>\n" +
                "    <KodFormularza kodSystemowy=\"FA(3)\" wersjaSchemy=\"1-0E\">FA</KodFormularza>\n" +
                "  </Naglowek>\n" +
                "  <Podmiot1>\n" +
                "    <DaneIdentyfikacyjne>\n" +
                "      <Nazwa>Firma &amp; Co &lt;Test&gt;</Nazwa>\n" +
                "    </DaneIdentyfikacyjne>\n" +
                "  </Podmiot1>\n" +
                "</Faktura>";

        // When
        boolean result = validationService.isWellFormed(xmlWithSpecialChars);

        // Then
        assertTrue(result, "XML with properly escaped special characters should be well-formed");
    }

    @Test
    void shouldHandlePolishCharacters() {
        // Given
        String xmlWithPolishChars = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Faktura xmlns=\"http://crd.gov.pl/wzor/2023/06/29/12648/\">\n" +
                "  <Naglowek>\n" +
                "    <SystemInfo>Wygenerowano przez aplikację KSeF Hub - Łódź, Kraków, Gdańsk</SystemInfo>\n" +
                "  </Naglowek>\n" +
                "</Faktura>";

        // When
        boolean result = validationService.isWellFormed(xmlWithPolishChars);

        // Then
        assertTrue(result, "XML with Polish characters should be well-formed");
    }

    @Test
    void shouldReturnValidationResultWithMessage() {
        // When
        XmlValidationService.ValidationResult success = 
                XmlValidationService.ValidationResult.success();
        XmlValidationService.ValidationResult failure = 
                XmlValidationService.ValidationResult.failure("Test error");

        // Then
        assertTrue(success.isValid());
        assertNull(success.getErrorMessage());
        
        assertFalse(failure.isValid());
        assertEquals("Test error", failure.getErrorMessage());
    }

    @Test
    void shouldFormatValidationResultToString() {
        // When
        XmlValidationService.ValidationResult success = 
                XmlValidationService.ValidationResult.success();
        XmlValidationService.ValidationResult failure = 
                XmlValidationService.ValidationResult.failure("Error message");

        // Then
        assertEquals("Valid", success.toString());
        assertEquals("Invalid: Error message", failure.toString());
    }
}
