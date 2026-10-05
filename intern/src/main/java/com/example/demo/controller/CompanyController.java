package com.example.demo.controller;

import com.example.demo.dto.CompanyDetailResponse;
import com.example.demo.dto.CompanyEmployeeResponse;
import com.example.demo.dto.CompanyFilterOptionsResponse;
import com.example.demo.dto.CompanySummaryResponse;
import com.example.demo.enume.CompanyType;
import com.example.demo.service.CompanyService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final EmployeeService employeeService;
    private final SearchHistoryService searchHistoryService;

    @GetMapping
    public Page<CompanySummaryResponse> search(
            @AuthenticationPrincipal Long clientId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) CompanyType companyType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("companyName").ascending());
        Page<CompanySummaryResponse> results = companyService.searchCompanies(
            keyword, industry, country, companyType, pageable);
        searchHistoryService.recordCompanySearch(clientId, keyword, industry, country,
            companyType == null ? null : companyType.name(), results.getTotalElements());
        return results;
    }

    @GetMapping("/{id}")
    public CompanyDetailResponse getById(@PathVariable Long id) {
        return companyService.getCompany(id);
    }

    @GetMapping("/{id}/employees")
    public List<CompanyEmployeeResponse> getEmployees(@PathVariable Long id) {
        return employeeService.getEmployeesForCompany(id);
    }

    @GetMapping("/filters")
    public CompanyFilterOptionsResponse getFilters() {
        return companyService.getFilterOptions();
    }
}