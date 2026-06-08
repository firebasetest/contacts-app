package com.mycompany.contact_app.service;

import lombok.Getter;
import java.util.UUID;

@Getter
public class ImportFinishedEvent {
    private final UUID jobId;
    private final String finalStatus;

    public ImportFinishedEvent(UUID jobId, String finalStatus) {
        this.jobId = jobId;
        this.finalStatus = finalStatus;
    }
}