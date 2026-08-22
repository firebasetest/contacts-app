package com.mycompany.contactmgr.config;

import com.mycompany.contactmgr.security.TenantContext;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Inner proxy class to lazily set PostgreSQL context variables
 * upon leasing a connection from the pool.
 */
class TenantAwareDataSource extends DelegatingDataSource {

    public TenantAwareDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        applyTenantContext(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        applyTenantContext(connection);
        return connection;
    }

    private void applyTenantContext(Connection connection) throws SQLException {
        String tenantId = TenantContext.getNonNullCurrentTenant();
        if (tenantId != null) {
            try (PreparedStatement stmt = connection.prepareStatement("SET LOCAL app.current_tenant = ?")) {
                // Use setString instead of concatenation for safety
                stmt.setString(1, tenantId);
                // Safely isolated within the leased transaction boundary
                stmt.execute();
            }
        }
    }
}