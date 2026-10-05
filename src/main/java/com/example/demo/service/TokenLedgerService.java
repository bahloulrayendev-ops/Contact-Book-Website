package com.example.demo.service;

import com.example.demo.entity.TokenTransaction;
import com.example.demo.entity.TransactionHistory;
import com.example.demo.entity.Wallet;
import com.example.demo.enume.TransactionStatus;
import com.example.demo.enume.TransactionType;
import com.example.demo.repository.TokenTransactionRepository;
import com.example.demo.repository.TransactionHistoryRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenLedgerService {

    private final TokenTransactionRepository tokenTransactionRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final WalletRepository walletRepository;

    public TokenLedgerService(TokenTransactionRepository tokenTransactionRepository,
                               TransactionHistoryRepository transactionHistoryRepository,
                               WalletRepository walletRepository) {
        this.tokenTransactionRepository = tokenTransactionRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.walletRepository = walletRepository;
    }

    /**
     * Writes a TokenTransaction + TransactionHistory pair and updates the
     * wallet balance. The wallet passed in must already be locked by the
     * caller (WalletRepository.findByClientIdForUpdate) - this method does
     * not do any locking itself.
     */
    @Transactional
    public TokenTransaction record(Wallet wallet, TransactionType type, int amount, String idempotencyKey) {
        TokenTransaction transaction = new TokenTransaction();
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.succeeded);
        transaction = tokenTransactionRepository.save(transaction);

        int signedAmount = (type == TransactionType.CREDIT) ? amount : -amount;
        wallet.setBalance(wallet.getBalance() + signedAmount);
        walletRepository.save(wallet);

        TransactionHistory history = new TransactionHistory();
        history.setTransactionType(type);
        history.setAmount(amount);
        history.setBalanceAfter(wallet.getBalance());
        history.setWallet(wallet);
        history.setTokenTransaction(transaction);
        transactionHistoryRepository.save(history);

        return transaction;
    }
}
