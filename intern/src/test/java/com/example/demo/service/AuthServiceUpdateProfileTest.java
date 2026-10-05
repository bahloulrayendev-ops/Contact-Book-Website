package com.example.demo.service;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.entity.Client;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUpdateProfileTest {

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
    void updateProfileSavesChangedFieldsForAuthenticatedClient() {
        Long clientId = 91L;
        Client existing = new Client();
        existing.setClientId(clientId);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setEmail("old@example.com");
        existing.setPhone("+216 00 000 000");
        existing.setPwd("hashed-password");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existing));
        when(clientRepository.findByEmail("new@example.com")).thenReturn(Optional.of(existing));
        when(clientRepository.save(existing)).thenReturn(existing);

        var response = authService.updateCurrentUser(clientId,
            new UpdateProfileRequest("New Name", "new@example.com", "+216 20 123 456"));

        ArgumentCaptor<Client> savedClient = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(savedClient.capture());
        assertEquals("New", savedClient.getValue().getFirstName());
        assertEquals("Name", savedClient.getValue().getLastName());
        assertEquals("new@example.com", savedClient.getValue().getEmail());
        assertEquals("+216 20 123 456", savedClient.getValue().getPhone());
        assertEquals("New Name", response.fullName());
        assertEquals("new@example.com", response.email());
        assertEquals("+216 20 123 456", response.phone());
    }
}
