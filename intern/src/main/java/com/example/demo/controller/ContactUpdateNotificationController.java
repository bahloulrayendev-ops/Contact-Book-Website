package com.example.demo.controller;

import com.example.demo.dto.ContactUpdateNotificationResponse;
import com.example.demo.service.ContactUpdateNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class ContactUpdateNotificationController {

    private final ContactUpdateNotificationService notificationService;

    @GetMapping
    public Page<ContactUpdateNotificationResponse> getNotifications(
            @AuthenticationPrincipal Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("createdAt").descending());
        return notificationService.getNotifications(clientId, pageable);
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(@AuthenticationPrincipal Long clientId) {
        return notificationService.getUnreadCount(clientId);
    }

    @PutMapping("/{notificationId}/read")
    public ContactUpdateNotificationResponse markRead(@AuthenticationPrincipal Long clientId,
                                                       @PathVariable Long notificationId) {
        return notificationService.markRead(clientId, notificationId);
    }

    @PostMapping("/{notificationId}/repurchase")
    public ContactUpdateNotificationResponse repurchase(@AuthenticationPrincipal Long clientId,
                                                        @PathVariable Long notificationId) {
        return notificationService.repurchaseUpdatedContact(clientId, notificationId);
    }
}