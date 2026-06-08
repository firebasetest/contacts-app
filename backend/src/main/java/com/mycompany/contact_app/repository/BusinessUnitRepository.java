package com.mycompany.contact_app.repository;

import com.mycompany.contact_app.entity.BusinessUnit;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface BusinessUnitRepository extends JpaRepository<BusinessUnit, UUID> {

    /**
     * Checks if a Business Unit exists.
     * The @Cacheable annotation is critical here to ensure this validation
     * step does not cause a database bottleneck on every API request.
     */
    @Cacheable(value = "business_units", key = "#id")
    boolean existsById(UUID id);
}