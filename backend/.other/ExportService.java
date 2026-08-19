@Service
public class ExportService {

    private final Map<String, ExportStrategy> strategies;
    private final StorageService storageService; // e.g., S3 integration

    public ExportService(List<ExportStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(s -> s.getFileExtension(), s -> s));
    }

    @Async
    public void generateExport(String format, List<Map<String, Object>> data, UUID jobId) {
        ExportStrategy strategy = strategies.get(format);
        byte[] content = strategy.export(data);
        
        // Upload to S3 and generate a signed URL
        String url = storageService.upload(content, "exports/" + jobId + "." + format);
        
        // Publish event to notify user via email/notification
        eventPublisher.publishEvent(new ExportCompleteEvent(jobId, url));
    }
}