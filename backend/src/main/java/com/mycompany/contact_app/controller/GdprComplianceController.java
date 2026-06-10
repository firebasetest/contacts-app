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
        ContactDataPortabilityDto portabilityDump = complianceService.exportPersonalDataDump(id);
        return ResponseEntity.ok(portabilityDump);
    }

    /**
     * Right to Be Forgotten / Erasure API Hook
     */
    @PutMapping("/{id}/anonymize")
    @PreAuthorize("hasRole('ROLE_DPO') or @contactSecurity.isInternalEmployee()")
    public ResponseEntity<Void> eraseUserInformationProfile(@PathVariable UUID id) {
        complianceService.executeRightToErasure(id);
        return ResponseEntity.noContent().build();
    }
}