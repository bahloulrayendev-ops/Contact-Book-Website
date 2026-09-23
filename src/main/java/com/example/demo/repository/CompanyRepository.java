package com.example.demo.repository;

import com.example.demo.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByIndustry(String industry);

    List<Company> findByCountry(String country);

    List<Company> findByCompanyName(String name);

    List<Company> findByCompanyType(String type);
}
