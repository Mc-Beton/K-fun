package pl.ksef.hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ksef.hub.api.dto.AuthRequest;
import pl.ksef.hub.api.dto.AuthResponse;
import pl.ksef.hub.api.dto.TenantDTO;
import pl.ksef.hub.api.dto.UserDTO;
import pl.ksef.hub.domain.entity.User;
import pl.ksef.hub.domain.repository.UserRepository;
import pl.ksef.hub.security.JwtService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        var jwtToken = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .authorities("ROLE_" + user.getRole().name())
                        .build()
        );
        var refreshToken = jwtService.generateRefreshToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .authorities("ROLE_" + user.getRole().name())
                        .build()
        );

        log.info("User {} authenticated successfully", user.getEmail());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(toUserDTO(user))
                .build();
    }

    private UserDTO toUserDTO(User user) {
        TenantDTO tenantDTO = TenantDTO.builder()
                .id(user.getTenant().getId())
                .nip(user.getTenant().getNip())
                .name(user.getTenant().getName())
                .build();

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .tenant(tenantDTO)
                .build();
    }
}
