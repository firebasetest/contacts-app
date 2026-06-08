package com.mycompany.contact_app.repository;

import com.mycompany.contact_app.entity.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, UUID> {

    /**
     * Retrieves all attribute definitions for a specific Business Unit.
     * This is the entry point for the MetadataRegistry validation logic.
     */
    List<AttributeDefinition> findByBuId(UUID buId);

    /**
     * Finds a specific attribute definition by BU and field name.
     */
    AttributeDefinition findByBuIdAndName(UUID buId, String name);
}