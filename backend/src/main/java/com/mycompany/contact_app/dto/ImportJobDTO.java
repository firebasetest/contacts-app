package com.mycompany.contact_app.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ImportJobDTO {
    private UUID jobId;
    private String status;
    private int totalRecords;
    private int processedRecords;
    private String errorMessage;
}