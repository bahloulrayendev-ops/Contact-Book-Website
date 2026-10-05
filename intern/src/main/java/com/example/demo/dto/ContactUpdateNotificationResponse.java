package com.example.demo.dto;

import com.example.demo.enume.ContactType;

import java.time.LocalDateTime;

public record ContactUpdateNotificationResponse(
        Long id,
        Long contactDetailId,
        String title,
        String message,
        ContactType contactType,
        String newValue,
        Integer discountedTokenCost,
        boolean read,
        boolean repurchaseCompleted,
        LocalDateTime createdAt
) {
}