import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "contacts")
@Audited // Automatically handles full audit logging (Requirement 1)
public class Contact extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactType type; // B2B, PROSPECT, PARTNER, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; // Hierarchical linking (Requirement 2)

    // No-code custom field engine (Requirement 2)
    // Stores dynamic BU requirements natively in PostgreSQL JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> customFields;

    // Getters, Setters, and Constructors omitted for brevity
}