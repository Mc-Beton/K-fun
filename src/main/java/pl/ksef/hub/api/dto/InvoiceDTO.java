package pl.ksef.hub.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    
    private Long id;
    private String invoiceNumber;
    private String ksefNumber;
    private String referenceNumber;
    private String type;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate saleDate;
    
    private String sellerNip;
    private String sellerName;
    private String buyerNip;
    private String buyerName;
    
    private BigDecimal netAmount;
    private BigDecimal vatAmount;
    private BigDecimal grossAmount;
    private String currency;
    
    private String qrCode;
    private String errorMessage;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentToKsefAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime acceptedByKsefAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
