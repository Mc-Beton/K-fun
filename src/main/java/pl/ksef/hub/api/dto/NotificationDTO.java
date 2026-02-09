package pl.ksef.hub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO dla powiadomienia systemowego
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    
    private Long id;
    private String category;     // HUB, KSEF
    private String level;        // SUCCESS, ERROR, WARNING, INFO
    private String title;
    private String message;
    private String details;
    private Boolean isRead;
    private OffsetDateTime createdAt;
    private OffsetDateTime readAt;
}
