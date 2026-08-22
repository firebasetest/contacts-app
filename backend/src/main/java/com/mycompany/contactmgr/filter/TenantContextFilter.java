package com.mycompany.contactmgr.filter;

import com.mycompany.contactmgr.security.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
import java.util.Optional;

/**
 * Filters all incoming requests to establish the tenant context.
 * Requires a custom header: X-Tenant-Id
 */
@Component
@Order(1) // Ensures the filter runs early in the custom filter execution pipeline
public class TenantContextFilter extends OncePerRequestFilter {

    // private static final String TENANT_HEADER = "X-Tenant-Id";
    public static final String TENANT_HEADER = "X-BU-ID";
    // public static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

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

        // 1. Attempt to extract the tenant ID from the custom header
        // Strategy A: Check for an explicit custom header parameter override
        String headerTenant = Optional.ofNullable(request.getHeader(TENANT_HEADER))
                .orElse(null);
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
        // 2. Handle missing/invalid context
        if (tenantId == null || tenantId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            response.getWriter().write("Authentication Error: Missing required header " + TENANT_HEADER);
            return;
        }

        try {
            // 3. Set the context (Success Path)
            // Apply tenant isolation context context if resolved
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setCurrentTenant(tenantId);
            }

            // 4. Continue the filter chain
            // Pass the execution control down along the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // 5. CRITICAL: Always clean up the ThreadLocal context
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