package com.mycompany.contact_app.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiException {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private String path;

    // This ensures the "errors" payload item is hidden entirely unless a validation
    // failure occurs
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> errors;
}
