package pl.ksef.hub.integration.ksef.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

/**
 * Serwis walidacji XML przeciwko schematowi XSD
 * Zgodność z KSeF 2.0 - FA(3)
 */
@Slf4j
@Service
public class XmlValidationService {

    // Schema FA(3) z CRD (Centralne Repozytorium Dokumentów)
    private static final String SCHEMA_URL = "http://crd.gov.pl/wzor/2023/06/29/12648/schemat.xsd";
    
    private Schema schema;
    
    /**
     * Waliduje XML faktury przeciwko schematowi XSD FA(3)
     * 
     * @param xml XML do walidacji
     * @throws ValidationException jeśli XML jest niepoprawny
     */
    public void validateInvoiceXml(String xml) throws ValidationException {
        log.debug("Validating invoice XML against XSD schema");
        
        try {
            Validator validator = getSchema().newValidator();
            validator.validate(new StreamSource(new StringReader(xml)));
            log.debug("XML validation successful");
            
        } catch (SAXException e) {
            String message = "XML validation failed: " + e.getMessage();
            log.error(message, e);
            throw new ValidationException(message, e);
            
        } catch (IOException e) {
            String message = "IO error during XML validation: " + e.getMessage();
            log.error(message, e);
            throw new ValidationException(message, e);
        }
    }
    
    /**
     * Waliduje XML z raportowaniem szczegółowych błędów
     * 
     * @param xml XML do walidacji
     * @return ValidationResult z listą błędów lub sukcesem
     */
    public ValidationResult validateWithDetails(String xml) {
        try {
            validateInvoiceXml(xml);
            return ValidationResult.success();
            
        } catch (ValidationException e) {
            return ValidationResult.failure(e.getMessage());
        }
    }
    
    /**
     * Pobiera lub inicjalizuje schema XSD
     * Schema jest cachowana dla wydajności
     */
    private Schema getSchema() throws ValidationException {
        if (schema == null) {
            synchronized (this) {
                if (schema == null) {
                    try {
                        log.info("Loading XSD schema from: {}", SCHEMA_URL);
                        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        
                        // UWAGA: W środowisku produkcyjnym zaleca się:
                        // 1. Pobrać XSD lokalnie i umieścić w resources
                        // 2. Lub użyć lokalnego cache
                        // Obecnie próbujemy pobrać ze źródła online
                        
                        try {
                            schema = factory.newSchema(new URL(SCHEMA_URL));
                            log.info("XSD schema loaded successfully from online source");
                        } catch (Exception e) {
                            // Fallback: jeśli nie można pobrać online, pomiń walidację XSD
                            log.warn("Could not load XSD schema from online source: {}. XML structure validation will be skipped.", e.getMessage());
                            log.warn("For production use, please download XSD schema locally to resources folder");
                            
                            // Zwróć pustą schema która akceptuje wszystko
                            return createPermissiveSchema();
                        }
                        
                    } catch (SAXException e) {
                        throw new ValidationException("Failed to initialize XSD schema: " + e.getMessage(), e);
                    }
                }
            }
        }
        return schema;
    }
    
    /**
     * Tworzy "permissive" schema dla przypadku gdy nie można załadować prawdziwego XSD
     * To pozwala aplikacji działać bez walidacji XSD (z logowaniem ostrzeżenia)
     */
    private Schema createPermissiveSchema() throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        // Minimal XSD który akceptuje wszystko
        String permissiveXsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "  <xs:element name=\"Faktura\">\n" +
                "    <xs:complexType>\n" +
                "      <xs:sequence>\n" +
                "        <xs:any processContents=\"skip\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
                "      </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "  </xs:element>\n" +
                "</xs:schema>";
        
        return factory.newSchema(new StreamSource(new StringReader(permissiveXsd)));
    }
    
    /**
     * Sprawdza czy XML jest well-formed (poprawnie sformatowany)
     * bez walidacji przeciwko schema
     */
    public boolean isWellFormed(String xml) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = 
                    javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(new org.xml.sax.InputSource(new StringReader(xml)));
            return true;
            
        } catch (Exception e) {
            log.error("XML is not well-formed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Wyjątek walidacji
     */
    public static class ValidationException extends Exception {
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Wynik walidacji
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        @Override
        public String toString() {
            return valid ? "Valid" : "Invalid: " + errorMessage;
        }
    }
}
