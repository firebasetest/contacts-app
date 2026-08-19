package com.mycompany.contact_app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldStoreCurrentTenant() {
        TenantContext.setCurrentTenant("tenant-a");

        assertEquals("tenant-a", TenantContext.getCurrentTenant());
    }

    @Test
    void shouldReplaceCurrentTenant() {
        TenantContext.setCurrentTenant("tenant-a");
        TenantContext.setCurrentTenant("tenant-b");

        assertEquals("tenant-b", TenantContext.getCurrentTenant());
    }

    @Test
    void shouldClearCurrentTenant() {
        TenantContext.setCurrentTenant("tenant-a");

        TenantContext.clear();

        assertNull(TenantContext.getCurrentTenant());
    }
}
