package com.example.demo.service;

import com.example.demo.dto.ContactUpdateNotificationResponse;
import com.example.demo.entity.ContactDetails;
import com.example.demo.entity.ContactUpdateNotification;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Unlock;
import com.example.demo.entity.UnlockId;
import com.example.demo.entity.Wallet;
import com.example.demo.enume.TransactionType;
import com.example.demo.exception.InsufficientTokensException;
import com.example.demo.repository.ContactUpdateNotificationRepository;
import com.example.demo.repository.UnlockRepository;
import com.example.demo.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContactUpdateNotificationService {

    private final ContactUpdateNotificationRepository notificationRepository;
    private final UnlockRepository unlockRepository;
    private final WalletRepository walletRepository;
    private final TokenLedgerService tokenLedgerService;

    @Transactional(readOnly = true)
    public Page<ContactUpdateNotificationResponse> getNotifications(Long clientId, Pageable pageable) {
        return notificationRepository.findByClientClientIdOrderByCreatedAtDesc(clientId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long clientId) {
        return notificationRepository.countByClientClientIdAndReadAtIsNull(clientId);
    }

    @Transactional
    public ContactUpdateNotificationResponse markRead(Long clientId, Long notificationId) {
        ContactUpdateNotification notification = findForClient(clientId, notificationId);
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        return toResponse(notification);
    }

    @Transactional
    public ContactUpdateNotificationResponse repurchaseUpdatedContact(Long clientId, Long notificationId) {
        ContactUpdateNotification notification = findForClient(clientId, notificationId);
        if (notification.isRepurchaseCompleted()) {
            return toResponse(notification);
        }
        if (!notification.isRequiresRepurchase()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "This update was already included with your active account");
        }

        int discountedCost = notification.getDiscountedTokenCost();
        if (discountedCost <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This notification has no repurchase offer");
        }

        Long contactDetailId = notification.getContactDetail().getCdId();
        Wallet wallet = walletRepository.findByClientIdForUpdate(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        if (wallet.getBalance() < discountedCost) {
            throw new InsufficientTokensException("Not enough tokens for the discounted updated contact");
        }

        Unlock unlock = unlockRepository.findById(new UnlockId(clientId, contactDetailId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "The original contact unlock could not be found"));

        tokenLedgerService.record(wallet, TransactionType.debit, discountedCost,
                "notification-repurchase-" + notificationId);
        unlock.setTokenSpent(unlock.getTokenSpent() + discountedCost);
        unlock.setUnlockedAt(LocalDateTime.now());

        LocalDateTime repurchasedAt = LocalDateTime.now();
        for (ContactUpdateNotification pending : notificationRepository
            .findByClientClientIdAndContactDetailCdIdAndRequiresRepurchaseTrueAndRepurchaseCompletedFalse(
                clientId, contactDetailId)) {
            pending.setNewValue(notification.getContactDetail().getValue());
            pending.setRepurchaseCompleted(true);
            pending.setReadAt(repurchasedAt);
        }
        return toResponse(notification);
    }

    private ContactUpdateNotification findForClient(Long clientId, Long notificationId) {
        return notificationRepository.findForClientForUpdate(notificationId, clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    }

    private ContactUpdateNotificationResponse toResponse(ContactUpdateNotification notification) {
        boolean canSeeValue = !notification.isRequiresRepurchase() || notification.isRepurchaseCompleted();
        String value = canSeeValue
            ? notification.getNewValue()
                : null;
        Integer discountedCost = notification.isRequiresRepurchase() && !notification.isRepurchaseCompleted()
            ? notification.getDiscountedTokenCost()
            : null;
        String title = formatNotificationTitle(notification);
        String message = formatNotificationMessage(notification);

        return new ContactUpdateNotificationResponse(
                notification.getNotificationId(),
                notification.getContactDetail().getCdId(),
                title,
                message,
                notification.getContactType(),
                value,
                discountedCost,
                notification.getReadAt() != null,
                notification.isRepurchaseCompleted(),
                notification.getCreatedAt());
    }

    private String formatNotificationTitle(ContactUpdateNotification notification) {
        String personContext = formatOwnerAndType(notification);
        return personContext == null ? notification.getTitle() : personContext + " was updated.";
    }

    private String formatNotificationMessage(ContactUpdateNotification notification) {
        String personContext = formatOwnerAndType(notification);
        if (personContext == null) {
            return notification.getMessage();
        }
        return personContext + " was updated.";
    }

    private String formatOwnerAndType(ContactUpdateNotification notification) {
        ContactDetails contactDetail = notification.getContactDetail();
        if (contactDetail == null) {
            return null;
        }

        Employee employee = contactDetail.getEmployee();
        String ownerName = employee == null
                ? "This contact"
                : employee.getFirstName() + " " + employee.getLastName();

        String typeLabel = notification.getContactType() == null
                ? "contact detail"
                : notification.getContactType().name().replace('_', ' ').toLowerCase();

        return ownerName + "'s " + typeLabel;
    }
}