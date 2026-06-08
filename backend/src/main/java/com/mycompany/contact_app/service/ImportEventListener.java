package com.mycompany.contact_app.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ImportEventListener {

    @EventListener
    public void handleImportFinished(ImportFinishedEvent event) {
        // Logic for notifications (e.g., Send email to user, Update Dashboard cache)
        System.out.println("Job " + event.getJobId() + " finished with status: " + event.getFinalStatus());
    }
}