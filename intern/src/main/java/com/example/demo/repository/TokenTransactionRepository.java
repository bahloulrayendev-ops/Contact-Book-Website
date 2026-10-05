package com.example.demo.repository;

import com.example.demo.entity.TokenTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, Long> {
    Optional<TokenTransaction> findByIdempotencyKey(String idempotencyKey);
}