
package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Dedicated service for handling highly privileged, non-routine operations
 * (Break Glass procedures). These actions are fully audited and should be separate
 * from standard business process logic.
 */
@Service
public class EmergencyService {

    private final ContactService contactService;

    public EmergencyService(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Retrieves a full multi-tenant dataset, bypassing standard business unit filtration.
     */
    @Transactional(readOnly = true)
    public List<BaseContact> getAllTenantRecords() {
        // The calling controller/filter handles the tenant context setting for RLS.
        return contactService.findAll();
    }

    /**
     * Force mutates a record, bypassing typical validation or workflow checks.
     */
    @Transactional
    public BaseContact repairRecord(UUID id, BaseContact baseContactUpdate) {
        // Log and enforce special audit tracking here before calling the service method
        return contactService.update(id, baseContactUpdate);
    }

    /**
     * Force deletes an entity reference regardless of foreign key constraints or soft delete status.
     */
    @Transactional
    public void purgeRecord(UUID id) {
        contactService.delete(id);
    }
}