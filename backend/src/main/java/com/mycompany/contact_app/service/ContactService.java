package com.mycompany.contact_app.service;

import com.mycompany.contact_app.config.TenantContextFilter;
import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.entity.ContactHistory;
import com.mycompany.contact_app.repository.ContactHistoryRepository;
import com.mycompany.contact_app.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
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

    public Contact save(Contact contact) {
        return repository.save(contact);
    }

    public Contact update(UUID id, Contact updatedContact) {
        return repository.findById(id).map(existing -> {
            // 1. Snapshot previous state before committing updates to the record
            captureTemporalSnapshot(existing, "UPDATE");

            // 2. Map standard updates
            existing.setName(updatedContact.getName());
            existing.setEmail(updatedContact.getEmail());
            existing.setPhoneNumber(updatedContact.getPhoneNumber());
            existing.setStatus(updatedContact.getStatus());
            existing.setNotes(updatedContact.getNotes()); // Persistent save of notes field
            existing.setCustomAttributes(updatedContact.getCustomAttributes());

            return repository.save(existing);
        }).orElseThrow();
    }

    public Contact findById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public List<Contact> findByStatus(String status) {
        return repository.findByStatus(status);
    }

    public List<Contact> findAll() {
        return repository.findAll();
    }

    public void delete(UUID id) {
        repository.findById(id).ifPresent(existing -> {
            // Capture terminal snapshot right before database evaporation
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

    private void captureTemporalSnapshot(Contact target, String action) {
        ContactHistory historyRecord = new ContactHistory();
        historyRecord.setContactId(target.getId());
        historyRecord.setBusinessUnitId(target.getBusinessUnitId());
        historyRecord.setName(target.getName());
        historyRecord.setStatus(target.getStatus());
        historyRecord.setEmail(target.getEmail());
        historyRecord.setPhoneNumber(target.getPhoneNumber());
        historyRecord.setNotes(target.getNotes());
        historyRecord.setCustomAttributes(target.getCustomAttributes());
        historyRecord.setSystemRole(target.getSystemRole());

        // Compute temporal boundaries
        // Valid from the last known entity modification up until right now
        historyRecord.setValidFrom(target.getUpdatedAt() != null ? target.getUpdatedAt() : target.getCreatedAt());
        historyRecord.setValidTo(LocalDateTime.now());

        historyRecord.setChangeAction(action);
        historyRecord.setModifiedBy(TenantContextFilter.CURRENT_TENANT.get()); // Resolve current actor

        historyRepository.save(historyRecord);
    }
}