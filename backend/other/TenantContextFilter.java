@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure it runs before any business logic
public class TenantContextFilter extends OncePerRequestFilter {

    private final DataSource dataSource;

    public TenantContextFilter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Retrieve info from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated()) {
            // Assume you have custom logic to extract claims from your JWT/Principal
            String buId = extractBuId(auth);
            String userId = extractUserId(auth);

            // 2. Set the variables on the current database connection
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Set the session variables for RLS and Auditing
                stmt.execute("SET LOCAL app.current_bu_id = '" + buId + "'");
                stmt.execute("SET LOCAL app.user_id = '" + userId + "'");
                
                // 3. Continue the chain
                filterChain.doFilter(request, response);
                
            } catch (SQLException e) {
                throw new ServletException("Failed to set tenant context", e);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
    
    // Helper methods to extract IDs from your authentication object
    private String extractBuId(Authentication auth) { ... }
    private String extractUserId(Authentication auth) { ... }
}