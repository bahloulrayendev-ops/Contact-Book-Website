package com.example.demo.dto;

public record SignupRequest(
        String firstName,
        String lastName,
        String phone,
        String email,
        String password
) {}