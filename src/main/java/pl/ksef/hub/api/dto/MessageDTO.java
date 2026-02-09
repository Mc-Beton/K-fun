package pl.ksef.hub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO dla wiadomości XML - zgodny z frontendem
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    
    /**
     * ID wiadomości
     */
    private String id;
    
    /**
     * Timestamp
     */
    private OffsetDateTime timestamp;
    
    /**
     * Kierunek: incoming lub outgoing
     */
    private String direction;
    
    /**
     * Źródło
     */
    private String source;
    
    /**
     * Cel
     */
    private String destination;
    
    /**
     * Status: success, error, pending
     */
    private String status;
    
    /**
     * Zawartość XML
     */
    private String xmlContent;
    
    /**
     * Odpowiedź (opcjonalna)
     */
    private String response;
    
    /**
     * Komunikat błędu (opcjonalny)
     */
    private String errorMessage;
}
