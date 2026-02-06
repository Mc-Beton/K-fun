package pl.ksef.hub.integration.ksef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO for KSeF 2.0 Error Response
 * Based on official KSeF 2.0 API specification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KsefErrorResponse {
    
    @JsonProperty("exception")
    private ExceptionDetails exception;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExceptionDetails {
        @JsonProperty("serviceCode")
        private String serviceCode; // HTTP status code
        
        @JsonProperty("serviceCtx")
        private String serviceCtx;
        
        @JsonProperty("serviceName")
        private String serviceName; // "KSeF"
        
        @JsonProperty("timestamp")
        private OffsetDateTime timestamp;
        
        @JsonProperty("referenceNumber")
        private String referenceNumber;
        
        @JsonProperty("exceptionDetailList")
        private List<ExceptionDetail> exceptionDetailList;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ExceptionDetail {
            @JsonProperty("exceptionCode")
            private Integer exceptionCode;
            
            @JsonProperty("exceptionDescription")
            private String exceptionDescription;
        }
    }
}
