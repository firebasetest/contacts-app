package com.mycompany.contact_app.service;

import com.mycompany.contact_app.dto.ContactDataPortabilityDto;
import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.entity.ContactHistory;
import com.mycompany.contact_app.repository.ContactHistoryRepository;
import com.mycompany.contact_app.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GdprComplianceService {

    private static final Logger log = LoggerFactory.getLogger(GdprComplianceService.class);

    private final ContactService contactService;
    private final ContactRepository contactRepository;
    private final ContactHistoryRepository historyRepository;

    public GdprComplianceService(ContactService contactService, ContactRepository contactRepository,
            ContactHistoryRepository historyRepository) {
        this.contactService = contactService;
        this.contactRepository = contactRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Art. 15 & 20 Compliance: Compiles a complete data profile dump for a natural
     * person.
     */
    @Transactional(readOnly = true)
    public ContactDataPortabilityDto exportPersonalDataDump(UUID contactId) {
        log.info("GDPR Compliance Action: Executing Data Portability extraction for Object ID: {}", contactId);

        Contact contactProfile = (Contact) contactService.findById(contactId);
        List<ContactHistory> structuralHistory = historyRepository.findByContactIdOrderByValidFromDesc(contactId);

        return new ContactDataPortabilityDto(contactProfile, structuralHistory);
    }

    /**
     * Art. 17 Compliance: Erases all tracking PII elements from both active and
     * historical logs.
     */
    public void executeRightToErasure(UUID contactId) {
        log.error("!!! GDPR MANDATE: Purging all PII records for Contact ID: {} !!!", contactId);

        Contact contact = (Contact) contactService.findById(contactId);

        // 1. Scrub active system-record attributes
        contact.setName("GDPR_ANONYMIZED_" + UUID.randomUUID().toString().substring(0, 8));
        contact.setEmail("anonymized_" + contactId + "@gdpr.internal");
        contact.setPhoneNumber("000-000-0000");
        contact.setNotes("This record has been completely anonymized under the Right to be Forgotten mandate.");
        contact.setStatus("ANONYMIZED");

        // Sever identity federation links completely so they can never authenticate
        // back into this row entity context
        contact.setExternalUserId(null);

        // Wipe custom metadata map attributes that might harbor untracked PII strings
        if (contact.getCustomAttributes() != null) {
            contact.getCustomAttributes().clear();
        }

        // Save active record mutations via normal pipeline
        contactRepository.save(contact);

        // 2. Clear out the entire legacy tracking graph in the audit snapshots
        historyRepository.anonymizeHistoryLogs(contactId);

        log.info("GDPR Compliance Success: Contact ID {} and associated history states scrubbed cleanly.", contactId);
    }
}