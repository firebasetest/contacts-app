package com.mycompany.contactmgr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Entity
@Table(name = "tenant_settings")
public class TenantSettings {

    // Getters and Setters
    @Setter
    @Id
    @Column(name = "business_unit_id")
    private UUID businessUnitId;

    @Setter
    @Column(name = "telephony_provider", nullable = false)
    private String telephonyProvider = "NATIVE_TEL"; // NONE, NATIVE_TEL, TWILIO, MS_TEAMS

    @Setter
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "telephony_credentials", columnDefinition = "jsonb")
    private Map<String, Object> telephonyCredentials = new HashMap<>();

    // Core platform configuration flags
    @Column(name = "is_gdpr_enabled")
    private boolean isGdprEnabled = true;

    @Column(name = "is_audit_view_enabled")
    private boolean isAuditViewEnabled = true;

    public void setGdprEnabled(boolean gdprEnabled) {
        this.isGdprEnabled = isGdprEnabled;
    }

    public void setAuditViewEnabled(boolean auditViewEnabled) {
        this.isAuditViewEnabled = auditViewEnabled;
    }
}