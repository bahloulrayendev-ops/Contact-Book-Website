package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.Client;
import com.example.demo.entity.Wallet;
import com.example.demo.enume.AccountStatus;
import com.example.demo.exception.BadCredentialsException;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final ClientRepository clientRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(ClientRepository clientRepository,
                       WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.clientRepository = clientRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        Client client = new Client();
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setPhone(request.phone());
        client.setEmail(request.email());
        client.setPwd(passwordEncoder.encode(request.password()));
        client.setStatusType(AccountStatus.active);

        Client savedClient = clientRepository.save(client);
        Wallet wallet = new Wallet();
        wallet.setClient(savedClient);
        wallet.setBalance(0); // Initial welcome tokens 
        walletRepository.save(wallet);

        String token = jwtService.generateToken(savedClient.getClientId());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        Client client = clientRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), client.getPwd())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        client.setLastLogAt(LocalDateTime.now());
        clientRepository.save(client);

        String token = jwtService.generateToken(client.getClientId());
        return new AuthResponse(token);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return toUserResponse(client);
    }

    @Transactional
    public UserResponse updateCurrentUser(Long clientId, UpdateProfileRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        clientRepository.findByEmail(request.email()).ifPresent(existing -> {
            if (!existing.getClientId().equals(clientId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
            }
        });

        String[] nameParts = request.fullName().trim().split("\\s+", 2);
        client.setFirstName(nameParts[0]);
        client.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        client.setEmail(request.email());
        client.setPhone(request.phone() == null ? "" : request.phone());

        return toUserResponse(clientRepository.save(client));
    }

    @Transactional
    public void changePassword(Long clientId, ChangePasswordRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!passwordEncoder.matches(request.currentPassword(), client.getPwd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        client.setPwd(passwordEncoder.encode(request.newPassword()));
        clientRepository.save(client);
    }

    private UserResponse toUserResponse(Client client) {
        return new UserResponse(client.getClientId().toString(),
                client.getFirstName() + " " + client.getLastName(),
                client.getEmail(), client.getPhone());
    }
}