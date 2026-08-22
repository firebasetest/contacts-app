package com.mycompany.contactmgr.service;

import com.mycompany.contactmgr.dto.ImportJobDTO;

public interface ImportService {
    ImportJobDTO triggerImport(String tenantId, String filePath, String entityType, int totalRecords);
}