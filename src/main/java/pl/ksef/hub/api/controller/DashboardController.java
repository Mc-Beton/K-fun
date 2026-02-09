package pl.ksef.hub.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ksef.hub.api.dto.HubStatusDTO;
import pl.ksef.hub.api.dto.MessageDTO;
import pl.ksef.hub.domain.entity.Invoice;
import pl.ksef.hub.domain.repository.InvoiceRepository;
import pl.ksef.hub.integration.ksef.client.KsefApiClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kontroler dla Dashboard (frontend)
 * Zapewnia endpointy do monitorowania i statusu systemu
 */
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard monitoring endpoints")
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final InvoiceRepository invoiceRepository;
    private final KsefApiClient ksefApiClient;

    @Operation(summary = "Get system status")
    @GetMapping("/status")
    public ResponseEntity<HubStatusDTO> getStatus() {
        log.debug("Fetching system status");
        
        // Policz faktury
        long totalInvoices = invoiceRepository.count();
        long sentToKsef = invoiceRepository.countByStatus(Invoice.InvoiceStatus.SENT);
        long receivedMessages = totalInvoices; // Wszystkie faktury to odebrane wiadomości
        
        HubStatusDTO status = HubStatusDTO.builder()
                .online(true)
                .ksefConnected(checkKsefConnection())
                .receivedMessagesCount((int) receivedMessages)
                .sentToKsefCount((int) sentToKsef)
                .lastUpdate(OffsetDateTime.now())
                .build();
        
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Get recent messages (invoices as XML)")
    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @RequestParam(defaultValue = "50") int limit) {
        log.debug("Fetching last {} messages", limit);
        
        // Pobierz ostatnie faktury
        List<Invoice> invoices = invoiceRepository.findAll(
                PageRequest.of(0, limit)
        ).getContent();
        
        // Przekształć na MessageDTO
        List<MessageDTO> messages = invoices.stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(messages);
    }

    /**
     * Sprawdza połączenie z KSeF poprzez wywołanie publicznego endpointu /common/Status
     */
    private Boolean checkKsefConnection() {
        try {
            return ksefApiClient.checkApiStatus();
        } catch (Exception e) {
            log.warn("Failed to check KSeF connection: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Konwertuje Invoice na MessageDTO dla frontendu
     */
    private MessageDTO toMessageDTO(Invoice invoice) {
        String direction = invoice.getStatus() == Invoice.InvoiceStatus.SENT 
                ? "outgoing" : "incoming";
        
        String status = mapInvoiceStatus(invoice.getStatus());
        
        String source = direction.equals("incoming") 
                ? invoice.getSellerName() 
                : "KSeF Hub";
        
        String destination = direction.equals("incoming") 
                ? "KSeF Hub" 
                : "KSeF API";
        
        // Konwersja LocalDateTime na OffsetDateTime
        OffsetDateTime timestamp = invoice.getCreatedAt() != null 
                ? invoice.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime()
                : OffsetDateTime.now();
        
        return MessageDTO.builder()
                .id(invoice.getId().toString())
                .timestamp(timestamp)
                .direction(direction)
                .source(source)
                .destination(destination)
                .status(status)
                .xmlContent(invoice.getXmlContent() != null ? invoice.getXmlContent() : "")
                .response(invoice.getReferenceNumber() != null ? invoice.getReferenceNumber() : null)
                .errorMessage(invoice.getErrorMessage())
                .build();
    }

    /**
     * Mapuje status faktury na status wiadomości
     */
    private String mapInvoiceStatus(Invoice.InvoiceStatus status) {
        if (status == null) {
            return "pending";
        }
        
        return switch (status) {
            case SENT -> "success";
            case ERROR -> "error";
            default -> "pending";
        };
    }
}
