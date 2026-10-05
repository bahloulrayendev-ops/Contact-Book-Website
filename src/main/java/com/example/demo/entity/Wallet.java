package com.example.demo.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="wallet")
@Entity
@Getter
@Setter
public class Wallet extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @Column(nullable = false)
    private int balance;

    @Version
    private Long version;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "clientId", referencedColumnName = "clientId", nullable = false, unique = true)
    private Client client;
}
