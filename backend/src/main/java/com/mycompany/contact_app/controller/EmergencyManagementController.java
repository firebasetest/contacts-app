package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.service.EmergencyService; // Use the new service
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emergency")
public class EmergencyManagementController {

    private static final Logger log = LoggerFactory.getLogger(EmergencyManagementController.class);
    private final EmergencyService emergencyService; // Use the new service

    // Dependency Injection updated to use EmergencyService
    public EmergencyManagementController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    /**
     * Emergency recovery bypass to pull data rows for a compromised/broken tenant
     * partition.
     * Expects 'X-Emergency-Break-Glass-Token' and 'X-BU-ID' passed via request
     * headers.
     */
    @GetMapping("/contacts")
    public ResponseEntity<List<BaseContact>> emergencyListTenantRecords() {
        log.error("EMERGENCY EXECUTION: Extracting full multi-tenant dataset rows.");
        // Delegate execution to the dedicated service method
        return ResponseEntity.ok(emergencyService.getAllTenantRecords());
    }

    /**
     * Emergency update bypass to repair broken records or class updates.
     */
    @PutMapping("/contacts/{id}")
    public ResponseEntity<BaseContact> emergencyUpdateRecord(@PathVariable UUID id,
            @RequestBody BaseContact baseContactUpdate) {
        log.error("EMERGENCY EXECUTION: Force muting record modification on Target Object ID: {}", id);
        // Delegate execution to the dedicated service method
        return ResponseEntity.ok(emergencyService.repairRecord(id, baseContactUpdate));
    }

    /**
     * Emergency eviction tool to clear out corrupt rows instantly.
     */
    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<Void> emergencyDeleteRecord(@PathVariable UUID id) {
        log.error("EMERGENCY EXECUTION: Force purging object entity reference tracking on ID: {}", id);
        // Delegate execution to the dedicated service method
        emergencyService.purgeRecord(id);
        return ResponseEntity.noContent().build();
    }
}