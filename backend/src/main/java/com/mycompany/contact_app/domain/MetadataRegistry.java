package com.mycompany.contact_app.domain;

import com.mycompany.contact_app.entity.AttributeDefinition;
import com.mycompany.contact_app.repository.AttributeDefinitionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MetadataRegistry {
    private final AttributeDefinitionRepository repository;

    public MetadataRegistry(AttributeDefinitionRepository repository) {
        this.repository = repository;
    }

    public void validateAttributes(UUID buId, Map<String, Object> attributes) {
        List<AttributeDefinition> definitions = repository.findByBuId(buId);

        for (AttributeDefinition def : definitions) {
            if (def.isRequired() && !attributes.containsKey(def.getName())) {
                throw new IllegalArgumentException("Missing required attribute: " + def.getName());
            }
        }
    }
}