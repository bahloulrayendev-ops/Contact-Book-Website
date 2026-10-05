package com.example.demo.entity;


import com.example.demo.enume.ContactType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="contactdetails")
@Entity
@Setter
@Getter
public class ContactDetails {
    @Id
    @GeneratedValue
    private Long cdId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContactType contactType;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private int tokenCost;

    @Column(nullable = false)
    private LocalDateTime lastVerified;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @JoinColumn(name="companyId", referencedColumnName = "companyId")
    @JsonIgnore
    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    private Company company;

    @JoinColumn(name="employeeId")
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    private Employee employee;
}
