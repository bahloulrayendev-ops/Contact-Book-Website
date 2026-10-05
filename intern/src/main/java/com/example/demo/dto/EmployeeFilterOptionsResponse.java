package com.example.demo.dto;

import java.util.List;

public record EmployeeFilterOptionsResponse(
        List<String> jobTitles,
        List<String> departments,
        List<String> industries,
        List<String> countries
) {
}