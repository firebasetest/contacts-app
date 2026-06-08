package com.mycompany.contact_app.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ContactResponse {
    private UUID id;
    private String name;
    private String email;
    private String status;
    private Map<String, Object> customAttributes;
    private LocalDateTime updatedAt;
}