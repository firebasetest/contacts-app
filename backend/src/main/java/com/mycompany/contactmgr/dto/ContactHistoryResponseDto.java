package com.mycompany.contactmgr.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
public class ContactHistoryResponseDto {
    // Getters and Setters
    private UUID historyId;
    private int version;
    private String captureType; // INSERT, UPDATE, DELETE
    private LocalDateTime validFrom;
    private String modifiedBy;
    private String name;
    private String email;
    private String phoneNumber;
    private Map<String, Object> customAttributes;
    private Map<String, String> fieldDeltas; // e.g., {"phone_number": "Changed from X to Y"}

}