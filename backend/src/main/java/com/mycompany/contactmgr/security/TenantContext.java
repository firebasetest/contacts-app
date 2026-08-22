package com.mycompany.contactmgr.security;

import java.util.UUID;
import java.util.Optional;

public class TenantContext {
    private TenantContext() {
        /* This utility class should not be instantiated */
    }

    // ThreadLocal ensures that the context is isolated per thread request
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Sets the current tenant ID for the duration of the thread's execution.
     * 
     * @param tenantId The ID of the current Business Unit.
     */
    public static void setCurrentTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID must not be null or blank.");
        }
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Gets the current tenant ID for the duration of the thread's execution.
     * 
     * @return Optional containing the ID, or empty if no context has been set.
     */
    public static Optional<String> getCurrentTenant() {
        return Optional.ofNullable(CURRENT_TENANT.get());
    }

    /**
     * Retrieves the current tenant ID, throwing a runtime exception if the context
     * is missing.
     * Use this only when the calling method absolutely guarantees that the context
     * MUST be present.
     * 
     * @throws IllegalStateException if the tenant context is not set.
     */
    public static String getNonNullCurrentTenant() {
        return getCurrentTenant()
                .orElseThrow(() -> new IllegalStateException("Tenant context is required but not set."));
    }

    /**
     * MUST be called in a finally block to clean up the thread's memory.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static boolean isSame(UUID tenantId) {
        String currentTenantId = CURRENT_TENANT.get();
        if (currentTenantId == null || currentTenantId.isBlank()) {
            throw new IllegalStateException("No current tenant set.");
        }
        return tenantId.equals(UUID.fromString(currentTenantId));
    }
}