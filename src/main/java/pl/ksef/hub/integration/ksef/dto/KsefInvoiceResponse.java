package pl.ksef.hub.integration.ksef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO for KSeF 2.0 Invoice Send Response
 * Based on official KSeF 2.0 API specification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KsefInvoiceResponse {
    
    @JsonProperty("elementReferenceNumber")
    private String elementReferenceNumber; // KSeF number faktury
    
    @JsonProperty("processingCode")
    private Integer processingCode;
    
    @JsonProperty("processingDescription")
    private String processingDescription;
    
    @JsonProperty("referenceNumber")
    private String referenceNumber;
    
    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;
}
