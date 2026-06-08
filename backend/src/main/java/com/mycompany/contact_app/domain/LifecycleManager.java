package com.mycompany.contact_app.domain;

import com.mycompany.contact_app.entity.BaseContact;
import org.springframework.stereotype.Component;

@Component
public class LifecycleManager {

    public void transitionTo(BaseContact contact, String newStatus) {
        // Business Rule: Validate state transition
        if ("ARCHIVED".equals(newStatus) && "LOCKED".equals(contact.getStatus())) {
            throw new IllegalStateException("Cannot archive a locked contact.");
        }

        // Business Rule: Update status
        contact.setStatus(newStatus);
    }
}