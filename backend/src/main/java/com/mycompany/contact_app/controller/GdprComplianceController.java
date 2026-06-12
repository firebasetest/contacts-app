package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.dto.ContactDataPortabilityDto;
import com.mycompany.contact_app.service.GdprComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/privacy/contacts")
public class GdprComplianceController {

    private final GdprComplianceService complianceService;

    public GdprComplianceController(GdprComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    /**
     * Data Access / Portability API Hook (Returns full JSON payload mapping)
     */
    @GetMapping("/{id}/export")
    @PreAuthorize("hasRole('ROLE_DPO') or @contactSecurity.isInternalEmployee()")
    public ResponseEntity<ContactDataPortabilityDto> exportUserInformationProfile(@PathVariable UUID id) {
        // Assuming complianceService handles the ResourceNotFound logic and throws a specific exception if user doesn't exist.
        // If it fails to fetch, we rely on its internal error handling or let the DAO layer throw a clear exception.
        ContactDataPortabilityDto portabilityDump = complianceService.exportPersonalDataDump(id);
        return ResponseEntity.ok(portabilityDump);
    }

    /**
     * Right to Be Forgotten / Erasure API Hook
     */
    @PutMapping("/{id}/anonymize")
    @PreAuthorize("hasRole('ROLE_DPO') or @contactSecurity.isInternalEmployee()")
    public ResponseEntity<Void> eraseUserInformationProfile(@PathVariable UUID id) {
        // If the resource is not found, we might consider returning 204 (No Content) rather than failing the request entirely,
        // but for consistency with other updates, throwing ResourceNotFoundException is safer here.
        complianceService.executeRightToErasure(id);
        return ResponseEntity.noContent().build();
    }
}