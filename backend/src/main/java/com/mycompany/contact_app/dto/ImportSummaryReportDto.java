package com.mycompany.contact_app.dto;

import com.mycompany.contact_app.entity.ImportErrorLog;
import java.util.List;
import java.util.UUID;

public class ImportSummaryReportDto {
    private UUID jobId;
    private String status;
    private int totalRecords;
    private int processedRecords;
    private int insertedRecords;
    private int updatedRecords;
    private int failedRecords;
    private String globalErrorMessage;
    private List<ImportErrorLog> errorLogs;

    // Getters and Setters
    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(int processedRecords) {
        this.processedRecords = processedRecords;
    }

    public int getInsertedRecords() {
        return insertedRecords;
    }

    public void setInsertedRecords(int insertedRecords) {
        this.insertedRecords = insertedRecords;
    }

    public int getUpdatedRecords() {
        return updatedRecords;
    }

    public void setUpdatedRecords(int updatedRecords) {
        this.updatedRecords = updatedRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(int failedRecords) {
        this.failedRecords = failedRecords;
    }

    public String getGlobalErrorMessage() {
        return globalErrorMessage;
    }

    public void setGlobalErrorMessage(String globalErrorMessage) {
        this.globalErrorMessage = globalErrorMessage;
    }

    public List<ImportErrorLog> getErrorLogs() {
        return errorLogs;
    }

    public void setErrorLogs(List<ImportErrorLog> errorLogs) {
        this.errorLogs = errorLogs;
    }
}