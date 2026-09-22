package com.example.demo.entity;

import com.example.demo.enume.CompanyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="company")
@Entity
@Getter
@Setter
public class Company extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String industry;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String website;

    @Column(nullable = false)
    private String linkedin;

    @Enumerated(EnumType.STRING)
    @Column(name = "CompanyType", nullable = false)
    private CompanyType companyType;

    @Column(name = "lastVerified", nullable = false)
    private LocalDateTime lastVerified;

}
