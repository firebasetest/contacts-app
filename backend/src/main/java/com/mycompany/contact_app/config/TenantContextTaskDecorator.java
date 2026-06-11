package com.mycompany.contact_app.config;

import com.mycompany.contact_app.security.TenantContext;
import org.springframework.core.task.TaskDecorator;

public class TenantContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture tenant ID from the web-request thread
        String tenantId = TenantContext.getCurrentTenant();

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