@RestController
@RequestMapping("/api/v1/contacts")
public class ContactImportController {

    private final ContactImportService importService;

    public ContactImportController(ContactImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, String>> uploadContacts(@RequestParam("file") MultipartFile file) {
        String jobId = importService.processImport(file);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId, "status", "Processing"));
    }
}