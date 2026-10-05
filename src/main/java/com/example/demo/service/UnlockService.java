package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.ContactDetails;
import com.example.demo.entity.Unlock;
import com.example.demo.entity.Wallet;
import com.example.demo.enume.TransactionType;
import com.example.demo.exception.InsufficientTokensException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.ContactDetailsRepository;
import com.example.demo.repository.UnlockRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UnlockService {

    private final UnlockRepository unlockRepository;
    private final ContactDetailsRepository contactDetailsRepository;
    private final WalletRepository walletRepository;
    private final ClientRepository clientRepository;
    private final TokenLedgerService tokenLedgerService;

    public UnlockService(UnlockRepository unlockRepository,
                          ContactDetailsRepository contactDetailsRepository,
                          WalletRepository walletRepository,
                          ClientRepository clientRepository,
                          TokenLedgerService tokenLedgerService) {
        this.unlockRepository = unlockRepository;
        this.contactDetailsRepository = contactDetailsRepository;
        this.walletRepository = walletRepository;
        this.clientRepository = clientRepository;
        this.tokenLedgerService = tokenLedgerService;
    }

    @Transactional
    public ContactDetails unlock(Long clientId, Long cdId) {
        ContactDetails contactDetail = contactDetailsRepository.findById(cdId)
                .orElseThrow(() -> new NotFoundException("Contact detail not found"));

        // Already unlocked - return it free, don't charge twice.
        if (unlockRepository.existsByClient_ClientIdAndContactDetail_CdId(clientId, cdId)) {
            return contactDetail;
        }

        Wallet wallet = walletRepository.findByClientIdForUpdate(clientId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        if (wallet.getBalance() < contactDetail.getTokenCost()) {
            throw new InsufficientTokensException("Not enough tokens to unlock this contact");
        }

        tokenLedgerService.record(wallet, TransactionType.DEBIT,
                contactDetail.getTokenCost(), "unlock-" + clientId + "-" + cdId);

        Client client = clientRepository.getReferenceById(clientId);

        Unlock unlock = new Unlock();
        unlock.setClient(client);
        unlock.setContactDetail(contactDetail);
        unlock.setTokenSpent(contactDetail.getTokenCost());
        unlock.setUnlockedAt(LocalDateTime.now());
        unlockRepository.save(unlock);

        return contactDetail;
    }
}
