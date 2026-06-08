package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.repository.ContactRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ContactService {
    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    public Contact save(Contact contact) {
        return repository.save(contact);
    }

    public Contact update(UUID id, Contact updatedContact) {
        return repository.findById(id).map(existing -> {
            updatedContact.setId(existing.getId());
            return repository.save(updatedContact);
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
        repository.deleteById(id);
    }
}