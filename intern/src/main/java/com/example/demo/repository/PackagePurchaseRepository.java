package com.example.demo.repository;

import com.example.demo.entity.PackagePurchase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PackagePurchaseRepository extends JpaRepository<PackagePurchase, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PackagePurchase p where p.packagePurchaseId = :purchaseId")
    Optional<PackagePurchase> findByIdForUpdate(@Param("purchaseId") Long purchaseId);
}