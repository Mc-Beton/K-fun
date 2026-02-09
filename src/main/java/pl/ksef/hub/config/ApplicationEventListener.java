package pl.ksef.hub.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.ksef.hub.service.SystemNotificationService;

import java.time.Duration;
import java.time.Instant;

/**
 * Listener dla zdarzeń cyklu życia aplikacji
 * Tworzy powiadomienia o uruchomieniu/zatrzymaniu Huba
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEventListener {

    private final SystemNotificationService notificationService;
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    private Instant startupTime;

    /**
     * Wywoływane gdy aplikacja jest gotowa do obsługi requestów
     */
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        startupTime = Instant.now();
        long startupTimeMs = Duration.between(
                Instant.ofEpochMilli(event.getApplicationContext().getStartupDate()),
                startupTime
        ).toMillis();
        
        String details = String.format(
                "{\"application\": \"%s\", \"profile\": \"%s\", \"startup_time_ms\": %d, \"java_version\": \"%s\"}",
                applicationName, activeProfile, startupTimeMs, System.getProperty("java.version")
        );
        
        notificationService.notifyHubStarted(details);
        log.info("Application started notification created");
    }

    /**
     * Wywoływane gdy kontekst aplikacji jest zamykany
     */
    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        String reason = "Normalne zatrzymanie aplikacji";
        
        if (startupTime != null) {
            long uptimeSeconds = Duration.between(startupTime, Instant.now()).toSeconds();
            reason = String.format("Normalne zatrzymanie (czas pracy: %d sekund)", uptimeSeconds);
        }
        
        notificationService.notifyHubStopped(reason);
        log.info("Application stopped notification created");
    }
}
