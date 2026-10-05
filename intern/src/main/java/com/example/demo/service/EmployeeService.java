package com.example.demo.service;

import com.example.demo.security.SecurityUtils;
import com.example.demo.dto.CompanyEmployeeResponse;
import com.example.demo.dto.EmployeeFilterOptionsResponse;
import com.example.demo.dto.CompanyRefResponse;
import com.example.demo.dto.ContactDetailResponse;
import com.example.demo.dto.PeopleHubEmployeeResponse;
import com.example.demo.entity.ContactDetails;
import com.example.demo.entity.Employee;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.UnlockRepository;
import com.example.demo.security.CurrentUser;
import com.example.demo.specification.EmployeeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final UnlockRepository unlockRepository;

        public EmployeeFilterOptionsResponse getFilterOptions() {
                return new EmployeeFilterOptionsResponse(
                                employeeRepository.findDistinctJobTitles(),
                                employeeRepository.findDistinctDepartments(),
                                companyRepository.findDistinctIndustries(),
                                companyRepository.findDistinctCountries());
        }

    public List<CompanyEmployeeResponse> getEmployeesForCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found: " + companyId);
        }

        List<Employee> employees = employeeRepository.findWithContactDetailsByCompanyId(companyId);
        Set<Long> unlockedCdIds = unlockedCdIdsFor(employees);

        return employees.stream()
            .map(employee -> {
                boolean nameRevealed = hasUnlockedContact(employee, unlockedCdIds);
                return new CompanyEmployeeResponse(
                    employee.getEmployeeId(),
                    nameRevealed ? employee.getFirstName() : "Anonymous",
                    nameRevealed ? employee.getLastName() : "Contact",
                    employee.getJobTitle(),
                    employee.getDepartment(),
                    toContactList(employee, unlockedCdIds));
            })
                .toList();
    }

    //people Hub: search across every company
    public Page<PeopleHubEmployeeResponse> searchEmployees(String keyword, String jobTitle, String department,
                                                            String industry, String country, Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(
                EmployeeSpecification.search(keyword, jobTitle, department, industry, country), pageable);

        Set<Long> unlockedCdIds = unlockedCdIdsFor(page.getContent());

        return page.map(employee -> {
            boolean nameRevealed = hasUnlockedContact(employee, unlockedCdIds);
            return new PeopleHubEmployeeResponse(
                employee.getEmployeeId(),
                nameRevealed ? employee.getFirstName() : "Anonymous",
                nameRevealed ? employee.getLastName() : "Contact",
                employee.getJobTitle(),
                employee.getDepartment(),
                new CompanyRefResponse(
                    employee.getCompany().getCompanyId(), employee.getCompany().getCompanyName(),
                    employee.getCompany().getCountry(), employee.getCompany().getIndustry()),
                toContactList(employee, unlockedCdIds));
        });
    }

    private Set<Long> unlockedCdIdsFor(List<Employee> employees) {
        Long clientId = SecurityUtils.getCurrentClientId();
        List<Long> cdIds = employees.stream()
            .filter(employee -> employee.getContactDetails() != null)
                .flatMap(e -> e.getContactDetails().stream())
                .map(ContactDetails::getCdId)
                .toList();

        if (cdIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(unlockRepository.findUnlockedCdIds(clientId, cdIds));
    }

    private List<ContactDetailResponse> toContactList(Employee e, Set<Long> unlockedCdIds) {
        if (e.getContactDetails() == null) {
            return List.of();
        }
        return e.getContactDetails().stream()
                .map(cd -> {
                    boolean locked = !unlockedCdIds.contains(cd.getCdId());
                    return new ContactDetailResponse(
                            cd.getCdId(),
                            cd.getContactType(),
                            cd.getTokenCost(),
                            cd.getLastVerified(),
                            locked,
                            locked ? null : cd.getValue()); //Security contre el leak
                })
                .toList();
    }

    private boolean hasUnlockedContact(Employee employee, Set<Long> unlockedCdIds) {
        return employee.getContactDetails() != null && employee.getContactDetails().stream()
                .anyMatch(contact -> unlockedCdIds.contains(contact.getCdId()));
    }
}
