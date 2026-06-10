package com.mycompany.contact_app.service;

import com.mycompany.contact_app.domain.MetadataRegistry;
import com.mycompany.contact_app.dto.ImportJobDTO;
import com.mycompany.contact_app.dto.ImportRowDto;
import com.mycompany.contact_app.entity.*;
import com.mycompany.contact_app.repository.ImportJobRepository;
import jakarta.persistence.EntityManager;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
public class PolymorphicImportService {

    private static final Logger log = LoggerFactory.getLogger(PolymorphicImportService.class);
    private static final int BATCH_SIZE = 100;

    private final ImportJobRepository jobRepository;
    private final MetadataRegistry metadataRegistry;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    public PolymorphicImportService(ImportJobRepository jobRepository,
            MetadataRegistry metadataRegistry,
            EntityManager entityManager,
            ApplicationEventPublisher eventPublisher) {
        this.jobRepository = jobRepository;
        this.metadataRegistry = metadataRegistry;
        this.entityManager = entityManager;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Spawns an asynchronous worker thread to parse, deduplicate, validate,
     * and persist batch datasets securely within structural multi-tenant
     * boundaries.
     */
    @Async("importTaskExecutor")
    @Transactional
    public void processImport(ImportJobDTO jobDto) {
        ImportJob job = jobRepository.findById(jobDto.getJobId())
                .orElseThrow(
                        () -> new NoSuchElementException("Import job profile missing for ID: " + jobDto.getJobId()));

        job.setStatus("PROCESSING");
        jobRepository.saveAndFlush(job);

        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "contact-imports");
        File file = locateUploadedFile(tempDir, job.getJobId());

        if (file == null || !file.exists()) {
            job.setStatus("FAILED");
            job.setErrorMessage("Staged target upload file could not be found on server scratch disks.");
            jobRepository.saveAndFlush(job);
            return;
        }

        int processed = 0;
        int inserted = 0;
        int updated = 0;
        int failed = 0;
        int currentRowIndex = 1; // Tracks current row location inside file spreadsheet layout

        try (IteratorWrapper rowIterator = createRowIterator(file)) {

            while (rowIterator.hasNext()) {
                currentRowIndex++;
                ImportRowDto row = rowIterator.next();

                // Isolate individual rows so a single failure doesn't crash the file
                try {
                    BaseContact targetEntity;
                    boolean isUpdate = false;

                    if ("COMPANY".equalsIgnoreCase(row.getRecordType())) {
                        Optional<Company> existingCompany = Optional.empty();
                        if (row.getTaxId() != null && !row.getTaxId().isBlank()) {
                            existingCompany = entityManager.createQuery(
                                    "SELECT c FROM Company c WHERE c.taxId = :taxId AND c.businessUnitId = :buId",
                                    Company.class)
                                    .setParameter("taxId", row.getTaxId())
                                    .setParameter("buId", job.getBusinessUnitId())
                                    .getResultStream().findFirst();
                        }

                        if (existingCompany.isPresent()) {
                            targetEntity = existingCompany.get();
                            ((Company) targetEntity).setIndustry(row.getIndustry());
                            isUpdate = true;
                        } else {
                            targetEntity = new Company();
                            ((Company) targetEntity).setTaxId(row.getTaxId());
                            ((Company) targetEntity).setIndustry(row.getIndustry());
                        }
                    } else {
                        Optional<Contact> existingContact = Optional.empty();
                        if (row.getEmail() != null && !row.getEmail().isBlank()) {
                            existingContact = entityManager.createQuery(
                                    "SELECT c FROM Contact c WHERE c.email = :email AND c.businessUnitId = :buId",
                                    Contact.class)
                                    .setParameter("email", row.getEmail())
                                    .setParameter("buId", job.getBusinessUnitId())
                                    .getResultStream().findFirst();
                        }

                        if (existingContact.isPresent()) {
                            targetEntity = existingContact.get();
                            ((Contact) targetEntity).setPhoneNumber(row.getPhoneNumber());
                            isUpdate = true;
                        } else {
                            targetEntity = new Contact();
                            ((Contact) targetEntity).setEmail(row.getEmail());
                            ((Contact) targetEntity).setPhoneNumber(row.getPhoneNumber());
                            ((Contact) targetEntity).setSource("BULK_INGEST_IMPORT");
                        }
                    }

                    // Hydrate Core Shared Fields
                    targetEntity.setName(row.getName());
                    targetEntity.setStatus("ACTIVE");
                    targetEntity.setBusinessUnitId(job.getBusinessUnitId());

                    if (isUpdate && targetEntity.getCustomAttributes() != null) {
                        targetEntity.getCustomAttributes().putAll(row.getGenericAttributes());
                    } else {
                        targetEntity.setCustomAttributes(row.getGenericAttributes());
                    }

                    // Validate Dynamic Schemas Against Metadata Requirements
                    metadataRegistry.validateAttributes(job.getBusinessUnitId(), targetEntity.getCustomAttributes());

                    if (isUpdate) {
                        entityManager.merge(targetEntity);
                        updated++;
                    } else {
                        entityManager.persist(targetEntity);
                        inserted++;
                    }

                } catch (Exception rowException) {
                    log.warn("Row-Level Exclusion caught on row {}. Reason: {}", currentRowIndex,
                            rowException.getMessage());
                    failed++;

                    // Log row breakdown directly to database tracking error tables
                    ImportErrorLog errorLog = new ImportErrorLog();
                    errorLog.setJobId(job.getJobId());
                    errorLog.setRowNumber(currentRowIndex);
                    errorLog.setRecordIdentifier(
                            "COMPANY".equalsIgnoreCase(row.getRecordType()) ? row.getTaxId() : row.getEmail());
                    errorLog.setErrorMessage(rowException.getMessage() != null ? rowException.getMessage()
                            : "Unknown structural evaluation error.");

                    entityManager.persist(errorLog);
                }

                processed++;

                // Sync and flush batch increments to maintain flat memory footings
                if (processed % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();

                    // Push partial counter increments live to the database for live UI status
                    // updating
                    job.setProcessedRecords(processed);
                    job.setInsertedRecords(inserted);
                    job.setUpdatedRecords(updated);
                    job.setFailedRecords(failed);
                    jobRepository.saveAndFlush(job);
                }
            }

            // Final Sync Wrap Up
            job.setStatus(failed == processed ? "FAILED" : (failed > 0 ? "PARTIAL_SUCCESS" : "COMPLETED"));
            job.setProcessedRecords(processed);
            job.setInsertedRecords(inserted);
            job.setUpdatedRecords(updated);
            job.setFailedRecords(failed);

        } catch (Exception fileException) {
            log.error("Fatal exception parsing root document file container stream.", fileException);
            job.setStatus("FAILED");
            job.setErrorMessage("Catastrophic container read error: " + fileException.getMessage());
        } finally {
            jobRepository.saveAndFlush(job);
            if (file.exists())
                file.delete();
            eventPublisher.publishEvent(new ImportFinishedEvent(job.getJobId(), job.getStatus()));
        }
    }

    /**
     * Searches the temporary upload workspace for the file matching the generated
     * job tracking UUID prefix.
     */
    private File locateUploadedFile(Path directory, UUID jobId) {
        try (Stream<Path> s = Files.list(directory)) {
            return s.filter(p -> p.getFileName().toString().startsWith(jobId.toString()))
                    .map(Path::toFile)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            log.error("Failed scanning staging directory for target import reference: {}", jobId, e);
            return null;
        }
    }

    /**
     * Resolves appropriate low-memory streaming parsers based on the target file
     * signature extension.
     */
    private IteratorWrapper createRowIterator(File file) throws Exception {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            return createExcelIterator(file);
        }
        return createCsvIterator(file);
    }

    private IteratorWrapper createCsvIterator(File file) throws IOException {
        Reader reader = new FileReader(file);
        CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .build()
                .parse(reader);

        Iterator<CSVRecord> csvIterator = parser.iterator();
        List<String> headers = parser.getHeaderNames();

        return new IteratorWrapper() {
            @Override
            public boolean hasNext() {
                return csvIterator.hasNext();
            }

            @Override
            public ImportRowDto next() {
                CSVRecord record = csvIterator.next();
                ImportRowDto dto = new ImportRowDto();
                dto.setRecordType(record.isMapped("record_type") ? record.get("record_type") : "GENERAL");
                dto.setName(record.isMapped("name") ? record.get("name") : "Unnamed Entry");
                dto.setEmail(record.isMapped("email") ? record.get("email") : null);
                dto.setPhoneNumber(record.isMapped("phone_number") ? record.get("phone_number") : null);
                dto.setTaxId(record.isMapped("tax_id") ? record.get("tax_id") : null);
                dto.setIndustry(record.isMapped("industry") ? record.get("industry") : null);

                // Isolate dynamic data properties straight into JSONB custom maps
                for (String header : headers) {
                    if (!Arrays.asList("record_type", "name", "email", "phone_number", "tax_id", "industry")
                            .contains(header)) {
                        dto.getGenericAttributes().put(header, record.get(header));
                    }
                }
                return dto;
            }

            @Override
            public void close() throws IOException {
                parser.close();
                reader.close();
            }
        };
    }

    private IteratorWrapper createExcelIterator(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        Map<Integer, String> headerMap = new HashMap<>();
        if (rowIterator.hasNext()) {
            Row headerRow = rowIterator.next();
            for (Cell cell : headerRow) {
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    headerMap.put(cell.getColumnIndex(), cell.getStringCellValue().trim());
                }
            }
        }

        return new IteratorWrapper() {
            @Override
            public boolean hasNext() {
                return rowIterator.hasNext();
            }

            @Override
            public ImportRowDto next() {
                Row row = rowIterator.next();
                ImportRowDto dto = new ImportRowDto();

                for (Map.Entry<Integer, String> header : headerMap.entrySet()) {
                    Cell cell = row.getCell(header.getKey(), Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
                    String val = getCellValueAsString(cell);

                    switch (header.getValue()) {
                        case "record_type" -> dto.setRecordType(val);
                        case "name" -> dto.setName(val);
                        case "email" -> dto.setEmail(val);
                        case "phone_number" -> dto.setPhoneNumber(val);
                        case "tax_id" -> dto.setTaxId(val);
                        case "industry" -> dto.setIndustry(val);
                        default -> dto.getGenericAttributes().put(header.getValue(), val);
                    }
                }

                if (dto.getRecordType() == null || dto.getRecordType().isBlank()) {
                    dto.setRecordType("GENERAL");
                }
                return dto;
            }

            @Override
            public void close() throws IOException {
                workbook.close();
                fis.close();
            }
        };
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    /**
     * Structural contract extension ensuring custom streaming iterators
     * release multi-part heap locks and system resources cleanly upon batch
     * completion.
     */
    private interface IteratorWrapper extends Iterator<ImportRowDto>, AutoCloseable {
        @Override
        void close() throws IOException;
    }
}