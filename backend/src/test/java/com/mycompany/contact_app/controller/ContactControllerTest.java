package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.service.BatchActionService;
import com.mycompany.contact_app.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
class ContactControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ContactService contactService;
    @MockBean
    private BatchActionService batchActionService;

    @Test
    @WithMockUser
    void shouldReturn200ForContactCreation() throws Exception {
        Contact contact = new Contact();
        contact.setName("Test Contact");
        when(contactService.save(any())).thenReturn(contact);

        mockMvc.perform(post("/api/v1/contacts")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json")
                .content("{\"name\": \"Test Contact\", \"email\": \"test@example.com\"}"))
                .andExpect(status().isOk());
    }
}