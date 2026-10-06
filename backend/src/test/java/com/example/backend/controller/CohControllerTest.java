package com.example.backend.controller;

import com.example.backend.dto.HistoricaContratos;
import com.example.backend.service.HistoricaADContratoSearch;
import com.example.backend.sqlserver2.repository.CohRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CohControllerTest {

    private static final Integer ENT = 1;
    private static final String EJE = "2026";
    private static final Integer CONTIP = 3;
    private static final String ROOT_CAUSE_MESSAGE = "Connection reset";
    private static final String EXPECTED_ERROR_BODY = "Error :" + ROOT_CAUSE_MESSAGE;

    @Mock
    private CohRepository cohRepository;

    @Mock
    private HistoricaADContratoSearch historicaADContratoSearch;

    @InjectMocks
    private CohController controller;

    @Test
    void fetchReturnsOkWithContratos() {
        List<HistoricaContratos> contratos = List.of(mock(HistoricaContratos.class));
        when(cohRepository.findByENTAndEJEAndCon_CONTIP(ENT, EJE, CONTIP)).thenReturn(contratos);

        ResponseEntity<?> response = controller.fetchHistoriaADContrato(ENT, EJE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(contratos, response.getBody());
        verify(cohRepository).findByENTAndEJEAndCon_CONTIP(ENT, EJE, CONTIP);
    }

    @Test
    void fetchReturnsNotFoundWhenEmpty() {
        when(cohRepository.findByENTAndEJEAndCon_CONTIP(ENT, EJE, CONTIP)).thenReturn(List.of());

        ResponseEntity<?> response = controller.fetchHistoriaADContrato(ENT, EJE);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Sin resultado", response.getBody());
    }

    @Test
    void fetchReturnsInternalServerErrorOnDataAccessException() {
        when(cohRepository.findByENTAndEJEAndCon_CONTIP(ENT, EJE, CONTIP))
            .thenThrow(dataAccessException());

        ResponseEntity<?> response = controller.fetchHistoriaADContrato(ENT, EJE);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(EXPECTED_ERROR_BODY, response.getBody());
    }

    @Test
    void searchReturnsBadRequestWhenAllCriteriaAreNull() {
        ResponseEntity<?> response = controller.searchHistoriaContrato(ENT, EJE, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Faltan datos obligatorios", response.getBody());
        verifyNoInteractions(historicaADContratoSearch);
    }

    @Test
    void searchDelegatesToServiceWhenOnlyCgeIsProvided() {
        List<HistoricaContratos> contratos = List.of(mock(HistoricaContratos.class));
        when(historicaADContratoSearch.historicaADContratoSearch(ENT, EJE, "CG01", null, null))
            .thenReturn(contratos);

        ResponseEntity<?> response = controller.searchHistoriaContrato(ENT, EJE, "CG01", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(contratos, response.getBody());
    }

    @Test
    void searchDelegatesToServiceWhenOnlyContratoIsProvided() {
        List<HistoricaContratos> contratos = List.of(mock(HistoricaContratos.class));
        when(historicaADContratoSearch.historicaADContratoSearch(ENT, EJE, null, "10", null))
            .thenReturn(contratos);

        ResponseEntity<?> response = controller.searchHistoriaContrato(ENT, EJE, null, "10", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(contratos, response.getBody());
    }

    @Test
    void searchDelegatesToServiceWhenOnlyProveedorIsProvided() {
        List<HistoricaContratos> contratos = List.of(mock(HistoricaContratos.class));
        when(historicaADContratoSearch.historicaADContratoSearch(ENT, EJE, null, null, "Proveedor"))
            .thenReturn(contratos);

        ResponseEntity<?> response = controller.searchHistoriaContrato(ENT, EJE, null, null, "Proveedor");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(contratos, response.getBody());
    }

    @Test
    void searchDelegatesToServiceWhenAllCriteriaAreProvided() {
        List<HistoricaContratos> contratos = List.of(mock(HistoricaContratos.class));
        when(historicaADContratoSearch.historicaADContratoSearch(ENT, EJE, "CG01", "10", "Proveedor"))
            .thenReturn(contratos);

        ResponseEntity<?> response =
            controller.searchHistoriaContrato(ENT, EJE, "CG01", "10", "Proveedor");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(contratos, response.getBody());
        verify(historicaADContratoSearch)
            .historicaADContratoSearch(ENT, EJE, "CG01", "10", "Proveedor");
    }

    @Test
    void searchReturnsNotFoundWhenServiceReturnsEmptyList() {
        when(historicaADContratoSearch.historicaADContratoSearch(ENT, EJE, "CG01", null, null))
            .thenReturn(List.of());

        ResponseEntity<?> response = controller.searchHistoriaContrato(ENT, EJE, "CG01", null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Sin resultado", response.getBody());
    }

    @Test
    void searchReturnsInternalServerErrorOnDataAccessException() {
        when(historicaADContratoSearch.historicaADContratoSearch(ENT, EJE, "CG01", null, null))
            .thenThrow(dataAccessException());

        ResponseEntity<?> response = controller.searchHistoriaContrato(ENT, EJE, "CG01", null, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(EXPECTED_ERROR_BODY, response.getBody());
    }

    private DataAccessException dataAccessException() {
        return new DataRetrievalFailureException(
            "Wrapper message",
            new RuntimeException(ROOT_CAUSE_MESSAGE)
        );
    }
}