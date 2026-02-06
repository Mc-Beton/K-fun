package pl.ksef.hub.integration.ksef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for KSeF 2.0 Invoice Send Request
 * Based on official KSeF 2.0 API specification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KsefInvoiceRequest {
    
    @JsonProperty("invoiceHash")
    private InvoiceHash invoiceHash;
    
    @JsonProperty("invoicePayload")
    private InvoicePayload invoicePayload;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceHash {
        @JsonProperty("hashSHA")
        private HashSHA hashSHA;
        
        @JsonProperty("fileSize")
        private Long fileSize; // Size in bytes
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HashSHA {
            @JsonProperty("algorithm")
            private String algorithm; // "SHA-256"
            
            @JsonProperty("encoding")
            private String encoding; // "Base64"
            
            @JsonProperty("value")
            private String value; // Base64 encoded hash
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoicePayload {
        @JsonProperty("type")
        private String type; // "plain" or "encrypted"
        
        @JsonProperty("invoiceBody")
        private String invoiceBody; // Base64 encoded XML FA(3)
    }
}
