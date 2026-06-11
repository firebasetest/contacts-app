package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.entity.Company;
import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.entity.ContactHistory;
import com.mycompany.contact_app.repository.ContactHistoryRepository;
import com.mycompany.contact_app.repository.ContactRepository;
import com.mycompany.contact_app.security.TenantContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactHistoryRepository historyRepository;

    public ContactService(ContactRepository contactRepository, ContactHistoryRepository historyRepository) {
        this.contactRepository = contactRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public List<BaseContact> findAll() {
        return contactRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BaseContact findById(UUID id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found for target ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<BaseContact> findByStatus(String status) {
        return contactRepository.findByStatus(status);
    }

    /**
     * Securely provisions a new Company or Contact record under the active tenant
     * context.
     */
    public BaseContact save(BaseContact contact) {
        // FIXED: Now reads uniformly from the standardized TenantContext utility
        String activeBuId = TenantContext.getCurrentTenant();
        if (activeBuId != null) {
            contact.setBusinessUnitId(UUID.fromString(activeBuId));
        } else if (contact.getBusinessUnitId() == null) {
            throw new IllegalStateException("Cannot create a company profile without a valid Business Unit context.");
        }

        if (contact instanceof Company) {
            contact.setSystemRole("REGULAR");
        }

        return contactRepository.save(contact);
    }

    /**
     * Complete Polymorphic Single-Table Update Layer with historical tracking
     * triggers.
     */
    public BaseContact update(UUID id, BaseContact updatedContact) {
        return contactRepository.findById(id).map(existing -> {

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

            return contactRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Record not found for update execution ID: " + id));
    }

    /**
     * Complete Delete Operation with terminal historical snapshot generation.
     */
    public void delete(UUID id) {
        contactRepository.findById(id).ifPresent(existing -> {
            captureTemporalSnapshot(existing, "DELETE");
            contactRepository.delete(existing);
        });
    }

    /**
     * Executes Point-in-Time querying on historical logs.
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

        if (target instanceof Contact c) {
            historyRecord.setEmail(c.getEmail());
            historyRecord.setPhoneNumber(c.getPhoneNumber());
            historyRecord.setSource(c.getSource());
        } else if (target instanceof Company comp) {
            historyRecord.setTaxId(comp.getTaxId());
            historyRecord.setIndustry(comp.getIndustry());
        }

        historyRecord.setValidFrom(target.getUpdatedAt() != null ? target.getUpdatedAt() : target.getCreatedAt());
        historyRecord.setValidTo(LocalDateTime.now());
        historyRecord.setChangeAction(action);

        // FIXED: Switched from broken filter field lookup to global context utility
        String currentActor = TenantContext.getCurrentTenant();
        historyRecord.setModifiedBy(currentActor != null ? currentActor : "SYSTEM_PROCESS");

        historyRepository.save(historyRecord);
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll().stream()
                .filter(baseContact -> baseContact instanceof Contact)
                .map(baseContact -> (Contact) baseContact)
                .collect(Collectors.toList());
    }

    public Contact getContactById(UUID id) {
        BaseContact baseContact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contact record not found or inaccessible under current security context: " + id));

        if (baseContact instanceof Contact contact) {
            return contact;
        }

        throw new IllegalArgumentException(
                "Requested record ID does not match the standard Contact schema subtype: " + id);
    }

    @Transactional
    public Contact createContact(Contact contact) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null) {
            contact.setBusinessUnitId(UUID.fromString(currentTenant));
        }

        BaseContact savedBase = contactRepository.save(contact);
        if (savedBase instanceof Contact savedContact) {
            return savedContact;
        }
        throw new IllegalStateException(
                "Database engine failed to return the expected concrete Contact entity subtype upon persistence.");
    }

    @Transactional
    public void anonymizeContact(UUID id) {
        Contact contact = getContactById(id);

        contact.setName("ANONYMIZED_CUSTOMER_" + UUID.randomUUID().toString().substring(0, 8));
        contact.setEmail("deleted@tenant.local");
        contact.setPhoneNumber(null);

        if (contact.getCustomAttributes() != null) {
            contact.getCustomAttributes().clear();
        } else {
            contact.setCustomAttributes(new HashMap<>());
        }

        contactRepository.save(contact);
    }

    @Transactional
    public void delegateAdminRights(UUID id) {
        Contact contact = getContactById(id);
        contact.setSystemRole("DELEGATED_ADMIN");
        contactRepository.save(contact);
    }

    @Transactional
    public Contact saveContact(Contact contact) {
        return createContact(contact);
    }
}