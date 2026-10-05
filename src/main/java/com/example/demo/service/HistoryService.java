package com.example.demo.service;

import com.example.demo.entity.PackagePurchase;
import com.example.demo.entity.TransactionHistory;
import com.example.demo.entity.Unlock;
import com.example.demo.entity.Wallet;
import com.example.demo.repository.PackagePurchaseRepository;
import com.example.demo.repository.TransactionHistoryRepository;
import com.example.demo.repository.UnlockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

    private final UnlockRepository unlockRepository;
    private final PackagePurchaseRepository packagePurchaseRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final WalletService walletService;

    public HistoryService(UnlockRepository unlockRepository,
                           PackagePurchaseRepository packagePurchaseRepository,
                           TransactionHistoryRepository transactionHistoryRepository,
                           WalletService walletService) {
        this.unlockRepository = unlockRepository;
        this.packagePurchaseRepository = packagePurchaseRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.walletService = walletService;
    }

    public Page<Unlock> getUnlockHistory(Long clientId, Pageable pageable) {
        return unlockRepository.findByClient_ClientId(clientId, pageable);
    }

    public Page<PackagePurchase> getPurchaseHistory(Long clientId, Pageable pageable) {
        Wallet wallet = walletService.getByClientId(clientId);
        return packagePurchaseRepository.findByWallet_WalletIdOrderByPurchaseDateDesc(wallet.getWalletId(), pageable);
    }

    public Page<TransactionHistory> getTransactionHistory(Long clientId, Pageable pageable) {
        Wallet wallet = walletService.getByClientId(clientId);
        return transactionHistoryRepository.findByWallet_WalletId(wallet.getWalletId(), pageable);
    }
}
