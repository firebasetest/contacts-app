@Service
public class ContactImportService {

    @Async
    public String processImport(MultipartFile file) {
        String jobId = UUID.randomUUID().toString();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // 1. Parsing logic (e.g., OpenCSV or Apache Commons CSV)
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            
            List<ContactDTO> batch = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                batch.add(mapToContact(record));
                
                // 2. Process in chunks of 500 to keep transaction time short
                if (batch.size() >= 500) {
                    executeBatchUpsert(batch);
                    batch.clear();
                }
            }
            // Process remaining records
            executeBatchUpsert(batch);
            
        } catch (Exception e) {
            log.error("Import failed for job {}", jobId, e);
        }
        return jobId;
    }

    @Transactional
    protected void executeBatchUpsert(List<ContactDTO> batch) {
        // Here you call the PostgreSQL Stored Procedure 
        // that handles the temporal 'valid_from'/'valid_to' logic
        jdbcTemplate.update("CALL batch_upsert_contacts(?)", convertToJSONB(batch));
    }
}