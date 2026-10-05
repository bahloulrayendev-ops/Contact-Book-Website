package com.example.demo.dto;

import java.util.List;

public record CompanyEmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String jobTitle,
        String department,
        List<ContactDetailResponse> contactDetails
) {
}
