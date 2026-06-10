package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class IdentityProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(IdentityProvisioningService.class);
    private final ContactRepository contactRepository;
    private final ContactService contactService; // Injecting ContactService to ensure temporal logging is triggered

    public IdentityProvisioningService(ContactRepository contactRepository, ContactService contactService) {
        this.contactRepository = contactRepository;
        this.contactService = contactService;
    }

    @Transactional
    public void ensureUserShadowRecordExists(Jwt jwt) {
        String externalId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String buIdClaim = jwt.getClaimAsString("business_unit_id");

        if (buIdClaim == null || buIdClaim.isBlank()) {
            throw new IllegalStateException(
                    "Mandatory multi-tenant business_unit_id claim is missing from security assertion token.");
        }
        UUID tokenBuId = UUID.fromString(buIdClaim);

        // State 1: User has logged in before (Standard matching path)
        Optional<Contact> existingByExternalId = contactRepository.findByExternalUserId(externalId)
                .map(contact -> (Contact) contact);

        if (existingByExternalId.isPresent()) {
            Contact employee = existingByExternalId.get();
            // MOVER CHECK: Has the employee been reassigned to a new Business Unit or
            // changed names?
            if (!employee.getBusinessUnitId().equals(tokenBuId) || !employee.getName().equals(name)) {
                log.info("Identity Mover Detected: Reconciling profile modifications for external user ID: {}",
                        externalId);

                employee.setName(name != null ? name : employee.getName());
                employee.setBusinessUnitId(tokenBuId); // Shifts their Postgres RLS boundary context

                // Saving via contactService ensures an 'UPDATE' snapshot is written to
                // contact_history
                contactService.update(employee.getId(), employee);
            }
            return;
        }

        // State 2: AOT Pre-Provisioning Linkage Fallback
        // Check if an administrative bulk operation pre-loaded this user via email but
        // hasn't mapped their external OAuth ID yet
        log.info("First login detected for external ID {}. Checking for pre-provisioned AOT records matching email: {}",
                externalId, email);

        // Custom query lookup or streaming evaluation against base repository entries
        Optional<Contact> preProvisionedContact = contactRepository.findAll().stream()
                .filter(c -> c instanceof Contact)
                .map(c -> (Contact) c)
                .filter(c -> email != null && email.equalsIgnoreCase(c.getEmail()) && c.getExternalUserId() == null)
                .findFirst();

        if (preProvisionedContact.isPresent()) {
            Contact employee = preProvisionedContact.get();
            log.info("Matching AOT shadow profile found (ID: {}). Stamping external identity context token references.",
                    employee.getId());

            employee.setExternalUserId(externalId);
            employee.setStatus("ACTIVE");
            employee.setBusinessUnitId(tokenBuId); // Sync partition target

            contactService.update(employee.getId(), employee);
            return;
        }

        // State 3: Pure JIT Provisioning (Neither exists yet)
        log.info("Executing fallback dynamic JIT provisioning mapping for completely brand new user identity: {}",
                email);
        Contact newEmployee = new Contact();
        newEmployee.setExternalUserId(externalId);
        newEmployee.setName(name != null ? name : (email != null ? email : "Federated Corporate Employee"));
        newEmployee.setEmail(email);
        newEmployee.setSystemRole("INTERNAL_EMPLOYEE");
        newEmployee.setStatus("ACTIVE");
        newEmployee.setBusinessUnitId(tokenBuId);
        newEmployee.setSource("HYBRID_IDENTITY_PROVIDER");

        contactService.save(newEmployee);
    }
}