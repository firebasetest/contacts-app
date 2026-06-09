package com.mycompany.contact_app.security;

import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.repository.ContactRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component("contactSecurity")
public class ContactSecurityEvaluator {

    private final ContactRepository contactRepository;

    public ContactSecurityEvaluator(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    /**
     * Extracts the OIDC unique 'sub' identifier from the current security context
     * context.
     */
    private String getCurrentExternalUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getSubject(); // Returns the OIDC token 'sub' field
        }
        throw new IllegalStateException("Authentication principal is not a valid OIDC JWT token.");
    }

    /**
     * Determines whether the current caller can manage the lifecycle of a target
     * contact record.
     */
    public boolean canManageContact(UUID targetContactId) {
        String externalId = getCurrentExternalUserId();

        // 1. Look up the actor contact profile
        BaseContact actor = contactRepository.findByExternalUserId(externalId)
                .orElseThrow(() -> new SecurityException("No registered contact profile found for actor identity."));

        // If internal employee, allow anything within their BU context (Postgres RLS
        // auto-filters BU scoping)
        if ("INTERNAL_EMPLOYEE".equalsIgnoreCase(actor.getSystemRole())) {
            return true;
        }

        // If delegated admin, perform structural company domain ownership matching
        if ("DELEGATED_ADMIN".equalsIgnoreCase(actor.getSystemRole())) {
            if (actor.getParentCompany() == null) {
                return false; // A delegated admin must be associated with a corporate entity
            }

            UUID adminCompanyId = actor.getParentCompany().getId();

            // Fetch target contact information
            BaseContact target = contactRepository.findById(targetContactId)
                    .orElseThrow(() -> new IllegalArgumentException("Target contact record not found."));

            // Allow management if and only if the target's company matches the delegated
            // admin's company
            return target.getParentCompany() != null && target.getParentCompany().getId().equals(adminCompanyId);
        }

        return false; // Baseline regular contacts cannot manage external records
    }

    /**
     * Determines whether the caller can create a new contact under a specific
     * parent company.
     */
    public boolean canCreateContactUnderCompany(UUID targetCompanyId) {
        String externalId = getCurrentExternalUserId();
        BaseContact actor = contactRepository.findByExternalUserId(externalId)
                .orElseThrow(() -> new SecurityException("Access Denied: Unrecognized actor identity."));

        if ("INTERNAL_EMPLOYEE".equalsIgnoreCase(actor.getSystemRole())) {
            return true;
        }

        if ("DELEGATED_ADMIN".equalsIgnoreCase(actor.getSystemRole())) {
            return actor.getParentCompany() != null && actor.getParentCompany().getId().equals(targetCompanyId);
        }

        return false;
    }

    /**
     * Validates whether the caller has the administrative authorization required to
     * assign delegated management roles.
     */
    public boolean isInternalEmployee() {
        String externalId = getCurrentExternalUserId();
        return contactRepository.findByExternalUserId(externalId)
                .map(actor -> "INTERNAL_EMPLOYEE".equalsIgnoreCase(actor.getSystemRole()))
                .orElse(false);
    }
}