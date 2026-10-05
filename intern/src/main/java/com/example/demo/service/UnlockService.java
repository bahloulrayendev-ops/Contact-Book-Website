package com.example.demo.service;

import com.example.demo.dto.UnlockResponse;
import com.example.demo.entity.ContactDetails;
import com.example.demo.entity.Unlock;
import com.example.demo.entity.UnlockId;
import com.example.demo.entity.Wallet;
import com.example.demo.enume.TransactionType;
import com.example.demo.exception.InsufficientTokensException;
import com.example.demo.repository.ContactDetailsRepository;
import com.example.demo.repository.ContactUpdateNotificationRepository;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.UnlockRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class UnlockService {

    private final UnlockRepository unlockRepository;
    private final ContactDetailsRepository contactDetailsRepository;
    private final WalletRepository walletRepository;
    private final ClientRepository clientRepository;
    private final TokenLedgerService tokenLedgerService;
    private final ContactUpdateNotificationRepository notificationRepository;

    public UnlockService(UnlockRepository unlockRepository,
                         ContactDetailsRepository contactDetailsRepository,
                         WalletRepository walletRepository,
                         ClientRepository clientRepository,
                         TokenLedgerService tokenLedgerService,
                         ContactUpdateNotificationRepository notificationRepository) {
        this.unlockRepository = unlockRepository;
        this.contactDetailsRepository = contactDetailsRepository;
        this.walletRepository = walletRepository;
        this.clientRepository = clientRepository;
        this.tokenLedgerService = tokenLedgerService;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public UnlockResponse unlock(Long clientId, Long contactDetailId) {
        ContactDetails contact = contactDetailsRepository.findById(contactDetailId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact detail not found"));
        Wallet wallet = walletRepository.findByClientIdForUpdate(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        if (unlockRepository.existsForClientAndContact(clientId, contactDetailId)) {
            if (notificationRepository
                .existsByClientClientIdAndContactDetailCdIdAndRequiresRepurchaseTrueAndRepurchaseCompletedFalse(
                    clientId, contactDetailId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "This contact was updated. Use its discounted offer in Notifications to unlock the new value.");
            }
            return toResponse(contact, 0, wallet.getBalance());
        }

        if (contact.getTokenCost() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Contact detail has an invalid token cost");
        }
        if (wallet.getBalance() < contact.getTokenCost()) {
            throw new InsufficientTokensException("Not enough tokens to unlock this contact");
        }

        tokenLedgerService.record(wallet, TransactionType.debit, contact.getTokenCost(),
                "unlock-" + clientId + "-" + contactDetailId);

        Unlock unlock = new Unlock();
        unlock.setId(new UnlockId(clientId, contactDetailId));
        unlock.setClient(clientRepository.getReferenceById(clientId));
        unlock.setContactDetail(contact);
        unlock.setTokenSpent(contact.getTokenCost());
        unlock.setUnlockedAt(LocalDateTime.now());
        unlockRepository.save(unlock);

        return toResponse(contact, contact.getTokenCost(), wallet.getBalance());
    }

    private UnlockResponse toResponse(ContactDetails contact, int tokensSpent, int balance) {
        return new UnlockResponse(contact.getCdId(), contact.getContactType(),
                contact.getValue(), tokensSpent, balance);
    }
}