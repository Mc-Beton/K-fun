package pl.ksef.hub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {
    
    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;
    
    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;
    
    @NotNull(message = "Sale date is required")
    private LocalDate saleDate;
    
    @NotBlank(message = "Seller NIP is required")
    private String sellerNip;
    
    @NotBlank(message = "Seller name is required")
    private String sellerName;
    
    @NotBlank(message = "Buyer NIP is required")
    private String buyerNip;
    
    @NotBlank(message = "Buyer name is required")
    private String buyerName;
    
    @NotNull(message = "Net amount is required")
    private BigDecimal netAmount;
    
    @NotNull(message = "VAT amount is required")
    private BigDecimal vatAmount;
    
    @NotNull(message = "Gross amount is required")
    private BigDecimal grossAmount;
    
    private String currency = "PLN";
    
    @NotBlank(message = "XML content is required")
    private String xmlContent;
}
