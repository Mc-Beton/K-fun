package pl.ksef.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * KSeF Hub Application - Multi-tenant KSeF Integration Platform
 * 
 * Provides comprehensive integration with Polish KSeF (Krajowy System e-Faktur)
 * including invoice management, certificate handling, and QR code generation.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class KsefHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(KsefHubApplication.class, args);
    }
}
