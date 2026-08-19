@Service
public class AttributeRulesEngine {

    // Validates a field against its rules defined in metadata
    public boolean evaluateVisibility(Map<String, Object> currentAttributes, Map<String, Object> rule) {
        if (rule == null || !rule.containsKey("visible_if")) return true;

        Map<String, Object> condition = (Map<String, Object>) rule.get("visible_if");
        String targetField = (String) condition.get("field");
        Object expectedValue = condition.get("equals");

        Object actualValue = currentAttributes.get(targetField);
        
        return actualValue != null && actualValue.equals(expectedValue);
    }

    // Handles simple arithmetic for computed fields
    public Object evaluateComputedField(Map<String, Object> currentAttributes, String formula) {
        // Implementation note: For production, consider using an expression 
        // library like SpEL (Spring Expression Language) or JEXL
        if ("field_a * field_b".equals(formula)) {
            Double a = (Double) currentAttributes.get("field_a");
            Double b = (Double) currentAttributes.get("field_b");
            return a * b;
        }
        return null;
    }
}