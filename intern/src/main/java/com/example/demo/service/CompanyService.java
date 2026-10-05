package com.example.demo.service;

import com.example.demo.dto.CompanyDetailResponse;
import com.example.demo.dto.CompanyFilterOptionsResponse;
import com.example.demo.dto.CompanySummaryResponse;
import com.example.demo.entity.Company;
import com.example.demo.entity.Country;
import com.example.demo.enume.CompanyType;
import com.example.demo.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

import static com.example.demo.specification.CompanySpecification.search;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    //company Hub
    public Page<CompanySummaryResponse> searchCompanies(String keyword, String industry, String country,
                                                        CompanyType type, Pageable pageable) {
        return companyRepository
                .findAll(search(keyword, industry, country, type), pageable)
                .map(this::toSummary);
    }

    //company detail page
    public CompanyDetailResponse getCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found: " + id));
        return toDetail(company);
    }

    //les valeurs elli fl listes déroulante
    public CompanyFilterOptionsResponse getFilterOptions() {
        return new CompanyFilterOptionsResponse(
                companyRepository.findDistinctIndustries(),
                companyRepository.findDistinctCountries(),
                Arrays.asList(CompanyType.values())
        );
    }

    //map helpers
    private CompanySummaryResponse toSummary(Company c) {
        return new CompanySummaryResponse(
                c.getCompanyId(),
                c.getCompanyName(),
                c.getIndustry(),
                c.getCity(),
                c.getCountry(),
                c.getWebsite(),
                c.getCompanyType(),
                companyRepository.countEmployeesByCompanyId(c.getCompanyId())
        );
    }

    private CompanyDetailResponse toDetail(Company c) {
        return new CompanyDetailResponse(
                c.getCompanyId(),
                c.getCompanyName(),
                c.getDescription(),
                c.getIndustry(),
                c.getCity(),
                c.getCountry(),
                c.getWebsite(),
                c.getLinkedin(),
                c.getEstabYear(),
                c.getCompanyType(),
                companyRepository.countEmployeesByCompanyId(c.getCompanyId()),
                c.getOperatingCountries().stream()
                        .map(Country::getCountryName)
                        .sorted()
                        .toList()
        );
    }
}