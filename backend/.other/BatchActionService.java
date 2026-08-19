@Service
@Slf4j
public class BatchActionService {

    private final ContactRepository repository;
    private final LifecycleManager lifecycleManager;

    @Async("importTaskExecutor")
    public void processBatchAction(String statusFilter, String action) {
        // 1. Fetch IDs based on the same filter used in the UI
        List<Contact> contacts = repository.findByStatus(statusFilter);
        
        // 2. Perform transition for each
        for (Contact contact : contacts) {
            try {
                lifecycleManager.transitionTo(contact, action);
                repository.save(contact);
            } catch (Exception e) {
                log.error("Failed to transition contact {}: {}", contact.getId(), e.getMessage());
            }
        }
    }
}