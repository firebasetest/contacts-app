
package com.mycompany.contactmgr.repository;

import com.mycompany.contactmgr.entity.UserConsentRecord;
import com.mycompany.contactmgr.model.ConsentPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserConsentRecordRepository extends JpaRepository<UserConsentRecord, Long> {
    /**
     * Finds the current consent status for a given principal (user/tenant) and purpose.
     */
    Optional<UserConsentRecord> findTopByPrincipalIdAndPurpose(UUID principalId, ConsentPurpose purpose);
}