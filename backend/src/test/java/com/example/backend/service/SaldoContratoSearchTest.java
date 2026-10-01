package com.example.backend.service;

import com.example.backend.dto.SaldoContrato;
import com.example.backend.sqlserver2.repository.CogRepository;

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
class SaldoContratoSearchTest {

    @Mock
    private CogRepository cogRepository;

    @InjectMocks
    private SaldoContratoSearch saldoContratoSearch;

    private List<SaldoContrato> mutableList(SaldoContrato... contratos) {
        return new ArrayList<>(List.of(contratos));
    }

    @Test
    void searchSaldoContratos_returnsEmptyWhenNoCriteria() {
        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verifyNoInteractions(cogRepository);
    }

    @Test
    void searchSaldoContratos_searchesByNumericProveedor() {
        SaldoContrato contrato = mock(SaldoContrato.class);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERCODOrENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERNIFContaining(
                1, "2026", 3, 1, 123, 1, "2026", 3, 1, "123"))
            .thenReturn(List.of(contrato));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, null, "123");

        assertEquals(1, result.size());
        assertSame(contrato, result.get(0));
    }

    @Test
    void searchSaldoContratos_searchesByTextProveedor() {
        SaldoContrato contrato = mock(SaldoContrato.class);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERNOMContainingOrENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERNIFContaining(
                1, "2026", 3, 1, "Proveedor", 1, "2026", 3, 1, "Proveedor"))
            .thenReturn(List.of(contrato));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, null, "Proveedor");

        assertEquals(1, result.size());
        assertSame(contrato, result.get(0));
    }

    @Test
    void searchSaldoContratos_searchesByCgeAndNumericContrato() {
        SaldoContrato contrato = mock(SaldoContrato.class);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECODAndCONCOD(
                1, "2026", 3, 1, "CG01", 123))
            .thenReturn(List.of(contrato));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", "CG01", "123", null);

        assertEquals(1, result.size());
        assertSame(contrato, result.get(0));
    }

    @Test
    void searchSaldoContratos_searchesByCgeAndTextContrato() {
        SaldoContrato contrato = mock(SaldoContrato.class);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECODAndCot_conn_CONDESContaining(
                1, "2026", 3, 1, "CG01", "Contrato"))
            .thenReturn(List.of(contrato));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", "CG01", "Contrato", null);

        assertEquals(1, result.size());
        assertSame(contrato, result.get(0));
    }

    @Test
    void searchSaldoContratos_searchesByCgeWithoutContrato() {
        SaldoContrato contrato = mock(SaldoContrato.class);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECOD(
                1, "2026", 3, 1, "CG01"))
            .thenReturn(List.of(contrato));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", "CG01", null, null);

        assertEquals(1, result.size());
        assertSame(contrato, result.get(0));
    }

    @Test
    void searchSaldoContratos_searchesByNumericContratoWithoutCge() {
        SaldoContrato contrato = mock(SaldoContrato.class);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCONCOD(
                1, "2026", 3, 1, 123))
            .thenReturn(List.of(contrato));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, "123", null);

        assertEquals(1, result.size());
        assertSame(contrato, result.get(0));
    }

    @Test
    void searchSaldoContratos_searchesByTextContratoWithoutCge() {
        SaldoContrato contrato = mock(SaldoContrato.class);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_conn_CONDESContaining(
                1, "2026", 3, 1, "Contrato"))
            .thenReturn(List.of(contrato));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, "Contrato", null);

        assertEquals(1, result.size());
        assertSame(contrato, result.get(0));
    }

    @Test
    void searchSaldoContratos_filtersNumericProveedorWithCgeAndContrato() {
        SaldoContrato matching = mock(SaldoContrato.class);
        SaldoContrato nonMatching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection matchingCot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.CotProjection nonMatchingCot =
            mock(SaldoContrato.CotProjection.class);

        when(matching.getCot()).thenReturn(matchingCot);
        when(nonMatching.getCot()).thenReturn(nonMatchingCot);

        when(matchingCot.getTERCOD()).thenReturn(123);
        when(nonMatchingCot.getTERCOD()).thenReturn(999);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECODAndCONCOD(
                1, "2026", 3, 1, "CG01", 10))
            .thenReturn(mutableList(matching, nonMatching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", "CG01", "10", "123");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_filtersTextProveedorWithCgeAndContrato() {
        SaldoContrato matching = mock(SaldoContrato.class);
        SaldoContrato nonMatching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection matchingCot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.CotProjection nonMatchingCot =
            mock(SaldoContrato.CotProjection.class);

        SaldoContrato.TerProjection matchingTer =
            mock(SaldoContrato.TerProjection.class);
        SaldoContrato.TerProjection nonMatchingTer =
            mock(SaldoContrato.TerProjection.class);

        when(matching.getCot()).thenReturn(matchingCot);
        when(nonMatching.getCot()).thenReturn(nonMatchingCot);

        when(matchingCot.getTer()).thenReturn(matchingTer);
        when(nonMatchingCot.getTer()).thenReturn(nonMatchingTer);

        when(matchingTer.getTERNOM()).thenReturn("Proveedor ABC");
        when(nonMatchingTer.getTERNOM()).thenReturn("Proveedor XYZ");

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECODAndCONCOD(
                1, "2026", 3, 1, "CG01", 10))
            .thenReturn(mutableList(matching, nonMatching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", "CG01", "10", "ABC");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_filtersNumericProveedorWithCgeOnly() {
        SaldoContrato matching = mock(SaldoContrato.class);
        SaldoContrato nonMatching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection matchingCot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.CotProjection nonMatchingCot =
            mock(SaldoContrato.CotProjection.class);

        when(matching.getCot()).thenReturn(matchingCot);
        when(nonMatching.getCot()).thenReturn(nonMatchingCot);

        when(matchingCot.getTERCOD()).thenReturn(123);
        when(nonMatchingCot.getTERCOD()).thenReturn(999);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECOD(
                1, "2026", 3, 1, "CG01"))
            .thenReturn(mutableList(matching, nonMatching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", "CG01", null, "123");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_filtersTextProveedorWithCgeOnly() {
        SaldoContrato matching = mock(SaldoContrato.class);
        SaldoContrato nonMatching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection matchingCot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.CotProjection nonMatchingCot =
            mock(SaldoContrato.CotProjection.class);

        SaldoContrato.TerProjection matchingTer =
            mock(SaldoContrato.TerProjection.class);
        SaldoContrato.TerProjection nonMatchingTer =
            mock(SaldoContrato.TerProjection.class);

        when(matching.getCot()).thenReturn(matchingCot);
        when(nonMatching.getCot()).thenReturn(nonMatchingCot);

        when(matchingCot.getTer()).thenReturn(matchingTer);
        when(nonMatchingCot.getTer()).thenReturn(nonMatchingTer);

        when(matchingTer.getTERNIF()).thenReturn("123 ABC");
        when(nonMatchingTer.getTERNIF()).thenReturn("999 XYZ");

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECOD(
                1, "2026", 3, 1, "CG01"))
            .thenReturn(mutableList(matching, nonMatching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", "CG01", null, "abc");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_filtersNumericProveedorWithNumericContrato() {
        SaldoContrato matching = mock(SaldoContrato.class);
        SaldoContrato nonMatching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection matchingCot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.CotProjection nonMatchingCot =
            mock(SaldoContrato.CotProjection.class);

        SaldoContrato.TerProjection matchingTer =
            mock(SaldoContrato.TerProjection.class);
        SaldoContrato.TerProjection nonMatchingTer =
            mock(SaldoContrato.TerProjection.class);

        when(matching.getCot()).thenReturn(matchingCot);
        when(nonMatching.getCot()).thenReturn(nonMatchingCot);

        when(matchingCot.getTERCOD()).thenReturn(123);
        when(nonMatchingCot.getTERCOD()).thenReturn(999);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCONCOD(
                1, "2026", 3, 1, 10))
            .thenReturn(mutableList(matching, nonMatching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, "10", "123");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_filtersTextProveedorWithTextContrato() {
        SaldoContrato matching = mock(SaldoContrato.class);
        SaldoContrato nonMatching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection matchingCot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.CotProjection nonMatchingCot =
            mock(SaldoContrato.CotProjection.class);

        SaldoContrato.TerProjection matchingTer =
            mock(SaldoContrato.TerProjection.class);
        SaldoContrato.TerProjection nonMatchingTer =
            mock(SaldoContrato.TerProjection.class);

        when(matching.getCot()).thenReturn(matchingCot);
        when(nonMatching.getCot()).thenReturn(nonMatchingCot);

        when(matchingCot.getTer()).thenReturn(matchingTer);
        when(nonMatchingCot.getTer()).thenReturn(nonMatchingTer);

        when(matchingTer.getTERNOM()).thenReturn("Proveedor ABC");
        when(nonMatchingTer.getTERNOM()).thenReturn("Proveedor XYZ");

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_conn_CONDESContaining(
                1, "2026", 3, 1, "Contrato"))
            .thenReturn(mutableList(matching, nonMatching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, "Contrato", "ABC");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_numericProveedorMatchesTernif() {
        SaldoContrato matching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection cot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.TerProjection ter =
            mock(SaldoContrato.TerProjection.class);

        when(matching.getCot()).thenReturn(cot);
        when(cot.getTERCOD()).thenReturn(null);
        when(cot.getTer()).thenReturn(ter);
        when(ter.getTERNIF()).thenReturn(" 123 456 ");

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCONCOD(
                1, "2026", 3, 1, 10))
            .thenReturn(mutableList(matching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, "10", "123");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_textProveedorMatchesTernif() {
        SaldoContrato matching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection cot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.TerProjection ter =
            mock(SaldoContrato.TerProjection.class);

        when(matching.getCot()).thenReturn(cot);
        when(cot.getTer()).thenReturn(ter);
        when(ter.getTERNIF()).thenReturn("ABC   DEF");
        when(ter.getTERNOM()).thenReturn(null);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCONCOD(
                1, "2026", 3, 1, 10))
            .thenReturn(mutableList(matching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, "10", "abc def");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_textProveedorMatchesTernom() {
        SaldoContrato matching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection cot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.TerProjection ter =
            mock(SaldoContrato.TerProjection.class);

        when(matching.getCot()).thenReturn(cot);
        when(cot.getTer()).thenReturn(ter);
        when(ter.getTERNIF()).thenReturn(null);
        when(ter.getTERNOM()).thenReturn("Proveedor   ABC");

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCONCOD(
                1, "2026", 3, 1, 10))
            .thenReturn(mutableList(matching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, "10", "abc");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_filtersOutNullCotForNumericProveedor() {
        SaldoContrato nullCot = mock(SaldoContrato.class);
        SaldoContrato matching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection cot =
            mock(SaldoContrato.CotProjection.class);

        when(nullCot.getCot()).thenReturn(null);
        when(matching.getCot()).thenReturn(cot);
        when(cot.getTERCOD()).thenReturn(123);

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCONCOD(
                1, "2026", 3, 1, 10))
            .thenReturn(mutableList(nullCot, matching));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, "10", "123");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchSaldoContratos_filtersOutNullCotAndTerForTextProveedor() {
        SaldoContrato nullCot = mock(SaldoContrato.class);
        SaldoContrato nullTer = mock(SaldoContrato.class);
        SaldoContrato matching = mock(SaldoContrato.class);

        SaldoContrato.CotProjection cotWithoutTer =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.CotProjection matchingCot =
            mock(SaldoContrato.CotProjection.class);
        SaldoContrato.TerProjection ter =
            mock(SaldoContrato.TerProjection.class);

        when(nullCot.getCot()).thenReturn(null);

        when(nullTer.getCot()).thenReturn(cotWithoutTer);
        when(cotWithoutTer.getTer()).thenReturn(null);

        when(matching.getCot()).thenReturn(matchingCot);
        when(matchingCot.getTer()).thenReturn(ter);
        when(ter.getTERNOM()).thenReturn("Proveedor ABC");

        when(cogRepository
            .findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCONCOD(
                1, "2026", 3, 1, 10))
            .thenReturn(mutableList(
                nullCot,
                nullTer,
                matching
            ));

        List<SaldoContrato> result =
            saldoContratoSearch.searchSaldoContratos(
                1, "2026", null, "10", "ABC");

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }
}