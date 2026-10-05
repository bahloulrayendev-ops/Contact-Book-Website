package com.example.demo.dto;

public record EmployeeSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String jobTitle,
        String department
) {
}
