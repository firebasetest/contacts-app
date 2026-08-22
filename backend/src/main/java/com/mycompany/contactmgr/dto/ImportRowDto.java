package com.mycompany.contactmgr.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class ImportRowDto {
    // Getters and Setters
    private String recordType; // COMPANY, GENERAL
    private String name;
    private String email;
    private String phoneNumber;
    private String taxId;
    private String industry;
    private Map<String, Object> genericAttributes = new HashMap<>();

}