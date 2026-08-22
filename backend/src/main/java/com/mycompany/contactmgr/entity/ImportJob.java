package com.mycompany.contactmgr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "import_jobs")
@Getter
@Setter
public class ImportJob extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "job_id")
    private UUID jobId;

    // Isolate job records per tenant boundary
    @Column(name = "business_unit_id", nullable = false)
    private UUID buId;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "file_path")
    private String filePath;

    // Track default target entity type (e.g., CONTACT, COMPANY, GENERAL)
    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "total_records")
    private int totalRecords;

    @Column(name = "processed_records")
    private int processedRecords;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Columns to capture the broken-down statistics for better reporting
    @Column(name = "inserted_records")
    private int insertedRecords = 0;

    @Column(name = "updated_records")
    private int updatedRecords = 0;

    @Column(name = "failed_records")
    private int failedRecords = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void setBusinessUnitId(UUID fromString) {
        this.buId = fromString;
    }

    public UUID getBusinessUnitId() {
        return this.buId;
    }
}