package com.example.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.config.TestExceptionHandler;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.albFacturaDto;
import com.example.backend.sqlserver2.model.Alb;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.repository.AdeRepository;
import com.example.backend.sqlserver2.repository.AlbRepository;
import com.example.backend.sqlserver2.repository.FacRepository;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AlbController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class AlbControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbRepository albRepository;

    @MockitoBean
    private FacRepository facRepository;

    @MockitoBean
    private AdeRepository adeRepository;

    @MockitoBean
    private FdeRepository fdeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAlbaranesByFactura_returns200WithList() throws Exception {
        Alb a = new Alb();
        a.setALBNUM(1);
        a.setALBREF("REF123");
        when(albRepository.findByENTAndEJEAndFACNUM(1, "E1", 100)).thenReturn(List.of(a));

        mockMvc.perform(get("/api/alb/albaranes/1/E1/100")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAlbaranesByFactura_returns404WhenEmpty() throws Exception {
        when(albRepository.findByENTAndEJEAndFACNUM(2, "E2", 200)).thenReturn(List.of());

        mockMvc.perform(get("/api/alb/albaranes/2/E2/200"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getAlbaranesByFactura_returns400OnDataAccessException() throws Exception {
        when(albRepository.findByENTAndEJEAndFACNUM(anyInt(), anyString(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/alb/albaranes/1/E1/100"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void fetchAlbaranesByServices_returnsResults() throws Exception {
        when(albRepository.findAlbFactura(anyInt(), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/alb/albaranes-factura/1/100/E1/C1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void fetchAlbaranesByServices_returnsNotFoundWhenEmpty() throws Exception {
        when(albRepository.findAlbFactura(anyInt(), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/alb/albaranes-factura/1/100/E1/C1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void addingAlbaranes_returnsNoContentOnSuccess() throws Exception {
        Alb alb = new Alb();
        when(albRepository.findById(any())).thenReturn(Optional.of(alb));
        when(facRepository.findById(any())).thenReturn(Optional.of(new Fac()));

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "EJE", "E1",
            "ALBNUM", 100,
            "CONCTP", "TP",
            "CONCPR", "PR",
            "CONCCR", "CR",
            "ALBBIM", 50.0,
            "FACNUM", 200
        );

        mockMvc.perform(patch("/api/alb/add-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(payload))))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(albRepository).save(any(Alb.class));
        verify(facRepository).save(any(Fac.class));
    }

    @Test
    void addingAlbaranes_returnsBadRequestWhenPayloadNull() throws Exception {
        mockMvc.perform(patch("/api/alb/add-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of())))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void addingAlbaranes_returnsBadRequestOnDataAccessException() throws Exception {
        when(albRepository.findById(any())).thenThrow(new DataAccessResourceFailureException("DB error"));

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "EJE", "E1",
            "ALBNUM", 100,
            "CONCTP", "TP",
            "CONCPR", "PR",
            "CONCCR", "CR",
            "ALBBIM", 50.0,
            "FACNUM", 200
        );

        mockMvc.perform(patch("/api/alb/add-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(payload))))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void quitarAlbaranes_returnsNoContentOnSuccess() throws Exception {
        Alb alb = new Alb();
        Fac fac = new Fac();
        fac.setFACIEC(100.0);
        when(albRepository.findById(any())).thenReturn(Optional.of(alb));
        when(facRepository.findById(any())).thenReturn(Optional.of(fac));

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "EJE", "E1",
            "ALBNUM", 100,
            "FACNUM", 200,
            "ALBBIM", 0.0
        );

        mockMvc.perform(patch("/api/alb/quitar-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(albRepository).save(any(Alb.class));
        verify(facRepository).save(any(Fac.class));
    }

    @Test
    void quitarAlbaranes_returnsBadRequestWhenPayloadNull() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ENT", null);

        mockMvc.perform(patch("/api/alb/quitar-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void quitarAlbaranes_returnsBadRequestOnDataAccessException() throws Exception {
        when(albRepository.findById(any())).thenThrow(new DataAccessResourceFailureException("DB error"));

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "EJE", "E1",
            "ALBNUM", 100,
            "FACNUM", 200,
            "FACIEC", 0.0
        );

        mockMvc.perform(patch("/api/alb/quitar-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void searchAlbaranesByDesde_returnsResultsWhenFound() throws Exception {
        albFacturaDto dto = new albFacturaDto() {
            @Override
            public String getALBREF() { return "REF123"; }
            @Override
            public LocalDateTime getALBDAT() { return LocalDateTime.now(); }
            @Override
            public Double getALBBIM() { return 100.0; }
            @Override
            public Integer getALBNUM() { return 1; }
            @Override
            public LocalDateTime getALBFRE() { return LocalDateTime.now(); }
            @Override
            public String getCONCTP() { return "TP"; }
            @Override
            public String getCONCPR() { return "PR"; }
            @Override
            public String getCONCCR() { return "CR"; }
            @Override
            public String getDEPCOD() { return "1"; }
            @Override
            public String getALBCOM() { return "ALB"; }
        };

        when(albRepository.findAlbFacturaGreaterThanEqual(anyInt(), anyInt(), anyInt(), any(LocalDateTime.class), anyString(), anyString()))
            .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/alb/search-albaranes-Desde/1/100/" + LocalDateTime.now() + "/E1/C1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void searchAlbaranesByDesde_returns404WhenEmpty() throws Exception {
        when(albRepository.findAlbFacturaGreaterThanEqual(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/alb/search-albaranes-Desde/1/100/" + LocalDateTime.now() + "/E1/C1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchAlbaranesByDesde_returnsBadRequestOnException() throws Exception {
        when(albRepository.findAlbFacturaGreaterThanEqual(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/alb/search-albaranes-Desde/1/100/" + LocalDateTime.now() + "/E1/C1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void searchAlbaranesByHasta_returnsResultsWhenFound() throws Exception {
        albFacturaDto dto = new albFacturaDto() {
            @Override
            public String getALBREF() { return "REF456"; }
            @Override
            public LocalDateTime getALBDAT() { return LocalDateTime.now(); }
            @Override
            public Double getALBBIM() { return 200.0; }
            @Override
            public Integer getALBNUM() { return 2; }
            @Override
            public LocalDateTime getALBFRE() { return LocalDateTime.now(); }
            @Override
            public String getCONCTP() { return "TP"; }
            @Override
            public String getCONCPR() { return "PR"; }
            @Override
            public String getCONCCR() { return "CR"; }
            @Override
            public String getDEPCOD() { return "2"; }
            @Override
            public String getALBCOM() { return "ALB"; }
        };

        when(albRepository.findAlbFacturaLessThanEqual(anyInt(), anyInt(), anyInt(), any(LocalDateTime.class), anyString(), anyString()))
            .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/alb/search-albaranes-Hasta/1/100/" + LocalDateTime.now() + "/E1/C1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void searchAlbaranesByHasta_returns404WhenEmpty() throws Exception {
        when(albRepository.findAlbFacturaLessThanEqual(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/alb/search-albaranes-Hasta/1/100/" + LocalDateTime.now() + "/E1/C1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchAlbaranesByHasta_returnsBadRequestOnException() throws Exception {
        when(albRepository.findAlbFacturaLessThanEqual(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/alb/search-albaranes-Hasta/1/100/" + LocalDateTime.now() + "/E1/C1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void addingAlbaranes_withAsuEcoImpProjection_updatesFdeSuccessfully() throws Exception {
        Alb alb = new Alb();
        Fac fac = new Fac();
        Fde fde = new Fde();
        fde.setFDEIMP(50.0);

        when(albRepository.findById(any())).thenReturn(Optional.of(alb));
        when(facRepository.findById(any())).thenReturn(Optional.of(fac));
        com.example.backend.dto.AsuEcoImpProjection projection = mock(com.example.backend.dto.AsuEcoImpProjection.class);
        when(projection.getASUECO()).thenReturn("ECO1");
        when(projection.getIMP()).thenReturn(50.0);
        when(adeRepository.findSumByEntAndAlbnum(1, 100)).thenReturn(Optional.of(projection));
        when(fdeRepository.findByENTAndEJEAndFACNUMAndFDEECO(1, "E1", 200, "ECO1")).thenReturn(Optional.of(fde));

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "EJE", "E1",
            "ALBNUM", 100,
            "CONCTP", "TP",
            "CONCPR", "PR",
            "CONCCR", "CR",
            "ALBBIM", 50.0,
            "FACNUM", 200
        );

        mockMvc.perform(patch("/api/alb/add-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(payload))))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(fdeRepository).save(any(Fde.class));
    }

    @Test
    void quitarAlbaranes_withAsuEcoImpProjection_subtractsFromFde() throws Exception {
        Alb alb = new Alb();
        Fac fac = new Fac();
        fac.setFACIEC(100.0);
        Fde fde = new Fde();
        fde.setFDEIMP(100.0);

        when(albRepository.findById(any())).thenReturn(Optional.of(alb));
        when(facRepository.findById(any())).thenReturn(Optional.of(fac));
        com.example.backend.dto.AsuEcoImpProjection projection = mock(com.example.backend.dto.AsuEcoImpProjection.class);
        when(projection.getASUECO()).thenReturn("ECO1");
        when(projection.getIMP()).thenReturn(50.0);
        when(adeRepository.findSumByEntAndAlbnum(1, 100)).thenReturn(Optional.of(projection));
        when(fdeRepository.findByENTAndEJEAndFACNUMAndFDEECO(1, "E1", 200, "ECO1")).thenReturn(Optional.of(fde));

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "EJE", "E1",
            "ALBNUM", 100,
            "FACNUM", 200,
            "ALBBIM", 50.0
        );

        mockMvc.perform(patch("/api/alb/quitar-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(fdeRepository).save(any(Fde.class));
    }

    @Test
    void addingAlbaranes_withoutFde_doesNotThrowError() throws Exception {
        Alb alb = new Alb();
        Fac fac = new Fac();

        when(albRepository.findById(any())).thenReturn(Optional.of(alb));
        when(facRepository.findById(any())).thenReturn(Optional.of(fac));
        when(adeRepository.findSumByEntAndAlbnum(1, 100)).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "EJE", "E1",
            "ALBNUM", 100,
            "CONCTP", "TP",
            "CONCPR", "PR",
            "CONCCR", "CR",
            "ALBBIM", 50.0,
            "FACNUM", 200
        );

        mockMvc.perform(patch("/api/alb/add-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(payload))))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    void fetchAlbaranesByServices_onException_returnsBadRequest() throws Exception {
        when(albRepository.findAlbFactura(anyInt(), anyInt(), anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/alb/albaranes-factura/1/100/E1/C1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void quitarAlbaranes_missingALBNUM_returnsBadRequest() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ENT", 1);
        payload.put("ALBNUM", null);
        payload.put("EJE", "E1");

        mockMvc.perform(patch("/api/alb/quitar-albaranes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }
}