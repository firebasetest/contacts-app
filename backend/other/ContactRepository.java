@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {
    
    // Example: Querying dynamic attributes using JSONB path expressions
    @Query(value = "SELECT * FROM contacts WHERE custom_attributes @> :filter", nativeQuery = true)
    List<Contact> findByCustomAttributes(@Param("filter") String jsonFilter);
}