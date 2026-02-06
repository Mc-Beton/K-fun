package pl.ksef.hub.integration.ksef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for KSeF 2.0 Session Initialization Request
 * Based on official KSeF 2.0 API specification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KsefSessionRequest {
    
    @JsonProperty("contextIdentifier")
    private ContextIdentifier contextIdentifier;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextIdentifier {
        @JsonProperty("type")
        private String type; // "onip" for NIP-based authentication
        
        @JsonProperty("identifier")
        private String identifier; // NIP podatnika
    }
}
