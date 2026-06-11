package com.mycompany.contact_app.config;

import com.mycompany.contact_app.security.TenantContext;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            try (Statement stmt = connection.createStatement()) {
                // Safely isolated within the leased transaction boundary
                stmt.execute("SET LOCAL app.current_tenant = '" + tenantId + "'");
            }
        }
    }
}