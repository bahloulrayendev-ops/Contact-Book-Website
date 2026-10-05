package com.example.demo.controller;

import com.example.demo.dto.PackagePurchaseResponse;
import com.example.demo.dto.PurchaseRequest;
import com.example.demo.service.PackagePurchaseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/tokens")
public class PackagePurchaseController {

    private final PackagePurchaseService packagePurchaseService;
    private final String webhookSecret;

    public PackagePurchaseController(PackagePurchaseService packagePurchaseService,
                                     @Value("${payment.webhook-secret:}") String webhookSecret) {
        this.packagePurchaseService = packagePurchaseService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/purchase")
    public PackagePurchaseResponse purchase(@AuthenticationPrincipal Long clientId,
                                            @Valid @RequestBody PurchaseRequest request) {
        return packagePurchaseService.initiate(clientId, request.tokenPackageId(), request.provider());
    }

    @PostMapping("/webhook")
    public PackagePurchaseResponse confirm(@RequestParam Long purchaseId,
                                           @RequestHeader(name = "X-Webhook-Secret", required = false) String suppliedSecret) {
        if (webhookSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment webhook secret is not configured");
        }
        if (suppliedSecret == null || !MessageDigest.isEqual(
                webhookSecret.getBytes(StandardCharsets.UTF_8), suppliedSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid payment webhook secret");
        }
        return packagePurchaseService.confirm(purchaseId);
    }
}