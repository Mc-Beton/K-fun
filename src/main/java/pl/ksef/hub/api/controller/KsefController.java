package pl.ksef.hub.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ksef.hub.api.dto.ApiResponse;
import pl.ksef.hub.domain.entity.Invoice;
import pl.ksef.hub.integration.ksef.service.KsefInvoiceService;
import pl.ksef.hub.integration.ksef.service.XmlSignatureService;

/**
 * Kontroler do zarządzania integracją z KSeF
 */
@Tag(name = "KSeF Integration", description = "KSeF system integration endpoints")
@RestController
@RequestMapping("/ksef")
@RequiredArgsConstructor
public class KsefController {

    private final KsefInvoiceService ksefInvoiceService;
    private final XmlSignatureService xmlSignatureService;
    private final pl.ksef.hub.integration.ksef.service.KsefAuthService ksefAuthService;

    @Operation(summary = "Send invoice to KSeF", 
               description = "Sends an invoice to the KSeF system. The invoice will be validated, signed with certificate, and sent.")
    @PostMapping("/invoices/{invoiceId}/send")
    public ResponseEntity<ApiResponse<InvoiceSendResponse>> sendInvoiceToKsef(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId,
            @Parameter(description = "Initial authentication token") @RequestParam String token) {
        
        try {
            Invoice invoice = ksefInvoiceService.sendInvoiceToKsef(invoiceId, token);
            
            InvoiceSendResponse response = InvoiceSendResponse.builder()
                    .invoiceId(invoice.getId())
                    .invoiceNumber(invoice.getInvoiceNumber())
                    .ksefNumber(invoice.getKsefNumber())
                    .referenceNumber(invoice.getReferenceNumber())
                    .status(invoice.getStatus().name())
                    .sentAt(invoice.getSentToKsefAt())
                    .message("Invoice sent successfully to KSeF")
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to send invoice to KSeF: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get UPO for invoice", 
               description = "Retrieves UPO (Official Confirmation of Receipt) from KSeF for a sent invoice")
    @GetMapping("/invoices/{invoiceId}/upo")
    public ResponseEntity<ApiResponse<UpoResponse>> getInvoiceUpo(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId,
            @Parameter(description = "Authentication token") @RequestParam String token) {
        
        try {
            String upo = ksefInvoiceService.getInvoiceUpo(invoiceId, token);
            
            UpoResponse response = UpoResponse.builder()
                    .invoiceId(invoiceId)
                    .upo(upo)
                    .message("UPO retrieved successfully")
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get UPO: " + e.getMessage()));
        }
    }

    @Operation(summary = "Check certificate status", 
               description = "Checks if XML signing certificate is configured and valid")
    @GetMapping("/certificate/status")
    public ResponseEntity<ApiResponse<CertificateStatusResponse>> getCertificateStatus() {
        boolean configured = xmlSignatureService.isCertificateConfigured();
        String info = xmlSignatureService.getCertificateInfo();
        
        CertificateStatusResponse response = CertificateStatusResponse.builder()
                .configured(configured)
                .info(info)
                .message(configured ? "Certificate is configured" : "Certificate not configured")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get certificate information", 
               description = "Returns detailed information about the configured certificate")
    @GetMapping("/certificate/info")
    public ResponseEntity<ApiResponse<String>> getCertificateInfo() {
        String info = xmlSignatureService.getCertificateInfo();
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @Operation(summary = "Initialize KSeF session", 
               description = "Initializes a new session with KSeF using NIP and initial token")
    @PostMapping("/auth/session/init")
    public ResponseEntity<ApiResponse<SessionInitResponse>> initSession(
            @Parameter(description = "NIP (Tax ID)") @RequestParam String nip,
            @Parameter(description = "Initial token from KSeF portal") @RequestParam String initialToken) {
        
        try {
            String sessionToken = ksefAuthService.initializeSession(nip, initialToken);
            
            SessionInitResponse response = SessionInitResponse.builder()
                    .nip(nip)
                    .sessionToken(sessionToken)
                    .message("Session initialized successfully")
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to initialize session: " + e.getMessage()));
        }
    }

    @Operation(summary = "Generate authorization token", 
               description = "Generates authorization token (SHA-256 hash) from initial token")
    @PostMapping("/auth/token/generate")
    public ResponseEntity<ApiResponse<TokenGenerateResponse>> generateAuthToken(
            @Parameter(description = "Initial token") @RequestParam String initialToken) {
        
        try {
            String authToken = ksefAuthService.generateAuthorizationToken(initialToken);
            
            TokenGenerateResponse response = TokenGenerateResponse.builder()
                    .authorizationToken(authToken)
                    .message("Authorization token generated successfully")
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate token: " + e.getMessage()));
        }
    }

    @Operation(summary = "Terminate KSeF session", 
               description = "Terminates an active KSeF session")
    @PostMapping("/auth/session/terminate")
    public ResponseEntity<ApiResponse<String>> terminateSession(
            @Parameter(description = "Session token") @RequestParam String sessionToken) {
        
        try {
            ksefAuthService.terminateSession(sessionToken);
            return ResponseEntity.ok(ApiResponse.success("Session terminated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to terminate session: " + e.getMessage()));
        }
    }

    @Operation(summary = "Check session validity", 
               description = "Checks if a KSeF session is still valid")
    @GetMapping("/auth/session/check")
    public ResponseEntity<ApiResponse<SessionCheckResponse>> checkSession(
            @Parameter(description = "NIP") @RequestParam String nip,
            @Parameter(description = "Initial token") @RequestParam String initialToken) {
        
        boolean valid = ksefAuthService.isSessionValid(nip, initialToken);
        
        SessionCheckResponse response = SessionCheckResponse.builder()
                .valid(valid)
                .message(valid ? "Session is valid" : "Session is invalid or expired")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // === Inner DTOs ===

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InvoiceSendResponse {
        private Long invoiceId;
        private String invoiceNumber;
        private String ksefNumber;
        private String referenceNumber;
        private String status;
        private java.time.LocalDateTime sentAt;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpoResponse {
        private Long invoiceId;
        private String upo;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CertificateStatusResponse {
        private boolean configured;
        private String info;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SessionInitResponse {
        private String nip;
        private String sessionToken;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenGenerateResponse {
        private String authorizationToken;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SessionCheckResponse {
        private boolean valid;
        private String message;
    }
}
