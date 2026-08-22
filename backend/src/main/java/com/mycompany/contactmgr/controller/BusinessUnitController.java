package com.mycompany.contactmgr.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mycompany.contactmgr.entity.BusinessUnit;
import com.mycompany.contactmgr.service.BusinessUnitService;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/business-units")
public class BusinessUnitController {

    private final BusinessUnitService service;

    public BusinessUnitController(BusinessUnitService service) {
        this.service = service;
    }

    // --- SUPER ADMIN ENDPOINTS ---
    // Only Super Admins can manage the Business Unit catalogue itself.
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public BusinessUnit createBusinessUnit(@RequestBody BusinessUnit businessUnit) {
        return service.create(businessUnit);
    }

    // --- CURRENT TENANT ENDPOINTS ---

    /**
     * Retrieves the currently active Business Unit context for verification.
     */
    @GetMapping("/{businessUnitId}")
    @PreAuthorize("hasRole('BUSINESS_UNIT_MEMBER')")
    public Optional<BusinessUnit> getBusinessUnit(@PathVariable String businessUnitId) {
        // The service retrieves the unit based on the assumed context ID match
        return service.findByBusinessUnitId(businessUnitId);
    }

    /**
     * Updates details for the current Business Unit (Owner modification).
     */
    @PutMapping("/{businessUnitId}")
    @PreAuthorize("hasRole('BUSINESS_UNIT_ADMIN')")
    public BusinessUnit updateBusinessUnit(@PathVariable String businessUnitId, @RequestBody BusinessUnit updatedData) {
        // The service enforces that the requested unit ID matches the current context.
        return service.update(businessUnitId, updatedData);
    }

    /**
     * Deletes the Business Unit. Restricted to the absolute highest authority.
     */
    @DeleteMapping("/{businessUnitId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteBusinessUnit(@PathVariable String businessUnitId) {
        service.delete(businessUnitId);
    }
}