@Service
public class EmployeeLifecycleManager extends LifecycleManager {
    
    public void activateEmployee(Employee emp) {
        if (emp.getCompanyId() == null) {
            throw new IllegalArgumentException("Cannot activate employee without a company association");
        }
        transitionTo(emp, "ACTIVE");
    }
}