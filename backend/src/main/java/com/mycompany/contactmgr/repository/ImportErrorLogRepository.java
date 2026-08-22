package com.mycompany.contact_app.repository;

import com.mycompany.contact_app.entity.ImportErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ImportErrorLogRepository extends JpaRepository<ImportErrorLog, UUID> {
    List<ImportErrorLog> findByJobIdOrderByRowNumberAsc(UUID jobId);
}