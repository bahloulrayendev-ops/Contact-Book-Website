package com.example.demo.controller;

import com.example.demo.dto.UnlockHistoryResponse;
import com.example.demo.dto.SearchHistoryResponse;
import com.example.demo.service.HistoryService;
import com.example.demo.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;
    private final SearchHistoryService searchHistoryService;

    @GetMapping("/unlocks")
    public Page<UnlockHistoryResponse> getUnlocks(
            @AuthenticationPrincipal Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("unlockedAt").descending());
        return historyService.getUnlocks(clientId, pageable);
    }

    @GetMapping("/searches")
    public Page<SearchHistoryResponse> getSearches(
            @AuthenticationPrincipal Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("searchedAt").descending());
        return searchHistoryService.getSearches(clientId, pageable);
    }
}