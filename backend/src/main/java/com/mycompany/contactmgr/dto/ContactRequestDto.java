package com.mycompany.contactmgr.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ContactRequestDto {
    private String name;
    private String email;
    private String phoneNumber;
    private String status;
    private Map<String, Object> customAttributes;
}
