@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter
public abstract class BaseContact extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String status; // ACTIVE, INACTIVE, ARCHIVED, DELETED
    private UUID businessUnitId;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> customAttributes;
}

@Entity
public class Company extends BaseContact {
    private String taxId;
    private String industry;
}

@Entity
public class Employee extends BaseContact {
    private String employeeId;
    private String department;
    private UUID companyId; // Link to the parent Company
}