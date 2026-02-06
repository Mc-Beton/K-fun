package pl.ksef.hub.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Certificate entity for storing KSeF certificates
 */
@Entity
@Table(name = "certificates", indexes = {
    @Index(name = "idx_cert_tenant", columnList = "tenant_id"),
    @Index(name = "idx_cert_status", columnList = "status"),
    @Index(name = "idx_cert_expiry", columnList = "expiresAt")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, unique = true, length = 100)
    private String certificateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CertificateType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.PENDING;

    @Column(nullable = false, length = 200)
    private String subjectDn;

    @Column(nullable = false, length = 200)
    private String issuerDn;

    @Column(length = 100)
    private String serialNumber;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String certificateData; // Base64 encoded certificate

    @Lob
    @Column(columnDefinition = "TEXT")
    private String privateKeyData; // Encrypted private key

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(length = 500)
    private String fingerprint;

    @Column(length = 1000)
    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum CertificateType {
        QUALIFIED,      // Kwalifikowany podpis elektroniczny
        KSEF_TOKEN,     // Token KSeF
        ORGANIZATION    // Certyfikat organizacji
    }

    public enum CertificateStatus {
        PENDING,
        ACTIVE,
        EXPIRED,
        REVOKED,
        SUSPENDED
    }

    @Transient
    public boolean isValid() {
        return status == CertificateStatus.ACTIVE 
            && expiresAt.isAfter(LocalDateTime.now());
    }
}
