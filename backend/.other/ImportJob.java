@Entity
@Table(name = "import_jobs")
@Getter @Setter
public class ImportJob {
    @Id
    private UUID jobId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private int processedCount;
    private int totalRecords;
}