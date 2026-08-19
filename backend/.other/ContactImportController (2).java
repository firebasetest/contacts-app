@RestController
@RequestMapping("/api/v1/contacts")
public class ContactImportController {

    private final AsynchronousImportService importService;
    private final ImportJobRepository jobRepository;

    @PostMapping("/import")
    public ResponseEntity<Map<String, String>> initiateImport(@RequestParam("file") MultipartFile file) {
        UUID jobId = UUID.randomUUID();
        jobRepository.save(new ImportJob(jobId, "PENDING"));
        
        importService.processBatchImport(jobId, file);
        
        return ResponseEntity.accepted().body(Map.of("jobId", jobId.toString()));
    }

    @GetMapping("/import/status/{jobId}")
    public ResponseEntity<ImportJob> getStatus(@PathVariable UUID jobId) {
        return jobRepository.findById(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}