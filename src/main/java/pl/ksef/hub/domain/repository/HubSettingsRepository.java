package pl.ksef.hub.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ksef.hub.domain.entity.HubSettings;

import java.util.Optional;

@Repository
public interface HubSettingsRepository extends JpaRepository<HubSettings, Long> {
    
    Optional<HubSettings> findFirstByOrderByIdDesc();
}
