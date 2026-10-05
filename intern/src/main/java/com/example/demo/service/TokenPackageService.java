package com.example.demo.service;

import com.example.demo.dto.TokenPackageResponse;
import com.example.demo.enume.Activestatus;
import com.example.demo.repository.TokenPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TokenPackageService {

    private final TokenPackageRepository tokenPackageRepository;

    public TokenPackageService(TokenPackageRepository tokenPackageRepository) {
        this.tokenPackageRepository = tokenPackageRepository;
    }

    @Transactional(readOnly = true)
    public List<TokenPackageResponse> getActivePackages() {
        return tokenPackageRepository.findByStatusOrderByPriceAsc(Activestatus.active).stream()
                .map(tokenPackage -> new TokenPackageResponse(
                        tokenPackage.getTokenPackageId(), tokenPackage.getName(),
                    tokenPackage.getPrice(), tokenPackage.getTokens(),
                    tokenPackage.getFeatures(), tokenPackage.getBadge()))
                .toList();
    }
}