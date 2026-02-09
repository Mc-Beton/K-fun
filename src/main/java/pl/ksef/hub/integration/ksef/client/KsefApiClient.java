package pl.ksef.hub.integration.ksef.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.ksef.hub.integration.ksef.dto.*;
import pl.ksef.hub.service.SystemNotificationService;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

/**
 * KSeF API Client - handles communication with Polish National e-Invoice System
 * API Documentation: https://ksef.mf.gov.pl/
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KsefApiClient {

    private final WebClient ksefWebClient;
    private final SystemNotificationService notificationService;

    @Value("${ksef.api.timeout:30000}")
    private int timeout;
    
    @Value("${ksef.api.base-url}")
    private String baseUrl;
    
    @Value("${ksef.api.environment}")
    private String environment;
    
    // Track connection state to avoid duplicate notifications
    private volatile Boolean lastConnectionState = null;

    /**
     * Otwiera sesję interaktywną w systemie KSeF
     * Endpoint: POST /api/online/Session/InitToken (KSeF 2.0)
     */
    public KsefSessionResponse initSession(String nip, String sessionToken) {
        log.info("Initializing KSeF session for NIP: {}", nip);

        KsefSessionRequest request = KsefSessionRequest.builder()
                .contextIdentifier(KsefSessionRequest.ContextIdentifier.builder()
                        .type("onip")
                        .identifier(nip)
                        .build())
                .build();

        try {
            return ksefWebClient.post()
                    .uri("/api/online/Session/InitToken")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("SessionToken", sessionToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(KsefSessionResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to initialize KSeF session. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to initialize KSeF session: " + e.getMessage(), e);
        }
    }

    /**
     * Wysyła fakturę do systemu KSeF
     * Endpoint: PUT /api/online/Invoice/Send (KSeF 2.0)
     */
    public KsefInvoiceResponse sendInvoice(String sessionToken, String invoiceXml) {
        log.info("Sending invoice to KSeF. XML size: {} bytes", invoiceXml.length());

        try {
            // Oblicz hash SHA-256 z XML
            String sha256Hash = calculateSHA256(invoiceXml);
            
            // Zakoduj XML do Base64
            String base64Xml = Base64.getEncoder().encodeToString(
                    invoiceXml.getBytes(StandardCharsets.UTF_8));

            KsefInvoiceRequest request = KsefInvoiceRequest.builder()
                    .invoiceHash(KsefInvoiceRequest.InvoiceHash.builder()
                            .hashSHA(KsefInvoiceRequest.InvoiceHash.HashSHA.builder()
                                    .algorithm("SHA-256")
                                    .encoding("Base64")
                                    .value(sha256Hash)
                                    .build())
                            .fileSize((long) invoiceXml.getBytes(StandardCharsets.UTF_8).length)
                            .build())
                    .invoicePayload(KsefInvoiceRequest.InvoicePayload.builder()
                            .type("plain")
                            .invoiceBody(base64Xml)
                            .build())
                    .build();

            return ksefWebClient.put()
                    .uri("/api/online/Invoice/Send")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("SessionToken", sessionToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(KsefInvoiceResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Failed to send invoice to KSeF. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send invoice: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Failed to calculate invoice hash", e);
        }
    }

    /**
     * Pobiera UPO (Urzędowe Poświadczenie Odbioru) dla faktury
     * Endpoint: GET /api/online/Invoice/Upo/{referenceNumber} (KSeF 2.0)
     */
    public KsefUpoResponse getUpo(String sessionToken, String referenceNumber) {
        log.info("Fetching UPO for reference number: {}", referenceNumber);

        try {
            return ksefWebClient.get()
                    .uri("/api/online/Invoice/Upo/{referenceNumber}", referenceNumber)
                    .header("SessionToken", sessionToken)
                    .retrieve()
                    .bodyToMono(KsefUpoResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Failed to fetch UPO. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch UPO: " + e.getMessage(), e);
        }
    }

    /**
     * Zamyka sesję w systemie KSeF
     * Endpoint: GET /api/online/Session/Terminate (KSeF 2.0)
     */
    public void terminateSession(String sessionToken) {
        log.info("Terminating KSeF session");

        try {
            ksefWebClient.get()
                    .uri("/api/online/Session/Terminate")
                    .header("SessionToken", sessionToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            log.info("KSeF session terminated successfully");

        } catch (WebClientResponseException e) {
            log.error("Failed to terminate session. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to terminate session: " + e.getMessage(), e);
        }
    }

    /**
     * Sprawdza status sesji
     * Endpoint: GET /api/online/Session/Status/{referenceNumber} (KSeF 2.0)
     */
    public KsefSessionResponse getSessionStatus(String sessionToken, String referenceNumber) {
        log.info("Checking session status for reference: {}", referenceNumber);

        try {
            return ksefWebClient.get()
                    .uri("/api/online/Session/Status/{referenceNumber}", referenceNumber)
                    .header("SessionToken", sessionToken)
                    .retrieve()
                    .bodyToMono(KsefSessionResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Failed to get session status. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get session status: " + e.getMessage(), e);
        }
    }

    /**
     * Sprawdza dostępność API KSeF
     * Endpoint: GET /common/Status (publicznie dostępny, bez autoryzacji)
     */
    public boolean checkApiStatus() {
        log.debug("Checking KSeF API status");

        try {
            String response = ksefWebClient.get()
                    .uri("/common/Status")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(5000)) // Krótszy timeout dla health check
                    .block();

            log.debug("KSeF API is available. Response: {}", response);
            
            // Notify on first successful connection or reconnection after failure
            if (lastConnectionState == null || !lastConnectionState) {
                notificationService.notifyKsefConnected(environment, baseUrl);
                lastConnectionState = true;
            }
            
            return true;

        } catch (WebClientResponseException e) {
            log.warn("KSeF API returned error: {} - {}", e.getStatusCode(), e.getMessage());
            
            // Notify on disconnection
            if (lastConnectionState == null || lastConnectionState) {
                String reason = String.format("HTTP %s: %s", e.getStatusCode(), e.getStatusText());
                String details = String.format(
                        "{\"status_code\": %d, \"url\": \"%s\", \"error\": \"%s\"}",
                        e.getStatusCode().value(), baseUrl, e.getStatusText()
                );
                notificationService.notifyKsefConnectionFailed(reason, details);
                lastConnectionState = false;
            }
            
            return false;
            
        } catch (Exception e) {
            log.warn("KSeF API is not available: {}", e.getMessage());
            
            // Notify on disconnection
            if (lastConnectionState == null || lastConnectionState) {
                String reason = e.getMessage() != null ? e.getMessage() : "Nieznany błąd połączenia";
                String details = String.format(
                        "{\"url\": \"%s\", \"exception\": \"%s\"}",
                        baseUrl, e.getClass().getSimpleName()
                );
                notificationService.notifyKsefConnectionFailed(reason, details);
                lastConnectionState = false;
            }
            
            return false;
        }
    }

    /**
     * Oblicza hash SHA-256 i koduje do Base64
     */
    private String calculateSHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
