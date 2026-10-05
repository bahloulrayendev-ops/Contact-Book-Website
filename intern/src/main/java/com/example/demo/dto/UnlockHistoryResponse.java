package com.example.demo.dto;

import com.example.demo.enume.ContactType;

import java.time.LocalDateTime;

public record UnlockHistoryResponse(
        String id,
        String employeeName,
        String companyName,
        ContactType fieldType,
        String value,
        int tokensSpent,
        LocalDateTime unlockedAt
) {
}