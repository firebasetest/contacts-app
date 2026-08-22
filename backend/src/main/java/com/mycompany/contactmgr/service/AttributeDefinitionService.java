package com.mycompany.contactmgr.service;

import com.mycompany.contactmgr.entity.AttributeDefinition;
import com.mycompany.contactmgr.repository.AttributeDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AttributeDefinitionService {
    private final AttributeDefinitionRepository repository;

    public AttributeDefinitionService(AttributeDefinitionRepository repository) {
        this.repository = repository;
    }

    public List<AttributeDefinition> findByBusinessUnit(UUID buId) {
        return repository.findByBuId(buId);
    }

    public AttributeDefinition create(AttributeDefinition definition) {
        return repository.save(definition);
    }
}
