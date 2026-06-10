package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.config.TenantContextFilter;
import com.mycompany.contact_app.entity.TenantSettings;
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
        UUID tenantId = UUID.fromString(TenantContextFilter.CURRENT_TENANT.get());

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
        Map<String, Object> publicCredentials = new HashMap<>();
        if (settings.getTelephonyCredentials() != null) {
            publicCredentials.put("fromNumber", settings.getTelephonyCredentials().get("fromNumber"));
        }
        sanitizedPayload.put("telephonyConfig", publicCredentials);

        return ResponseEntity.ok(sanitizedPayload);
    }
}