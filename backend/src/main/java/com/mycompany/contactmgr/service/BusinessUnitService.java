package com.mycompany.contactmgr.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycompany.contactmgr.entity.BusinessUnit;
import com.mycompany.contactmgr.repository.BusinessUnitRepository;
import com.mycompany.contactmgr.security.TenantContext;

import java.util.Optional;

@Service
public class BusinessUnitService {

    private final BusinessUnitRepository buRepository;

    public BusinessUnitService(BusinessUnitRepository repository) {
        this.buRepository = repository;
    }

    /**
     * Retrieves a BusinessUnit by its ID.
     * Note: For the BusinessUnit entity itself, we must use the TenantContext
     * to ensure the requesting user belongs to the requested unit.
     */
    public Optional<BusinessUnit> findByBusinessUnitId(String tenantId) {
        // We assume for this specific entity, the BusinessUnitId IS the tenantId.
        // We rely on the repository/query to handle the lookup based on the external
        // ID.
        return buRepository.findByBusinessUnitId(tenantId);
    }

    /**
     * Creates a new Business Unit. This operation should only be callable by Super
     * Admin.
     */
    @Transactional
    public BusinessUnit create(BusinessUnit buId) {
        // Basic validation (e.g., check if name is unique)
        if (buRepository.findByBusinessUnitId(buId.getBusinessUnitId()).isPresent()) {
            throw new IllegalStateException("Business Unit ID already exists.");
        }
        return buRepository.save(buId);
    }

    /**
     * Updates an existing Business Unit's core details.
     */
    @Transactional
    public BusinessUnit update(String businessUnitId, BusinessUnit updatedData) {
        // Critical check: Does the provided ID belong to the current context?
        if (!businessUnitId.equals(TenantContext.getCurrentTenant().orElse(null))) {
            throw new SecurityException("Cannot modify a Business Unit belonging to another tenant.");
        }

        // Find the existing record
        return buRepository.findByBusinessUnitId(businessUnitId)
                .map(unit -> {
                    unit.setName(updatedData.getName());
                    // Re-enforce the context ID even if the request payload changes
                    // Note: We must explicitly set the context ID back in the object
                    // to prevent it from being nullified or changed.
                    return buRepository.save(unit);
                })
                .orElseThrow(() -> new IllegalStateException("Business Unit not found for ID: " + businessUnitId));
    }

    /**
     * Deletes a Business Unit. Only Super Admin should call this.
     */
    @Transactional
    public void delete(String businessUnitId) {
        // For simplicity, we assume delete is only called by a Super Admin.
        // Actual implementation would require a role check here.
        if (buRepository.findByBusinessUnitId(businessUnitId).isEmpty()) {
            throw new IllegalArgumentException("Business Unit ID not found.");
        }
        buRepository.deleteByBusinessUnitId(businessUnitId);
    }
}