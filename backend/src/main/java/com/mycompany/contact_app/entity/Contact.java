package com.mycompany.contact_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("GENERAL")
@Getter
@Setter
public class Contact extends BaseContact {
    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "source")
    private String source; // e.g., "Web Form", "CSV Import"
}