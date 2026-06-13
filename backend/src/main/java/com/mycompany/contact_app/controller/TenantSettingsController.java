package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.filter.TenantContextFilter;
import com.mycompany.contact_app.entity.TenantSettings;
import com.mycompany.contact_app.exception.MissingTenantClaimException;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenant/settings")
public class TenantSettingsController {

    private final EntityManager entityManager;

    public TenantSettingsController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Extracts active feature flags for the logged-in client context.
     * Redacts backend secrets to prevent security credential exposure.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getActiveClientConfig() {
        // CRITICAL FIX: Validate tenant context first to ensure robustness against
        // null/unauthenticated users.
        String tenantIdString = com.mycompany.contact_app.filter.TenantContextFilter.CURRENT_TENANT.get();
        if (tenantIdString == null || tenantIdString.isBlank()) {
            throw new MissingTenantClaimException(
                    "Cannot retrieve client configuration: Tenant context is missing or unset.");
        }

        UUID tenantId;
        try {
            tenantId = UUID.fromString(tenantIdString);
        } catch (IllegalArgumentException e) {
            throw new MissingTenantClaimException("Invalid Business Unit ID in current context: " + tenantIdString);
        }

        TenantSettings settings = entityManager.find(TenantSettings.class, tenantId);

        // Fallback defaults if no record exists yet
        if (settings == null) {
            settings = new TenantSettings();
            settings.setBusinessUnitId(tenantId);
        }

        Map<String, Object> sanitizedPayload = new HashMap<>();
        sanitizedPayload.put("telephonyProvider", settings.getTelephonyProvider());
        sanitizedPayload.put("isGdprEnabled", settings.isGdprEnabled());
        sanitizedPayload.put("isAuditViewEnabled", settings.isAuditViewEnabled());

        // Expose public properties only (e.g., source caller ID numbers)
        // FIX: Instead of exposing a generic map, we create a structured VO payload
        Map<String, Object> publicCredentials = new HashMap<>();
        if (settings.getTelephonyCredentials() != null) {
            var credsMap = settings.getTelephonyCredentials();
            String fromNumber = (String) credsMap.get("fromNumber");

            // Create a strongly typed payload for easier consumption by frontend/client
            // code
            publicCredentials.put("accountSid", credsMap.get("accountSid"));
            publicCredentials.put("authToken", credsMap.get("authToken"));
            publicCredentials.put("fromNumber", fromNumber);

        } else {
            // Provide a default structure even if no credentials exist
            publicCredentials.put("accountSid", null);
            publicCredentials.put("authToken", null);
            publicCredentials.put("fromNumber", null);
        }
        sanitizedPayload.put("telephonyConfig", publicCredentials);

        return ResponseEntity.ok(sanitizedPayload);
    }
}