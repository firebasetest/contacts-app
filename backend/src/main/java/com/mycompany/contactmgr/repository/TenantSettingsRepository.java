package com.mycompany.contactmgr.repository;

import com.mycompany.contactmgr.entity.TenantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantSettingsRepository extends JpaRepository<TenantSettings, UUID> {

    /**
     * Look up custom settings by the primary Business Unit ID mapping.
     */
    Optional<TenantSettings> findByBusinessUnitId(UUID businessUnitId);
}