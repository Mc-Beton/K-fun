package pl.ksef.hub.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Audit log entity for tracking all operations
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_tenant", columnList = "tenant_id"),
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_created", columnList = "createdAt"),
    @Index(name = "idx_audit_entity", columnList = "entityType, entityId")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActionType actionType;

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(length = 100)
    private String entityId;

    @Column(length = 200)
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String details; // JSON with additional details

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ActionType {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        SEND_INVOICE,
        RECEIVE_UPO,
        SESSION_OPEN,
        SESSION_CLOSE,
        CERTIFICATE_UPLOAD,
        CERTIFICATE_REVOKE,
        ERROR
    }
}
