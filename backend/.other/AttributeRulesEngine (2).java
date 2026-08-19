@Service
public class AttributeRulesEngine {

    public boolean validate(Map<String, Object> attributes, AttributeDefinition def) {
        Map<String, Object> rules = def.getValidationRules();
        
        // 1. Evaluate "Required"
        if ((boolean) rules.getOrDefault("required", false)) {
            if (!attributes.containsKey(def.getName())) return false;
        }

        // 2. Evaluate "visible_if" logic
        if (rules.containsKey("visible_if")) {
            return evaluateVisibility(attributes, (Map<String, Object>) rules.get("visible_if"));
        }
        
        return true;
    }

    private boolean evaluateVisibility(Map<String, Object> attrs, Map<String, Object> condition) {
        String field = (String) condition.get("field");
        Object expected = condition.get("equals");
        return expected.equals(attrs.get(field));
    }
}