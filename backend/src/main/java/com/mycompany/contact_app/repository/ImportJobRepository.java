package com.mycompany.contact_app.repository;

import com.mycompany.contact_app.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {

    // Find all jobs for a specific BU
    List<ImportJob> findByBuId(UUID buId);

    // Find jobs by status (e.g., for finding stuck jobs)
    List<ImportJob> findByStatus(String status);
}