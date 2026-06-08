package com.mycompany.contact_app.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("GENERAL")
@Getter
@Setter
public class Contact extends BaseContact {
    // Standard contact specific fields
    private String email;
    private String phoneNumber;
    private String source; // e.g., "Web Form", "CSV Import"
}