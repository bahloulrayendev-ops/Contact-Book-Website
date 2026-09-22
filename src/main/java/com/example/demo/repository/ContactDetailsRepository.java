package com.example.demo.repository;

import com.example.demo.entity.ContactDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactDetailsRepository extends JpaRepository<ContactDetails, Long> {

    List<ContactDetails> findByEmployee_EmployeeId(Long employeeId);
    List<ContactDetails> findByCompany_CompanyIdAndEmployeeIsNull(Long companyId);
}
