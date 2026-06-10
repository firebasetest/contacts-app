package com.mycompany.contact_app.dto;

import java.util.HashMap;
import java.util.Map;

public class ImportRowDto {
    private String recordType; // COMPANY, GENERAL
    private String name;
    private String email;
    private String phoneNumber;
    private String taxId;
    private String industry;
    private Map<String, Object> genericAttributes = new HashMap<>();

    // Getters and Setters
    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Map<String, Object> getGenericAttributes() {
        return genericAttributes;
    }

    public void setGenericAttributes(Map<String, Object> genericAttributes) {
        this.genericAttributes = genericAttributes;
    }
}