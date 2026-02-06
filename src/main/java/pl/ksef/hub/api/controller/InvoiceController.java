package pl.ksef.hub.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ksef.hub.api.dto.ApiResponse;
import pl.ksef.hub.api.dto.CreateInvoiceRequest;
import pl.ksef.hub.api.dto.InvoiceDTO;
import pl.ksef.hub.domain.entity.Invoice;
import pl.ksef.hub.service.InvoiceService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Invoices", description = "Invoice management endpoints")
@RestController
@RequestMapping("/tenants/{tenantId}/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "Get all invoices for tenant")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InvoiceDTO>>> getInvoices(
            @PathVariable Long tenantId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InvoiceDTO> invoices = invoiceService.findByTenant(tenantId, pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @Operation(summary = "Search invoices")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<InvoiceDTO>>> searchInvoices(
            @PathVariable Long tenantId,
            @RequestParam(required = false) Invoice.InvoiceStatus status,
            @RequestParam(required = false) String invoiceNumber,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InvoiceDTO> invoices = invoiceService.searchInvoices(tenantId, status, invoiceNumber, pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @Operation(summary = "Get invoices by date range")
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getInvoicesByDateRange(
            @PathVariable Long tenantId,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<InvoiceDTO> invoices = invoiceService.findByDateRange(tenantId, startDate, endDate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @Operation(summary = "Get invoice by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getInvoiceById(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        Invoice invoice = invoiceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(toDTO(invoice)));
    }

    @Operation(summary = "Get invoice by KSeF number")
    @GetMapping("/ksef/{ksefNumber}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getInvoiceByKsefNumber(
            @PathVariable Long tenantId,
            @PathVariable String ksefNumber) {
        Invoice invoice = invoiceService.findByKsefNumber(ksefNumber);
        return ResponseEntity.ok(ApiResponse.success(toDTO(invoice)));
    }

    @Operation(summary = "Create new invoice")
    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDTO>> createInvoice(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateInvoiceRequest request) {
        Invoice invoice = Invoice.builder()
                .invoiceNumber(request.getInvoiceNumber())
                .type(Invoice.InvoiceType.FA_VAT)
                .invoiceDate(request.getInvoiceDate())
                .saleDate(request.getSaleDate())
                .sellerNip(request.getSellerNip())
                .sellerName(request.getSellerName())
                .buyerNip(request.getBuyerNip())
                .buyerName(request.getBuyerName())
                .netAmount(request.getNetAmount())
                .vatAmount(request.getVatAmount())
                .grossAmount(request.getGrossAmount())
                .currency(request.getCurrency())
                .xmlContent(request.getXmlContent())
                .build();
        
        Invoice created = invoiceService.create(tenantId, invoice);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created successfully", toDTO(created)));
    }

    @Operation(summary = "Update invoice")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> updateInvoice(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            @Valid @RequestBody CreateInvoiceRequest request) {
        Invoice invoice = Invoice.builder()
                .sellerName(request.getSellerName())
                .buyerName(request.getBuyerName())
                .netAmount(request.getNetAmount())
                .vatAmount(request.getVatAmount())
                .grossAmount(request.getGrossAmount())
                .build();
        
        Invoice updated = invoiceService.update(id, invoice);
        return ResponseEntity.ok(ApiResponse.success("Invoice updated successfully", toDTO(updated)));
    }

    @Operation(summary = "Delete invoice")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted successfully", null));
    }

    @Operation(summary = "Get invoice QR code")
    @GetMapping("/{id}/qrcode")
    public ResponseEntity<ApiResponse<String>> getInvoiceQRCode(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        Invoice invoice = invoiceService.findById(id);
        if (invoice.getQrCode() == null) {
            return ResponseEntity.ok(ApiResponse.error("QR code not available for this invoice"));
        }
        return ResponseEntity.ok(ApiResponse.success(invoice.getQrCode()));
    }

    private InvoiceDTO toDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .ksefNumber(invoice.getKsefNumber())
                .referenceNumber(invoice.getReferenceNumber())
                .type(invoice.getType().name())
                .status(invoice.getStatus().name())
                .invoiceDate(invoice.getInvoiceDate())
                .saleDate(invoice.getSaleDate())
                .sellerNip(invoice.getSellerNip())
                .sellerName(invoice.getSellerName())
                .buyerNip(invoice.getBuyerNip())
                .buyerName(invoice.getBuyerName())
                .netAmount(invoice.getNetAmount())
                .vatAmount(invoice.getVatAmount())
                .grossAmount(invoice.getGrossAmount())
                .currency(invoice.getCurrency())
                .qrCode(invoice.getQrCode())
                .errorMessage(invoice.getErrorMessage())
                .sentToKsefAt(invoice.getSentToKsefAt())
                .acceptedByKsefAt(invoice.getAcceptedByKsefAt())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
