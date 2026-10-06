package com.example.backend.service;

import com.example.backend.dto.HistoricaContratos;
import com.example.backend.sqlserver2.repository.CohRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricaADContratoSearchTest {

    private static final Integer ENT = 1;
    private static final String EJE = "2026";
    private static final Integer CONTIP = 3;

    @Mock
    private CohRepository cohRepository;

    @InjectMocks
    private HistoricaADContratoSearch service;

    @Test
    void returnsEmptyWhenNoCriteria() {
        assertTrue(service.historicaADContratoSearch(ENT, EJE, null, null, null).isEmpty());
        verifyNoInteractions(cohRepository);
    }

    @Test
    void searchesProviderByNumericTercodAndTernifAndRemovesDuplicates() {
        HistoricaContratos matching = projectionWithProvider(123, "123 ABC", "Proveedor");
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERCOD(
            ENT, EJE, CONTIP, 123)).thenReturn(List.of(matching));
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERNIFContaining(
            ENT, EJE, CONTIP, "123")).thenReturn(List.of(matching));

        List<HistoricaContratos> result =
            service.historicaADContratoSearch(ENT, EJE, null, null, "123");

        assertEquals(List.of(matching), result);
        verify(cohRepository).findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERCOD(
            ENT, EJE, CONTIP, 123);
        verify(cohRepository).findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERNIFContaining(
            ENT, EJE, CONTIP, "123");
    }

    @Test
    void searchesProviderByTextTernomAndTernif() {
        HistoricaContratos matching = mock(HistoricaContratos.class);
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERNOMContaining(
            ENT, EJE, CONTIP, "Proveedor")).thenReturn(List.of(matching));
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERNIFContaining(
            ENT, EJE, CONTIP, "Proveedor")).thenReturn(List.of());

        assertEquals(List.of(matching),
            service.historicaADContratoSearch(ENT, EJE, null, null, "Proveedor"));
    }

    @Test
    void searchesByCgeAndNumericContract() {
        HistoricaContratos result = mock(HistoricaContratos.class);
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECODAndConn_CONCOD(
            ENT, EJE, CONTIP, "CG01", 10)).thenReturn(List.of(result));

        assertEquals(List.of(result),
            service.historicaADContratoSearch(ENT, EJE, "CG01", "10", null));
    }

    @Test
    void searchesByCgeAndTextContract() {
        HistoricaContratos result = mock(HistoricaContratos.class);
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECODAndConn_CONDESContaining(
            ENT, EJE, CONTIP, "CG01", "Contrato")).thenReturn(List.of(result));

        assertEquals(List.of(result),
            service.historicaADContratoSearch(ENT, EJE, "CG01", "Contrato", null));
    }

    @Test
    void searchesByCgeWithoutContract() {
        HistoricaContratos result = mock(HistoricaContratos.class);
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECOD(
            ENT, EJE, CONTIP, "CG01")).thenReturn(List.of(result));

        assertEquals(List.of(result),
            service.historicaADContratoSearch(ENT, EJE, "CG01", null, null));
    }

    @Test
    void searchesByCgeAndContractThenFiltersNumericProvider() {
        HistoricaContratos matching = projectionWithProvider(123, null, null);
        HistoricaContratos nonMatching = projectionWithProvider(999, null, null);
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECODAndConn_CONCOD(
            ENT, EJE, CONTIP, "CG01", 10)).thenReturn(List.of(matching, nonMatching));

        assertEquals(List.of(matching),
            service.historicaADContratoSearch(ENT, EJE, "CG01", "10", "123"));
    }

    @Test
    void searchesByCgeAndContractThenFiltersTextProvider() {
        HistoricaContratos matching = projectionWithProvider(null, null, "Proveedor ABC");
        HistoricaContratos nonMatching = projectionWithProvider(null, null, "Proveedor XYZ");
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECODAndConn_CONCOD(
            ENT, EJE, CONTIP, "CG01", 10)).thenReturn(List.of(matching, nonMatching));

        assertEquals(List.of(matching),
            service.historicaADContratoSearch(ENT, EJE, "CG01", "10", "abc"));
    }

    @Test
    void searchesByCgeThenFiltersProvider() {
        HistoricaContratos matching = projectionWithProvider(123, null, null);
        HistoricaContratos nonMatching = projectionWithProvider(999, null, null);
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECOD(
            ENT, EJE, CONTIP, "CG01")).thenReturn(List.of(matching, nonMatching));

        assertEquals(List.of(matching),
            service.historicaADContratoSearch(ENT, EJE, "CG01", null, "123"));
    }

    @Test
    void searchesNumericContractThenFiltersProvider() {
        HistoricaContratos matching = projectionWithProvider(null, "123 456", null);
        HistoricaContratos nonMatching = projectionWithProvider(null, "999", null);
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_CONCOD(
            ENT, EJE, CONTIP, 10)).thenReturn(List.of(matching, nonMatching));

        assertEquals(List.of(matching),
            service.historicaADContratoSearch(ENT, EJE, null, "10", "123"));
    }

    @Test
    void searchesTextContractThenFiltersProvider() {
        HistoricaContratos matching = projectionWithProvider(null, null, "Proveedor ABC");
        HistoricaContratos nonMatching = projectionWithProvider(null, null, "Proveedor XYZ");
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_CONDESContaining(
            ENT, EJE, CONTIP, "Contrato")).thenReturn(List.of(matching, nonMatching));

        assertEquals(List.of(matching),
            service.historicaADContratoSearch(ENT, EJE, null, "Contrato", "ABC"));
    }

    @Test
    void providerFiltersIgnoreMissingRelationships() {
        HistoricaContratos noConn = mock(HistoricaContratos.class);
        HistoricaContratos noCots = mock(HistoricaContratos.class);
        when(noCots.getCon()).thenReturn(mock(HistoricaContratos.ConnProjection.class));
        when(noCots.getCon().getCots()).thenReturn(null);
        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECOD(
            ENT, EJE, CONTIP, "CG01")).thenReturn(List.of(noConn, noCots));

        assertTrue(service.historicaADContratoSearch(
            ENT, EJE, "CG01", null, "123").isEmpty());
    }

    @Test
    void textProviderFiltersTernifAndHandlesMissingTer() {
        HistoricaContratos matching = projectionWithProvider(null, "ABC DEF", null);
        HistoricaContratos missingTer = projectionWithProvider(null, null, null);
        HistoricaContratos noTerProjection = mock(HistoricaContratos.class);
        HistoricaContratos.ConnProjection conn = mock(HistoricaContratos.ConnProjection.class);
        HistoricaContratos.CotProjection cot = mock(HistoricaContratos.CotProjection.class);
        when(noTerProjection.getCon()).thenReturn(conn);
        when(conn.getCots()).thenReturn(List.of(cot));
        when(cot.getTer()).thenReturn(null);

        when(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_CONCOD(
            ENT, EJE, CONTIP, 10)).thenReturn(List.of(matching, missingTer, noTerProjection));

        assertEquals(List.of(matching),
            service.historicaADContratoSearch(ENT, EJE, null, "10", " abc   def "));
    }

    private HistoricaContratos projectionWithProvider(
        Integer tercod,
        String ternif,
        String ternom
    ) {
        HistoricaContratos projection = mock(HistoricaContratos.class);
        HistoricaContratos.ConnProjection conn = mock(HistoricaContratos.ConnProjection.class);
        HistoricaContratos.CotProjection cot = mock(HistoricaContratos.CotProjection.class);
        HistoricaContratos.TerProjection ter = mock(HistoricaContratos.TerProjection.class);

        lenient().when(projection.getCon()).thenReturn(conn);
        lenient().when(conn.getCots()).thenReturn(List.of(cot));
        lenient().when(cot.getTERCOD()).thenReturn(tercod);
        lenient().when(cot.getTer()).thenReturn(ter);
        lenient().when(ter.getTERNIF()).thenReturn(ternif);
        lenient().when(ter.getTERNOM()).thenReturn(ternom);

        return projection;
    }
}
