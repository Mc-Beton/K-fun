package pl.ksef.hub.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ksef.hub.api.dto.ApiResponse;
import pl.ksef.hub.domain.entity.Invoice;
import pl.ksef.hub.domain.entity.KsefSession;
import pl.ksef.hub.domain.entity.KsefSession.SessionType;
import pl.ksef.hub.integration.ksef.service.KsefInvoiceService;
import pl.ksef.hub.integration.ksef.service.KsefSessionService;

import java.util.HashMap;
import java.util.Map;

/**
 * KSeF Integration Controller - handles communication with KSeF API
 */
@Slf4j
@Tag(name = "KSeF Integration", description = "KSeF API integration endpoints")
@RestController
@RequestMapping("/tenants/{tenantId}/ksef")
@RequiredArgsConstructor
public class KsefIntegrationController {

    private final KsefInvoiceService ksefInvoiceService;
    private final KsefSessionService ksefSessionService;

    @Operation(summary = "Send invoice to KSeF", 
               description = "Sends an invoice to the Polish National e-Invoice System (KSeF)")
    @PostMapping("/invoices/{invoiceId}/send")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendInvoice(
            @PathVariable Long tenantId,
            @PathVariable Long invoiceId,
            @RequestParam String sessionToken) {
        
        log.info("Sending invoice {} to KSeF for tenant {}", invoiceId, tenantId);
        
        Invoice invoice = ksefInvoiceService.sendInvoiceToKsef(invoiceId, sessionToken);
        
        Map<String, Object> result = new HashMap<>();
        result.put("invoiceId", invoice.getId());
        result.put("ksefNumber", invoice.getKsefNumber());
        result.put("status", invoice.getStatus());
        result.put("sentAt", invoice.getSentToKsefAt());
        
        return ResponseEntity.ok(ApiResponse.success("Invoice sent to KSeF successfully", result));
    }

    @Operation(summary = "Get invoice UPO from KSeF",
               description = "Retrieves the official confirmation (UPO) for an invoice from KSeF")
    @GetMapping("/invoices/{invoiceId}/upo")
    public ResponseEntity<ApiResponse<String>> getInvoiceUpo(
            @PathVariable Long tenantId,
            @PathVariable Long invoiceId,
            @RequestParam String sessionToken) {
        
        log.info("Fetching UPO for invoice {} from KSeF", invoiceId);
        
        String upo = ksefInvoiceService.getInvoiceUpo(invoiceId, sessionToken);
        
        return ResponseEntity.ok(ApiResponse.success("UPO retrieved successfully", upo));
    }

    @Operation(summary = "Open KSeF session",
               description = "Opens a new session with KSeF API")
    @PostMapping("/session/open")
    public ResponseEntity<ApiResponse<Map<String, Object>>> openSession(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "ONLINE") String sessionType,
            @RequestParam String initialToken) {
        
        log.info("Opening KSeF session for tenant {}, type: {}", tenantId, sessionType);
        
        KsefSession session = ksefSessionService.openSession(
                tenantId, 
                SessionType.valueOf(sessionType.toUpperCase()),
                initialToken
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("referenceNumber", session.getReferenceNumber());
        result.put("status", session.getStatus());
        result.put("tokenExpiresAt", session.getTokenExpiresAt());
        
        return ResponseEntity.ok(ApiResponse.success("KSeF session opened successfully", result));
    }

    @Operation(summary = "Close KSeF session",
               description = "Terminates an active KSeF session")
    @PostMapping("/session/{sessionId}/close")
    public ResponseEntity<ApiResponse<Void>> closeSession(
            @PathVariable Long tenantId,
            @PathVariable Long sessionId) {
        
        log.info("Closing KSeF session {} for tenant {}", sessionId, tenantId);
        
        ksefSessionService.closeSession(sessionId);
        
        return ResponseEntity.ok(ApiResponse.success("KSeF session closed successfully", null));
    }

    @Operation(summary = "Get active session",
               description = "Gets the currently active KSeF session for a tenant")
    @GetMapping("/session/active")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveSession(
            @PathVariable Long tenantId) {
        
        log.info("Getting active KSeF session for tenant {}", tenantId);
        
        KsefSession session = ksefSessionService.getActiveSession(tenantId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("referenceNumber", session.getReferenceNumber());
        result.put("status", session.getStatus());
        result.put("tokenExpiresAt", session.getTokenExpiresAt());
        result.put("sessionType", session.getSessionType());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
