package com.example.demo.controller;

import com.example.demo.dto.WalletResponse;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public WalletResponse getWallet(@AuthenticationPrincipal Long clientId) {
        return walletService.getByClientId(clientId);
    }

    @GetMapping("/transactions")
    public Page<TransactionResponse> getTransactions(
            @AuthenticationPrincipal Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
        return walletService.getTransactions(clientId, pageable);
    }
}