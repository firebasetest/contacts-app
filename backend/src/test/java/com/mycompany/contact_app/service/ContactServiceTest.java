package com.mycompany.contact_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.repository.ContactRepository;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {
    @Mock
    private ContactRepository repository;
    @InjectMocks
    private ContactService contactService;

    @Test
    void shouldSaveContactSuccessfully() {
        Contact contact = new Contact();
        when(repository.save(any())).thenReturn(contact);

        Contact saved = contactService.save(contact);
        assertNotNull(saved);
        verify(repository, times(1)).save(contact);
    }
}