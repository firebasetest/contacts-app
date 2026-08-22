package com.mycompany.contact_app.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class ContactHistoryResponseDto {
    private UUID historyId;
    private int version;
    private String captureType; // INSERT, UPDATE, DELETE
    private LocalDateTime validFrom;
    private String modifiedBy;
    private String name;
    private String email;
    private String phoneNumber;
    private Map<String, Object> customAttributes;
    private Map<String, String> fieldDeltas; // e.g., {"phone_number": "Changed from X to Y"}

    // Getters and Setters
    public UUID getHistoryId() {
        return historyId;
    }

    public void setHistoryId(UUID historyId) {
        this.historyId = historyId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCaptureType() {
        return captureType;
    }

    public void setCaptureType(String captureType) {
        this.captureType = captureType;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Map<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public Map<String, String> getFieldDeltas() {
        return fieldDeltas;
    }

    public void setFieldDeltas(Map<String, String> fieldDeltas) {
        this.fieldDeltas = fieldDeltas;
    }
}