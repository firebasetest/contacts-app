package com.mycompany.contact_app.config;

import com.mycompany.contact_app.repository.BusinessUnitRepository;
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
    private final BusinessUnitRepository buRepository;

    public TenantContextFilter(DataSource dataSource, BusinessUnitRepository buRepository) {
        this.dataSource = dataSource;
        this.buRepository = buRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String buIdString = httpRequest.getHeader(TENANT_HEADER);

        // 1. Basic Presence Validation
        if (buIdString == null || buIdString.isEmpty()) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-BU-ID header");
            return;
        }

        // 2. Strict Business Unit Database Existence Validation
        try {
            UUID buId = UUID.fromString(buIdString);
            if (!buRepository.existsById(buId)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or non-existent Business Unit ID");
                return;
            }
        } catch (IllegalArgumentException e) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID format in X-BU-ID header");
            return;
        }

        // 3. Mount Tenant Context on Database Connection Session and Execution Thread
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET LOCAL app.current_tenant = '" + buIdString + "'");
                CURRENT_TENANT.set(buIdString);
                chain.doFilter(request, response);
            } finally {
                CURRENT_TENANT.remove();
            }
        } catch (Exception e) {
            throw new ServletException("Could not apply multi-tenancy context", e);
        }
    }
}