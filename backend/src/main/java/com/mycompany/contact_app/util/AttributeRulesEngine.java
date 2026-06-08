package com.mycompany.contact_app.util;

import com.mycompany.contact_app.entity.AttributeDefinition;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AttributeRulesEngine {

    public boolean validate(Map<String, Object> attributes, AttributeDefinition def) {
        Map<String, Object> rules = def.getValidationRules();

        // 1. Mandatory check
        if ((boolean) rules.getOrDefault("required", false)) {
            if (!attributes.containsKey(def.getName()))
                return false;
        }

        // 2. Conditional visibility logic
        if (rules.containsKey("visible_if")) {
            return evaluateCondition(attributes, (Map<String, Object>) rules.get("visible_if"));
        }

        return true;
    }

    private boolean evaluateCondition(Map<String, Object> attrs, Map<String, Object> cond) {
        String targetField = (String) cond.get("field");
        Object expectedValue = cond.get("equals");
        return expectedValue.equals(attrs.get(targetField));
    }
}