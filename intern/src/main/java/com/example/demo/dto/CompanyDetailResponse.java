package com.example.demo.dto;

import com.example.demo.enume.CompanyType;

import java.time.LocalDateTime;
import java.util.List;

public record CompanyDetailResponse(
        Long id,
        String name,
        String description,
        String industry,
        String city,
        String country,
        String website,
        String linkedin,
        int estabYear,
        CompanyType companyType,
        long employeeCount,
        List<String> geographicPresence
) {
}
