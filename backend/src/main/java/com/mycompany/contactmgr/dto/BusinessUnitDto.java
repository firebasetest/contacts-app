package com.mycompany.contactmgr.dto;

import java.time.Instant;
import java.util.UUID;
import com.mycompany.contactmgr.model.BusinessUnitStatus;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BusinessUnitDto {
    private UUID businessUnitId;
    private String name;
    private String slug;
    private String description;
    private BusinessUnitStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // Constructor, Getters, and Setters (Omitted for brevity)
}