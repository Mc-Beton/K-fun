package pl.ksef.hub.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.ksef.hub.domain.entity.SystemNotification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository dla powiadomień systemowych
 */
@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {

    /**
     * Znajdź najnowsze nieprzeczytane powiadomienia
     */
    List<SystemNotification> findByIsReadFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Znajdź najnowsze powiadomienia (przeczytane i nieprzeczytane)
     */
    List<SystemNotification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Policz nieprzeczytane powiadomienia
     */
    long countByIsReadFalse();

    /**
     * Znajdź powiadomienia według kategorii
     */
    List<SystemNotification> findByCategoryOrderByCreatedAtDesc(
            SystemNotification.NotificationCategory category, 
            Pageable pageable
    );

    /**
     * Znajdź powiadomienia utworzone po określonej dacie
     */
    List<SystemNotification> findByCreatedAtAfterOrderByCreatedAtDesc(
            LocalDateTime after, 
            Pageable pageable
    );

    /**
     * Usuń stare powiadomienia (starsze niż podana data)
     */
    void deleteByCreatedAtBefore(LocalDateTime before);
}
