package pl.ksef.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ksef.hub.domain.entity.Certificate;
import pl.ksef.hub.domain.entity.Tenant;
import pl.ksef.hub.domain.repository.CertificateRepository;
import pl.ksef.hub.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final TenantService tenantService;

    @Transactional(readOnly = true)
    public List<Certificate> findByTenant(Long tenantId) {
        return certificateRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public Certificate findById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Certificate findByCertificateId(String certificateId) {
        return certificateRepository.findByCertificateId(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with ID: " + certificateId));
    }

    @Transactional(readOnly = true)
    public List<Certificate> findActiveCertificates(Long tenantId) {
        return certificateRepository.findActiveCertificatesByTenant(tenantId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Certificate> findExpiringCertificates(int daysAhead) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(daysAhead);
        return certificateRepository.findExpiringCertificates(expiryDate);
    }

    @Transactional
    public Certificate create(Long tenantId, Certificate certificate) {
        Tenant tenant = tenantService.findById(tenantId);
        certificate.setTenant(tenant);
        certificate.setStatus(Certificate.CertificateStatus.PENDING);
        
        log.info("Creating new certificate for tenant: {}", tenantId);
        return certificateRepository.save(certificate);
    }

    @Transactional
    public Certificate update(Long id, Certificate certificateData) {
        Certificate certificate = findById(id);
        
        certificate.setStatus(certificateData.getStatus());
        certificate.setNotes(certificateData.getNotes());
        
        log.info("Updated certificate: {}", id);
        return certificateRepository.save(certificate);
    }

    @Transactional
    public void activate(Long id) {
        Certificate certificate = findById(id);
        certificate.setStatus(Certificate.CertificateStatus.ACTIVE);
        certificateRepository.save(certificate);
        log.info("Activated certificate: {}", id);
    }

    @Transactional
    public void revoke(Long id) {
        Certificate certificate = findById(id);
        certificate.setStatus(Certificate.CertificateStatus.REVOKED);
        certificateRepository.save(certificate);
        log.info("Revoked certificate: {}", id);
    }

    @Transactional
    public void delete(Long id) {
        Certificate certificate = findById(id);
        log.info("Deleting certificate: {}", id);
        certificateRepository.delete(certificate);
    }

    /**
     * Check and update expired certificates
     */
    @Transactional
    public void updateExpiredCertificates() {
        List<Certificate> activeCerts = certificateRepository.findByTenantIdAndStatus(
                null, Certificate.CertificateStatus.ACTIVE);
        
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = 0;
        
        for (Certificate cert : activeCerts) {
            if (cert.getExpiresAt().isBefore(now)) {
                cert.setStatus(Certificate.CertificateStatus.EXPIRED);
                certificateRepository.save(cert);
                updatedCount++;
                log.info("Marked certificate {} as expired", cert.getId());
            }
        }
        
        if (updatedCount > 0) {
            log.info("Updated {} expired certificates", updatedCount);
        }
    }
}
