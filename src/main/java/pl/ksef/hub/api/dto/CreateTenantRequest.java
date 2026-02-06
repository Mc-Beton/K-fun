package pl.ksef.hub.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {
    
    @NotBlank(message = "NIP is required")
    @Pattern(regexp = "\\d{10}", message = "NIP must be 10 digits")
    private String nip;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String fullName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    private String address;
    private String notes;
}
