package pl.ksef.hub.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ksef.hub.domain.entity.KsefSession;

import java.util.List;
import java.util.Optional;

@Repository
public interface KsefSessionRepository extends JpaRepository<KsefSession, Long> {
    
    List<KsefSession> findByTenantId(Long tenantId);
    
    Optional<KsefSession> findByReferenceNumber(String referenceNumber);
    
    List<KsefSession> findByTenantIdAndStatus(Long tenantId, KsefSession.SessionStatus status);
    
    Optional<KsefSession> findFirstByTenantIdAndStatusOrderByCreatedAtDesc(
        Long tenantId, KsefSession.SessionStatus status);
}
