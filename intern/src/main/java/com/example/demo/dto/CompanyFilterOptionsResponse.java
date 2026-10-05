package com.example.demo.dto;

import com.example.demo.enume.CompanyType;

import java.util.List;

public record CompanyFilterOptionsResponse(
        List<String> industries,
        List<String> countries,
        List<CompanyType> companyTypes
) {
}
