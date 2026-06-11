package com.mycompany.contact_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import com.mycompany.contact_app.filter.TenantContextFilter;
import com.mycompany.contact_app.dto.ImportJobDTO;
import com.mycompany.contact_app.dto.ImportSummaryReportDto;
import com.mycompany.contact_app.entity.ImportErrorLog;
import com.mycompany.contact_app.entity.ImportJob;
import com.mycompany.contact_app.service.PolymorphicImportService;
import com.mycompany.contact_app.repository.ImportErrorLogRepository;
import com.mycompany.contact_app.repository.ImportJobRepository;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

@RestController
@RequestMapping("/api/v1/imports")
public class ImportController {

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
    public ResponseEntity<ImportJobDTO> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot process an empty file upload.");
        }

        UUID jobId = UUID.randomUUID();
        String originalName = file.getOriginalFilename();

        // Extract file extension to choose proper parsing strategy downstream
        String extension = ".csv";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }

        // Setup temporary isolated staging directory
        Path importDir = Paths.get(System.getProperty("java.io.tmpdir"), "contact-imports");
        Files.createDirectories(importDir);

        // Retain extension so the service thread correctly instantiates Apache POI or
        // Commons CSV
        Path targetFile = importDir.resolve(jobId + extension);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        // Calculate total structural record lines beforehand to maintain accurate
        // progress indicators
        int totalRecords = calculateRecordCount(targetFile, extension);

        // 1. Initialize the Multi-Tenant Job Tracking Record
        ImportJob job = new ImportJob();
        job.setJobId(jobId);
        job.setStatus("PENDING");
        job.setBusinessUnitId(UUID.fromString(TenantContextFilter.CURRENT_TENANT.get()));
        job.setTotalRecords(totalRecords);
        job.setProcessedRecords(0);
        jobRepository.save(job);

        // Map state out to transfer object
        ImportJobDTO dto = new ImportJobDTO();
        dto.setJobId(job.getJobId());
        dto.setStatus(job.getStatus());
        dto.setTotalRecords(job.getTotalRecords());
        dto.setProcessedRecords(0);

        // 2. Fire-and-forget execution block context onto dedicated worker pools
        importService.processImport(dto);

        // 3. Return immediate tracking handle acknowledgement back to the client
        return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
    }

    @PostMapping("/trigger")
    public UUID triggerImport(@RequestBody ImportJobDTO jobDto) {
        importService.processImport(jobDto);
        return jobDto.getJobId();
    }

    @GetMapping("/{jobId}")
    public ImportJob getStatus(@PathVariable UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Import track profile reference not found for ID: " + jobId));
    }

    /**
     * Helper to compute file size thresholds safely depending on document metadata
     * characteristics.
     */
    private int calculateRecordCount(Path file, String extension) throws IOException {
        if (".xlsx".equals(extension) || ".xls".equals(extension)) {
            try (InputStream is = Files.newInputStream(file);
                    Workbook workbook = WorkbookFactory.create(is)) {
                var sheet = workbook.getSheetAt(0);
                // Total lines equals physical mapped rows minus the index row header line block
                int totalRows = sheet.getPhysicalNumberOfRows();
                return totalRows > 0 ? totalRows - 1 : 0;
            } catch (Exception e) {
                throw new IOException("Failed parsing row structures within the uploaded Excel sheet.", e);
            }
        } else {
            // High efficiency line counter sequence stream for flat CSV files
            try (var lines = Files.lines(file)) {
                return (int) lines.skip(1).count();
            }
        }
    }

    @GetMapping("/{jobId}/report")
    public ResponseEntity<ImportSummaryReportDto> getImportJobReport(@PathVariable UUID jobId) {
        ImportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Import job not found for tracking reference: " + jobId));

        List<ImportErrorLog> granularErrors = errorLogRepository.findByJobIdOrderByRowNumberAsc(jobId);

        ImportSummaryReportDto report = new ImportSummaryReportDto();
        report.setJobId(job.getJobId());
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