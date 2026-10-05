package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.ContactDetails;
import com.example.demo.entity.ContactUpdateNotification;
import com.example.demo.entity.Employee;
import com.example.demo.entity.TokenTransaction;
import com.example.demo.entity.Unlock;
import com.example.demo.entity.UnlockId;
import com.example.demo.entity.Wallet;
import com.example.demo.enume.AccountStatus;
import com.example.demo.enume.ContactType;
import com.example.demo.enume.TransactionType;
import com.example.demo.repository.ContactUpdateNotificationRepository;
import com.example.demo.repository.UnlockRepository;
import com.example.demo.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactUpdateNotificationServiceTest {

    private static final Long CLIENT_ID = 17L;
    private static final Long CONTACT_ID = 23L;
    private static final Long NOTIFICATION_ID = 31L;

    @Mock
    private ContactUpdateNotificationRepository notificationRepository;
    @Mock
    private UnlockRepository unlockRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TokenLedgerService tokenLedgerService;
    @InjectMocks
    private ContactUpdateNotificationService notificationService;

    @Test
    void activeClientCanSeeUpdatedValueWithoutRepurchaseOffer() {
        ContactUpdateNotification notification = notification(AccountStatus.active);
                notification.getClient().setStatusType(AccountStatus.pending);
        when(notificationRepository.findByClientClientIdOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(CLIENT_ID), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(notification)));

        var response = notificationService.getNotifications(CLIENT_ID, PageRequest.of(0, 20))
                .getContent().get(0);

        assertEquals("new-value@example.invalid", response.newValue());
        assertNull(response.discountedTokenCost());
    }

    @Test
    void nonActiveClientSeesDiscountButNotUpdatedValueUntilPurchase() {
                ContactUpdateNotification notification = notification(AccountStatus.pending);
                notification.getClient().setStatusType(AccountStatus.active);
        when(notificationRepository.findByClientClientIdOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(CLIENT_ID), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(notification)));

        var response = notificationService.getNotifications(CLIENT_ID, PageRequest.of(0, 20))
                .getContent().get(0);

        assertNull(response.newValue());
        assertEquals(3, response.discountedTokenCost());
    }

    @Test
    void notificationIncludesUpdatedEmployeesName() {
        ContactUpdateNotification notification = notification(AccountStatus.active);
        when(notificationRepository.findByClientClientIdOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(CLIENT_ID), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(notification)));

        var response = notificationService.getNotifications(CLIENT_ID, PageRequest.of(0, 20))
                .getContent().get(0);

        assertEquals("Jane Doe's email was updated.", response.title());
        assertEquals("Jane Doe's email was updated.", response.message());
    }

    @Test
    void discountedRepurchaseDebitsWalletAndRevealsCurrentContactValue() {
        ContactUpdateNotification notification = notification(AccountStatus.pending);
        notification.setNotificationId(NOTIFICATION_ID);
        Wallet wallet = new Wallet();
        wallet.setBalance(8);
        Unlock unlock = new Unlock();
        unlock.setId(new UnlockId(CLIENT_ID, CONTACT_ID));
        unlock.setTokenSpent(5);

        when(notificationRepository.findForClientForUpdate(NOTIFICATION_ID, CLIENT_ID))
                .thenReturn(Optional.of(notification));
        when(notificationRepository
                .findByClientClientIdAndContactDetailCdIdAndRequiresRepurchaseTrueAndRepurchaseCompletedFalse(
                        CLIENT_ID, CONTACT_ID))
                .thenReturn(List.of(notification));
        when(walletRepository.findByClientIdForUpdate(CLIENT_ID)).thenReturn(Optional.of(wallet));
        when(unlockRepository.findById(new UnlockId(CLIENT_ID, CONTACT_ID))).thenReturn(Optional.of(unlock));
        when(tokenLedgerService.record(wallet, TransactionType.debit, 3,
                "notification-repurchase-" + NOTIFICATION_ID)).thenReturn(new TokenTransaction());

        var response = notificationService.repurchaseUpdatedContact(CLIENT_ID, NOTIFICATION_ID);

        assertEquals("new-value@example.invalid", response.newValue());
        assertNull(response.discountedTokenCost());
        assertTrue(response.repurchaseCompleted());
        assertEquals(8, unlock.getTokenSpent());
        verify(tokenLedgerService).record(wallet, TransactionType.debit, 3,
                "notification-repurchase-" + NOTIFICATION_ID);
    }

    private ContactUpdateNotification notification(AccountStatus status) {
        Client client = new Client();
        client.setClientId(CLIENT_ID);
        client.setStatusType(status);

        ContactDetails contact = new ContactDetails();
        contact.setCdId(CONTACT_ID);
        contact.setContactType(ContactType.email);
        contact.setValue("new-value@example.invalid");

        Employee employee = new Employee();
        employee.setFirstName("Jane");
        employee.setLastName("Doe");
        contact.setEmployee(employee);

        ContactUpdateNotification notification = new ContactUpdateNotification();
        notification.setNotificationId(NOTIFICATION_ID);
        notification.setClient(client);
        notification.setContactDetail(contact);
        notification.setTitle("Contact details updated");
        notification.setMessage("A contact you unlocked has changed.");
        notification.setContactType(ContactType.email);
        notification.setNewValue("new-value@example.invalid");
        notification.setDiscountedTokenCost(3);
        notification.setRequiresRepurchase(status != AccountStatus.active);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}