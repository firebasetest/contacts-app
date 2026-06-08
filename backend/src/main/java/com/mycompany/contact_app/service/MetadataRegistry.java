package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.AttributeDefinition;
import com.mycompany.contact_app.repository.AttributeDefinitionRepository;
import com.mycompany.contact_app.util.AttributeRulesEngine;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MetadataRegistry {

    private final AttributeDefinitionRepository definitionRepository;
    private final AttributeRulesEngine rulesEngine;

    public MetadataRegistry(AttributeDefinitionRepository repository, AttributeRulesEngine rulesEngine) {
        this.definitionRepository = repository;
        this.rulesEngine = rulesEngine;
    }

    /**
     * Validates dynamic attributes against BU-specific metadata definitions.
     */
    public boolean isValid(UUID buId, Map<String, Object> attributes) {
        List<AttributeDefinition> definitions = definitionRepository.findByBuId(buId);

        for (AttributeDefinition def : definitions) {
            if (!rulesEngine.validate(attributes, def)) {
                return false;
            }
        }
        return true;
    }

    public List<AttributeDefinition> getDefinitionsForBu(UUID buId) {
        return definitionRepository.findByBuId(buId);
    }

    /**
     * Validates dynamic attributes against BU-specific definitions.
     */
    public void validateAttributes(UUID buId, Map<String, Object> attributes) {
        // Fetch definitions for the specific tenant (Business Unit)
        List<AttributeDefinition> definitions = definitionRepository.findByBuId(buId);

        for (AttributeDefinition def : definitions) {
            // Check if a required field is missing
            if (def.isRequired() && (attributes == null || !attributes.containsKey(def.getName()))) {
                throw new IllegalArgumentException("Missing required attribute: " + def.getName());
            }

            // Logic for data type validation would also go here (e.g., check if value
            // matches dataType)
        }
    }

}