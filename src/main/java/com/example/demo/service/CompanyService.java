package com.example.demo.service;

import com.example.demo.entity.Company;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAll(){
        return companyRepository.findAll();
    }

    public Company getById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
    }

    public Page<Company> list(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    public List<Company> searchByIndustry(String industry) {
        return companyRepository.findByIndustry(industry);
    }
    public List<Company> searchByCountry(String country) {
        return companyRepository.findByCountry(country);
    }

    public List<Company> searchByCompanyName(String name) {
        return companyRepository.findByCompanyName(name);
    }

    public List<Company> SearchBycompanyType(String type) {
        return companyRepository.findByCompanyType(type);
    }
}