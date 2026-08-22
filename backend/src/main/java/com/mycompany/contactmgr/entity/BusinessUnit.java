package com.mycompany.contactmgr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

import com.mycompany.contactmgr.model.Status;

@Entity
@Table(name = "business_units")
@Getter
@Setter
public class BusinessUnit {
    @Id
    private UUID id;
    // *** CRITICAL: This defines the tenant key ***
    @Column(unique = true, nullable = false)
    private String businessUnitId; // Corresponds to the X-Tenant-Id header

    @Column(unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    private boolean active;
}