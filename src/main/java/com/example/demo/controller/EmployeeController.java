package com.example.demo.controller;

import com.example.demo.entity.Employee;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")

public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/all")
    public List<Employee> getAll() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployee(
            @RequestParam(required = false) String first,
            @RequestParam(required = false) String last,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String company)
    {
        if (first != null && !first.trim().isEmpty()) {
            return ResponseEntity.ok(employeeService.searchByFirstName(first));
        }
        if (last != null && !last.trim().isEmpty()) {
            return ResponseEntity.ok(employeeService.searchByLastName(last));
        }
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            return ResponseEntity.ok(employeeService.searchByJobTitle(jobTitle));
        }
        if (company != null && !company.trim().isEmpty()) {
            return ResponseEntity.ok(employeeService.searchEmployeesByCompanyName(company));
        }
        if (department != null && !department.trim().isEmpty()) {
            return ResponseEntity.ok(employeeService.searchByDepartment(department));
        }
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
}