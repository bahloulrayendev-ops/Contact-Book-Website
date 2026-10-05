package com.example.demo.dto;

import com.example.demo.enume.ContactType;

import java.time.LocalDateTime;


public record ContactDetailResponse(
        Long id,
        ContactType contactType,
        int tokenCost,
        LocalDateTime lastVerified,
        boolean locked,
        String value
) {
}
