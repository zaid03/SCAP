package com.example.backend.controller;

import com.example.backend.dto.HistoricaContratos;
import com.example.backend.sqlserver2.model.Cog;
import com.example.backend.sqlserver2.model.CogId;
import com.example.backend.sqlserver2.model.Coh;
import com.example.backend.service.HistoricaADContratoSearch;
import com.example.backend.sqlserver2.repository.CohRepository;
import com.example.backend.sqlserver2.repository.CogRepository;

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
import java.util.Optional;

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

    @Mock
    private CogRepository cogRepository;

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

    @Test
    void historicoSetMovesSecondDToFirstAndClearsSecondD() {
        Integer concod = 55;
        String cgecod = "1E";
        Integer cohcod = 12;
        CogId id = new CogId(ENT, EJE, concod, cgecod);
        Cog cog = new Cog();
        CohController.historico payload = new CohController.historico(
            ENT,
            EJE,
            concod,
            5000.0,
            cgecod,
            "220260016813",
            "22026006710",
            10000.0,
            "220260016811",
            "22026006711"
        );

        when(cohRepository.getNextCohcod()).thenReturn(cohcod);
        when(cogRepository.findById(id)).thenReturn(Optional.of(cog));

        ResponseEntity<?> response = controller.historicoSet(payload);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(10000.0, cog.getCOGIMP());
        assertEquals("220260016811", cog.getCOGOPD());
        assertEquals("22026006711", cog.getCOGRFD());
        assertEquals(0.0, cog.getCOGIM2());
        assertEquals("", cog.getCOGOP2());
        assertEquals("", cog.getCOGRF2());
        verify(cohRepository).save(argThat(history ->
            history.getENT().equals(ENT)
                && history.getEJE().equals(EJE)
                && history.getCONCOD().equals(concod)
                && history.getCOHCOD().equals(cohcod)
                && history.getCGECOD().equals(cgecod)
                && history.getCOHOPD().equals("220260016813")
                && history.getCOHRFD().equals("22026006710")
                && history.getCOHFEC() != null
        ));
        verify(cogRepository).saveAndFlush(cog);
    }

    @Test
    void historicoSetReturnsBadRequestWhenRequiredPayloadDataIsMissing() {
        CohController.historico payload = new CohController.historico(
            ENT, EJE, 55, null, "1E", "OP1", "RF1", 100.0, "OP2", "RF2"
        );

        ResponseEntity<?> response = controller.historicoSet(payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Faltan datos obligatorios", response.getBody());
        verifyNoInteractions(cohRepository, cogRepository);
    }

    @Test
    void historicoSetReturnsNotFoundWhenCogDoesNotExist() {
        Integer concod = 55;
        String cgecod = "1E";
        CogId id = new CogId(ENT, EJE, concod, cgecod);
        CohController.historico payload = new CohController.historico(
            ENT, EJE, concod, 5000.0, cgecod, "OP1", "RF1", 100.0, "OP2", "RF2"
        );

        when(cohRepository.getNextCohcod()).thenReturn(12);
        when(cogRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.historicoSet(payload);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(cohRepository).save(any(Coh.class));
        verify(cogRepository, never()).saveAndFlush(any(Cog.class));
    }

    @Test
    void historicoSetReturnsInternalServerErrorOnDataAccessException() {
        CohController.historico payload = new CohController.historico(
            ENT, EJE, 55, 5000.0, "1E", "OP1", "RF1", 100.0, "OP2", "RF2"
        );
        when(cohRepository.getNextCohcod()).thenThrow(dataAccessException());

        ResponseEntity<?> response = controller.historicoSet(payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(EXPECTED_ERROR_BODY, response.getBody());
        verifyNoInteractions(cogRepository);
    }

    private DataAccessException dataAccessException() {
        return new DataRetrievalFailureException(
            "Wrapper message",
            new RuntimeException(ROOT_CAUSE_MESSAGE)
        );
    }
}