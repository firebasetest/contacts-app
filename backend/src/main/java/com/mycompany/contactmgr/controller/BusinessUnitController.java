package com.mycompany.contactmgr.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mycompany.contactmgr.dto.BusinessUnitDto;
import com.mycompany.contactmgr.dto.BusinessUnitInputDto;
import com.mycompany.contactmgr.dto.BusinessUnitSearchResponse;
import com.mycompany.contactmgr.entity.BusinessUnit;
import com.mycompany.contactmgr.model.BusinessUnitStatus;
import com.mycompany.contactmgr.service.BusinessUnitService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-units")
public class BusinessUnitController {

    private final BusinessUnitService businessUnitService;

    public BusinessUnitController(BusinessUnitService service) {
        this.businessUnitService = service;
    }

    // --- SUPER ADMIN ENDPOINTS ---
    // Only Super Admins can manage the Business Unit catalogue itself.
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public BusinessUnit createBusinessUnit(@RequestBody BusinessUnit businessUnit) {
        return businessUnitService.create(businessUnit);
    }

    // --- 1. LISTING ALL BUs (The Table Feed) ---
    /**
     * Retrieves paginated list of business units for the given organization.
     * SECURITY CHECK: Requires BUSINESS_UNIT_READ permission.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('BUSINESS_UNIT_READ')")
    public ResponseEntity<BusinessUnitSearchResponse> getBusinessUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String searchQuery) {

        // 1. BUSINESS LOGIC: Handle search logic and pagination constraints
        // 2. SECURITY: The service must ensure the BUs returned belong to the current
        // tenant.
        BusinessUnitSearchResponse response = businessUnitService.findBusinessUnits(page, size, searchQuery);
        return ResponseEntity.ok(response);
    }

    // --- 2. CREATING A NEW BU ---
    /**
     * Creates a new Business Unit. This is a mission-critical operation.
     * SECURITY CHECK: Requires BUSINESS_UNIT_MANAGE permission.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('BUSINESS_UNIT_MANAGE')")
    public ResponseEntity<BusinessUnitDto> createBusinessUnit(@RequestBody BusinessUnitInputDto input) {
        // Validation will happen in the service layer (e.g., name uniqueness, valid
        // status)
        BusinessUnitDto newUnit = businessUnitService.createBusinessUnit(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUnit);
    }

    // --- 3. UPDATING A BU (Activation/Deactivation) ---
    @PutMapping("/{businessUnitId}/status")
    @PreAuthorize("hasAuthority('BUSINESS_UNIT_MANAGE')")
    public ResponseEntity<BusinessUnitDto> updateBusinessUnitStatus(
            @PathVariable String businessUnitId,
            @RequestParam BusinessUnitStatus newStatus) {

        // Use transactional logic: Change status and update the 'updatedAt' field.
        BusinessUnitDto updatedUnit = businessUnitService.updateStatus(UUID.fromString(businessUnitId), newStatus);
        return ResponseEntity.ok(updatedUnit);
    }

    // NOTE: Deletion might require soft-delete logic to retain historical data.

    // --- CURRENT TENANT ENDPOINTS ---

    /**
     * Retrieves the currently active Business Unit context for verification.
     */
    @GetMapping("/{businessUnitId}")
    @PreAuthorize("hasRole('BUSINESS_UNIT_MEMBER')")
    public Optional<BusinessUnit> getBusinessUnit(@PathVariable String businessUnitId) {
        // The service retrieves the unit based on the assumed context ID match
        return businessUnitService.findByBusinessUnitId(businessUnitId);
    }

    /**
     * Updates details for the current Business Unit (Owner modification).
     */
    @PutMapping("/{businessUnitId}")
    @PreAuthorize("hasRole('BUSINESS_UNIT_ADMIN')")
    public BusinessUnit updateBusinessUnit(@PathVariable String businessUnitId, @RequestBody BusinessUnit updatedData) {
        // The service enforces that the requested unit ID matches the current context.
        return businessUnitService.update(businessUnitId, updatedData);
    }

    /**
     * Deletes the Business Unit. Restricted to the absolute highest authority.
     */
    @DeleteMapping("/{businessUnitId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteBusinessUnit(@PathVariable String businessUnitId) {
        businessUnitService.delete(businessUnitId);
    }
}