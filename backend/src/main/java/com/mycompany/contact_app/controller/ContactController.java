package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.entity.ContactHistory; // Injected history entity
import com.mycompany.contact_app.service.BatchActionService;
import com.mycompany.contact_app.service.ContactService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {
    private final ContactService contactService;
    private final BatchActionService batchActionService;

    public ContactController(ContactService cs, BatchActionService bas) {
        this.contactService = cs;
        this.batchActionService = bas;
    }

    @GetMapping
    public List<Contact> list(@RequestParam(required = false) String status) {
        return status == null ? contactService.findAll() : contactService.findByStatus(status);
    }

    @GetMapping("/{id}")
    public Contact get(@PathVariable UUID id) {
        return contactService.findById(id);
    }

    @PostMapping
    public Contact create(@RequestBody Contact contact) {
        return contactService.save(contact);
    }

    @PutMapping("/{id}")
    public Contact update(@PathVariable UUID id, @RequestBody Contact contact) {
        return contactService.update(id, contact);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        contactService.delete(id);
    }

    @PostMapping("/batch-action")
    public void executeBatch(@RequestParam String status, @RequestParam String action) {
        batchActionService.processBatchAction(status, action);
    }

    // --- New Temporal Query Endpoint ---
    @GetMapping("/{id}/historical")
    public ResponseEntity<ContactHistory> getAsOf(
            @PathVariable UUID id,
            @RequestParam("asOf") String asOfIsoString) {

        // Parse the requested historical timeline window string
        LocalDateTime targetTime = LocalDateTime.parse(asOfIsoString);

        return contactService.getContactHistoricalState(id, targetTime)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}