@Service
public class ReportingService {

    private final JdbcTemplate jdbcTemplate;

    public ReportingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Fetches core KPIs for the dashboard.
     * Uses Materialized Views for performance.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardKpis() {
        // Querying a pre-aggregated Materialized View
        String query = "SELECT status, COUNT(*) as count FROM contact_summary_view GROUP BY status";
        
        List<Map<String, Object>> kpis = jdbcTemplate.queryForList(query);
        
        return Map.of("stats", kpis, "generatedAt", LocalDateTime.now());
    }

    /**
     * Retrieves distribution data for visualizations (histograms, pie charts).
     */
    public List<Map<String, Object>> getContactDistributionByRegion() {
        // Efficient extraction from JSONB custom_attributes
        String query = """
            SELECT 
                custom_attributes->>'region' as region, 
                COUNT(*) as total 
            FROM contacts 
            WHERE valid_to = '9999-12-31' 
            GROUP BY 1
            """;
        return jdbcTemplate.queryForList(query);
    }
}