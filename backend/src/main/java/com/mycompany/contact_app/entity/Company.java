package com.mycompany.contact_app.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("COMPANY")
@Getter
@Setter
public class Company extends BaseContact {
    private String taxId;
    private String industry;
}