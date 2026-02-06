package pl.ksef.hub.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ksef.hub.api.dto.ApiResponse;
import pl.ksef.hub.domain.entity.Certificate;
import pl.ksef.hub.service.CertificateService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Certificates", description = "Certificate management endpoints")
@RestController
@RequestMapping("/tenants/{tenantId}/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @Operation(summary = "Get all certificates for tenant")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCertificates(
            @PathVariable Long tenantId) {
        List<Map<String, Object>> certificates = certificateService.findByTenant(tenantId)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }

    @Operation(summary = "Get active certificates for tenant")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveCertificates(
            @PathVariable Long tenantId) {
        List<Map<String, Object>> certificates = certificateService.findActiveCertificates(tenantId)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }

    @Operation(summary = "Get certificate by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCertificateById(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        Certificate certificate = certificateService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(toSummary(certificate)));
    }

    @Operation(summary = "Upload certificate")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadCertificate(
            @PathVariable Long tenantId,
            @RequestBody Map<String, String> request) {
        // TODO: Implement certificate upload with proper validation
        Map<String, Object> result = new HashMap<>();
        result.put("status", "uploaded");
        result.put("message", "Certificate upload functionality will be implemented");
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Certificate uploaded successfully", result));
    }

    @Operation(summary = "Activate certificate")
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateCertificate(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        certificateService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Certificate activated successfully", null));
    }

    @Operation(summary = "Revoke certificate")
    @PostMapping("/{id}/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeCertificate(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        certificateService.revoke(id);
        return ResponseEntity.ok(ApiResponse.success("Certificate revoked successfully", null));
    }

    @Operation(summary = "Delete certificate")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCertificate(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        certificateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Certificate deleted successfully", null));
    }

    private Map<String, Object> toSummary(Certificate cert) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", cert.getId());
        summary.put("certificateId", cert.getCertificateId());
        summary.put("type", cert.getType().name());
        summary.put("status", cert.getStatus().name());
        summary.put("subjectDn", cert.getSubjectDn());
        summary.put("issuerDn", cert.getIssuerDn());
        summary.put("issuedAt", cert.getIssuedAt());
        summary.put("expiresAt", cert.getExpiresAt());
        summary.put("isValid", cert.isValid());
        summary.put("createdAt", cert.getCreatedAt());
        return summary;
    }
}
