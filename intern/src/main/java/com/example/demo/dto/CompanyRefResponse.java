package com.example.demo.dto;

public record CompanyRefResponse(
        Long id,
        String name,
        String country,
        String industry
) {
}
