package com.mycompany.contact_app.service;

import com.mycompany.contact_app.config.TenantContextFilter;
import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.entity.Company;
import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.entity.ContactHistory;
import com.mycompany.contact_app.repository.ContactHistoryRepository;
import com.mycompany.contact_app.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ContactService {

    private final ContactRepository repository;
    private final ContactHistoryRepository historyRepository;

    public ContactService(ContactRepository repository, ContactHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public List<BaseContact> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public BaseContact findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found for target ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<BaseContact> findByStatus(String status) {
        return repository.findByStatus(status);
    }

    /**
     * Securely provisions a new Company or Contact record under the active tenant
     * context.
     */
    public BaseContact save(BaseContact contact) {
        // Enforce Tenant Isolation: Capture business unit ID from the secure tenant
        // filter thread context
        String activeBuId = TenantContextFilter.CURRENT_TENANT.get();
        if (activeBuId != null) {
            contact.setBusinessUnitId(UUID.fromString(activeBuId));
        } else if (contact.getBusinessUnitId() == null) {
            throw new IllegalStateException("Cannot create a company profile without a valid Business Unit context.");
        }

        // Default initial system role for companies
        if (contact instanceof Company) {
            contact.setSystemRole("REGULAR");
        }

        return repository.save(contact);
    }

    /**
     * Complete Polymorphic Single-Table Update Layer with historical tracking
     * triggers.
     */
    public BaseContact update(UUID id, BaseContact updatedContact) {
        return repository.findById(id).map(existing -> {

            // 1. Snapshot prior state before executing row modifications
            captureTemporalSnapshot(existing, "UPDATE");

            // 2. Map standard base metadata attributes
            existing.setName(updatedContact.getName());
            existing.setStatus(updatedContact.getStatus());
            existing.setNotes(updatedContact.getNotes());
            existing.setCustomAttributes(updatedContact.getCustomAttributes());
            existing.setParentCompany(updatedContact.getParentCompany());

            // 3. Match subclass signatures safely using pattern matching
            if (existing instanceof Contact existingGen && updatedContact instanceof Contact newGen) {
                existingGen.setEmail(newGen.getEmail());
                existingGen.setPhoneNumber(newGen.getPhoneNumber());
                existingGen.setSource(newGen.getSource());
            } else if (existing instanceof Company existingComp && updatedContact instanceof Company newComp) {
                existingComp.setTaxId(newComp.getTaxId());
                existingComp.setIndustry(newComp.getIndustry());
            } else {
                throw new IllegalArgumentException("Altering original contact classification type is forbidden.");
            }

            return repository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Record not found for update execution ID: " + id));
    }

    /**
     * Complete Delete Operation with terminal historical snapshot generation.
     */
    public void delete(UUID id) {
        repository.findById(id).ifPresent(existing -> {
            captureTemporalSnapshot(existing, "DELETE");
            repository.delete(existing);
        });
    }

    /**
     * Restored: Executes Point-in-Time querying on historical logs.
     */
    @Transactional(readOnly = true)
    public Optional<ContactHistory> getContactHistoricalState(UUID id, LocalDateTime asOfTime) {
        return historyRepository.findAsOf(id, asOfTime);
    }

    /**
     * Core Temporal Engine Hook: Assembles and records historical tracking maps.
     */
    private void captureTemporalSnapshot(BaseContact target, String action) {
        ContactHistory historyRecord = new ContactHistory();
        historyRecord.setContactId(target.getId());
        historyRecord.setBusinessUnitId(target.getBusinessUnitId());
        historyRecord.setName(target.getName());
        historyRecord.setStatus(target.getStatus());
        historyRecord.setNotes(target.getNotes());
        historyRecord.setCustomAttributes(target.getCustomAttributes());
        historyRecord.setSystemRole(target.getSystemRole());

        // Extract sub-type payload variables polymorphically safely
        if (target instanceof Contact c) {
            historyRecord.setEmail(c.getEmail());
            historyRecord.setPhoneNumber(c.getPhoneNumber());
            historyRecord.setSource(c.getSource());
        } else if (target instanceof Company comp) {
            historyRecord.setTaxId(comp.getTaxId());
            historyRecord.setIndustry(comp.getIndustry());
        }

        // Establish chronological data bounds
        historyRecord.setValidFrom(target.getUpdatedAt() != null ? target.getUpdatedAt() : target.getCreatedAt());
        historyRecord.setValidTo(LocalDateTime.now());
        historyRecord.setChangeAction(action);

        // Trace mutations using the current active filter context execution context
        String currentActor = TenantContextFilter.CURRENT_TENANT.get();
        historyRecord.setModifiedBy(currentActor != null ? currentActor : "SYSTEM_PROCESS");

        historyRepository.save(historyRecord);
    }
}