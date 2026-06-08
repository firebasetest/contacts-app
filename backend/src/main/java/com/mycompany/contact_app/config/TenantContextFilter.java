package com.mycompany.contact_app.config;

import com.mycompany.contact_app.repository.BusinessUnitRepository; // New import
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

@Component
public class TenantContextFilter implements Filter {

    public static final String TENANT_HEADER = "X-BU-ID";
    public static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private final DataSource dataSource;
    private final BusinessUnitRepository buRepository; // Injected repository

    public TenantContextFilter(DataSource dataSource, BusinessUnitRepository buRepository) {
        this.dataSource = dataSource;
        this.buRepository = buRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String buIdString = httpRequest.getHeader(TENANT_HEADER);

        // 1. Basic validation
        if (buIdString == null || buIdString.isEmpty()) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-BU-ID header");
            return;
        }

        // 2. Business Unit Existence Check
        try {
            UUID buId = UUID.fromString(buIdString);
            if (!buRepository.existsById(buId)) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Invalid Business Unit ID");
                return;
            }
        } catch (IllegalArgumentException e) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID format");
            return;
        }

        // 3. Apply Tenant Context
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET LOCAL app.current_tenant = '" + buIdString + "'");
                CURRENT_TENANT.set(buIdString);
                chain.doFilter(request, response);
            } finally {
                CURRENT_TENANT.remove();
            }
        } catch (Exception e) {
            throw new ServletException("Could not set tenant context", e);
        }
    }
}