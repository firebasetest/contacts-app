package com.mycompany.contact_app.service;

import com.mycompany.contact_app.dto.ImportJobDTO;
import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.entity.ImportJob;
import com.mycompany.contact_app.repository.ContactRepository;
import com.mycompany.contact_app.repository.ImportJobRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsynchronousImportService {
    private static final Logger log = LoggerFactory.getLogger(AsynchronousImportService.class);
    private final ApplicationEventPublisher eventPublisher;
    private final ImportJobRepository jobRepository;
    private final ContactService contactService;
    private final ContactRepository contactRepository;
    private final MetadataRegistry metadataRegistry;

    public AsynchronousImportService(ApplicationEventPublisher ep, ImportJobRepository jr, ContactService cs,
            ContactRepository contactRepository, MetadataRegistry metadataRegistry) {
        this.eventPublisher = ep;
        this.jobRepository = jr;
        this.contactService = cs;
        this.contactRepository = contactRepository;
        this.metadataRegistry = metadataRegistry;
    }

    @Async("importTaskExecutor")
    @Transactional
    public void processImport(ImportJobDTO jobDto) {
        // Map DTO back to Entity for processing
        ImportJob job = jobRepository.findById(jobDto.getJobId())
                .orElseThrow(() -> new NoSuchElementException("Job not found"));

        job.setStatus("PROCESSING");
        jobRepository.save(job);

        try {
            // Logic to process items
            // ... (batch processing loop)
            // Business logic: parse file, validate via MetadataRegistry, save
            // Process items one by one to catch individual record failures
            List<Contact> contacts = fetchContactsFromSource(job);
            for (Contact contact : contacts) {
                try {
                    contactService.save(contact);
                    job.setProcessedRecords(job.getProcessedRecords() + 1);
                } catch (Exception e) {
                    log.error("Failed to import record: {}", contact.getName(), e);
                    // Append error to the job for the user to see
                    String existingError = job.getErrorMessage();
                    String updatedError = (existingError == null ? "" : existingError + "\n")
                            + "Failed ID " + contact.getId() + ": " + e.getMessage();
                    job.setErrorMessage(updatedError);
                }
            }
            job.setStatus("COMPLETED");

        } catch (Exception e) {
            log.error("Critical failure in job {}: {}", job.getJobId(), e.getMessage());
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
        } finally {
            // Finalize by publishing event
            jobRepository.save(job);
            eventPublisher.publishEvent(new ImportFinishedEvent(job.getJobId(), job.getStatus()));
        }
    }

    /**
     * Parses the raw input file into Contact entities.
     * In a real-world scenario, you would pull the file content from
     * storage (e.g., AWS S3 or a local temp directory) using the job ID.
     */
    private List<Contact> fetchContactsFromSource(ImportJob job) {
        List<Contact> contacts = new ArrayList<>();

        // Example: Path where the file was uploaded
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "contact-imports");
        Path filePath = tempDir.resolve(job.getJobId() + ".csv");

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                Contact contact = new Contact();
                contact.setName(values[0]);
                contact.setEmail(values[1]);
                contact.setStatus("ACTIVE");

                // CRITICAL: Ensure the contact belongs to the current tenant
                contact.setBusinessUnitId(job.getBusinessUnitId());

                contacts.add(contact);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse import file for job: " + job.getJobId(), e);
        }

        return contacts;
    }

    /**
     * Streams the file record by record, persisting in chunks to
     * maintain constant memory usage.
     */
    public void processImportStream(ImportJob job) {
        String filePath = "/temp/imports/" + job.getJobId() + ".csv";
        int chunkSize = 500;
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip header if necessary
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                Contact contact = parseLineToContact(line, job.getBusinessUnitId());

                // Validate against metadata registry
                metadataRegistry.validateAttributes(job.getBusinessUnitId(), contact.getCustomAttributes());

                contactRepository.save(contact);
                count++;

                // Optional: Periodically update job progress
                if (count % chunkSize == 0) {
                    // Update job.setProcessedRecords(count) in DB
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Streaming import failed", e);
        }
    }

    private Contact parseLineToContact(String line, UUID buId) {
        String[] values = line.split(",");
        Contact contact = new Contact();
        contact.setName(values[0]);
        contact.setEmail(values[1]);
        contact.setBusinessUnitId(buId);
        return contact;
    }

}
