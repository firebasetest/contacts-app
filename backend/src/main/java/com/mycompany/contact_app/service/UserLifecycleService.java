package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(UserLifecycleService.class);
    private final ContactRepository contactRepository;
    private final ContactService contactService;

    public UserLifecycleService(ContactRepository contactRepository, ContactService contactService) {
        this.contactRepository = contactRepository;
        this.contactService = contactService;
    }

    /**
     * Proactive AOT Offboarding (Leavers Pattern)
     * Securely deactivates an employee profile and moves all their assigned
     * accounts
     * to a replacement administrator in a single transactional batch operation.
     */
    @PreAuthorize("hasRole('ROLE_EMERGENCY_ADMIN') or @contactSecurity.isInternalEmployee()")
    public void processEmployeeTermination(UUID departingEmployeeId, UUID replacementEmployeeId) {
        log.info("Executing Bulk AOT Lifecycle Decommission: Transitioning assignments from {} to {}",
                departingEmployeeId, replacementEmployeeId);

        Contact departingEmployee = (Contact) contactService.findById(departingEmployeeId);
        Contact replacementEmployee = (Contact) contactService.findById(replacementEmployeeId);

        // 1. Mark employee status as INACTIVE to permanently sever login authentication
        // loops
        departingEmployee.setStatus("TERMINATED");
        departingEmployee.setExternalUserId(null); // Sever identity linkage mapping
        contactService.update(departingEmployeeId, departingEmployee);

        // 2. Perform bulk cleanup/migration of their relational entity data mappings
        List<BaseContact> fullyManagedAccounts = contactRepository.findAll().stream()
                .filter(bc -> bc.getAssignedAdmin() != null
                        && bc.getAssignedAdmin().getId().equals(departingEmployeeId))
                .toList();

        log.info("Migrating {} assigned administrative references to new supervisor profile ID: {}",
                fullyManagedAccounts.size(), replacementEmployeeId);
        for (BaseContact entity : fullyManagedAccounts) {
            entity.setAssignedAdmin(replacementEmployee);
            contactService.update(entity.getId(), entity); // Automatically preserves temporal tracking history updates
        }
    }
}