package com.example.backend.service;

import com.example.backend.dto.ArticleProjection;
import com.example.backend.sqlserver2.repository.ArtRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExistenciasSearchTest {

    @Mock
    private ArtRepository artRepository;

    @InjectMocks
    private ExistenciasSearch existenciasSearch;

    @Test
    void searchExistencias_filtersByAfaOrAsuWhenCampoIsBlank() {
        ArticleProjection article = mock(ArticleProjection.class);

        when(artRepository.findByENTAndARTBLONotAndAFACODOrENTAndARTBLONotAndASUCOD(
                1, 0, "A1", 1, 0, "S1"))
            .thenReturn(List.of(article));

        List<ArticleProjection> result =
            existenciasSearch.searchExistencias(1, "", "A1", "S1");

        assertEquals(1, result.size());
        assertSame(article, result.get(0));

        verify(artRepository).findByENTAndARTBLONotAndAFACODOrENTAndARTBLONotAndASUCOD(
            1, 0, "A1", 1, 0, "S1");
    }

    @Test
    void searchExistencias_searchesByShortCampo() {
        ArticleProjection article = mock(ArticleProjection.class);

        when(artRepository.findByENTAndARTBLONotAndARTCODOrENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(
                1, 0, "ABC", 1, 0, "ABC", 1, 0, "ABC"))
            .thenReturn(new ArrayList<>(List.of(article)));

        List<ArticleProjection> result =
            existenciasSearch.searchExistencias(1, "ABC", null, null);

        assertEquals(1, result.size());

        verify(artRepository).findByENTAndARTBLONotAndARTCODOrENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(
            1, 0, "ABC", 1, 0, "ABC", 1, 0, "ABC");
    }

    @Test
    void searchExistencias_searchesByLongCampo() {
        ArticleProjection article = mock(ArticleProjection.class);

        String campo = "12345678901";

        when(artRepository.findByENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(
                1, 0, campo, 1, 0, campo))
            .thenReturn(new ArrayList<>(List.of(article)));

        List<ArticleProjection> result =
            existenciasSearch.searchExistencias(1, campo, null, null);

        assertEquals(1, result.size());

        verify(artRepository).findByENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(
            1, 0, campo, 1, 0, campo);
    }

    @Test
    void searchExistencias_filtersResultsByAfacodWhenCampoProvided() {
        ArticleProjection matching = mock(ArticleProjection.class);
        ArticleProjection nonMatching = mock(ArticleProjection.class);

        when(matching.getAFACOD()).thenReturn("A1");
        when(nonMatching.getAFACOD()).thenReturn("A2");

        when(artRepository.findByENTAndARTBLONotAndARTCODOrENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(
                1, 0, "ABC", 1, 0, "ABC", 1, 0, "ABC"))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<ArticleProjection> result =
            existenciasSearch.searchExistencias(1, "ABC", "A1", null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchExistencias_filtersResultsByAsucodWhenCampoProvided() {
        ArticleProjection matching = mock(ArticleProjection.class);
        ArticleProjection nonMatching = mock(ArticleProjection.class);

        when(matching.getASUCOD()).thenReturn("S1");
        when(nonMatching.getASUCOD()).thenReturn("S2");

        when(artRepository.findByENTAndARTBLONotAndARTCODOrENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(
                1, 0, "ABC", 1, 0, "ABC", 1, 0, "ABC"))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<ArticleProjection> result =
            existenciasSearch.searchExistencias(1, "ABC", null, "S1");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchExistencias_returnsEmptyWhenNoCriteriaMatch() {
        List<ArticleProjection> result =
            existenciasSearch.searchExistencias(1, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verifyNoInteractions(artRepository);
    }

    @Test
    void searchExistencias_returnsEmptyWhenCampoIsBlankWithoutFilters() {
        List<ArticleProjection> result =
            existenciasSearch.searchExistencias(1, "   ", null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verifyNoInteractions(artRepository);
    }
}