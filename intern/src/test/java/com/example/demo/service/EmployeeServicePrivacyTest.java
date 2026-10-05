package com.example.demo.service;

import com.example.demo.entity.Company;
import com.example.demo.entity.ContactDetails;
import com.example.demo.entity.Employee;
import com.example.demo.enume.ContactType;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.UnlockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServicePrivacyTest {

    private static final Long CLIENT_ID = 22L;
    private static final Long COMPANY_ID = 31L;
    private static final Long EMPLOYEE_ID = 41L;
    private static final Long CONTACT_ID = 51L;

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UnlockRepository unlockRepository;
    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void authenticateClient() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CLIENT_ID, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hidesNameInCompanyEmployeeListUntilClientUnlocksContact() {
        Employee employee = employeeWithContact();
        when(companyRepository.existsById(COMPANY_ID)).thenReturn(true);
        when(employeeRepository.findWithContactDetailsByCompanyId(COMPANY_ID)).thenReturn(List.of(employee));
        when(unlockRepository.findUnlockedCdIds(CLIENT_ID, List.of(CONTACT_ID))).thenReturn(List.of());

        var result = employeeService.getEmployeesForCompany(COMPANY_ID).get(0);

        assertEquals("Anonymous", result.firstName());
        assertEquals("Contact", result.lastName());
        assertEquals(true, result.contactDetails().get(0).locked());
    }

    @Test
    void revealsNameInPeopleSearchAfterClientUnlocksAnyContact() {
        Employee employee = employeeWithContact();
        when(employeeRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(employee)));
        when(unlockRepository.findUnlockedCdIds(CLIENT_ID, List.of(CONTACT_ID))).thenReturn(List.of(CONTACT_ID));

        var result = employeeService.searchEmployees(
                null, null, null, null, null,
                PageRequest.of(0, 10, Sort.by("lastName").ascending())).getContent().get(0);

        assertEquals("Nadia", result.firstName());
        assertEquals("Sample", result.lastName());
        assertEquals(false, result.contactDetails().get(0).locked());
    }

    private Employee employeeWithContact() {
        Company company = new Company();
        company.setCompanyId(COMPANY_ID);
        company.setCompanyName("Example Company");
        company.setCountry("Tunisia");
        company.setIndustry("Machinery");

        ContactDetails contact = new ContactDetails();
        contact.setCdId(CONTACT_ID);
        contact.setContactType(ContactType.email);
        contact.setValue("nadia@example.invalid");
        contact.setTokenCost(5);

        Employee employee = new Employee();
        employee.setEmployeeId(EMPLOYEE_ID);
        employee.setFirstName("Nadia");
        employee.setLastName("Sample");
        employee.setJobTitle("Sales Director");
        employee.setDepartment("Sales");
        employee.setCompany(company);
        employee.setContactDetails(List.of(contact));
        return employee;
    }
}