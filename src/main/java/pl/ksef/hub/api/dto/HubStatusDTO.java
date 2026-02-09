package pl.ksef.hub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO dla statusu Hub - zgodny z frontendem
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubStatusDTO {
    
    /**
     * Czy aplikacja jest online
     */
    private Boolean online;
    
    /**
     * Czy połączenie z KSeF działa
     */
    private Boolean ksefConnected;
    
    /**
     * Liczba odebranych wiadomości/faktur
     */
    private Integer receivedMessagesCount;
    
    /**
     * Liczba wysłanych do KSeF
     */
    private Integer sentToKsefCount;
    
    /**
     * Ostatnia aktualizacja
     */
    private OffsetDateTime lastUpdate;
}
