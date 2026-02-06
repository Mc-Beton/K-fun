package pl.ksef.hub.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ksef.hub.api.dto.ApiResponse;
import pl.ksef.hub.api.dto.CreateTenantRequest;
import pl.ksef.hub.api.dto.TenantDTO;
import pl.ksef.hub.domain.entity.Tenant;
import pl.ksef.hub.service.TenantService;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Tenants", description = "Tenant management endpoints")
@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "Get all tenants")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TenantDTO>>> getAllTenants(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TenantDTO> tenants = tenantService.findAll(pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(ApiResponse.success(tenants));
    }

    @Operation(summary = "Get tenant by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantDTO>> getTenantById(@PathVariable Long id) {
        Tenant tenant = tenantService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(toDTO(tenant)));
    }

    @Operation(summary = "Get tenant by NIP")
    @GetMapping("/nip/{nip}")
    public ResponseEntity<ApiResponse<TenantDTO>> getTenantByNip(@PathVariable String nip) {
        Tenant tenant = tenantService.findByNip(nip);
        return ResponseEntity.ok(ApiResponse.success(toDTO(tenant)));
    }

    @Operation(summary = "Create new tenant")
    @PostMapping
    public ResponseEntity<ApiResponse<TenantDTO>> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = Tenant.builder()
                .nip(request.getNip())
                .name(request.getName())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .notes(request.getNotes())
                .build();
        
        Tenant created = tenantService.create(tenant);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tenant created successfully", toDTO(created)));
    }

    @Operation(summary = "Update tenant")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantDTO>> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .notes(request.getNotes())
                .build();
        
        Tenant updated = tenantService.update(id, tenant);
        return ResponseEntity.ok(ApiResponse.success("Tenant updated successfully", toDTO(updated)));
    }

    @Operation(summary = "Activate tenant")
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateTenant(@PathVariable Long id) {
        tenantService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant activated successfully", null));
    }

    @Operation(summary = "Deactivate tenant")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateTenant(@PathVariable Long id) {
        tenantService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant deactivated successfully", null));
    }

    @Operation(summary = "Delete tenant")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(@PathVariable Long id) {
        tenantService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant deleted successfully", null));
    }

    private TenantDTO toDTO(Tenant tenant) {
        return TenantDTO.builder()
                .id(tenant.getId())
                .nip(tenant.getNip())
                .name(tenant.getName())
                .fullName(tenant.getFullName())
                .email(tenant.getEmail())
                .phone(tenant.getPhone())
                .address(tenant.getAddress())
                .active(tenant.getActive())
                .status(tenant.getStatus().name())
                .notes(tenant.getNotes())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
