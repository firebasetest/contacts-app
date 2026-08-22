package com.mycompany.contactmgr.controller;

import com.mycompany.contactmgr.dto.ContactHistoryResponseDto;
import com.mycompany.contactmgr.service.ContactAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class ContactAuditController {

    private final ContactAuditService auditService;

    public ContactAuditController(ContactAuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Retrieves a clean chronological update ledger for a specific entity ID.
     */
    @GetMapping("/entities/{id}")
    @PreAuthorize("hasRole('ROLE_INTERNAL_EMPLOYEE') or @contactSecurity.isInternalEmployee()")
    public ResponseEntity<List<ContactHistoryResponseDto>> getEntityAuditTrail(@PathVariable UUID id) {
        List<ContactHistoryResponseDto> trailReport = auditService.getEntityChangelog(id);
        return ResponseEntity.ok(trailReport);
    }
}