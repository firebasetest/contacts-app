@RestController
@RequestMapping("/api/v1/reporting")
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKpis() {
        return ResponseEntity.ok(reportingService.getDashboardKpis());
    }
}