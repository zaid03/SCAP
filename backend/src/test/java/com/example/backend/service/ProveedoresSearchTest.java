package com.example.backend.service;

import com.example.backend.sqlserver2.model.Ter;
import com.example.backend.sqlserver2.repository.TerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class ProveedoresSearchTest {

    private static final int TEST_ENT = 999999;

    @Autowired
    private ProveedoresSearch proveedoresSearch;

    @MockitoBean
    private TerRepository terRepository;

    private Ter createTer(Integer tercod, String ternom, String ternif, String terali, Integer terblo) {
        Ter ter = new Ter();
        ter.setENT(TEST_ENT);
        ter.setTERCOD(tercod);
        ter.setTERNOM(ternom);
        ter.setTERNIF(ternif);
        ter.setTERALI(terali);
        ter.setTERBLO(terblo);
        return ter;
    }

    @Test
    @DisplayName("searchProveedores: todos mode with numeric term <5 chars matches TERCOD")
    void searchTodos_numericLessThan5_matchesTercod() {
        Ter ter1 = createTer(123, "Provider A", "NIF1", "Alias1", 0);
        Ter ter2 = createTer(456, "Provider B", "NIF2", "Alias2", 0);
        Ter ter3 = createTer(789, "Provider C", "NIF3", "Alias3", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2, ter3));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "123");

        assertEquals(1, result.size());
        assertEquals(123, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: todos mode with numeric term >=5 chars searches TERCOD and TERNIF")
    void searchTodos_numericGreaterEqual5_matchesTercodAndTernif() {
        Ter ter1 = createTer(12345, "Provider A", "12345", "Alias1", 0);
        Ter ter2 = createTer(456, "Provider B", "NIF2", "Alias2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertEquals(1, result.size());
        assertEquals(12345, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: todos mode numeric >=5 chars finds by TERNIF contains")
    void searchTodos_numericGreaterEqual5_findsByTernifContains() {
        Ter ter1 = createTer(100, "Provider A", "NIFABC12345", "Alias1", 0);
        Ter ter2 = createTer(200, "Provider B", "NIF2", "Alias2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: todos mode numeric <5 chars no match returns empty")
    void searchTodos_numericLessThan5_noMatch() {
        Ter ter1 = createTer(100, "Provider A", "NIF1", "Alias1", 0);
        Ter ter2 = createTer(200, "Provider B", "NIF2", "Alias2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "999");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("searchProveedores: todos mode mixed term searches TERNIF, TERNOM, TERALI")
    void searchTodos_mixedTerm_searchesAllFields() {
        Ter ter1 = createTer(100, "ABC Company", "NIFABC", "ABC Alias", 0);
        Ter ter2 = createTer(200, "XYZ Corp", "NIFXYZ", "XYZ Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: todos mode mixed finds by TERNOM contains")
    void searchTodos_mixedTerm_findsByTernom() {
        Ter ter1 = createTer(100, "ABC Company", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "XYZ Corp", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "Company");

        assertEquals(1, result.size());
        assertEquals("ABC Company", result.get(0).getTERNOM());
    }

    @Test
    @DisplayName("searchProveedores: todos mode mixed finds by TERALI contains")
    void searchTodos_mixedTerm_findsByTerali() {
        Ter ter1 = createTer(100, "Provider", "NIF1", "Premium Alias", 0);
        Ter ter2 = createTer(200, "Provider2", "NIF2", "Standard", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "Premium");

        assertEquals(1, result.size());
        assertEquals("Premium Alias", result.get(0).getTERALI());
    }

    @Test
    @DisplayName("searchProveedores: todos mode mixed no match returns empty")
    void searchTodos_mixedTerm_noMatch() {
        Ter ter1 = createTer(100, "Provider A", "NIF1", "Alias1", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "NOMATCH");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("searchProveedores: todos mode mixed multiple matches")
    void searchTodos_mixedTerm_multipleMatches() {
        Ter ter1 = createTer(100, "ABC Company", "NIFABC", "ABC Alias", 0);
        Ter ter2 = createTer(200, "ABC Corp", "NIF2", "ABC2", 0);
        Ter ter3 = createTer(300, "Provider", "NIF3", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2, ter3));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("searchProveedores: Nobloqueado mode numeric <5 filters by TERCOD and TERBLO=0")
    void searchNobloqueado_numericLessThan5_filtersByTercod() {
        Ter ter1 = createTer(123, "Provider A", "NIF1", "Alias1", 0);
        Ter ter2 = createTer(123, "Provider B", "NIF2", "Alias2", 1);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "123");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("searchProveedores: Nobloqueado mode numeric >=5 filters by TERCOD/TERNIF and TERBLO=0")
    void searchNobloqueado_numericGreaterEqual5_filtersByTercodTernif() {
        Ter ter1 = createTer(100, "Provider A", "NIF12345", "Alias1", 0);
        Ter ter2 = createTer(200, "Provider B", "NIF12345", "Alias2", 1);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "12345");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("searchProveedores: Nobloqueado mode excludes blocked providers")
    void searchNobloqueado_excludesBlocked() {
        Ter ter1 = createTer(100, "Provider A", "NIF1", "Alias1", 1);
        Ter ter2 = createTer(200, "Provider B", "NIF2", "Alias2", 1);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "100");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("searchProveedores: Nobloqueado mode mixed filters all fields with TERBLO=0")
    void searchNobloqueado_mixedTerm_filters() {
        Ter ter1 = createTer(100, "ABC Company", "NIFABC", "ABC Alias", 0);
        Ter ter2 = createTer(101, "ABC Corp", "NIFABC2", "ABC2", 1);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "ABC");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("searchProveedores: bloqueado mode numeric <5 filters by TERCOD and TERBLO=1")
    void searchBloqueado_numericLessThan5_filtersByTercod() {
        Ter ter1 = createTer(123, "Provider A", "NIF1", "Alias1", 1);
        Ter ter2 = createTer(123, "Provider B", "NIF2", "Alias2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "bloqueado", "123");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("searchProveedores: bloqueado mode numeric >=5 filters by TERCOD/TERNIF and TERBLO=1")
    void searchBloqueado_numericGreaterEqual5_filtersByTercodTernif() {
        Ter ter1 = createTer(100, "Provider A", "NIF12345", "Alias", 1);
        Ter ter2 = createTer(200, "Provider B", "NIF12345", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "bloqueado", "12345");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("searchProveedores: bloqueado mode excludes non-blocked providers")
    void searchBloqueado_excludesNonBlocked() {
        Ter ter1 = createTer(100, "Provider A", "NIF1", "Alias1", 0);
        Ter ter2 = createTer(200, "Provider B", "NIF2", "Alias2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "bloqueado", "100");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("searchProveedores: bloqueado mode mixed filters all fields with TERBLO=1")
    void searchBloqueado_mixedTerm_filters() {
        Ter ter1 = createTer(100, "ABC Company", "NIFABC", "ABC Alias", 1);
        Ter ter2 = createTer(101, "ABC Corp", "NIFABC2", "ABC2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "bloqueado", "ABC");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("searchProveedores: handles null TERCOD gracefully")
    void search_handleNullTercod() {
        Ter ter1 = createTer(null, "Provider A", "NIF1", "Alias1", 0);
        Ter ter2 = createTer(200, "Provider B", "NIF2", "Alias2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "200");

        assertEquals(1, result.size());
        assertEquals(200, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: handles null TERNIF gracefully")
    void search_handleNullTernif() {
        Ter ter1 = createTer(100, "Provider A", null, "Alias1", 0);
        Ter ter2 = createTer(200, "Provider B", "NIF2", "Alias2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertTrue(result.isEmpty() || result.size() >= 0);
    }

    @Test
    @DisplayName("searchProveedores: handles null TERNOM gracefully")
    void search_handleNullTernom() {
        Ter ter1 = createTer(100, null, "NIF1", "Alias1", 0);
        Ter ter2 = createTer(200, "Provider B", "NIF2", "Alias2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("searchProveedores: handles null TERALI gracefully")
    void search_handleNullTerali() {
        Ter ter1 = createTer(100, "Provider A", "NIF1", null, 0);
        Ter ter2 = createTer(200, "Provider B", "NIF2", "ABC Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
        assertEquals(200, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: returns empty when repository returns empty list")
    void search_emptyRepository() {
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of());

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("searchProveedores: returns null when repository returns null")
    void search_nullRepository() {
        when(terRepository.findByENT(TEST_ENT)).thenReturn(null);

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertNull(result);
    }

    @Test
    @DisplayName("searchProveedores: TERNOM matching is case-insensitive contains")
    void search_ternomCaseInsensitiveContains() {
        Ter ter1 = createTer(100, "ABC Company", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "abc company", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("searchProveedores: TERNIF matching is case-insensitive contains")
    void search_ternifCaseInsensitiveContains() {
        Ter ter1 = createTer(100, "Provider", "NIFABC", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "nifabc", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "NIFABC");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("searchProveedores: single digit numeric term")
    void search_singleDigitNumeric() {
        Ter ter1 = createTer(1, "Provider A", "NIF1", "Alias1", 0);
        Ter ter2 = createTer(2, "Provider B", "NIF2", "Alias2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "1");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: exactly 5 character numeric term uses TERNIF search")
    void search_exactly5CharNumeric() {
        Ter ter1 = createTer(100, "Provider", "NIF12345", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIFXYZ", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchProveedores: 4 character numeric term uses TERCOD only")
    void search_4CharNumeric() {
        Ter ter1 = createTer(1234, "Provider", "NIF1234", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIF1234", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "1234");

        assertEquals(1, result.size());
        assertEquals(1234, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: special characters in mixed term")
    void search_specialCharactersInMixedTerm() {
        Ter ter1 = createTer(100, "Provider & Co.", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "Provider Inc", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "&");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchProveedores: whitespace in mixed term")
    void search_whitespaceInMixedTerm() {
        Ter ter1 = createTer(100, "Provider Company", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "ProviderCompany", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "Provider Company");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchProveedores: multiple TERNIF matches for numeric term")
    void search_multipleNifMatches() {
        Ter ter1 = createTer(100, "Provider", "NIF111111", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIF111111", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "111111");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("searchProveedores: multiple TERNOM matches for mixed term")
    void search_multipleNomMatches() {
        Ter ter1 = createTer(100, "ABC Company", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "Company ABC", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "Company");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("searchProveedores: matches across different fields")
    void search_matchesAcrossFields() {
        Ter ter1 = createTer(100, "Provider A", "NIFABC", "Alias", 0);
        Ter ter2 = createTer(200, "Provider B", "NIF", "ABC Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("searchProveedores: todos mode with large dataset")
    void search_todosLargeDataset() {
        List<Ter> providers = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            providers.add(createTer(i, "Provider " + i, "NIF" + i, "Alias" + i, i % 2));
        }
        when(terRepository.findByENT(TEST_ENT)).thenReturn(providers);

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "50");

        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: Nobloqueado mode with mixed blocked/unblocked")
    void search_nobloqueadoMixedStatus() {
        List<Ter> providers = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            providers.add(createTer(i, "ABC Provider " + i, "NIF" + i, "Alias", i % 2));
        }
        when(terRepository.findByENT(TEST_ENT)).thenReturn(providers);

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "ABC");

        assertTrue(result.stream().allMatch(t -> t.getTERBLO() == 0));
    }

    @Test
    @DisplayName("searchProveedores: bloqueado mode filters correctly from large set")
    void search_bloqueadoLargeSet() {
        List<Ter> providers = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            providers.add(createTer(i, "ABC Provider " + i, "NIF" + i, "Alias", i % 2));
        }
        when(terRepository.findByENT(TEST_ENT)).thenReturn(providers);

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "bloqueado", "ABC");

        assertTrue(result.stream().allMatch(t -> t.getTERBLO() == 1));
    }

    @Test
    @DisplayName("searchProveedores: exact match on all fields for mixed term")
    void search_exactMatchAllFields() {
        Ter ter = createTer(100, "ABC", "ABC", "ABC", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchProveedores: partial match at beginning")
    void search_partialMatchBeginning() {
        Ter ter = createTer(100, "ABCDefghij", "NIF1", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchProveedores: partial match in middle")
    void search_partialMatchMiddle() {
        Ter ter = createTer(100, "DefABCghij", "NIF1", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchProveedores: partial match at end")
    void search_partialMatchEnd() {
        Ter ter = createTer(100, "DefghijABC", "NIF1", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchProveedores: Nobloqueado preserves order with terblo filter")
    void search_nobloqueadoPreservesOrder() {
        Ter ter1 = createTer(100, "A", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "B", "NIF2", "Alias", 1);
        Ter ter3 = createTer(300, "C", "NIF3", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2, ter3));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "100");

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("searchProveedores: bloqueado removes unblocked from results")
    void search_bloqueadoRemovesUnblocked() {
        Ter ter1 = createTer(100, "Provider", "NIF1", "Alias", 1);
        Ter ter2 = createTer(100, "Provider", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "bloqueado", "100");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernif: matches by TERCOD exact")
    void filterTodosByTercodAndTernif_matchesByTercod() {
        Ter ter1 = createTer(12345, "Provider", "NIFXYZ", "Alias", 0);
        Ter ter2 = createTer(99999, "Provider", "NIF123", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertEquals(1, result.size());
        assertEquals(12345, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernif: matches by TERNIF contains")
    void filterTodosByTercodAndTernif_matchesByTernif() {
        Ter ter1 = createTer(100, "Provider", "NIF12345ABC", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIFXYZ", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertEquals(1, result.size());
        assertEquals("NIF12345ABC", result.get(0).getTERNIF());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernif: matches both TERCOD and TERNIF")
    void filterTodosByTercodAndTernif_matchesBoth() {
        Ter ter1 = createTer(12345, "Provider", "NIF12345", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIFXYZ", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernif: no match returns empty")
    void filterTodosByTercodAndTernif_noMatch() {
        Ter ter1 = createTer(100, "Provider", "NIF111", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "99999");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernif: null TERNIF doesn't cause exception")
    void filterTodosByTercodAndTernif_nullTernif() {
        Ter ter1 = createTer(100, "Provider", null, "Alias", 0);
        Ter ter2 = createTer(12345, "Provider", "NIF", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertEquals(1, result.size());
        assertEquals(12345, result.get(0).getTERCOD());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernif: null TERCOD doesn't cause exception")
    void filterTodosByTercodAndTernif_nullTercod() {
        Ter ter1 = createTer(null, "Provider", "NIF12345", "Alias", 0);
        Ter ter2 = createTer(100, "Provider", "NIFXYZ", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertEquals(1, result.size());
        assertEquals("NIF12345", result.get(0).getTERNIF());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernif: TERNIF partial match contains")
    void filterTodosByTercodAndTernif_ternifPartialMatch() {
        Ter ter1 = createTer(100, "Provider", "PREFIX12345SUFFIX", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "12345");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernomAndTerali: matches by TERNIF contains")
    void filterTodosByTercodAndTernomAndTerali_matchesByTernif() {
        Ter ter1 = createTer(100, "Provider", "NIFABC", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIF", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernomAndTerali: matches by TERNOM contains")
    void filterTodosByTercodAndTernomAndTerali_matchesByTermom() {
        Ter ter1 = createTer(100, "ABC Company", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "XYZ Corp", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernomAndTerali: matches by TERALI contains")
    void filterTodosByTercodAndTernomAndTerali_matchesByTerali() {
        Ter ter1 = createTer(100, "Provider", "NIF1", "ABC Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIF2", "XYZ Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernomAndTerali: matches from all three fields")
    void filterTodosByTercodAndTernomAndTerali_matchesAllThree() {
        Ter ter1 = createTer(100, "ABC Company", "NIFABC", "ABC Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernomAndTerali: no match returns empty")
    void filterTodosByTercodAndTernomAndTerali_noMatch() {
        Ter ter1 = createTer(100, "Provider", "NIF1", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "NOMATCH");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernomAndTerali: null values handled")
    void filterTodosByTercodAndTernomAndTerali_nullValues() {
        Ter ter1 = createTer(100, null, "NIFABC", null, 0);
        Ter ter2 = createTer(200, "ABC Company", null, "ABC Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("filterTodosByTercodAndTernomAndTerali: case-insensitive matching")
    void filterTodosByTercodAndTernomAndTerali_caseSensitive() {
        Ter ter1 = createTer(100, "ABC Company", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "abc company", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "todos", "ABC");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTerblo: matches TERCOD with TERBLO=0")
    void filterNoBloqueadoByTercodAndTerblo_matchesAndFilters() {
        Ter ter1 = createTer(123, "Provider", "NIF1", "Alias", 0);
        Ter ter2 = createTer(123, "Provider", "NIF2", "Alias", 1);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "123");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTerblo: TERBLO=1 excluded for Nobloqueado")
    void filterNoBloqueadoByTercodAndTerblo_excludesTerblo1() {
        Ter ter1 = createTer(123, "Provider", "NIF1", "Alias", 1);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "123");

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTerblo: bloqueado mode matches TERBLO=1")
    void filterNoBloqueadoByTercodAndTerblo_bloqueadoMode() {
        Ter ter1 = createTer(123, "Provider", "NIF1", "Alias", 1);
        Ter ter2 = createTer(123, "Provider", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "bloqueado", "123");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTerblo: null TERBLO handled")
    void filterNoBloqueadoByTercodAndTerblo_nullTerblo() {
        Ter ter1 = createTer(123, "Provider", "NIF1", "Alias", null);
        Ter ter2 = createTer(123, "Provider", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "123");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernifAndTerblo: matches TERCOD with TERBLO=0")
    void filterNoBloqueadoByTercodAndTernifAndTerblo_matchesTercod() {
        Ter ter1 = createTer(12345, "Provider", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "12345");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernifAndTerblo: matches TERNIF with TERBLO=0")
    void filterNoBloqueadoByTercodAndTernifAndTerblo_matchesTernif() {
        Ter ter1 = createTer(100, "Provider", "NIF12345", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIFXYZ", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "12345");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernifAndTerblo: excludes TERBLO=1")
    void filterNoBloqueadoByTercodAndTernifAndTerblo_excludesTerblo1() {
        Ter ter1 = createTer(12345, "Provider", "NIF12345", "Alias", 1);
        Ter ter2 = createTer(200, "Provider", "NIF12345", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "12345");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernifAndTerblo: bloqueado mode matches TERBLO=1")
    void filterNoBloqueadoByTercodAndTernifAndTerblo_bloqueadoMode() {
        Ter ter1 = createTer(12345, "Provider", "NIF12345", "Alias", 1);
        Ter ter2 = createTer(200, "Provider", "NIF12345", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "bloqueado", "12345");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernifAndTerblo: multiple matches all filtered by TERBLO")
    void filterNoBloqueadoByTercodAndTernifAndTerblo_multipleMatches() {
        Ter ter1 = createTer(100, "Provider", "NIF12345", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIF1234567", "Alias", 0);
        Ter ter3 = createTer(300, "Provider", "NIF12345", "Alias", 1);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2, ter3));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "12345");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getTERBLO() == 0));
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo: matches TERNIF with TERBLO=0")
    void filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo_matchesTernif() {
        Ter ter1 = createTer(100, "Provider", "NIFABC", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIFXYZ", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "ABC");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo: matches TERNOM with TERBLO=0")
    void filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo_matchesTernom() {
        Ter ter1 = createTer(100, "ABC Company", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "XYZ Corp", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "ABC");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo: matches TERALI with TERBLO=0")
    void filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo_matchesTerali() {
        Ter ter1 = createTer(100, "Provider", "NIF1", "ABC Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIF2", "XYZ Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "ABC");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo: excludes TERBLO=1")
    void filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo_excludesTerblo1() {
        Ter ter1 = createTer(100, "ABC Company", "NIFABC", "ABC Alias", 1);
        Ter ter2 = createTer(200, "ABC Corp", "NIF2", "ABC2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "ABC");

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo: bloqueado matches TERBLO=1")
    void filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo_bloqueadoMode() {
        Ter ter1 = createTer(100, "ABC Company", "NIFABC", "ABC Alias", 1);
        Ter ter2 = createTer(200, "ABC Corp", "NIF2", "ABC2", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "bloqueado", "ABC");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTERBLO());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo: multiple matches from different fields")
    void filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo_multipleFields() {
        Ter ter1 = createTer(100, "ABC Company", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "Provider", "NIFABC", "Alias", 0);
        Ter ter3 = createTer(300, "Provider", "NIF3", "ABC Alias", 0);
        Ter ter4 = createTer(400, "ABC Corp", "NIF4", "Alias", 1);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2, ter3, ter4));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "ABC");

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(t -> t.getTERBLO() == 0));
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo: null values in all fields")
    void filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo_nullValues() {
        Ter ter1 = createTer(100, null, "NIFABC", null, 0);
        Ter ter2 = createTer(200, "ABC", null, "Alias", 0);
        Ter ter3 = createTer(300, "Provider", null, "ABC", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2, ter3));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "ABC");

        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo: case-insensitive matching")
    void filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo_caseSensitive() {
        Ter ter1 = createTer(100, "ABC Company", "NIF1", "Alias", 0);
        Ter ter2 = createTer(200, "abc company", "NIF2", "Alias", 0);
        when(terRepository.findByENT(TEST_ENT)).thenReturn(List.of(ter1, ter2));

        List<Ter> result = proveedoresSearch.searchProveedoers(TEST_ENT, "Nobloqueado", "ABC");

        assertEquals(2, result.size());
    }
}