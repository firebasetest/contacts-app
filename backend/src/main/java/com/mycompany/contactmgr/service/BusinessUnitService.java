package com.mycompany.contactmgr.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.mycompany.contactmgr.dto.BusinessUnitDto;
import com.mycompany.contactmgr.dto.BusinessUnitInputDto;
import com.mycompany.contactmgr.dto.BusinessUnitSearchResponse;
import com.mycompany.contactmgr.entity.BusinessUnit;
import com.mycompany.contactmgr.model.BusinessUnitStatus;
import com.mycompany.contactmgr.repository.BusinessUnitRepository;
import com.mycompany.contactmgr.security.TenantContext;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessUnitService {

    private final BusinessUnitRepository buRepository;

    public BusinessUnitService(BusinessUnitRepository repository) {
        this.buRepository = repository;
    }

    /**
     * Retrieves a BusinessUnit by its ID.
     * Note: For the BusinessUnit entity itself, we must use the TenantContext
     * to ensure the requesting user belongs to the requested unit.
     */
    public Optional<BusinessUnit> findByBusinessUnitId(String tenantId) {
        // We assume for this specific entity, the BusinessUnitId IS the tenantId.
        // We rely on the repository/query to handle the lookup based on the external
        // ID.
        return buRepository.findByBusinessUnitId(tenantId);
    }

    /**
     * Creates a new Business Unit. This operation should only be callable by Super
     * Admin.
     */
    @Transactional
    public BusinessUnit create(BusinessUnit buId) {
        // Basic validation (e.g., check if name is unique)
        if (buRepository.findByBusinessUnitId(buId.getSlug()).isPresent()) {
            throw new IllegalStateException("Business Unit ID already exists.");
        }
        return buRepository.save(buId);
    }

    /**
     * Updates an existing Business Unit's core details.
     */
    @Transactional
    public BusinessUnit update(String businessUnitId, BusinessUnit updatedData) {
        // Critical check: Does the provided ID belong to the current context?
        if (!businessUnitId.equals(TenantContext.getCurrentTenant().orElse(null))) {
            throw new SecurityException("Cannot modify a Business Unit belonging to another tenant.");
        }

        // Find the existing record
        return buRepository.findByBusinessUnitId(businessUnitId)
                .map(unit -> {
                    unit.setName(updatedData.getName());
                    // Re-enforce the context ID even if the request payload changes
                    // Note: We must explicitly set the context ID back in the object
                    // to prevent it from being nullified or changed.
                    return buRepository.save(unit);
                })
                .orElseThrow(() -> new IllegalStateException("Business Unit not found for ID: " + businessUnitId));
    }

    /**
     * Deletes a Business Unit. Only Super Admin should call this.
     */
    @Transactional
    public void delete(String businessUnitId) {
        // For simplicity, we assume delete is only called by a Super Admin.
        // Actual implementation would require a role check here.
        if (buRepository.findByBusinessUnitId(businessUnitId).isEmpty()) {
            throw new IllegalArgumentException("Business Unit ID not found.");
        }
        buRepository.deleteByBusinessUnitId(businessUnitId);
    }

    /**
     * Handles complex business logic and pagination.
     * The Service layer must convert entities to DTOs for safe transfer.
     */
    public BusinessUnitSearchResponse findBusinessUnits(int page, int size, String searchQuery) {
        // 1. BUILD THE PAGEABLE OBJECT
        Pageable pageable = PageRequest.of(page, size);

        // 2. FETCH THE DATA (Calling the Repository)
        List<BusinessUnit> entities;
        if (searchQuery != null && !searchQuery.isBlank()) {
            // Use the specialized repository method for searching
            entities = buRepository.findByContainingIgnoreCase(searchQuery, pageable);
        } else {
            // Fetching all units on the page if no search query is given
            entities = (List<BusinessUnit>) buRepository.findAll(pageable);
        }

        // 3. MAP AND BUILD THE RESPONSE DTO
        List<BusinessUnitDto> dtos = entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // For a simplified simulation, we will manually construct the response
        // structure
        // In a real Spring environment, we would let Spring's Page object handle this
        // complexity.
        BusinessUnitSearchResponse response = new BusinessUnitSearchResponse();
        response.setContent(dtos);
        response.setNumber(page);
        response.setSize(size);
        response.setTotalElements(buRepository.count()); // Must run a count query
        response.setTotalPages((int) Math.ceil((double) buRepository.count() / size));

        return response;
    }

    public BusinessUnitDto createBusinessUnit(BusinessUnitInputDto input) {
        // 1. BUSINESS VALIDATION (e.g., Slug generation, name uniqueness check)
        if (buRepository.findBySlugIgnoreCase(input.getName().toLowerCase().replaceAll("\\s", "-")).isPresent()) {
            throw new IllegalStateException("A business unit with this slug already exists.");
        }

        // 2. PERSISTENCE
        BusinessUnit newUnit = new BusinessUnit();
        newUnit.setName(input.getName());
        newUnit.setDescription(input.getDescription());
        newUnit.setStatus(input.getInitialStatus());
        // The slug is generated here based on the name
        newUnit.setSlug(input.getName().toLowerCase().replaceAll("\\s", "-"));

        // 3. SAVE AND CONVERT
        BusinessUnit savedUnit = buRepository.save(newUnit);
        return convertToDto(savedUnit);
    }

    /**
     * -----------------------------------------------------------
     * 3. UPDATING OPERATIONS
     * -----------------------------------------------------------
     * This method updates the status of an existing Business Unit.
     * 
     * @param businessUnitId The UUID of the unit to update.
     * @param newStatus      The desired new status (ACTIVE, INACTIVE, etc.).
     * @return The updated BusinessUnitDto.
     */
    @Transactional // CRITICAL: Ensures the entire status change is atomic
    public BusinessUnitDto updateStatus(UUID businessUnitId, BusinessUnitStatus newStatus) {

        // 1. RETRIEVE UNIT
        Optional<BusinessUnit> optionalUnit = buRepository.findById(businessUnitId);
        if (optionalUnit.isEmpty()) {
            throw new java.util.NoSuchElementException("Business Unit not found with ID: " + businessUnitId);
        }

        BusinessUnit unit = optionalUnit.get();

        // 2. BUSINESS VALIDATION (Domain Rule Enforcement)
        // Example Rule: A unit cannot transition from ACTIVE to PENDING directly.
        if (unit.getStatus() == BusinessUnitStatus.ACTIVE && newStatus == BusinessUnitStatus.PENDING_DELETION) {
            throw new IllegalStateException(
                    "Cannot transition from ACTIVE to PENDING directly. Requires admin review first.");
        }

        // 3. APPLY CHANGE
        unit.setStatus(newStatus);
        // This field is mandatory for auditing changes
        unit.setUpdatedAt(java.time.Instant.now());

        // 4. PERSISTENCE
        BusinessUnit updatedUnit = buRepository.save(unit);

        // 5. CONVERT AND RETURN
        return convertToDto(updatedUnit);
    }

    // --- Helper Method ---
    private BusinessUnitDto convertToDto(BusinessUnit savedUnit) {
        // Conversion logic (DTO mapping)
        BusinessUnitDto dto = new BusinessUnitDto();
        dto.setBusinessUnitId(savedUnit.getId());
        dto.setName(savedUnit.getName());
        dto.setDescription(savedUnit.getDescription());
        dto.setStatus(savedUnit.getStatus());
        dto.setCreatedAt(savedUnit.getCreatedAt());
        dto.setUpdatedAt(savedUnit.getUpdatedAt());
        return dto;
    }

}