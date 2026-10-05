package com.example.demo.entity;

import com.example.demo.enume.ContactType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_client_created", columnList = "client_id, created_at")
})
@Getter
@Setter
public class ContactUpdateNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_detail_id", nullable = false)
    private ContactDetails contactDetail;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 32)
    private ContactType contactType;

    @Column(name = "new_value", columnDefinition = "text")
    private String newValue;

    @Column(name = "discounted_token_cost", nullable = false)
    private int discountedTokenCost;

    @Column(name = "requires_repurchase", nullable = false)
    private boolean requiresRepurchase;

    @Column(name = "repurchase_completed", nullable = false)
    private boolean repurchaseCompleted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}