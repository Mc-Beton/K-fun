package pl.ksef.hub.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity reprezentujący powiadomienia systemowe (Hub, KSeF)
 */
@Entity
@Table(name = "system_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Kategoria powiadomienia: HUB lub KSEF
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    /**
     * Poziom powiadomienia: SUCCESS, ERROR, WARNING, INFO
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationLevel level;

    /**
     * Tytuł powiadomienia
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Treść powiadomienia
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Dodatkowe szczegóły (JSON lub tekst)
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * Czy powiadomienie zostało przeczytane
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Data utworzenia
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Data przeczytania
     */
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Kategoria powiadomienia
     */
    public enum NotificationCategory {
        HUB,    // Dotyczące samego Hub
        KSEF    // Dotyczące połączenia z KSeF
    }

    /**
     * Poziom ważności powiadomienia
     */
    public enum NotificationLevel {
        SUCCESS,  // Sukces
        ERROR,    // Błąd
        WARNING,  // Ostrzeżenie
        INFO      // Informacja
    }
}
