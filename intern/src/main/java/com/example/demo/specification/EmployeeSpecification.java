package com.example.demo.specification;

import com.example.demo.entity.Company;
import com.example.demo.entity.Employee;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class EmployeeSpecification {

    private EmployeeSpecification() {
    }

    public static Specification<Employee> search(String keyword, String jobTitle, String department,
                                                   String industry, String country) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Employee, Company> company = root.join("company", JoinType.LEFT);

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("jobTitle")), pattern)
                ));
            }

            if (jobTitle != null && !jobTitle.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("jobTitle")), jobTitle.trim().toLowerCase()));
            }

            if (department != null && !department.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase()));
            }

            if (industry != null && !industry.isBlank()) {
                predicates.add(cb.equal(cb.lower(company.get("industry")), industry.trim().toLowerCase()));
            }

            if (country != null && !country.isBlank()) {
                predicates.add(cb.equal(cb.lower(company.get("country")), country.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
