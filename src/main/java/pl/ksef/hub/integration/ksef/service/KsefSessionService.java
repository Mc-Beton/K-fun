package pl.ksef.hub.integration.ksef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ksef.hub.domain.entity.KsefSession;
import pl.ksef.hub.domain.entity.KsefSession.SessionStatus;
import pl.ksef.hub.domain.entity.KsefSession.SessionType;
import pl.ksef.hub.domain.entity.Tenant;
import pl.ksef.hub.domain.repository.KsefSessionRepository;
import pl.ksef.hub.domain.repository.TenantRepository;
import pl.ksef.hub.integration.ksef.client.KsefApiClient;
import pl.ksef.hub.integration.ksef.dto.KsefSessionResponse;

import java.time.LocalDateTime;

/**
 * Zarządzanie sesjami KSeF
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KsefSessionService {

    private final KsefApiClient ksefApiClient;
    private final KsefSessionRepository ksefSessionRepository;
    private final TenantRepository tenantRepository;

    /**
     * Otwiera nową sesję KSeF dla klienta
     */
    @Transactional
    public KsefSession openSession(Long tenantId, SessionType sessionType, String initialToken) {
        log.info("Opening KSeF session for tenant: {}, type: {}", tenantId, sessionType);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));

        // Wywołaj API KSeF
        KsefSessionResponse response = ksefApiClient.initSession(tenant.getNip(), initialToken);

        // Zapisz sesję w bazie
        KsefSession session = KsefSession.builder()
                .tenant(tenant)
                .sessionType(sessionType)
                .status(SessionStatus.OPENED)
                .referenceNumber(response.getReferenceNumber())
                .accessToken(response.getToken()) // Updated for KSeF 2.0
                .tokenExpiresAt(LocalDateTime.now().plusSeconds(
                        response.getSessionToken() != null && response.getSessionToken().getExpiresIn() != null
                                ? response.getSessionToken().getExpiresIn()
                                : 3600L)) // Default 1 hour
                .openedAt(LocalDateTime.now())
                .build();

        KsefSession savedSession = ksefSessionRepository.save(session);
        log.info("KSeF session opened successfully. Reference: {}", savedSession.getReferenceNumber());

        return savedSession;
    }

    /**
     * Pobiera aktywną sesję dla klienta
     */
    public KsefSession getActiveSession(Long tenantId) {
        return ksefSessionRepository.findByTenantIdAndStatus(tenantId, SessionStatus.OPENED)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active KSeF session found for tenant: " + tenantId));
    }

    /**
     * Zamyka sesję KSeF
     */
    @Transactional
    public void closeSession(Long sessionId) {
        log.info("Closing KSeF session: {}", sessionId);

        KsefSession session = ksefSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        try {
            // Wywołaj API KSeF do zamknięcia sesji
            ksefApiClient.terminateSession(session.getAccessToken());

            // Zaktualizuj status w bazie
            session.setStatus(SessionStatus.CLOSED);
            session.setClosedAt(LocalDateTime.now());
            ksefSessionRepository.save(session);

            log.info("KSeF session closed successfully: {}", sessionId);

        } catch (Exception e) {
            log.error("Failed to close KSeF session: {}", sessionId, e);
            session.setStatus(SessionStatus.ERROR);
            session.setErrorMessage(e.getMessage());
            ksefSessionRepository.save(session);
            throw e;
        }
    }

    /**
     * Sprawdza czy sesja jest nadal aktywna (nie wygasła)
     */
    public boolean isSessionActive(KsefSession session) {
        if (session.getStatus() != SessionStatus.OPENED) {
            return false;
        }

        if (session.getTokenExpiresAt() != null &&
                session.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            // Token wygasł
            session.setStatus(SessionStatus.EXPIRED);
            ksefSessionRepository.save(session);
            return false;
        }

        return true;
    }

    /**
     * Pobiera token sesji (lub tworzy nową sesję jeśli nie istnieje/wygasła)
     */
    @Transactional
    public String getOrCreateSessionToken(Long tenantId, String initialToken) {
        try {
            KsefSession activeSession = getActiveSession(tenantId);
            
            if (isSessionActive(activeSession)) {
                log.debug("Using existing active session: {}", activeSession.getReferenceNumber());
                return activeSession.getAccessToken();
            }
        } catch (RuntimeException e) {
            log.debug("No active session found, creating new one");
        }

        // Utwórz nową sesję
        KsefSession newSession = openSession(tenantId, SessionType.ONLINE, initialToken);
        return newSession.getAccessToken();
    }
}
