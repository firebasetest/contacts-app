package com.mycompany.contactmgr.dto;

import com.mycompany.contactmgr.model.BusinessUnitStatus;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BusinessUnitInputDto {
    private String name;
    private String description;
    private BusinessUnitStatus initialStatus;

    // Getters and Setters
}
