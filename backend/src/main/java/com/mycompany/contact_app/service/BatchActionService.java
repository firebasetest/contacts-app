package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.repository.ContactRepository;
import com.mycompany.contact_app.domain.LifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchActionService {
    private static final Logger log = LoggerFactory.getLogger(BatchActionService.class);

    private final ContactRepository repository;
    private final LifecycleManager lifecycleManager;

    public BatchActionService(ContactRepository repository, LifecycleManager lifecycleManager) {
        this.repository = repository;
        this.lifecycleManager = lifecycleManager;
    }

    @Async("importTaskExecutor")
    @Transactional
    public void processBatchAction(String statusFilter, String action) {
        log.info("Starting batch action: {} for status: {}", action, statusFilter);

        // This query is scoped by the TenantContext set by the Filter
        repository.findByStatus(statusFilter).forEach(contact -> {
            try {
                lifecycleManager.transitionTo(contact, action);
                repository.save(contact);
            } catch (Exception e) {
                log.error("Failed to transition contact {}: {}", contact.getId(), e.getMessage());
            }
        });
    }
}
