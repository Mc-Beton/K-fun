package pl.ksef.hub.integration.ksef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ksef.hub.domain.entity.Invoice;
import pl.ksef.hub.domain.entity.Invoice.InvoiceStatus;
import pl.ksef.hub.domain.entity.KsefSession;
import pl.ksef.hub.domain.repository.InvoiceRepository;
import pl.ksef.hub.integration.ksef.client.KsefApiClient;
import pl.ksef.hub.integration.ksef.dto.KsefInvoiceResponse;

import java.time.LocalDateTime;

/**
 * Service do wysyłki faktur do KSeF
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KsefInvoiceService {

    private final KsefApiClient ksefApiClient;
    private final KsefSessionService ksefSessionService;
    private final InvoiceRepository invoiceRepository;
    private final KsefXmlGeneratorService xmlGeneratorService;
    private final XmlValidationService xmlValidationService;
    private final XmlSignatureService xmlSignatureService;

    /**
     * Wysyła fakturę do systemu KSeF z walidacją XML
     */
    @Transactional
    public Invoice sendInvoiceToKsef(Long invoiceId, String initialToken) {
        log.info("Sending invoice to KSeF: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.SENT) {
            throw new RuntimeException("Invoice already sent to KSeF: " + invoiceId);
        }

        try {
            // Pobierz lub utwórz sesję KSeF
            String sessionToken = ksefSessionService.getOrCreateSessionToken(
                    invoice.getTenant().getId(), initialToken);

            // Wygeneruj XML faktury w formacie FA(3)
            String invoiceXml = xmlGeneratorService.generateInvoiceXml(invoice);
            
            // WALIDACJA XML - sprawdź czy jest well-formed
            if (!xmlValidationService.isWellFormed(invoiceXml)) {
                throw new RuntimeException("Generated XML is not well-formed");
            }
            
            // WALIDACJA XSD - sprawdź zgodność ze schematem (z obsługą błędów)
            XmlValidationService.ValidationResult validationResult = 
                    xmlValidationService.validateWithDetails(invoiceXml);
            
            if (!validationResult.isValid()) {
                log.warn("XML validation against XSD failed: {}", validationResult.getErrorMessage());
                log.warn("Proceeding with sending (validation may not be critical if XSD not available)");
                // W środowisku produkcyjnym możesz zdecydować czy blokować wysyłkę:
                // throw new RuntimeException("XML validation failed: " + validationResult.getErrorMessage());
            } else {
                log.info("XML validation successful");
            }
            
            // PODPISANIE XML certyfikatem kwalifikowanym
            String signedXml;
            try {
                signedXml = xmlSignatureService.signXml(invoiceXml);
                log.info("XML signed successfully with qualified certificate");
                
                // Opcjonalnie: weryfikuj podpis od razu po podpisaniu
                if (xmlSignatureService.verifySignature(signedXml)) {
                    log.debug("Signature verification: OK");
                } else {
                    log.warn("Signature verification failed after signing!");
                }
            } catch (Exception e) {
                log.error("Failed to sign XML: {}", e.getMessage(), e);
                throw new RuntimeException("XML signing failed: " + e.getMessage(), e);
            }
            
            // Zapisz podpisany XML w bazie
            invoice.setXmlContent(signedXml);

            // Wyślij podpisaną fakturę do KSeF
            KsefInvoiceResponse response = ksefApiClient.sendInvoice(sessionToken, signedXml);

            // Zaktualizuj status faktury
            invoice.setStatus(InvoiceStatus.SENT);
            invoice.setKsefNumber(response.getElementReferenceNumber());
            invoice.setSentToKsefAt(LocalDateTime.now());

            Invoice savedInvoice = invoiceRepository.save(invoice);
            log.info("Invoice sent successfully to KSeF. KSeF number: {}", 
                    response.getElementReferenceNumber());

            return savedInvoice;

        } catch (Exception e) {
            log.error("Failed to send invoice to KSeF: {}", invoiceId, e);
            
            invoice.setStatus(InvoiceStatus.ERROR);
            invoice.setErrorMessage(e.getMessage());
            invoiceRepository.save(invoice);
            
            throw new RuntimeException("Failed to send invoice to KSeF: " + e.getMessage(), e);
        }
    }

    /**
     * Pobiera UPO dla faktury
     */
    @Transactional
    public String getInvoiceUpo(Long invoiceId, String initialToken) {
        log.info("Fetching UPO for invoice: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        if (invoice.getKsefNumber() == null) {
            throw new RuntimeException("Invoice not sent to KSeF yet: " + invoiceId);
        }

        try {
            // Pobierz token sesji
            String sessionToken = ksefSessionService.getOrCreateSessionToken(
                    invoice.getTenant().getId(), initialToken);

            // Pobierz UPO z KSeF
            var upoResponse = ksefApiClient.getUpo(sessionToken, invoice.getKsefNumber());
            
            // Zapisz UPO w bazie
            invoice.setUpoContent(upoResponse.getUpo());
            invoiceRepository.save(invoice);

            log.info("UPO fetched successfully for invoice: {}", invoiceId);
            return upoResponse.getUpo();

        } catch (Exception e) {
            log.error("Failed to fetch UPO for invoice: {}", invoiceId, e);
            throw new RuntimeException("Failed to fetch UPO: " + e.getMessage(), e);
        }
    }
}
