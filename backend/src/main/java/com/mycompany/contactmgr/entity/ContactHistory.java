package com.mycompany.contact_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "contacts_history")
@Getter
@Setter
public class ContactHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID historyId;

    @Column(name = "contact_id", nullable = false)
    private UUID contactId;

    @Column(name = "business_unit_id", nullable = false)
    private UUID businessUnitId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "tax_id", nullable = false)
    private String taxId;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes", columnDefinition = "jsonb")
    private Map<String, Object> customAttributes;

    @Column(name = "system_role", nullable = false)
    private String systemRole;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;

    @Column(name = "change_action", nullable = false)
    private String changeAction;

    @Column(name = "capture_type", nullable = false)
    private String captureType; // e.g., "INSERT", "UPDATE", "DELETE"

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_deltas", columnDefinition = "jsonb")
    private Map<String, String> fieldDeltas;

}