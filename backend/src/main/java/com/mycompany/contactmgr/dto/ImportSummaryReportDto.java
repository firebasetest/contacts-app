package com.mycompany.contactmgr.dto;

import com.mycompany.contactmgr.entity.ImportErrorLog;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class ImportSummaryReportDto {
    // Getters and Setters
    private UUID jobId;
    private String status;
    private String businessUnitId;
    private String entityType; // Added for downstream analytics and reporting UI
    private int totalRecords;
    private int processedRecords;
    private int insertedRecords;
    private int updatedRecords;
    private int failedRecords;
    private String globalErrorMessage;
    private List<ImportErrorLog> errorLogs;

}