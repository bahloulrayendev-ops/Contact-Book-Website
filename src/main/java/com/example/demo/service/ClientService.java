package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client getById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    // updating email needs verification(does it already exist?) and password needs to check current password before changing
    public Client updateProfile(Long clientId, Client updates) {
        Client existing = getById(clientId);
        existing.setFirstName(updates.getFirstName());
        existing.setLastName(updates.getLastName());
        existing.setPhone(updates.getPhone());
        return clientRepository.save(existing);
    }
}