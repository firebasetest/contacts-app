package com.mycompany.contactmgr.repository;

import com.mycompany.contactmgr.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityId(UUID entityId);

    List<AuditLog> findByEntityType(String entityType);
}