package pl.ksef.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ksef.hub.domain.entity.Tenant;
import pl.ksef.hub.domain.repository.TenantRepository;
import pl.ksef.hub.exception.ResourceNotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Tenant> findAll(Pageable pageable) {
        return tenantRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Tenant findById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Tenant findByNip(String nip) {
        return tenantRepository.findByNip(nip)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with NIP: " + nip));
    }

    @Transactional
    public Tenant create(Tenant tenant) {
        if (tenantRepository.existsByNip(tenant.getNip())) {
            throw new IllegalArgumentException("Tenant with NIP " + tenant.getNip() + " already exists");
        }
        
        log.info("Creating new tenant: {} ({})", tenant.getName(), tenant.getNip());
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant update(Long id, Tenant tenantData) {
        Tenant tenant = findById(id);
        
        tenant.setName(tenantData.getName());
        tenant.setFullName(tenantData.getFullName());
        tenant.setEmail(tenantData.getEmail());
        tenant.setPhone(tenantData.getPhone());
        tenant.setAddress(tenantData.getAddress());
        tenant.setActive(tenantData.getActive());
        tenant.setStatus(tenantData.getStatus());
        tenant.setNotes(tenantData.getNotes());
        
        log.info("Updated tenant: {}", id);
        return tenantRepository.save(tenant);
    }

    @Transactional
    public void delete(Long id) {
        Tenant tenant = findById(id);
        log.info("Deleting tenant: {} ({})", tenant.getName(), id);
        tenantRepository.delete(tenant);
    }

    @Transactional
    public void activate(Long id) {
        Tenant tenant = findById(id);
        tenant.setActive(true);
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        tenantRepository.save(tenant);
        log.info("Activated tenant: {}", id);
    }

    @Transactional
    public void deactivate(Long id) {
        Tenant tenant = findById(id);
        tenant.setActive(false);
        tenant.setStatus(Tenant.TenantStatus.INACTIVE);
        tenantRepository.save(tenant);
        log.info("Deactivated tenant: {}", id);
    }
}
