package com.mycompany.contactmgr.config;

import com.mycompany.contactmgr.security.TenantContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

public class TenantContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(@NonNull Runnable runnable) {
        // Capture tenant ID from the web-request thread
        String tenantId = TenantContext.getNonNullCurrentTenant();

        return () -> {
            try {
                if (tenantId != null) {
                    // Apply it to the background worker thread pool execution
                    TenantContext.setCurrentTenant(tenantId);
                }
                runnable.run();
            } finally {
                // Prevent memory leaks on worker pool threads
                TenantContext.clear();
            }
        };
    }
}