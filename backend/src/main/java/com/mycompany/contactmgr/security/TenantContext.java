package com.mycompany.contact_app.security;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static boolean isSame(UUID tenantId) {
        return tenantId.equals(UUID.fromString(CURRENT_TENANT.get()));
    }
}