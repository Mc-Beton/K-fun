package pl.ksef.hub.integration.ksef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.ksef.hub.integration.ksef.client.KsefApiClient;
import pl.ksef.hub.integration.ksef.dto.KsefSessionResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serwis do zarządzania tokenami autoryzacyjnymi KSeF
 * Obsługuje generowanie challengeTokenów i zarządzanie sesjami
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KsefAuthService {

    private final KsefApiClient ksefApiClient;
    
    // Cache tokenów sesji (w produkcji użyj Redis lub innego cache)
    private final Map<String, SessionCache> sessionTokens = new ConcurrentHashMap<>();

    /**
     * Generuje token autoryzacyjny (SHA-256 hash) z tokena początkowego
     * 
     * @param initialToken Token otrzymany z KSeF (np. z portalu podatnika)
     * @return Token autoryzacyjny (challenge token) do użycia w API
     */
    public String generateAuthorizationToken(String initialToken) {
        try {
            log.debug("Generating authorization token from initial token");
            
            // KSeF wymaga: SHA-256(initialToken)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(initialToken.getBytes(StandardCharsets.UTF_8));
            String authToken = Base64.getEncoder().encodeToString(hash);
            
            log.debug("Authorization token generated successfully");
            return authToken;
            
        } catch (Exception e) {
            log.error("Failed to generate authorization token", e);
            throw new RuntimeException("Failed to generate authorization token: " + e.getMessage(), e);
        }
    }

    /**
     * Inicjalizuje sesję i zwraca token sesyjny
     * Cache'uje token na 10 minut (typowy czas życia sesji KSeF)
     */
    public String initializeSession(String nip, String initialToken) {
        String cacheKey = nip + ":" + initialToken;
        
        // Sprawdź cache
        SessionCache cached = sessionTokens.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("Using cached session token for NIP: {}", nip);
            return cached.getSessionToken();
        }

        // Generuj token autoryzacyjny
        String authToken = generateAuthorizationToken(initialToken);
        
        // Inicjalizuj sesję w KSeF
        log.info("Initializing new KSeF session for NIP: {}", nip);
        KsefSessionResponse sessionResponse = ksefApiClient.initSession(nip, authToken);
        
        String sessionToken = sessionResponse.getSessionToken().getToken();
        
        // Zapisz w cache (10 minut)
        sessionTokens.put(cacheKey, new SessionCache(sessionToken, LocalDateTime.now().plusMinutes(10)));
        
        log.info("KSeF session initialized successfully. Session token obtained.");
        return sessionToken;
    }

    /**
     * Kończy sesję KSeF
     */
    public void terminateSession(String sessionToken) {
        try {
            ksefApiClient.terminateSession(sessionToken);
            
            // Usuń z cache
            sessionTokens.entrySet().removeIf(entry -> 
                entry.getValue().getSessionToken().equals(sessionToken)
            );
            
            log.info("KSeF session terminated successfully");
        } catch (Exception e) {
            log.error("Failed to terminate KSeF session", e);
            // Nie rzucaj wyjątku - sesja i tak wygaśnie
        }
    }

    /**
     * Sprawdza czy token sesji jest ważny (w cache)
     */
    public boolean isSessionValid(String nip, String initialToken) {
        String cacheKey = nip + ":" + initialToken;
        SessionCache cached = sessionTokens.get(cacheKey);
        return cached != null && !cached.isExpired();
    }

    /**
     * Pobiera token sesji z cache lub tworzy nowy
     */
    public String getOrCreateSessionToken(String nip, String initialToken) {
        String cacheKey = nip + ":" + initialToken;
        SessionCache cached = sessionTokens.get(cacheKey);
        
        if (cached != null && !cached.isExpired()) {
            return cached.getSessionToken();
        }
        
        return initializeSession(nip, initialToken);
    }

    /**
     * Czyści wygasłe sesje z cache (wywoływane okresowo)
     */
    public void cleanupExpiredSessions() {
        int before = sessionTokens.size();
        sessionTokens.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int after = sessionTokens.size();
        
        if (before > after) {
            log.info("Cleaned up {} expired sessions from cache", before - after);
        }
    }

    // === Inner class dla cache ===
    
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class SessionCache {
        private String sessionToken;
        private LocalDateTime expiresAt;
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
}
