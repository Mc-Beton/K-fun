package pl.ksef.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ksef.hub.domain.entity.SystemNotification;
import pl.ksef.hub.domain.repository.SystemNotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serwis zarządzania powiadomieniami systemowymi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemNotificationService {

    private final SystemNotificationRepository notificationRepository;

    /**
     * Utwórz nowe powiadomienie
     */
    @Transactional
    public SystemNotification createNotification(
            SystemNotification.NotificationCategory category,
            SystemNotification.NotificationLevel level,
            String title,
            String message,
            String details
    ) {
        SystemNotification notification = SystemNotification.builder()
                .category(category)
                .level(level)
                .title(title)
                .message(message)
                .details(details)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        SystemNotification saved = notificationRepository.save(notification);
        log.info("Created notification: {} - {} - {}", category, level, title);
        return saved;
    }

    /**
     * Powiadomienie o uruchomieniu Huba
     */
    @Transactional
    public void notifyHubStarted(String details) {
        createNotification(
                SystemNotification.NotificationCategory.HUB,
                SystemNotification.NotificationLevel.SUCCESS,
                "System uruchomiony",
                "KSeF Hub został pomyślnie uruchomiony i jest gotowy do pracy.",
                details
        );
    }

    /**
     * Powiadomienie o zatrzymaniu Huba
     */
    @Transactional
    public void notifyHubStopped(String reason) {
        createNotification(
                SystemNotification.NotificationCategory.HUB,
                SystemNotification.NotificationLevel.WARNING,
                "System zatrzymany",
                "KSeF Hub został zatrzymany. Powód: " + reason,
                null
        );
    }

    /**
     * Powiadomienie o błędzie Huba
     */
    @Transactional
    public void notifyHubError(String title, String message, String details) {
        createNotification(
                SystemNotification.NotificationCategory.HUB,
                SystemNotification.NotificationLevel.ERROR,
                title,
                message,
                details
        );
    }

    /**
     * Powiadomienie informacyjne o Hubie
     */
    @Transactional
    public void notifyHubInfo(String title, String message) {
        createNotification(
                SystemNotification.NotificationCategory.HUB,
                SystemNotification.NotificationLevel.INFO,
                title,
                message,
                null
        );
    }

    /**
     * Powiadomienie o udanym połączeniu z KSeF
     */
    @Transactional
    public void notifyKsefConnected(String environment, String url) {
        String message = String.format(
                "Połączono z serwerem KSeF. Środowisko: %s, URL: %s",
                environment, url
        );
        String details = String.format(
                "{\"environment\": \"%s\", \"url\": \"%s\", \"connected_at\": \"%s\"}",
                environment, url, LocalDateTime.now()
        );
        
        createNotification(
                SystemNotification.NotificationCategory.KSEF,
                SystemNotification.NotificationLevel.SUCCESS,
                "Połączono z KSeF",
                message,
                details
        );
    }

    /**
     * Powiadomienie o nieudanym połączeniu z KSeF
     */
    @Transactional
    public void notifyKsefConnectionFailed(String reason, String details) {
        String message = String.format(
                "Nie udało się połączyć z serwerem KSeF. Powód: %s",
                reason
        );
        
        createNotification(
                SystemNotification.NotificationCategory.KSEF,
                SystemNotification.NotificationLevel.ERROR,
                "Błąd połączenia z KSeF",
                message,
                details
        );
    }

    /**
     * Powiadomienie o rozłączeniu z KSeF
     */
    @Transactional
    public void notifyKsefDisconnected(String reason) {
        createNotification(
                SystemNotification.NotificationCategory.KSEF,
                SystemNotification.NotificationLevel.WARNING,
                "Rozłączono z KSeF",
                "Połączenie z serwerem KSeF zostało przerwane. Powód: " + reason,
                null
        );
    }

    /**
     * Pobierz najnowsze powiadomienia (limit 5)
     */
    public List<SystemNotification> getRecentNotifications(int limit) {
        return notificationRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(0, limit)
        );
    }

    /**
     * Pobierz nieprzeczytane powiadomienia
     */
    public List<SystemNotification> getUnreadNotifications(int limit) {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc(
                PageRequest.of(0, limit)
        );
    }

    /**
     * Policz nieprzeczytane powiadomienia
     */
    public long countUnread() {
        return notificationRepository.countByIsReadFalse();
    }

    /**
     * Oznacz powiadomienie jako przeczytane
     */
    @Transactional
    public boolean markAsRead(Long notificationId) {
        Optional<SystemNotification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isPresent()) {
            SystemNotification notification = optNotification.get();
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.debug("Marked notification {} as read", notificationId);
            return true;
        }
        return false;
    }

    /**
     * Oznacz wszystkie powiadomienia jako przeczytane
     */
    @Transactional
    public int markAllAsRead() {
        List<SystemNotification> unread = notificationRepository.findByIsReadFalseOrderByCreatedAtDesc(
                PageRequest.of(0, 1000)
        );
        
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });
        
        notificationRepository.saveAll(unread);
        log.info("Marked {} notifications as read", unread.size());
        return unread.size();
    }

    /**
     * Pobierz powiadomienie po ID
     */
    public Optional<SystemNotification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    /**
     * Usuń stare powiadomienia (starsze niż 30 dni)
     */
    @Transactional
    public void cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Cleaned up notifications older than {} days", daysToKeep);
    }
}
