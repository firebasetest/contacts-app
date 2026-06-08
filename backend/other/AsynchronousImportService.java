@Service
@Slf4j
public class AsynchronousImportService {

    private final ContactRepository contactRepository;
    private final MetadataRegistry metadataRegistry;
    private final ImportJobRepository jobRepository;

    public AsynchronousImportService(ContactRepository repo, MetadataRegistry meta, ImportJobRepository jobRepo) {
        this.contactRepository = repo;
        this.metadataRegistry = meta;
        this.jobRepository = jobRepo;
    }

    @Async("importTaskExecutor")
    public void processBatchImport(UUID jobId, MultipartFile file) {
        try {
            updateJobStatus(jobId, "PROCESSING");
            
            // Using Apache Commons CSV for memory-efficient streaming
            try (Reader reader = new InputStreamReader(file.getInputStream());
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                
                List<Contact> batch = new ArrayList<>();
                for (CSVRecord record : csvParser) {
                    Contact contact = mapToEntity(record);
                    
                    // Validate against MetadataRegistry (Rules Engine)
                    if (metadataRegistry.isValid(contact)) {
                        batch.add(contact);
                    }

                    if (batch.size() >= 500) {
                        saveBatch(batch);
                        batch.clear();
                    }
                }
                saveBatch(batch);
                updateJobStatus(jobId, "COMPLETED");
            }
        } catch (Exception e) {
            log.error("Import failed for job {}", jobId, e);
            updateJobStatus(jobId, "FAILED");
        }
    }

    @Transactional
    protected void saveBatch(List<Contact> batch) {
        contactRepository.saveAll(batch);
    }
}