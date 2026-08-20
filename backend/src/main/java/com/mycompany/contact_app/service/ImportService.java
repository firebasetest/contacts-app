package com.mycompany.contact_app.service;

import com.mycompany.contact_app.dto.ImportJobDTO;

public interface ImportService {
    ImportJobDTO triggerImport(String tenantId, String filePath, String entityType, int totalRecords);
}