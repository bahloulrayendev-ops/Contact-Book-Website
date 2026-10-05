package com.example.demo.service;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.entity.Client;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServicePasswordTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    @Test
    void verifiesCurrentPasswordAndSavesEncodedNewPassword() {
        Long clientId = 12L;
        Client client = new Client();
        client.setClientId(clientId);
        client.setPwd("old-hash");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("current-pass", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-pass-123")).thenReturn("new-hash");
        when(clientRepository.save(client)).thenReturn(client);

        authService.changePassword(clientId, new ChangePasswordRequest("current-pass", "new-pass-123"));

        assertEquals("new-hash", client.getPwd());
        verify(clientRepository).save(client);
    }

    @Test
    void rejectsIncorrectCurrentPasswordWithoutSaving() {
        Long clientId = 12L;
        Client client = new Client();
        client.setPwd("old-hash");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("wrong-pass", "old-hash")).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> authService.changePassword(clientId, new ChangePasswordRequest("wrong-pass", "new-pass-123")));

        verify(clientRepository, never()).save(client);
    }
}