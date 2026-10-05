package com.example.demo.service;

import com.example.demo.entity.ContactDetails;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.ContactDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactDetailsService {

    private final ContactDetailsRepository contactDetailsRepository;
    private final CompanyService companyService;
    private final EmployeeService employeeService;

    public ContactDetailsService(ContactDetailsRepository contactDetailsRepository,
                                  CompanyService companyService,
                                  EmployeeService employeeService) {
        this.contactDetailsRepository = contactDetailsRepository;
        this.companyService = companyService;
        this.employeeService = employeeService;
    }

    // employeeId is optional - null means this is a company-level contact,not tied to a specific person.
    public ContactDetails create(Long companyId, Long employeeId, ContactDetails contactDetails) {
        contactDetails.setCompany(companyService.getById(companyId));
        if (employeeId != null) {
            contactDetails.setEmployee(employeeService.getById(employeeId));
        }
        return contactDetailsRepository.save(contactDetails);
    }

    public ContactDetails update(Long cdId, ContactDetails updates) {
        ContactDetails existing = getById(cdId);
        existing.setContactType(updates.getContactType());
        existing.setValue(updates.getValue());
        existing.setTokenCost(updates.getTokenCost());
        existing.setLastVerified(updates.getLastVerified());
        return contactDetailsRepository.save(existing);
    }

    public ContactDetails getById(Long cdId) {
        return contactDetailsRepository.findById(cdId)
                .orElseThrow(() -> new NotFoundException("Contact detail not found"));
    }

    public List<ContactDetails> listByEmployee(Long employeeId) {
        return contactDetailsRepository.findByEmployee_EmployeeId(employeeId);
    }

    public List<ContactDetails> listCompanyLevelByCompany(Long companyId) {
        return contactDetailsRepository.findByCompany_CompanyIdAndEmployeeIsNull(companyId);
    }

    public void delete(Long cdId) {
        contactDetailsRepository.delete(getById(cdId));
    }
}
