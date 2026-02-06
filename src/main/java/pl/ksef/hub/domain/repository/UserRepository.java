package pl.ksef.hub.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ksef.hub.domain.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByTenantId(Long tenantId);
    
    boolean existsByEmail(String email);
}
