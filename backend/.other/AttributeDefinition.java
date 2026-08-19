@Entity
@Table(name = "attribute_definitions")
@Getter @Setter
public class AttributeDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    private UUID buId; // Business Unit owner
    private String name; // e.g., "insurance_policy"
    private String dataType; // NUMBER, DATE, TEXT
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> validationRules; // Stores 'visible_if', 'computed' rules
}