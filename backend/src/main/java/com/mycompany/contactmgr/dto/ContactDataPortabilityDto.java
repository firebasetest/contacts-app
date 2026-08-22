package com.mycompany.contact_app.dto;

import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.entity.ContactHistory;

import java.util.List;

public class ContactDataPortabilityDto {
    private BaseContact currentProfile;
    private List<ContactHistory> auditTrailHistory;

    public ContactDataPortabilityDto(BaseContact currentProfile, List<ContactHistory> auditTrailHistory) {
        this.currentProfile = currentProfile;
        this.auditTrailHistory = auditTrailHistory;
    }

    // Getters and Setters
    public BaseContact getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(BaseContact currentProfile) {
        this.currentProfile = currentProfile;
    }

    public List<ContactHistory> getAuditTrailHistory() {
        return auditTrailHistory;
    }

    public void setAuditTrailHistory(List<ContactHistory> auditTrailHistory) {
        this.auditTrailHistory = auditTrailHistory;
    }
}