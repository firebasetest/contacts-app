package com.mycompany.contact_app.filter;

import com.mycompany.contact_app.security.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantId = extractTenantFromJwt(request);
            if (tenantId != null) {
                // Validate string format strictly as a UUID to neutralize SQL Injection
                TenantContext.setCurrentTenant(UUID.fromString(tenantId).toString());
            }
            filterChain.doFilter(request, response);
        } finally {
            // Crucial: Clear context after request completion to avoid thread-leak
            // contamination
            TenantContext.clear();
        }
    }

    private String extractTenantFromJwt(HttpServletRequest request) {
        // Implement your existing JWT parsing logic here
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Extract and return business_unit_id claim...
        }
        return null;
    }
}