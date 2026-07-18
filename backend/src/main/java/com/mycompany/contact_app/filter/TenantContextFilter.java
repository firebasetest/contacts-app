package com.mycompany.contact_app.filter;

import com.mycompany.contact_app.repository.BusinessUnitRepository;
import com.mycompany.contact_app.security.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@Order(1) // Ensures the filter runs early in the custom filter execution pipeline
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-BU-ID";
    public static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /*
     * 
     * 
     * private final DataSource dataSource;
     * private final BusinessUnitRepository buRepository;
     * 
     * public TenantContextFilter(DataSource dataSource, BusinessUnitRepository
     * buRepository) {
     * this.dataSource = dataSource;
     * this.buRepository = buRepository;
     * }
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String tenantId = null;

        // Strategy A: Check for an explicit custom header parameter override
        String headerTenant = request.getHeader(TENANT_HEADER);
        if (headerTenant != null && !headerTenant.trim().isEmpty()) {
            tenantId = headerTenant.trim();
        } else {
            // Strategy B: Fallback to reading security claims from decoded JWT token
            // identities
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                tenantId = jwt.getClaimAsString("business_unit_id");
            }
        }

        // Apply tenant isolation context context if resolved
        if (tenantId != null && !tenantId.isEmpty()) {
            TenantContext.setCurrentTenant(tenantId);
        }

        try {
            // Pass the execution control down along the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: Always release thread-local state allocations to prevent context
            // pollution
            // inside pooled worker threads serving other client routines
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

    /*
     * @Override
     * public void doFilter(ServletRequest request, ServletResponse response,
     * FilterChain chain)
     * throws IOException, ServletException {
     * 
     * HttpServletRequest httpRequest = (HttpServletRequest) request;
     * HttpServletResponse httpResponse = (HttpServletResponse) response;
     * String buIdString = httpRequest.getHeader(TENANT_HEADER);
     * 
     * // 1. Basic Presence Validation
     * if (buIdString == null || buIdString.isEmpty()) {
     * httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
     * "Missing X-BU-ID header");
     * return;
     * }
     * 
     * // 2. Strict Business Unit Database Existence Validation
     * try {
     * UUID buId = UUID.fromString(buIdString);
     * if (!buRepository.existsById(buId)) {
     * httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
     * "Invalid or non-existent Business Unit ID");
     * return;
     * }
     * } catch (IllegalArgumentException e) {
     * httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
     * "Invalid UUID format in X-BU-ID header");
     * return;
     * }
     * 
     * // 3. Mount Tenant Context on Database Connection Session and Execution
     * Thread
     * try (Connection conn = dataSource.getConnection()) {
     * try (Statement stmt = conn.createStatement()) {
     * stmt.execute("SET LOCAL app.current_tenant = '" + buIdString + "'");
     * CURRENT_TENANT.set(buIdString);
     * chain.doFilter(request, response);
     * } finally {
     * CURRENT_TENANT.remove();
     * }
     * } catch (Exception e) {
     * throw new ServletException("Could not apply multi-tenancy context", e);
     * }
     * }
     */
}