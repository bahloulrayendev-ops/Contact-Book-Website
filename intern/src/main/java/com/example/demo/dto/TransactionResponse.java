package com.example.demo.dto;

import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String type,
        int amount,
        LocalDateTime createdAt,
        int balanceAfter,
        String description
) {
}