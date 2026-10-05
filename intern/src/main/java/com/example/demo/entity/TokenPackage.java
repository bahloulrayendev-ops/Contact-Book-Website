package com.example.demo.entity;

import com.example.demo.enume.Activestatus;
import com.example.demo.enume.TransactionType;
import org.hibernate.annotations.JdbcTypeCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> features = new ArrayList<>();

    @Column(length = 40)
    private String badge;

    @Enumerated(EnumType.STRING)
    @Column(name = "active", nullable = false)
    private Activestatus status;
}
