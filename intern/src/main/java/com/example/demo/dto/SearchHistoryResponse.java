package com.example.demo.dto;

import com.example.demo.enume.SearchType;

import java.time.LocalDateTime;

public record SearchHistoryResponse(
        String id,
        SearchType type,
        String query,
        String filters,
        long resultCount,
        LocalDateTime searchedAt
) {
}