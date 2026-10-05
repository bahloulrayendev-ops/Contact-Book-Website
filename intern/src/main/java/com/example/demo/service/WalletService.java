package com.example.demo.service;

import com.example.demo.dto.WalletResponse;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.entity.TransactionHistory;
import com.example.demo.enume.TransactionType;
import com.example.demo.repository.TransactionHistoryRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    public WalletService(WalletRepository walletRepository,
                         TransactionHistoryRepository transactionHistoryRepository) {
        this.walletRepository = walletRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getByClientId(Long clientId) {
        return walletRepository.findByClientClientId(clientId)
                .map(wallet -> new WalletResponse(
                        wallet.getWalletId(), wallet.getBalance(),
                        transactionHistoryRepository.sumAmountByClientAndType(clientId, TransactionType.debit),
                        transactionHistoryRepository.sumAmountByClientAndType(clientId, TransactionType.credit)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Long clientId, Pageable pageable) {
        return transactionHistoryRepository.findByWalletClientClientIdOrderByCreatedAtDesc(clientId, pageable)
                .map(this::toTransactionResponse);
    }

    private TransactionResponse toTransactionResponse(TransactionHistory history) {
        boolean credit = history.getTransactionType() == TransactionType.credit;
        String key = history.getTokenTransaction().getIdempotencyKey();
        String description = key.startsWith("purchase-") ? "Token package purchase" : "Contact unlock";
        return new TransactionResponse(
                history.getTraceId(), credit ? "PURCHASE" : "UNLOCK",
                credit ? history.getAmount() : -history.getAmount(), history.getCreatedAt(),
                history.getBalanceAfter(), description);
    }
}