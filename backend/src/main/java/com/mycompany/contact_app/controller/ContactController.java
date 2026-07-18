package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.dto.ContactRequestDto;
import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.entity.Company;
import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.entity.ContactHistory;
import com.mycompany.contact_app.exception.ResourceNotFoundException;
import com.mycompany.contact_app.service.BatchActionService;
import com.mycompany.contact_app.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {
    private final ContactService contactService;
    private final BatchActionService batchActionService;

    public ContactController(ContactService contactService, BatchActionService batchActionService) {
        this.contactService = contactService;
        this.batchActionService = batchActionService;
    }

    @GetMapping
    public List<BaseContact> list(@RequestParam(required = false) String status) {
        return status == null ? contactService.findAll() : contactService.findByStatus(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseContact> get(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(contactService.findById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/historical")
    public ResponseEntity<ContactHistory> getAsOf(
            @PathVariable UUID id,
            @RequestParam("asOf") String asOfIsoString) {

        LocalDateTime targetTime = LocalDateTime.parse(asOfIsoString);
        return contactService.getContactHistoricalState(id, targetTime)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Historical record not found for ID: " + id)); // Throw
                                                                                                                // specific
                                                                                                                // exception
    }

    @PostMapping
    public BaseContact create(@RequestBody ContactRequestDto request) {
        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        contact.setCustomAttributes(request.getCustomAttributes());
        return contactService.save(contact);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseContact> update(@PathVariable UUID id, @RequestBody BaseContact contact) {
        try {
            return ResponseEntity.ok(contactService.update(id, contact));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delegation Execution API Endpoint
     */
    @PostMapping("/{id}/delegate-admin")
    public ResponseEntity<Void> delegateAdminRole(@PathVariable UUID id) {
        // Routed directly to the corresponding capability in ContactService
        contactService.delegateAdminRights(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to create a new Company profile.
     */
    @PostMapping("/companies")
    @PreAuthorize("@contactSecurity.canCreateCompany()")
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        Company savedCompany = (Company) contactService.save(company);
        return ResponseEntity.ok(savedCompany);
    }

    /**
     * Endpoint to update an existing Company profile.
     */
    @PutMapping("/companies/{id}")
    @PreAuthorize("@contactSecurity.canManageContact(#id)")
    public ResponseEntity<Company> updateCompany(@PathVariable UUID id, @RequestBody Company company) {
        Company updatedCompany = (Company) contactService.update(id, company);
        return ResponseEntity.ok(updatedCompany);
    }
}