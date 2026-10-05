package com.example.demo.service;

import com.example.demo.entity.PackagePurchase;
import com.example.demo.entity.TokenPackage;
import com.example.demo.entity.Wallet;
import com.example.demo.enume.TransactionStatus;
import com.example.demo.enume.TransactionType;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.PackagePurchaseRepository;
import com.example.demo.repository.TokenPackageRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Step 1 - called when the client starts checkout. Creates a PENDING
    // record before redirecting to the payment gateway.
    @Transactional
    public PackagePurchase initiate(Long clientId, Long tokenPackageId, String provider) {
        Wallet wallet = walletRepository.findByClient_ClientId(clientId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));
        TokenPackage tokenPackage = tokenPackageRepository.findById(tokenPackageId)
                .orElseThrow(() -> new NotFoundException("Package not found"));

        PackagePurchase purchase = new PackagePurchase();
        purchase.setWallet(wallet);
        purchase.setTokenPackage(tokenPackage);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setTransactionStatus(TransactionStatus.pending);
        purchase.setProvider(provider);
        return packagePurchaseRepository.save(purchase);
    }

    // Step 2 - called from the payment gateway's webhook, never directly from
    // the frontend. Safe to call more than once with the same purchaseId.
    @Transactional
    public PackagePurchase confirm(Long purchaseId) {
        PackagePurchase purchase = packagePurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));

        if (purchase.getTransactionStatus() == TransactionStatus.succeeded) {
            return purchase; // already processed - webhook retries are safe
        }

        Long clientId = purchase.getWallet().getClient().getClientId();
        Wallet wallet = walletRepository.findByClientIdForUpdate(clientId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        purchase.setTransactionStatus(TransactionStatus.succeeded);
        packagePurchaseRepository.save(purchase);

        tokenLedgerService.record(wallet, TransactionType.CREDIT,
                purchase.getTokenPackage().getTokens(), "purchase-" + purchase.getPackagePurchaseId());

        return purchase;
    }
}
