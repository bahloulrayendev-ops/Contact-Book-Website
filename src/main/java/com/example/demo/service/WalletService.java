package com.example.demo.service;

import com.example.demo.entity.Wallet;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet getByClientId(Long clientId) {
        return walletRepository.findByClient_ClientId(clientId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));
    }
}
