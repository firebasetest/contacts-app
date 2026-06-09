package com.mycompany.contact_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("COMPANY")
@Getter
@Setter
public class Company extends BaseContact {
    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "industry")
    private String industry;
}