package com.example.demo.repository;

import com.example.demo.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    @Query("select distinct e.jobTitle from Employee e where e.jobTitle is not null order by e.jobTitle")
    List<String> findDistinctJobTitles();

    @Query("select distinct e.department from Employee e where e.department is not null order by e.department")
    List<String> findDistinctDepartments();

    @Query("select distinct e from Employee e " +
           "left join fetch e.contactDetails " +
           "where e.company.companyId = :companyId " +
           "order by e.lastName, e.firstName")
    List<Employee> findWithContactDetailsByCompanyId(@Param("companyId") Long companyId);
}
