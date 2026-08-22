package com.mycompany.contactmgr.dto;

import com.mycompany.contactmgr.entity.BaseContact;
import com.mycompany.contactmgr.entity.ContactHistory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ContactDataPortabilityDto {
    // Getters and Setters
    private BaseContact currentProfile;
    private List<ContactHistory> auditTrailHistory;

    public ContactDataPortabilityDto(BaseContact currentProfile, List<ContactHistory> auditTrailHistory) {
        this.currentProfile = currentProfile;
        this.auditTrailHistory = auditTrailHistory;
    }

}