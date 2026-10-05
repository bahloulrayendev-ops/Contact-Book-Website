package com.example.demo.dto;

import java.util.List;

public record PeopleHubEmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String jobTitle,
        String department,
        CompanyRefResponse company,
        List<ContactDetailResponse> contactDetails
) {
}
