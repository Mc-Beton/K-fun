package pl.ksef.hub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private Boolean active;
    private Boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private TenantDTO tenant;
}
