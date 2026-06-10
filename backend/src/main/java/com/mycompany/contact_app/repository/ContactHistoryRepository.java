package com.mycompany.contact_app.repository;

import com.mycompany.contact_app.entity.ContactHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactHistoryRepository extends JpaRepository<ContactHistory, UUID> {

        List<ContactHistory> findByContactIdOrderByValidFromDesc(UUID contactId);

        /**
         * Overwrites all historical PII snapshots for a given contact record.
         */
        @Modifying
        @Query("UPDATE ContactHistory ch SET ch.name = 'GDPR_ANONYMIZED', ch.email = 'anonymized@gdpr.internal', " +
                        "ch.phoneNumber = '000-000-0000', ch.notes = 'Purged under GDPR Art 17 Right to Erase' " +
                        "WHERE ch.contactId = :contactId")
        void anonymizeHistoryLogs(@Param("contactId") UUID contactId);

        /**
         * Core AS-OF Point-in-time selection query logic.
         */
        @Query("SELECT h FROM ContactHistory h WHERE h.contactId = :contactId " +
                        "AND :targetTime >= h.validFrom AND :targetTime < h.validTo")
        Optional<ContactHistory> findAsOf(@Param("contactId") UUID contactId,
                        @Param("targetTime") LocalDateTime targetTime);
}