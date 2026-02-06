package pl.ksef.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ksef.hub.domain.entity.Invoice;
import pl.ksef.hub.domain.entity.Tenant;
import pl.ksef.hub.domain.repository.InvoiceRepository;
import pl.ksef.hub.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TenantService tenantService;
    private final QRCodeService qrCodeService;

    @Transactional(readOnly = true)
    public Page<Invoice> findByTenant(Long tenantId, Pageable pageable) {
        return invoiceRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Invoice findByKsefNumber(String ksefNumber) {
        return invoiceRepository.findByKsefNumber(ksefNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with KSeF number: " + ksefNumber));
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByDateRange(Long tenantId, LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> searchInvoices(Long tenantId, Invoice.InvoiceStatus status, 
                                       String invoiceNumber, Pageable pageable) {
        return invoiceRepository.searchInvoices(tenantId, status, invoiceNumber, pageable);
    }

    @Transactional
    public Invoice create(Long tenantId, Invoice invoice) {
        Tenant tenant = tenantService.findById(tenantId);
        
        if (invoiceRepository.existsByTenantIdAndInvoiceNumber(tenantId, invoice.getInvoiceNumber())) {
            throw new IllegalArgumentException("Invoice with number " + invoice.getInvoiceNumber() + " already exists");
        }
        
        invoice.setTenant(tenant);
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        
        log.info("Creating new invoice: {} for tenant: {}", invoice.getInvoiceNumber(), tenantId);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice update(Long id, Invoice invoiceData) {
        Invoice invoice = findById(id);
        
        // Update only mutable fields
        invoice.setSellerName(invoiceData.getSellerName());
        invoice.setBuyerName(invoiceData.getBuyerName());
        invoice.setNetAmount(invoiceData.getNetAmount());
        invoice.setVatAmount(invoiceData.getVatAmount());
        invoice.setGrossAmount(invoiceData.getGrossAmount());
        
        log.info("Updated invoice: {}", id);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void updateStatus(Long id, Invoice.InvoiceStatus status) {
        Invoice invoice = findById(id);
        invoice.setStatus(status);
        
        if (status == Invoice.InvoiceStatus.SENT) {
            invoice.setSentToKsefAt(LocalDateTime.now());
        } else if (status == Invoice.InvoiceStatus.ACCEPTED) {
            invoice.setAcceptedByKsefAt(LocalDateTime.now());
        }
        
        invoiceRepository.save(invoice);
        log.info("Updated invoice {} status to: {}", id, status);
    }

    @Transactional
    public void updateKsefNumber(Long id, String ksefNumber) {
        Invoice invoice = findById(id);
        invoice.setKsefNumber(ksefNumber);
        
        // Generate QR code for the invoice
        try {
            String qrCodeData = generateQRCodeData(invoice);
            String qrCodeBase64 = qrCodeService.generateQRCodeBase64(qrCodeData);
            invoice.setQrCode(qrCodeBase64);
        } catch (Exception e) {
            log.error("Failed to generate QR code for invoice {}", id, e);
        }
        
        invoiceRepository.save(invoice);
        log.info("Updated invoice {} with KSeF number: {}", id, ksefNumber);
    }

    @Transactional
    public void updateUpo(Long id, String upoContent) {
        Invoice invoice = findById(id);
        invoice.setUpoContent(upoContent);
        invoiceRepository.save(invoice);
        log.info("Updated invoice {} with UPO", id);
    }

    @Transactional
    public void delete(Long id) {
        Invoice invoice = findById(id);
        
        if (invoice.getStatus() != Invoice.InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Cannot delete invoice that has been sent to KSeF");
        }
        
        log.info("Deleting invoice: {}", id);
        invoiceRepository.delete(invoice);
    }

    private String generateQRCodeData(Invoice invoice) {
        // Format: numer_faktury|data_wystawienia|kwota_brutto|nip_sprzedawcy|nip_nabywcy|ksef_number
        return String.format("%s|%s|%.2f|%s|%s|%s",
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getGrossAmount(),
                invoice.getSellerNip(),
                invoice.getBuyerNip(),
                invoice.getKsefNumber());
    }
}
