@Service
public class LifecycleManager {

    private final Map<String, List<String>> validTransitions = Map.of(
        "ACTIVE", List.of("INACTIVE", "ARCHIVED"),
        "INACTIVE", List.of("ACTIVE", "ARCHIVED"),
        "ARCHIVED", List.of("ACTIVE")
    );

    public void transitionTo(BaseContact contact, String newStatus) {
        String currentStatus = contact.getStatus();
        
        if (!validTransitions.getOrDefault(currentStatus, List.of()).contains(newStatus)) {
            throw new IllegalStateException("Invalid transition from " + currentStatus + " to " + newStatus);
        }
        
        contact.setStatus(newStatus);
        // Trigger audit logging for this transition
    }
}