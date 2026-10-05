package com.example.demo.controller;

import com.example.demo.dto.UnlockResponse;
import com.example.demo.service.UnlockService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unlock")
public class UnlockController {

    private final UnlockService unlockService;

    public UnlockController(UnlockService unlockService) {
        this.unlockService = unlockService;
    }

    @PostMapping("/{contactDetailId}")
    public UnlockResponse unlock(@AuthenticationPrincipal Long clientId,
                                 @PathVariable Long contactDetailId) {
        return unlockService.unlock(clientId, contactDetailId);
    }
}