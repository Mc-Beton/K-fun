package pl.ksef.hub.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ksef.hub.domain.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByTenantId(Long tenantId, Pageable pageable);
    
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
    
    List<AuditLog> findByTenantIdAndCreatedAtBetween(
        Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);
}
