
package com.mycompany.contact_app.entity;

import com.mycompany.contact_app.model.ConsentPurpose;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records the explicit consent granted by a principal (user/tenant) for specific purposes.
 */
@Entity
@Table(name = "user_consent_record")
@Data
public class UserConsentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Unique identifier for the individual whose consent is being tracked (User ID or Tenant ID)
    @Column(nullable = false, unique = true)
    private UUID principalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_purpose", nullable = false)
    private ConsentPurpose purpose;

    // True if consent is active and valid for the PurposeKey
    private boolean granted;

    // The date/time when this specific decision was recorded
    private LocalDateTime timestamp;

}