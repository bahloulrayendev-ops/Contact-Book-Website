package com.example.demo.service;

import com.example.demo.entity.TokenPackage;
import com.example.demo.enume.Activestatus;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.TokenPackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenPackageService {

    private final TokenPackageRepository tokenPackageRepository;

    public TokenPackageService(TokenPackageRepository tokenPackageRepository) {
        this.tokenPackageRepository = tokenPackageRepository;
    }

    // Assumes Activestatus has an ACTIVE constant - adjust if yours is named
    // differently.
    public List<TokenPackage> listActive() {
        return tokenPackageRepository.findByStatus(Activestatus.active);
    }

    public TokenPackage create(TokenPackage tokenPackage) {
        return tokenPackageRepository.save(tokenPackage);
    }

    public TokenPackage update(Long packageId, TokenPackage updates) {
        TokenPackage existing = getById(packageId);
        existing.setName(updates.getName());
        existing.setPrice(updates.getPrice());
        existing.setTokens(updates.getTokens());
        existing.setStatus(updates.getStatus());
        return tokenPackageRepository.save(existing);
    }

    public TokenPackage getById(Long packageId) {
        return tokenPackageRepository.findById(packageId)
                .orElseThrow(() -> new NotFoundException("Package not found"));
    }
}
