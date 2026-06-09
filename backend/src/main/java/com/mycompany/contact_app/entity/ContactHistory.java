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

    private String name;
    private String status;
    private String email;
    private String phoneNumber;
    private String source;
    private String taxId;
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes", columnDefinition = "jsonb")
    private Map<String, Object> customAttributes;

    private String systemRole;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    private String modifiedBy;
    private String changeAction;
}