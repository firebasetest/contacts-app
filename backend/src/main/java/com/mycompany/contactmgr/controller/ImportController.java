package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.security.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.mycompany.contact_app.dto.ImportJobDTO;
import com.mycompany.contact_app.dto.ImportSummaryReportDto;
import com.mycompany.contact_app.entity.ImportErrorLog;
import com.mycompany.contact_app.entity.ImportJob;
import com.mycompany.contact_app.service.PolymorphicImportService;
import com.mycompany.contact_app.repository.ImportErrorLogRepository;
import com.mycompany.contact_app.repository.ImportJobRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/imports")
public class ImportController {

    private static final Logger log = LoggerFactory.getLogger(ImportController.class);
    private final PolymorphicImportService importService;
    private final ImportJobRepository jobRepository;
    private final ImportErrorLogRepository errorLogRepository;

    public ImportController(PolymorphicImportService importService,
            ImportJobRepository jobRepository,
            ImportErrorLogRepository errorLogRepository) {
        this.importService = importService;
        this.jobRepository = jobRepository;
        this.errorLogRepository = errorLogRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<ImportJobDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "entityType", defaultValue = "GENERAL") String entityType) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file cannot be empty.");
        }

        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant context missing.");
        }

        Path tenantUploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "contact-imports", tenantId);
        Path targetPath = null;

        try {
            Files.createDirectories(tenantUploadDir);
            String originalFilename = StringUtils.cleanPath(
                    Objects.requireNonNullElse(file.getOriginalFilename(), "import.csv"));
            targetPath = tenantUploadDir.resolve(UUID.randomUUID() + "_" + originalFilename);

            // Copy stream directly to disk
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Efficient streaming record count
            int totalRecords = importService.calculateRecordCount(targetPath, originalFilename);

            // 2. Fire-and-forget execution block context onto dedicated worker pools
            ImportJobDTO jobDTO = importService.triggerImport(tenantId, targetPath.toString(), entityType,
                    totalRecords);
            // 3. Return immediate tracking handle acknowledgement back to the client
            return ResponseEntity.accepted().body(jobDTO);

        } catch (Exception e) {
            if (targetPath != null) {
                try {
                    Files.deleteIfExists(targetPath);
                } catch (IOException ignored) {
                }
            }
            log.error("Failed file upload processing for tenant {}", tenantId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Import upload initialization failed.");
        }
    }

    @PostMapping("/trigger")
    public ResponseEntity<UUID> triggerImport(
            @RequestBody ImportJobDTO jobDto,
            @RequestParam(value = "entityType", defaultValue = "GENERAL") String entityType) {

        if (jobDto.getEntityType() == null || jobDto.getEntityType().isBlank()) {
            jobDto.setEntityType(entityType);
        }
        importService.processImport(jobDto);
        return ResponseEntity.ok(jobDto.getJobId());
    }

    @GetMapping("/{jobId}")
    public ImportJob getStatus(@PathVariable @NonNull UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Import track profile reference not found for ID: " + jobId));
    }

    @GetMapping("/{jobId}/report")
    public ResponseEntity<ImportSummaryReportDto> getImportJobReport(@PathVariable @NonNull UUID jobId) {
        ImportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Import job not found: " + jobId));

        List<ImportErrorLog> granularErrors = errorLogRepository.findByJobIdOrderByRowNumberAsc(jobId);

        ImportSummaryReportDto report = new ImportSummaryReportDto();
        report.setJobId(job.getJobId());
        report.setBusinessUnitId(job.getBusinessUnitId().toString());
        report.setEntityType(job.getEntityType());
        report.setStatus(job.getStatus());
        report.setTotalRecords(job.getTotalRecords());
        report.setProcessedRecords(job.getProcessedRecords());
        report.setInsertedRecords(job.getInsertedRecords());
        report.setUpdatedRecords(job.getUpdatedRecords());
        report.setFailedRecords(job.getFailedRecords());
        report.setGlobalErrorMessage(job.getErrorMessage());
        report.setErrorLogs(granularErrors);

        return ResponseEntity.ok(report);
    }
}