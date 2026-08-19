@Service
public class ReportingService {

    private final JdbcTemplate jdbcTemplate;

    // Cache results for 10 minutes to minimize database load
    @Cacheable(value = "dashboardData", key = "#buId")
    public List<Map<String, Object>> getRegionalDistribution(UUID buId) {
        String sql = """
            SELECT region, contact_count 
            FROM contact_analytics_view 
            WHERE business_unit_id = ?
            """;
        return jdbcTemplate.queryForList(sql, buId);
    }
}