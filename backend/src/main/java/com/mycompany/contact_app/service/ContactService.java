package com.mycompany.contact_app.service;

import com.mycompany.contact_app.config.TenantContextFilter;
import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.entity.Company;
import com.mycompany.contact_app.entity.ContactHistory;
import com.mycompany.contact_app.repository.ContactHistoryRepository;
import com.mycompany.contact_app.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing contact-related operations.
 */
@Service
@Transactional
public class ContactService {
    private final ContactRepository repository;
    private final ContactHistoryRepository historyRepository;

    public ContactService(ContactRepository repository, ContactHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    public List<BaseContact> java() {
        return repository.findAll();
    }

    public BaseContact findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found for ID: " + id));
    }

    public List<BaseContact> findByStatus(String status) {
        return repository.findByStatus(status);
    }

    public List<BaseContact> findAll() {
        return repository.findAll();
    }

    public BaseContact save(BaseContact contact) {
        return repository.save(contact);
    }

    public BaseContact update(UUID id, BaseContact updatedContact) {
        return repository.findById(id).map(existing -> {
            // 1. Capture history snapshot *before* mutating fields in the database
            captureTemporalSnapshot(existing, "UPDATE");

            // 2. Map standard base shared properties
            existing.setName(updatedContact.getName());
            existing.setStatus(updatedContact.getStatus());
            existing.setNotes(updatedContact.getNotes());
            existing.setCustomAttributes(updatedContact.getCustomAttributes());
            existing.setParentCompany(updatedContact.getParentCompany());

            // 3. Safe polymorphic subclass field matching
            if (existing instanceof Contact existingGen && updatedContact instanceof Contact newGen) {
                existingGen.setEmail(newGen.getEmail());
                existingGen.setPhoneNumber(newGen.getPhoneNumber());
                existingGen.setSource(newGen.getSource());
            } else if (existing instanceof Company existingComp && updatedContact instanceof Company newComp) {
                existingComp.setTaxId(newComp.getTaxId());
                existingComp.setIndustry(newComp.getIndustry());
            } else {
                throw new IllegalArgumentException("Changing contact classification type is not permitted.");
            }

            return repository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Record not found for update ID: " + id));
    }

    public void delete(UUID id) {
        repository.findById(id).ifPresent(existing -> {
            // Capture terminal snapshot right before database evacuation
            captureTemporalSnapshot(existing, "DELETE");
            repository.delete(existing);
        });
    }

    /**
     * Executes the historical Point-In-Time query (AS-OF execution lookup).
     */
    public Optional<ContactHistory> getContactHistoricalState(UUID id, LocalDateTime asOfTime) {
        return historyRepository.findAsOf(id, asOfTime);
    }

    /**
     * Internal method that builds and saves the historical audit state.
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

        // Extract sub-type unique metrics polymorphically safely
        if (target instanceof Contact c) {
            historyRecord.setEmail(c.getEmail());
            historyRecord.setPhoneNumber(c.getPhoneNumber());
            historyRecord.setSource(c.getSource());
        } else if (target instanceof Company comp) {
            historyRecord.setTaxId(comp.getTaxId());
            historyRecord.setIndustry(comp.getIndustry());
        }

        // Set temporal tracking timelines
        historyRecord.setValidFrom(target.getUpdatedAt() != null ? target.getUpdatedAt() : target.getCreatedAt());
        historyRecord.setValidTo(LocalDateTime.now());
        historyRecord.setChangeAction(action);

        // Resolve security actor context safely
        String currentActor = TenantContextFilter.CURRENT_TENANT.get();
        historyRecord.setModifiedBy(currentActor != null ? currentActor : "SYSTEM");

        historyRepository.save(historyRecord);
    }
}