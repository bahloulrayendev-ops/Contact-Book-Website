package com.example.demo.security;

import com.example.demo.entity.Client;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Client client)) {
            throw new IllegalStateException("No authenticated client found in SecurityContext");
        }
        return client.getClientId();
    }
}
