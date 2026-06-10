package com.mycompany.contact_app.security;

import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.repository.ContactRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("contactSecurity")
public class ContactSecurityEvaluator {

    private final ContactRepository contactRepository;

    public ContactSecurityEvaluator(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    /**
     * Determines whether the current actor can manage (update, delete) an existing
     * contact.
     */
    public boolean canManageContact(UUID targetContactId) {
        BaseContact actor = getCurrentActor();

        // Rule A: Internal Employees & Onboarding Teams have global management access
        if (isInternalOrOnboarding(actor)) {
            return true;
        }

        // Rule B: Delegated Admins can only mutate contacts belonging to their own
        // company
        if ("DELEGATED_ADMIN".equalsIgnoreCase(actor.getSystemRole())) {
            BaseContact targetContact = contactRepository.findById(targetContactId)
                    .orElseThrow(() -> new IllegalArgumentException("Target contact record not found."));

            return isSameCompanyHierarchy(actor, targetContact);
        }

        return false;
    }

    /**
     * Preserved: Validates if an actor can provision a new contact under a parent
     * entity.
     */
    public boolean canCreateContactUnderCompany(UUID targetCompanyId) {
        BaseContact actor = getCurrentActor();

        if (isInternalOrOnboarding(actor)) {
            return true;
        }

        if ("DELEGATED_ADMIN".equalsIgnoreCase(actor.getSystemRole())) {
            return actor.getParentCompany() != null && actor.getParentCompany().getId().equals(targetCompanyId);
        }

        return false;
    }

    /**
     * Preserved: Validates global administrative authority (e.g., role delegation
     * targets).
     */
    public boolean isInternalEmployee() {
        try {
            BaseContact actor = getCurrentActor();
            return isInternalOrOnboarding(actor);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper to isolate high-privilege operations personnel contexts.
     */
    private boolean isInternalOrOnboarding(BaseContact actor) {
        String role = actor.getSystemRole();
        return "INTERNAL_EMPLOYEE".equalsIgnoreCase(role) || "ONBOARDING_TEAM".equalsIgnoreCase(role);
    }

    /**
     * Enforces corporate scoping boundaries between delegated actors and targets.
     */
    private boolean isSameCompanyHierarchy(BaseContact actor, BaseContact target) {
        if (actor.getParentCompany() == null || target.getParentCompany() == null) {
            return false;
        }
        return actor.getParentCompany().getId().equals(target.getParentCompany().getId());
    }

    /**
     * Preserved: Resolves the active external subject context string from the JWT
     * OIDC layer.
     */
    public String getCurrentExternalUserId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new SecurityException("No active authentication context detected.");
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Helper to load the current system actor profile out of the multi-tenant data
     * layer.
     */
    private BaseContact getCurrentActor() {
        String externalId = getCurrentExternalUserId();
        return contactRepository.findByExternalUserId(externalId)
                .orElseThrow(() -> new SecurityException("Access Denied: Unrecognized caller system identity."));
    }

    /**
     * Rule: Only internal operations or onboarding team members can create a new
     * company profile.
     * Delegated admins manage contacts within their pre-existing company but cannot
     * create new companies.
     */
    public boolean canCreateCompany() {
        try {
            BaseContact actor = getCurrentActor();
            return isInternalOrOnboarding(actor);
        } catch (Exception e) {
            return false;
        }
    }
}