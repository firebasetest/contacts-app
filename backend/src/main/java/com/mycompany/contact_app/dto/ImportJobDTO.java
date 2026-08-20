package com.mycompany.contact_app.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ImportJobDTO {
    private UUID jobId;
    private String businessUnitId;
    private String status;
    private int totalRecords;
    private int processedRecords;
    private String errorMessage;
    private String filePath;
    private String entityType;
    private LocalDateTime createdAt;
}