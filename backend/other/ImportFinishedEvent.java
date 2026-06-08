public class ImportFinishedEvent {
    private final UUID jobId;
    private final String status;
    private final String buId;

    public ImportFinishedEvent(UUID jobId, String status, String buId) {
        this.jobId = jobId;
        this.status = status;
        this.buId = buId;
    }
    // Getters
}