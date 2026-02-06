package pl.ksef.hub.integration.ksef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO for KSeF 2.0 UPO (Urzędowe Poświadczenie Odbioru) Response
 * Based on official KSeF 2.0 API specification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KsefUpoResponse {
    
    @JsonProperty("referenceNumber")
    private String referenceNumber;
    
    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;
    
    @JsonProperty("upo")
    private String upo; // Base64 encoded UPO XML
    
    @JsonProperty("elementReferenceNumber")
    private String elementReferenceNumber; // KSeF invoice number
}
