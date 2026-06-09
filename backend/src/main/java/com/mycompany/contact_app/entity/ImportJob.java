package com.mycompany.contact_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

    @Column(name = "business_unit_id", nullable = false)
    private UUID buId;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "total_records")
    private int totalRecords;

    @Column(name = "processed_records")
    private int processedRecords;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public void setBusinessUnitId(UUID fromString) {
        this.buId = fromString;
    }

    public UUID getBusinessUnitId() {
        return this.buId;
    }
}