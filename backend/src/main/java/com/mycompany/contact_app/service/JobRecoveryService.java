package com.mycompany.contact_app.service;

import java.time.LocalDateTime;

import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mycompany.contact_app.entity.ImportJob;
import com.mycompany.contact_app.repository.ImportJobRepository;

@Component
public class JobRecoveryService {

    private final ImportJobRepository repository;

    public JobRecoveryService(ImportJobRepository repository) {
        this.repository = repository;
    }

    /*
     * @Scheduled(fixedRate = 3600000) // Every hour
     * public void recoverStuckJobs() {
     * List<ImportJob> stuckJobs = repository.findByStatus("PROCESSING");
     * for (ImportJob job : stuckJobs) {
     * // If the job hasn't updated in 2 hours, mark as FAILED
     * if (job.getUpdatedAt().isBefore(LocalDateTime.now().minusHours(2))) {
     * job.setStatus("FAILED");
     * job.setErrorMessage("System timeout: Job stuck in processing.");
     * repository.save(job);
     * }
     * }
     * }
     */
    @Scheduled(fixedRate = 3600000) // Runs every hour
    public void recoverStuckJobs() {
        repository.findByStatus("PROCESSING").stream()
                .filter(job -> job.getUpdatedAt().isBefore(LocalDateTime.now().minusHours(2)))
                .forEach(job -> {
                    job.setStatus("FAILED");
                    job.setErrorMessage("Auto-recovery: Job timed out.");
                    repository.save(job);
                });
    }
}