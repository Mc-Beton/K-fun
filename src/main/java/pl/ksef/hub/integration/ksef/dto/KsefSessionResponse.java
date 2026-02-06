package pl.ksef.hub.integration.ksef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO for KSeF 2.0 Session Response
 * Based on official KSeF 2.0 API specification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KsefSessionResponse {
    
    @JsonProperty("sessionToken")
    private SessionToken sessionToken;
    
    @JsonProperty("referenceNumber")
    private String referenceNumber;
    
    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;
    
    @JsonProperty("processingCode")
    private Integer processingCode;
    
    @JsonProperty("processingDescription")
    private String processingDescription;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionToken {
        @JsonProperty("token")
        private String token;
        
        @JsonProperty("expiresIn")
        private Long expiresIn; // seconds
    }
    
    public String getToken() {
        return sessionToken != null && sessionToken.getToken() != null 
            ? sessionToken.getToken() 
            : null;
    }
}
