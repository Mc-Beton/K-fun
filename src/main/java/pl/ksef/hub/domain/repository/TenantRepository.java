package pl.ksef.hub.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ksef.hub.domain.entity.Tenant;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    Optional<Tenant> findByNip(String nip);
    
    boolean existsByNip(String nip);
}
