package com.mycompany.contactmgr.repository;

import com.mycompany.contactmgr.entity.ImportErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ImportErrorLogRepository extends JpaRepository<ImportErrorLog, UUID> {
    List<ImportErrorLog> findByJobIdOrderByRowNumberAsc(UUID jobId);
}