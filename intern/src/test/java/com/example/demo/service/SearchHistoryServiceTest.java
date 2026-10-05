package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.SearchHistory;
import com.example.demo.enume.SearchType;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.SearchHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;
    @Mock
    private ClientRepository clientRepository;
    @InjectMocks
    private SearchHistoryService searchHistoryService;

    @Test
    void recordsCompanySearchForAuthenticatedClient() {
        Long clientId = 42L;
        Client client = new Client();
        when(clientRepository.getReferenceById(clientId)).thenReturn(client);

        searchHistoryService.recordCompanySearch(
                clientId, "Atlas", "Machinery", "Tunisia", "Exporter", 7);

        ArgumentCaptor<SearchHistory> historyCaptor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryRepository).save(historyCaptor.capture());
        SearchHistory history = historyCaptor.getValue();
        assertEquals(client, history.getClient());
        assertEquals(SearchType.company, history.getSearchType());
        assertEquals("Atlas", history.getQuery());
        assertEquals("Industry: Machinery · Country: Tunisia · Type: Exporter", history.getFilters());
        assertEquals(7, history.getResultCount());
        assertNotNull(history.getSearchedAt());
    }

    @Test
    void recordsUnfilteredPeopleSearchWithDefaultQuery() {
        Long clientId = 7L;
        when(clientRepository.getReferenceById(clientId)).thenReturn(new Client());

        searchHistoryService.recordPeopleSearch(clientId, " ", null, null, null, null, 0);

        ArgumentCaptor<SearchHistory> historyCaptor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryRepository).save(historyCaptor.capture());
        SearchHistory history = historyCaptor.getValue();
        assertEquals(SearchType.people, history.getSearchType());
        assertEquals("All people", history.getQuery());
        assertEquals("No filters", history.getFilters());
        assertEquals(0, history.getResultCount());
    }
}