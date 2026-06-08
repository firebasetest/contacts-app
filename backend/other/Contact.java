@Entity
@Table(name = "contacts")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Contact extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String firstName;
    private String lastName;
    private String email;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> customAttributes;

    private UUID businessUnitId;
    
    // Temporal fields
    private LocalDateTime validFrom;
    private LocalDateTime validTo = LocalDateTime.parse("9999-12-31T23:59:59");
}