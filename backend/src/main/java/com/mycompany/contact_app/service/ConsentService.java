
package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.UserConsentRecord;
import com.mycompany.contact_app.exception.MissingTenantClaimException;
import com.mycompany.contact_app.model.ConsentPurpose;
import com.mycompany.contact_app.repository.UserConsentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the lifecycle and validation of user consent across the application.
 */
@Service
public class ConsentService {

    private final UserConsentRecordRepository recordRepository;

    public ConsentService(UserConsentRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    /**
     * Checks if a specific user or tenant has granted consent for a given purpose.
     * @param principalId The ID of the entity whose consent is checked (e.g., Tenant UUID).
     * @param purpose The usage case to check against (e.g., PHONE_CALLING).
     * @return true if explicit, active consent exists; false otherwise.
     */
    public boolean hasConsent(UUID principalId, ConsentPurpose purpose) {
        Optional<UserConsentRecord> record = recordRepository.findTopByPrincipalIdAndPurpose(principalId, purpose);

        if (record.isPresent()) {
            return record.get().isGranted();
        }
        // If no record exists, we assume NO consent is granted by default (fail-safe).
        return false;
    }

    /**
     * Grants explicit consent for a purpose and records the event.
     */
    @Transactional
    public void grantConsent(UUID principalId, ConsentPurpose purpose) {
        // Check if a record already exists to prevent potential data overlap/error states
        Optional<UserConsentRecord> existing = recordRepository.findTopByPrincipalIdAndPurpose(principalId, purpose);

        if (existing.isPresent() && existing.get().isGranted()) {
            return; // Already consented
        }

        // Create and save the new explicit consent record
        UserConsentRecord newRecord = new UserConsentRecord();
        newRecord.setPrincipalId(principalId);
        newRecord.setPurpose(purpose);
        newRecord.setGranted(true);
        newRecord.setTimestamp(LocalDateTime.now());
        recordRepository.save(newRecord);
    }

    /**
     * Revokes consent for a purpose and records the event.
     */
    @Transactional
    public void revokeConsent(UUID principalId, ConsentPurpose purpose) {
        // Find existing record or create a placeholder/update pattern depending on business rule
        Optional<UserConsentRecord> optionalRecord = recordRepository.findTopByPrincipalIdAndPurpose(principalId, purpose);

        if (optionalRecord.isPresent()) {
            UserConsentRecord recordToRevoke = optionalRecord.get();
            recordToRevoke.setGranted(false); // Change status instead of deleting the history
            recordRepository.save(recordToRevoke);
        } else {
            // If no existing record, we still log the revocation event for auditing purposes
            // (optional: depends on audit requirements)
        }
    }
}