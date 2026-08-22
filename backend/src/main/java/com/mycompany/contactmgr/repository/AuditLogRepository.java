package com.mycompany.contact_app.repository;

import com.mycompany.contact_app.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityId(UUID entityId);

    List<AuditLog> findByEntityType(String entityType);
}