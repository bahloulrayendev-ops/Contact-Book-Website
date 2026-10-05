package com.example.demo.controller;

import com.example.demo.dto.TokenPackageResponse;
import com.example.demo.service.TokenPackageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tokens/packages")
public class TokenPackageController {

    private final TokenPackageService tokenPackageService;

    public TokenPackageController(TokenPackageService tokenPackageService) {
        this.tokenPackageService = tokenPackageService;
    }

    @GetMapping
    public List<TokenPackageResponse> getActivePackages() {
        return tokenPackageService.getActivePackages();
    }
}