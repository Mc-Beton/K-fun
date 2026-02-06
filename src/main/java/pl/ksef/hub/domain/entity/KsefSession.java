package pl.ksef.hub.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * KSeF Session entity for tracking sessions with KSeF API
 */
@Entity
@Table(name = "ksef_sessions", indexes = {
    @Index(name = "idx_session_tenant", columnList = "tenant_id"),
    @Index(name = "idx_session_reference", columnList = "referenceNumber"),
    @Index(name = "idx_session_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KsefSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, unique = true, length = 100)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.OPENED;

    @Column(length = 500)
    private String accessToken;

    @Column(length = 500)
    private String refreshToken;

    private LocalDateTime tokenExpiresAt;

    @Column(length = 100)
    private String contextIdentifier;

    private Integer invoiceCount;

    private Integer successfulInvoiceCount;

    private Integer failedInvoiceCount;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    @Column(length = 1000)
    private String errorMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum SessionType {
        ONLINE,     // Sesja online - pojedyncze faktury
        BATCH       // Sesja wsadowa - wiele faktur
    }

    public enum SessionStatus {
        OPENED,
        ACTIVE,
        CLOSED,
        ERROR,
        EXPIRED
    }

    @Transient
    public boolean isActive() {
        return status == SessionStatus.OPENED || status == SessionStatus.ACTIVE;
    }

    @Transient
    public boolean isTokenValid() {
        return tokenExpiresAt != null && tokenExpiresAt.isAfter(LocalDateTime.now());
    }
}
