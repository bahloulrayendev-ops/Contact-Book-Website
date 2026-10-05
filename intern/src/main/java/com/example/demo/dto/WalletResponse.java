package com.example.demo.dto;

public record WalletResponse(Long walletId, int balance, long totalSpent, long totalPurchased) {
}