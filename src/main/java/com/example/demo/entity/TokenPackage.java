package com.example.demo.entity;

import com.example.demo.enume.Activestatus;
import com.example.demo.enume.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Table(name="tokenpackage")
@Entity
@Getter
@Setter
public class TokenPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenPackageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int tokens;

    @Enumerated(EnumType.STRING)
    @Column(name = "active", nullable = false)
    private Activestatus status;
}
