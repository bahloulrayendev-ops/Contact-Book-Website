package com.example.demo.controller;

import com.example.demo.dto.PeopleHubEmployeeResponse;
import com.example.demo.dto.EmployeeFilterOptionsResponse;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SearchHistoryService searchHistoryService;

    @GetMapping("/filters")
    public EmployeeFilterOptionsResponse getFilters() {
        return employeeService.getFilterOptions();
    }

    @GetMapping
    public Page<PeopleHubEmployeeResponse> search(
            @AuthenticationPrincipal Long clientId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("lastName").ascending());
        Page<PeopleHubEmployeeResponse> results = employeeService.searchEmployees(
            keyword, jobTitle, department, industry, country, pageable);
        searchHistoryService.recordPeopleSearch(clientId, keyword, jobTitle, department,
            industry, country, results.getTotalElements());
        return results;
    }
}
