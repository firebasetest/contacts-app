package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.repository.ContactRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delegated-admin")
public class DelegatedAdminController {
    private final ContactRepository contactRepository;

    public DelegatedAdminController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<BaseContact>> listDelegates() {
        List<BaseContact> delegates = contactRepository.findAll().stream()
                .filter(contact -> "DELEGATED_ADMIN".equalsIgnoreCase(contact.getSystemRole()))
                .toList();
        return ResponseEntity.ok(delegates);
    }

    @PostMapping("/contacts/{id}/revoke")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        contactRepository.findById(id).ifPresent(contact -> {
            contact.setSystemRole("REGULAR");
            contactRepository.save(contact);
        });
        return ResponseEntity.noContent().build();
    }
}
