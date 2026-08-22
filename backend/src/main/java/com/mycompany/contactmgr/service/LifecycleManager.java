package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.BaseContact;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class LifecycleManager {
    private final Map<String, List<String>> validTransitions = Map.of(
            "ACTIVE", List.of("INACTIVE", "ARCHIVED"),
            "INACTIVE", List.of("ACTIVE", "ARCHIVED"),
            "ARCHIVED", List.of("ACTIVE"));

    public void transitionTo(BaseContact contact, String newStatus) {
        if (!validTransitions.getOrDefault(contact.getStatus(), List.of()).contains(newStatus)) {
            throw new IllegalStateException(
                    "Illegal state transition from " + contact.getStatus() + " to " + newStatus);
        }
        // Business Rule: Validate state transition
        if ("ARCHIVED".equals(newStatus) && "LOCKED".equals(contact.getStatus())) {
            throw new IllegalStateException("Cannot archive a locked contact.");
        }

        // Business Rule: Update status
        contact.setStatus(newStatus);
    }
}