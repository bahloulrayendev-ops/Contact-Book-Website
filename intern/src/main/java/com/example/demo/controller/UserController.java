package com.example.demo.controller;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal Long clientId) {
        return authService.getCurrentUser(clientId);
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(@AuthenticationPrincipal Long clientId,
                                         @Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateCurrentUser(clientId, request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Long clientId,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(clientId, request);
        return ResponseEntity.noContent().build();
    }
}