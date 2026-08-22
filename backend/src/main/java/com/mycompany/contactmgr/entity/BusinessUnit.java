package com.mycompany.contact_app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "business_units")
@Getter
@Setter
public class BusinessUnit {
    @Id
    private UUID id;
    private String name;
    private boolean active;
}