package com.mycompany.contactmgr.service;

import lombok.Getter;
import java.util.UUID;

@Getter
public class ImportFinishedEvent {
    private final UUID jobId;
    private final String finalStatus;
    private String businessUnitId;
    private String entityType;

    public ImportFinishedEvent(UUID jobId, String businessUnitId, String entityType, String finalStatus) {
        this.jobId = jobId;
        this.businessUnitId = businessUnitId;
        this.entityType = entityType;
        this.finalStatus = finalStatus;
    }

    // TODO: Event listeners handling audit logging, webhooks, or notification
    // emails need entityType to craft context-aware alerts (e.g., "Contact import
    // completed" vs "Company import completed").
}