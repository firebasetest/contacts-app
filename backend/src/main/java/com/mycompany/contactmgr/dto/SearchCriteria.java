package com.mycompany.contact_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCriteria {
    private String keyword;
    private String sortBy;
    private String sortOrder;
    private int page;
    private int size;
}
