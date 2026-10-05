package com.example.demo.repository;

import com.example.demo.entity.TransactionHistory;
import com.example.demo.enume.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

	Page<TransactionHistory> findByWalletClientClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);

	@Query("select coalesce(sum(h.amount), 0) from TransactionHistory h " +
			"where h.wallet.client.clientId = :clientId and h.transactionType = :type")
	long sumAmountByClientAndType(@Param("clientId") Long clientId, @Param("type") TransactionType type);
}