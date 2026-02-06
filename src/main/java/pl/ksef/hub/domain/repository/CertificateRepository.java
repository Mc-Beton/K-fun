package pl.ksef.hub.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.ksef.hub.domain.entity.Certificate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    List<Certificate> findByTenantId(Long tenantId);
    
    Optional<Certificate> findByCertificateId(String certificateId);
    
    List<Certificate> findByTenantIdAndStatus(Long tenantId, Certificate.CertificateStatus status);
    
    @Query("SELECT c FROM Certificate c WHERE c.tenant.id = :tenantId " +
           "AND c.status = 'ACTIVE' AND c.expiresAt > :now ORDER BY c.expiresAt DESC")
    List<Certificate> findActiveCertificatesByTenant(Long tenantId, LocalDateTime now);
    
    @Query("SELECT c FROM Certificate c WHERE c.expiresAt < :expiryDate AND c.status = 'ACTIVE'")
    List<Certificate> findExpiringCertificates(LocalDateTime expiryDate);
}
