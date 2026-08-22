package com.mycompany.contact_app.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tenant_settings")
public class TenantSettings {

    @Id
    @Column(name = "business_unit_id")
    private UUID businessUnitId;

    @Column(name = "telephony_provider", nullable = false)
    private String telephonyProvider = "NATIVE_TEL"; // NONE, NATIVE_TEL, TWILIO, MS_TEAMS

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "telephony_credentials", columnDefinition = "jsonb")
    private Map<String, Object> telephonyCredentials = new HashMap<>();

    // Core platform configuration flags
    @Column(name = "is_gdpr_enabled")
    private boolean isGdprEnabled = true;

    @Column(name = "is_audit_view_enabled")
    private boolean isAuditViewEnabled = true;

    // Getters and Setters
    public UUID getBusinessUnitId() {
        return businessUnitId;
    }

    public void setBusinessUnitId(UUID businessUnitId) {
        this.businessUnitId = businessUnitId;
    }

    public String getTelephonyProvider() {
        return telephonyProvider;
    }

    public void setTelephonyProvider(String telephonyProvider) {
        this.telephonyProvider = telephonyProvider;
    }

    public Map<String, Object> getTelephonyCredentials() {
        return telephonyCredentials;
    }

    public void setTelephonyCredentials(Map<String, Object> telephonyCredentials) {
        this.telephonyCredentials = telephonyCredentials;
    }

    public boolean isGdprEnabled() {
        return isGdprEnabled;
    }

    public void setGdprEnabled(boolean gdprEnabled) {
        this.isGdprEnabled = isGdprEnabled;
    }

    public boolean isAuditViewEnabled() {
        return isAuditViewEnabled;
    }

    public void setAuditViewEnabled(boolean auditViewEnabled) {
        this.isAuditViewEnabled = auditViewEnabled;
    }
}