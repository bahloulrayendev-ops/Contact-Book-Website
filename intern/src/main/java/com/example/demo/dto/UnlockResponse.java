package com.example.demo.dto;

import com.example.demo.enume.ContactType;

public record UnlockResponse(
        Long contactDetailId,
        ContactType contactType,
        String value,
        int tokensSpent,
        int balance
) {
}