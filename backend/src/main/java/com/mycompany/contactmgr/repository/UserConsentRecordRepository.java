
package com.mycompany.contact_app.repository;

import com.mycompany.contact_app.entity.UserConsentRecord;
import com.mycompany.contact_app.model.ConsentPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserConsentRecordRepository extends JpaRepository<UserConsentRecord, Long> {
    /**
     * Finds the current consent status for a given principal (user/tenant) and purpose.
     */
    Optional<UserConsentRecord> findTopByPrincipalIdAndPurpose(UUID principalId, ConsentPurpose purpose);
}