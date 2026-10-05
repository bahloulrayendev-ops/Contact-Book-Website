package com.example.demo.dto;

import com.example.demo.enume.CompanyType;

public record CompanySummaryResponse(
        Long id,
        String name,
        String industry,
        String city,
        String country,
        String website,
        CompanyType companyType,
        long employeeCount
) {
}
