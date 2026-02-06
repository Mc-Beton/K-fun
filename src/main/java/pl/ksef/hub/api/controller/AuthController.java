package pl.ksef.hub.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ksef.hub.api.dto.ApiResponse;
import pl.ksef.hub.api.dto.AuthRequest;
import pl.ksef.hub.api.dto.AuthResponse;
import pl.ksef.hub.service.AuthService;

@Tag(name = "Authentication", description = "Authentication endpoints")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT is stateless, logout is handled on client side
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
