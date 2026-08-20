package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.security.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import com.mycompany.contact_app.dto.ImportJobDTO;
import com.mycompany.contact_app.dto.ImportSummaryReportDto;
import com.mycompany.contact_app.entity.ImportErrorLog;
import com.mycompany.contact_app.entity.ImportJob;
import com.mycompany.contact_app.service.PolymorphicImportService;
import com.mycompany.contact_app.repository.ImportErrorLogRepository;
import com.mycompany.contact_app.repository.ImportJobRepository;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.monitorjbl.xlsx.StreamingReader; // Excel Streaming Reader library
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

    /*
     * Problem: Files copied to the temporary directory (/tmp/contact-imports/) are
     * never deleted after execution, causing disk space exhaustion over time.
     * Furthermore, loading Excel files with WorkbookFactory.create(is) reads entire
     * workbooks into memory at once, risking OutOfMemoryError on large file
     * uploads.
     * Fix: Implement a finally block or worker cleanup process to delete staged
     * temporary files, and switch to a streaming Excel reader (such as Excel
     * Streaming Reader or POI SAX handler) for counting rows.
     */

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
        job.setBusinessUnitId(UUID.fromString(TenantContext.getCurrentTenant()));
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

    @PostMapping("/upload")
    public ResponseEntity<ImportJobDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file cannot be empty.");
        }

        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant context missing.");
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        Path tenantUploadDir = Paths.get(tmpDir, "contact-imports", tenantId);
        Path targetPath = null;

        try {
            Files.createDirectories(tenantUploadDir);

            String originalFilename = StringUtils.cleanPath(
                    Objects.requireNonNullElse(file.getOriginalFilename(), "import.csv"));
            String safeFilename = UUID.randomUUID() + "_" + originalFilename;
            targetPath = tenantUploadDir.resolve(safeFilename);

            // Copy stream directly to disk
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Efficient streaming record count
            int totalRecords = calculateRecordCount(targetPath, originalFilename);

            // 2. Fire-and-forget execution block context onto dedicated worker pools
            ImportJobDTO jobDTO = importService.triggerImport(tenantId, targetPath.toString(), entityType,
                    totalRecords);

            // 3. Return immediate tracking handle acknowledgement back to the client
            return ResponseEntity.ok(jobDTO);

        } catch (IllegalArgumentException e) {
            cleanupTempFile(targetPath);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            cleanupTempFile(targetPath);
            log.error("Failed to process file upload for tenant {}", tenantId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File processing failed.");
        }
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
    private int calculateRecordCount(Path filePath, String filename) throws IOException {
        String lowerName = filename.toLowerCase();

        if (lowerName.endsWith(".csv")) {
            try (Stream<String> lines = Files.lines(filePath)) {
                long lineCount = lines.count();
                return lineCount > 1 ? (int) lineCount - 1 : 0; // Exclude header row
            }
        } else if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
            return countExcelRowsStreaming(filePath);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Supported formats: .csv, .xlsx, .xls");
        }
    }

    private int countExcelRowsStreaming(Path filePath) throws IOException {
        // Stream rows using a small memory buffer instead of reading the entire DOM
        // into heap
        try (InputStream is = Files.newInputStream(filePath);
                Workbook workbook = StreamingReader.builder()
                        .rowCacheSize(100)
                        .bufferSize(4096)
                        .open(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 0;
            for (var row : sheet) {
                rowCount++;
            }
            return rowCount > 1 ? rowCount - 1 : 0; // Exclude header row
        }
    }

    private void cleanupTempFile(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.warn("Failed to delete temporary staging file: {}", path, e);
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