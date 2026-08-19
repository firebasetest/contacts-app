@Service
public class AsynchronousImportService {

    private final ApplicationEventPublisher eventPublisher;

    @Async("importTaskExecutor")
    public void processBatchImport(UUID jobId, MultipartFile file) {
        // ... processing logic ...
        
        // Finalize
        updateJobStatus(jobId, "COMPLETED");
        eventPublisher.publishEvent(new ImportFinishedEvent(jobId, "COMPLETED", currentBuId));
    }
}