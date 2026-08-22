package com.mycompany.contactmgr.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This class acts as the wrapper for our paginated results
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessUnitSearchResponse {
    private List<BusinessUnitDto> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;

    // Constructor, Getters, and Setters
}
