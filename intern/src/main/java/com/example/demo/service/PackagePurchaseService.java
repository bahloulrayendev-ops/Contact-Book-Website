package com.example.demo.service;

import com.example.demo.dto.PackagePurchaseResponse;
import com.example.demo.entity.PackagePurchase;
import com.example.demo.entity.TokenPackage;
import com.example.demo.entity.Wallet;
import com.example.demo.enume.Activestatus;
import com.example.demo.enume.TransactionStatus;
import com.example.demo.enume.TransactionType;
import com.example.demo.repository.PackagePurchaseRepository;
import com.example.demo.repository.TokenPackageRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class PackagePurchaseService {

    private final PackagePurchaseRepository packagePurchaseRepository;
    private final TokenPackageRepository tokenPackageRepository;
    private final WalletRepository walletRepository;
    private final TokenLedgerService tokenLedgerService;

    public PackagePurchaseService(PackagePurchaseRepository packagePurchaseRepository,
                                  TokenPackageRepository tokenPackageRepository,
                                  WalletRepository walletRepository,
                                  TokenLedgerService tokenLedgerService) {
        this.packagePurchaseRepository = packagePurchaseRepository;
        this.tokenPackageRepository = tokenPackageRepository;
        this.walletRepository = walletRepository;
        this.tokenLedgerService = tokenLedgerService;
    }

    @Transactional
    public PackagePurchaseResponse initiate(Long clientId, Long tokenPackageId, String provider) {
        Wallet wallet = walletRepository.findByClientIdForUpdate(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        TokenPackage tokenPackage = tokenPackageRepository.findById(tokenPackageId)
                .filter(item -> item.getStatus() == Activestatus.active)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active package not found"));

        PackagePurchase purchase = new PackagePurchase();
        purchase.setWallet(wallet);
        purchase.setTokenPackage(tokenPackage);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setTransactionStatus(TransactionStatus.pending);
        purchase.setProvider(provider.trim());
        return toResponse(packagePurchaseRepository.save(purchase));
    }

    @Transactional
    public PackagePurchaseResponse confirm(Long purchaseId) {
        PackagePurchase purchase = packagePurchaseRepository.findByIdForUpdate(purchaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));

        if (purchase.getTransactionStatus() == TransactionStatus.succeeded) {
            return toResponse(purchase);
        }
        if (purchase.getTransactionStatus() != TransactionStatus.pending) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Purchase is not pending");
        }

        Long clientId = purchase.getWallet().getClient().getClientId();
        Wallet wallet = walletRepository.findByClientIdForUpdate(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        tokenLedgerService.record(wallet, TransactionType.credit,
                purchase.getTokenPackage().getTokens(), "purchase-" + purchaseId);
        purchase.setTransactionStatus(TransactionStatus.succeeded);

        return toResponse(purchase);
    }

    private PackagePurchaseResponse toResponse(PackagePurchase purchase) {
        TokenPackage tokenPackage = purchase.getTokenPackage();
        return new PackagePurchaseResponse(
                purchase.getPackagePurchaseId(), purchase.getTransactionStatus(),
                tokenPackage.getName(), tokenPackage.getPrice(), tokenPackage.getTokens(),
                purchase.getPurchaseDate());
    }
}