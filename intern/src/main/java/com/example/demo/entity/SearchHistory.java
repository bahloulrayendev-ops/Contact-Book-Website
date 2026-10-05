package com.example.demo.entity;

import com.example.demo.enume.SearchType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "searchhistory", indexes = {
        @Index(name = "idx_searchhistory_client_searched_at", columnList = "client_id, searched_at")
})
@Getter
@Setter
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Long searchId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "search_type", nullable = false, length = 16)
    private SearchType searchType;

    @Column(nullable = false, length = 500)
    private String query;

    @Column(nullable = false, columnDefinition = "text")
    private String filters;

    @Column(name = "result_count", nullable = false)
    private long resultCount;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;
}