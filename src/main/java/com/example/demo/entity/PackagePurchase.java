package com.example.demo.entity;


import com.example.demo.enume.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name="packagepurchase")
@Entity
@Setter
@Getter
public class PackagePurchase extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packagePurchaseId;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(nullable = false)
    private String provider;

    @JoinColumn(name="tokenPackageId",referencedColumnName ="TokenPackageId" ,nullable = false)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private TokenPackage tokenPackage;


    @JsonIgnore
    @JoinColumn(name="walletId" , referencedColumnName = "walletId",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Wallet wallet;




}
