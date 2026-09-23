package com.example.demo.service;

import com.example.demo.entity.Company;
import com.example.demo.entity.Employee;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    public List<Employee> searchByFirstName(String firstName) {
        return employeeRepository.findByFirstName(firstName);
    }

    public List<Employee> searchByLastName(String LastName) {
        return employeeRepository.findByLastName(LastName);
    }

    public List<Employee> searchByJobTitle(String jobTitle) {
        return employeeRepository.findByJobTitle(jobTitle);
    }

    public List<Employee> searchEmployeesByCompanyName(String companyName) {
        return employeeRepository.findByCompany_CompanyName(companyName);
    }

    public List<Employee> searchByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }
}
