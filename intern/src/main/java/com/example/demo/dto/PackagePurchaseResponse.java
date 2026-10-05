package com.example.demo.dto;

import com.example.demo.enume.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PackagePurchaseResponse(
        Long purchaseId,
        TransactionStatus status,
        String packageName,
        BigDecimal price,
        int tokens,
        LocalDateTime purchaseDate
) {
}