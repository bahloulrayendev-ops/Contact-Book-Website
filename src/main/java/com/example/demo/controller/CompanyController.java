package com.example.demo.controller;

import com.example.demo.entity.Company;
import com.example.demo.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")

public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @GetMapping("/all")
    public List<Company> getAll() {
        return companyService.getAll();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Company>> searchCompany(
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String companyType
    )
    {

        if (industry != null && !industry.trim().isEmpty()) {
            return ResponseEntity.ok(companyService.searchByIndustry(industry));
        }
        if (country != null && !country.trim().isEmpty()) {
            return ResponseEntity.ok(companyService.searchByCountry(country));
        }
        if (companyName != null && !companyName.trim().isEmpty()) {
            return ResponseEntity.ok(companyService.searchByCompanyName(companyName));
        }
        if (companyType != null && !companyType.trim().isEmpty()) {
            return ResponseEntity.ok(companyService.SearchBycompanyType(companyType));
        }
        return ResponseEntity.ok(companyService.getAll());
    }
}
