package com.example.demo.repository;

import com.example.demo.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    @Query("select distinct c.industry from Company c order by c.industry")
    List<String> findDistinctIndustries();

    @Query("select distinct c.country from Company c order by c.country")
    List<String> findDistinctCountries();

    @Query("select count(e) from Employee e where e.company.companyId = :companyId")
    long countEmployeesByCompanyId(@Param("companyId") Long companyId);
}
