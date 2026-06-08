package com.mycompany.contact_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.mycompany.contact_app.config.TenantContextFilter;
import com.mycompany.contact_app.dto.ImportJobDTO;
import com.mycompany.contact_app.entity.ImportJob;
import com.mycompany.contact_app.service.AsynchronousImportService;
import com.mycompany.contact_app.repository.ImportJobRepository;

@RestController
@RequestMapping("/api/v1/imports")
public class ImportController {
    private final AsynchronousImportService importService;
    private final ImportJobRepository jobRepository;

    public ImportController(AsynchronousImportService importService, ImportJobRepository jobRepository) {
        this.importService = importService;
        this.jobRepository = jobRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<ImportJobDTO> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        UUID jobId = UUID.randomUUID();

        Path importDir = Paths.get(System.getProperty("java.io.tmpdir"), "contact-imports");
        Files.createDirectories(importDir);
        Path targetFile = importDir.resolve(jobId + ".csv");
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        // 1. Initialize the Job record
        ImportJob job = new ImportJob();
        job.setJobId(jobId);
        job.setStatus("PENDING");
        job.setBusinessUnitId(UUID.fromString(TenantContextFilter.CURRENT_TENANT.get()));
        job.setTotalRecords(countCsvRecords(targetFile));
        jobRepository.save(job);

        // Map to DTO
        ImportJobDTO dto = new ImportJobDTO();
        dto.setJobId(job.getJobId());
        dto.setStatus(job.getStatus());
        dto.setTotalRecords(job.getTotalRecords());

        // 2. Trigger the async process using DTO
        importService.processImport(dto);

        // 3. Return immediate response
        return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
    }

    private int countCsvRecords(Path file) throws IOException {
        try (var lines = Files.lines(file)) {
            return (int) lines.skip(1).count();
        }
    }

    @PostMapping("/trigger")
    public UUID triggerImport(@RequestBody ImportJobDTO jobDto) {
        importService.processImport(jobDto);
        return jobDto.getJobId();
    }

    @GetMapping("/{jobId}")
    public ImportJob getStatus(@PathVariable UUID jobId) {
        return jobRepository.findById(jobId).orElseThrow();
    }
}