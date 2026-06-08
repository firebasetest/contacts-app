@Service
public class SystemTaskService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void executeTaskForBU(UUID buId, Runnable task) {
        // Manually open a transaction and set context
        jdbcTemplate.execute("SET LOCAL app.current_bu_id = '" + buId + "'");
        
        // Execute the task logic
        task.run();
        
        // Context is reset automatically when the transaction ends
    }
}