package com.mycompany.contact_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mycompany.contact_app.service.BatchActionService;
import com.mycompany.contact_app.service.ContactService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
class ContactControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ContactService contactService;
    @MockBean
    private BatchActionService batchActionService;

    @Test
    void shouldReturn200ForContactCreation() throws Exception {
        mockMvc.perform(post("/api/v1/contacts")
                .contentType("application/json")
                .content("{\"name\": \"Test Contact\"}"))
                .andExpect(status().isOk());
    }
}