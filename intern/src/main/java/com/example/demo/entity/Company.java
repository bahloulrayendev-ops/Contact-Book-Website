package com.example.demo.entity;

import com.example.demo.enume.CompanyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Table(name = "company")
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
    private String description;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private int estabYear;

    @Column(nullable = false)
    private String website;

    @Column(nullable = false)
    private String linkedin;

    @Enumerated(EnumType.STRING)
    @Column(name = "CompanyType", nullable = false)
    private CompanyType companyType;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private Set<Employee> employees = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "company_operating_country",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "country_id")
    )
    private Set<Country> operatingCountries = new HashSet<>();

    @Transient
    public int getEmployeeCount() {
        return employees != null ? employees.size() : 0;
    }
}
