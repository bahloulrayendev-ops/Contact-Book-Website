package com.example.demo.repository;

import com.example.demo.entity.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    Page<SearchHistory> findByClientClientIdOrderBySearchedAtDesc(Long clientId, Pageable pageable);
}