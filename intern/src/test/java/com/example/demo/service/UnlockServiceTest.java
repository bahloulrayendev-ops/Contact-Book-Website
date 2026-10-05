package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.ContactDetails;
import com.example.demo.entity.TokenTransaction;
import com.example.demo.entity.Unlock;
import com.example.demo.entity.Wallet;
import com.example.demo.enume.ContactType;
import com.example.demo.enume.TransactionType;
import com.example.demo.exception.InsufficientTokensException;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.ContactDetailsRepository;
import com.example.demo.repository.ContactUpdateNotificationRepository;
import com.example.demo.repository.UnlockRepository;
import com.example.demo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnlockServiceTest {

    private static final Long CLIENT_ID = 11L;
    private static final Long CONTACT_ID = 24L;

    @Mock
    private UnlockRepository unlockRepository;
    @Mock
    private ContactDetailsRepository contactDetailsRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private TokenLedgerService tokenLedgerService;
    @Mock
    private ContactUpdateNotificationRepository notificationRepository;

    private UnlockService unlockService;
    private ContactDetails contact;
    private Wallet wallet;
    private Client client;

    @BeforeEach
    void setUp() {
        unlockService = new UnlockService(unlockRepository, contactDetailsRepository,
            walletRepository, clientRepository, tokenLedgerService, notificationRepository);

        contact = new ContactDetails();
        contact.setCdId(CONTACT_ID);
        contact.setContactType(ContactType.email);
        contact.setValue("person@example.com");
        contact.setTokenCost(5);

        wallet = new Wallet();
        wallet.setBalance(12);

        client = new Client();
        client.setClientId(CLIENT_ID);
    }

    @Test
    void unlockDebitsOnceAndReturnsContactValueAndRemainingBalance() {
        when(contactDetailsRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));
        when(walletRepository.findByClientIdForUpdate(CLIENT_ID)).thenReturn(Optional.of(wallet));
        when(unlockRepository.existsForClientAndContact(CLIENT_ID, CONTACT_ID)).thenReturn(false);
        when(clientRepository.getReferenceById(CLIENT_ID)).thenReturn(client);
        doAnswer(invocation -> {
            wallet.setBalance(wallet.getBalance() - invocation.getArgument(2, Integer.class));
            return new TokenTransaction();
        }).when(tokenLedgerService).record(wallet, TransactionType.debit, 5, "unlock-11-24");

        var response = unlockService.unlock(CLIENT_ID, CONTACT_ID);

        assertEquals("person@example.com", response.value());
        assertEquals(5, response.tokensSpent());
        assertEquals(7, response.balance());
        ArgumentCaptor<Unlock> savedUnlock = ArgumentCaptor.forClass(Unlock.class);
        verify(unlockRepository).save(savedUnlock.capture());
        assertEquals(CLIENT_ID, savedUnlock.getValue().getId().getUserId());
        assertEquals(CONTACT_ID, savedUnlock.getValue().getId().getCdId());
    }

    @Test
    void alreadyUnlockedContactIsReturnedWithoutAnotherDebit() {
        wallet.setBalance(7);
        when(contactDetailsRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));
        when(walletRepository.findByClientIdForUpdate(CLIENT_ID)).thenReturn(Optional.of(wallet));
        when(unlockRepository.existsForClientAndContact(CLIENT_ID, CONTACT_ID)).thenReturn(true);

        var response = unlockService.unlock(CLIENT_ID, CONTACT_ID);

        assertEquals("person@example.com", response.value());
        assertEquals(0, response.tokensSpent());
        assertEquals(7, response.balance());
        verify(tokenLedgerService, never()).record(any(), any(), anyInt(), anyString());
        verify(unlockRepository, never()).save(any());
    }

    @Test
    void pendingUpdatedContactCannotBeUnlockedAtTheFullPriceEndpoint() {
        when(contactDetailsRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));
        when(walletRepository.findByClientIdForUpdate(CLIENT_ID)).thenReturn(Optional.of(wallet));
        when(unlockRepository.existsForClientAndContact(CLIENT_ID, CONTACT_ID)).thenReturn(true);
        when(notificationRepository
                .existsByClientClientIdAndContactDetailCdIdAndRequiresRepurchaseTrueAndRepurchaseCompletedFalse(
                        CLIENT_ID, CONTACT_ID)).thenReturn(true);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> unlockService.unlock(CLIENT_ID, CONTACT_ID));
        verify(tokenLedgerService, never()).record(any(), any(), anyInt(), anyString());
    }

    @Test
    void insufficientBalanceDoesNotCreateUnlockOrLedgerEntry() {
        wallet.setBalance(4);
        when(contactDetailsRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));
        when(walletRepository.findByClientIdForUpdate(CLIENT_ID)).thenReturn(Optional.of(wallet));
        when(unlockRepository.existsForClientAndContact(CLIENT_ID, CONTACT_ID)).thenReturn(false);

        assertThrows(InsufficientTokensException.class,
                () -> unlockService.unlock(CLIENT_ID, CONTACT_ID));

        verify(tokenLedgerService, never()).record(any(), any(), anyInt(), anyString());
        verify(unlockRepository, never()).save(any());
    }
}