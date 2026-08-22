package com.mycompany.contactmgr.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mycompany.contactmgr.entity.AttributeDefinition;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AttributeRulesEngineTest {

    @Test
    void shouldValidateRequiredAttributeWhenPresent() {
        AttributeRulesEngine engine = new AttributeRulesEngine();
        AttributeDefinition definition = new AttributeDefinition();
        definition.setName("email");
        definition.setValidationRules(Map.of("required", true));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "person@example.com");

        assertTrue(engine.validate(attributes, definition));
    }

    @Test
    void shouldFailValidationWhenRequiredAttributeIsMissing() {
        AttributeRulesEngine engine = new AttributeRulesEngine();
        AttributeDefinition definition = new AttributeDefinition();
        definition.setName("email");
        definition.setValidationRules(Map.of("required", true));

        Map<String, Object> attributes = new HashMap<>();

        assertFalse(engine.validate(attributes, definition));
    }

    @Test
    void shouldAllowOptionalAttributeWhenMissing() {
        AttributeRulesEngine engine = new AttributeRulesEngine();
        AttributeDefinition definition = new AttributeDefinition();
        definition.setName("email");
        definition.setValidationRules(Map.of("required", false));

        Map<String, Object> attributes = new HashMap<>();

        assertTrue(engine.validate(attributes, definition));
    }

    @Test
    void shouldApplyVisibleIfCondition() {
        AttributeRulesEngine engine = new AttributeRulesEngine();
        AttributeDefinition definition = new AttributeDefinition();
        definition.setName("region");
        definition.setValidationRules(Map.of(
            "required", false,
            "visible_if", Map.of(
                "field", "country",
                "equals", "US"
            )
        ));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("country", "US");

        assertTrue(engine.validate(attributes, definition));

        attributes.put("country", "CA");
        assertFalse(engine.validate(attributes, definition));
    }
}
