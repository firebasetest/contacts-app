package com.mycompany.contact_app.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ContactRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String status;
    private Map<String, Object> customAttributes;
}
