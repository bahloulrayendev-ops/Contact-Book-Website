package com.example.demo.service;

import com.example.demo.dto.UnlockHistoryResponse;
import com.example.demo.entity.Company;
import com.example.demo.entity.ContactDetails;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Unlock;
import com.example.demo.repository.UnlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final UnlockRepository unlockRepository;

    @Transactional(readOnly = true)
    public Page<UnlockHistoryResponse> getUnlocks(Long clientId, Pageable pageable) {
        return unlockRepository.findByClientClientIdOrderByUnlockedAtDesc(clientId, pageable)
                .map(this::toResponse);
    }

    private UnlockHistoryResponse toResponse(Unlock unlock) {
        ContactDetails contact = unlock.getContactDetail();
        Employee employee = contact.getEmployee();
        Company company = employee != null ? employee.getCompany() : contact.getCompany();
        String employeeName = employee == null ? "Company contact" :
                employee.getFirstName() + " " + employee.getLastName();

        return new UnlockHistoryResponse(
                unlock.getId().getUserId() + "-" + unlock.getId().getCdId(),
                employeeName,
                company == null ? "" : company.getCompanyName(),
                contact.getContactType(),
                contact.getValue(),
                unlock.getTokenSpent(),
                unlock.getUnlockedAt());
    }
}