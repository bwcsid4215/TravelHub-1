package com.bwc.employee_management_service.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String keyword;
    private String department;
    private Boolean active;
    private int page = 0;
    private int size = 10;
    private String sortBy = "fullName";
    private String sortDirection = "asc";
}