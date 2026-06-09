import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

// Enables querying historical data for temporal AS-OF filtering
public interface ContactRepository extends JpaRepository<Contact, UUID>, RevisionRepository<Contact, UUID, Integer> {
    // Custom query methods here
}