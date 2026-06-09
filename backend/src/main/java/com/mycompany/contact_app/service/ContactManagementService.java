package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.repository.ContactRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional
public class ContactManagementService {

    private final ContactRepository contactRepository;

    public ContactManagementService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    /**
     * Promotes an existing contact to a Delegated Admin. Restricted to internal
     * employees.
     */
    @PreAuthorize("@contactSecurity.isInternalEmployee()")
    public void assignDelegatedAdminRights(UUID contactId) {
        Contact contact = (Contact) contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact record not found."));

        if (contact.getParentCompany() == null) {
            throw new IllegalStateException("Cannot grant administrative rights to an unlinked contact.");
        }

        contact.setSystemRole("DELEGATED_ADMIN");
        contactRepository.save(contact);
    }

    /**
     * Updates an existing contact record. Accessible by internal employees or
     * company delegated admins.
     */
    @PreAuthorize("@contactSecurity.canManageContact(#contactId)")
    public void updateContactLifecycleDetails(UUID contactId, Contact updatedDetails) {
        Contact existingContact = (Contact) contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found."));

        existingContact.setName(updatedDetails.getName());
        existingContact.setEmail(updatedDetails.getEmail());
        existingContact.setPhoneNumber(updatedDetails.getPhoneNumber());
        existingContact.setStatus(updatedDetails.getStatus());
        existingContact.setCustomAttributes(updatedDetails.getCustomAttributes());

        contactRepository.save(existingContact);
    }

    /**
     * Permanently deletes a contact record. Accessible by internal employees or
     * company delegated admins.
     */
    @PreAuthorize("@contactSecurity.canManageContact(#contactId)")
    public void deleteContactFromSystem(UUID contactId) {
        contactRepository.deleteById(contactId);
    }
}