package pl.ksef.hub.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Invoice entity representing invoices sent through KSeF
 */
@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoice_tenant", columnList = "tenant_id"),
    @Index(name = "idx_invoice_ksef_number", columnList = "ksefNumber"),
    @Index(name = "idx_invoice_number", columnList = "invoiceNumber"),
    @Index(name = "idx_invoice_status", columnList = "status"),
    @Index(name = "idx_invoice_date", columnList = "invoiceDate")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String invoiceNumber;

    @Column(unique = true, length = 100)
    private String ksefNumber; // KSeF reference number

    @Column(length = 100)
    private String referenceNumber; // Session reference number

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false)
    private LocalDate saleDate;

    @Column(nullable = false, length = 10)
    private String sellerNip;

    @Column(nullable = false, length = 200)
    private String sellerName;

    @Column(nullable = false, length = 10)
    private String buyerNip;

    @Column(nullable = false, length = 200)
    private String buyerName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal vatAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal grossAmount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "PLN";

    @Lob
    @Column(columnDefinition = "TEXT")
    private String xmlContent; // Original FA XML

    @Lob
    @Column(columnDefinition = "TEXT")
    private String upoContent; // UPO XML from KSeF

    @Column(length = 500)
    private String qrCode; // Base64 encoded QR code

    @Column(length = 1000)
    private String errorMessage;

    @Column(length = 2000)
    private String notes; // Additional notes, buyer data, etc.

    private LocalDateTime sentToKsefAt;

    private LocalDateTime acceptedByKsefAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum InvoiceType {
        FA,         // Faktura
        FA_VAT,     // Faktura VAT
        FA_CORRECTIVE, // Faktura korygująca
        RR          // Rachunek/Rejestr
    }

    public enum InvoiceStatus {
        DRAFT,
        PENDING,
        SENT,
        ACCEPTED,
        REJECTED,
        ERROR
    }
}
