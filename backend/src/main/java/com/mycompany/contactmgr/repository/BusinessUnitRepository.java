package com.mycompany.contactmgr.repository;

import com.mycompany.contactmgr.entity.BusinessUnit;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

/**
 * JpaRepository provides basic CRUD methods.
 * Note: The JPA logic (and service layer) must ensure that all queries
 * automatically include the 'WHERE business_unit_id = :currentTenantId' clause.
 */
@Repository
public interface BusinessUnitRepository extends JpaRepository<BusinessUnit, UUID> {

    /**
     * Checks if a Business Unit exists.
     * The @Cacheable annotation is critical here to ensure this validation
     * step does not cause a database bottleneck on every API request.
     */
    @Cacheable(value = "business_units", key = "#id")
    boolean existsById(UUID id);

    /**
     * Finds a BusinessUnit by its unique external ID (X-Tenant-Id).
     */
    Optional<BusinessUnit> findByBusinessUnitId(String businessUnitId);

    // Custom method to find units by name/slug, respecting pagination.
    // Spring Data JPA handles the complex SQL Query Builder for us.
    // 'Pageable' ensures the query uses OFFSET/LIMIT clauses.
    java.util.Optional<BusinessUnit> findBySlugIgnoreCase(String slug);

    // This method implements the complex search requirement
    java.util.List<BusinessUnit> findByContainingIgnoreCase(String search, Pageable pageable);

    /**
     * Deletes a BusinessUnit by its unique external ID.
     * (Spring Data JPA automatically generates the implementation for this method.)
     */
    void deleteByBusinessUnitId(String businessUnitId);

}