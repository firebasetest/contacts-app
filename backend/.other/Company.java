@Entity
@Table(name = "contacts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "contact_type")
public abstract class BaseContact extends BaseEntity {
    // Shared fields...
}

@Entity
@DiscriminatorValue("COMPANY")
public class Company extends BaseContact {
    private String taxId;
    private String industry;
}

@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends BaseContact {
    private String employeeId;
    private String department;
    private UUID companyId;
}