package pl.ksef.hub.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ksef.hub.api.dto.NotificationDTO;
import pl.ksef.hub.domain.entity.SystemNotification;
import pl.ksef.hub.service.SystemNotificationService;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kontroler dla powiadomień systemowych
 */
@Slf4j
@Tag(name = "Notifications", description = "System notifications endpoints")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final SystemNotificationService notificationService;

    @Operation(summary = "Get recent notifications")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getRecentNotifications(
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.debug("Fetching {} recent notifications", limit);
        
        List<NotificationDTO> notifications = notificationService.getRecentNotifications(limit)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.debug("Fetching {} unread notifications", limit);
        
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(limit)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread count")
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.countUnread();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @Operation(summary = "Get notification by ID")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        log.debug("Fetching notification {}", id);
        
        return notificationService.getNotificationById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Mark notification as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        log.debug("Marking notification {} as read", id);
        
        boolean success = notificationService.markAsRead(id);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead() {
        log.debug("Marking all notifications as read");
        
        int count = notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of("markedCount", count));
    }

    /**
     * Konwertuje SystemNotification na NotificationDTO
     */
    private NotificationDTO toDTO(SystemNotification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .category(notification.getCategory().name())
                .level(notification.getLevel().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .details(notification.getDetails())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt() != null 
                        ? notification.getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime()
                        : null)
                .readAt(notification.getReadAt() != null 
                        ? notification.getReadAt().atZone(ZoneId.systemDefault()).toOffsetDateTime()
                        : null)
                .build();
    }
}
