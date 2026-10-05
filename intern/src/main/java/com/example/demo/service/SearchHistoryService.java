package com.example.demo.service;

import com.example.demo.dto.SearchHistoryResponse;
import com.example.demo.entity.SearchHistory;
import com.example.demo.enume.SearchType;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public void recordCompanySearch(Long clientId, String keyword, String industry, String country,
                                   String companyType, long resultCount) {
        List<String> filters = new ArrayList<>();
        addFilter(filters, "Industry", industry);
        addFilter(filters, "Country", country);
        addFilter(filters, "Type", companyType);
        record(clientId, SearchType.company, keyword, "All companies", filters, resultCount);
    }

    @Transactional
    public void recordPeopleSearch(Long clientId, String keyword, String jobTitle, String department,
                                  String industry, String country, long resultCount) {
        List<String> filters = new ArrayList<>();
        addFilter(filters, "Job title", jobTitle);
        addFilter(filters, "Department", department);
        addFilter(filters, "Industry", industry);
        addFilter(filters, "Country", country);
        record(clientId, SearchType.people, keyword, "All people", filters, resultCount);
    }

    @Transactional(readOnly = true)
    public Page<SearchHistoryResponse> getSearches(Long clientId, Pageable pageable) {
        return searchHistoryRepository.findByClientClientIdOrderBySearchedAtDesc(clientId, pageable)
                .map(this::toResponse);
    }

    private void record(Long clientId, SearchType type, String keyword, String defaultQuery,
                        List<String> filters, long resultCount) {
        SearchHistory history = new SearchHistory();
        history.setClient(clientRepository.getReferenceById(clientId));
        history.setSearchType(type);
        history.setQuery(keyword == null || keyword.isBlank() ? defaultQuery : keyword.trim());
        history.setFilters(filters.isEmpty() ? "No filters" : String.join(" · ", filters));
        history.setResultCount(resultCount);
        history.setSearchedAt(LocalDateTime.now());
        searchHistoryRepository.save(history);
    }

    private void addFilter(List<String> filters, String label, String value) {
        if (value != null && !value.isBlank()) {
            filters.add(label + ": " + value.trim());
        }
    }

    private SearchHistoryResponse toResponse(SearchHistory history) {
        return new SearchHistoryResponse(
                history.getSearchId().toString(),
                history.getSearchType(),
                history.getQuery(),
                history.getFilters(),
                history.getResultCount(),
                history.getSearchedAt());
    }
}