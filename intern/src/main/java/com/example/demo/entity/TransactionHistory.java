package com.example.demo.entity;


import com.example.demo.enume.TransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.transaction.Transaction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="transactionhistory")
@Entity
@Setter
@Getter
public class TransactionHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long traceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private int balanceAfter;

    @JoinColumn(name = "walletId", referencedColumnName = "walletId", nullable = false)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Wallet wallet;


    @JoinColumn( name ="transactionId", referencedColumnName = "transactionId",nullable = false)
    @JsonIgnore
    @OneToOne
    private TokenTransaction tokenTransaction;



}
