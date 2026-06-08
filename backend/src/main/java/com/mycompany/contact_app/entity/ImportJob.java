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
    private UUID jobId;

    private UUID buId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private int totalRecords;
    private int processedRecords;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public void setBusinessUnitId(UUID fromString) {
        this.buId = fromString;
    }

    public UUID getBusinessUnitId() {
        return this.buId;
    }
}