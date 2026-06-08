@Service
@Slf4j
public class WebhookDispatcher {

    private final RestTemplate restTemplate = new RestTemplate();

    @EventListener
    public void handleImportFinished(ImportFinishedEvent event) {
        // 1. Fetch registered endpoints for this Business Unit
        List<String> endpoints = webhookRepository.findByBuId(event.getBuId());
        
        // 2. Prepare payload
        Map<String, String> payload = Map.of(
            "jobId", event.getJobId().toString(),
            "status", event.getStatus()
        );

        // 3. Dispatch to all registered partners
        for (String url : endpoints) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Webhook-Signature", signPayload(payload));
                
                restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Void.class);
            } catch (Exception e) {
                log.error("Failed to notify endpoint: {}", url);
            }
        }
    }

    private String signPayload(Map<String, String> payload) {
        // HMAC-SHA256 signing logic as previously discussed
        return "signed-token"; 
    }
}