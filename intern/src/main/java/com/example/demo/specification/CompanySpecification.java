package com.example.demo.specification;

import com.example.demo.entity.Company;
import com.example.demo.enume.CompanyType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class CompanySpecification {

    private CompanySpecification() {
    }

    public static Specification<Company> search(String keyword, String industry, String country, CompanyType type) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>(); 

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("companyName")), pattern),
                        cb.like(cb.lower(root.get("industry")), pattern)
                ));
            }

            if (industry != null && !industry.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("industry")), industry.trim().toLowerCase()));
            }

            if (country != null && !country.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("country")), country.trim().toLowerCase()));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("companyType"), type));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
