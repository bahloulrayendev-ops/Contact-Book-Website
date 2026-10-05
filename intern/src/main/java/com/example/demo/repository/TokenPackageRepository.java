package com.example.demo.repository;

import com.example.demo.entity.TokenPackage;
import com.example.demo.enume.Activestatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenPackageRepository extends JpaRepository<TokenPackage, Long> {
    List<TokenPackage> findByStatusOrderByPriceAsc(Activestatus status);
}