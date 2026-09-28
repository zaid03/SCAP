package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.dto.GbsWithCgeDto;
import com.example.backend.sqlserver2.model.Cge;
import com.example.backend.sqlserver2.model.CgeId;
import com.example.backend.sqlserver2.model.Gbs;
import com.example.backend.sqlserver2.model.GbsId;
import com.example.backend.sqlserver2.repository.CgeRepository;
import com.example.backend.sqlserver2.repository.GbsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GbsController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class GbsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GbsRepository gbsRepository;

    @MockitoBean
    private CgeRepository cgeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getBolsas_returns200WithList() throws Exception {
        Gbs g = new Gbs();
        g.setGBSREF("REF1");
        g.setGBSIMP(123.45);
        g.setGBSFOP(LocalDateTime.of(2026, 1, 22, 12, 0));
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "E1", "C1")).thenReturn(List.of(g));

        Cge cge = new Cge();
        cge.setCGECOD("C1");
        cge.setCGEDES("Desc Cge");
        when(cgeRepository.findById(new CgeId(1, "E1", "C1"))).thenReturn(Optional.of(cge));

        mockMvc.perform(get("/api/gbs/fetch-all/1/E1/C1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].cgecod").value("C1"))
            .andExpect(jsonPath("$[0].cgedes").value("Desc Cge"))
            .andExpect(jsonPath("$[0].gbsref").value("REF1"))
            .andExpect(jsonPath("$[0].gbsimp").value(123.45));
    }

    @Test
    void getBolsas_returns404WhenCgeMissing() throws Exception {
        when(gbsRepository.findByENTAndEJEAndCGECOD(2, "E2", "C2")).thenReturn(List.of());
        when(cgeRepository.findById(new CgeId(2, "E2", "C2"))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/gbs/fetch-all/2/E2/C2")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getBolsas_returnsBadRequestOnDataAccessException() throws Exception {
        when(gbsRepository.findByENTAndEJEAndCGECOD(anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        when(cgeRepository.findById(any())).thenReturn(Optional.of(new Cge()));

        mockMvc.perform(get("/api/gbs/fetch-all/1/E1/C1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateBolsa_returnsNoContentOnSuccess() throws Exception {
        GbsId id = new GbsId(1, "E1", "C1", "REF1");
        Gbs existing = new Gbs();
        existing.setGBSREF("REF1");
        when(gbsRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String, Object> payload = Map.of(
            "GBSIMP", 200.5,
            "GBSIUS", 5.0,
            "GBSICO", 10,
            "GBSFOP", "2026-01-22T12:00:00",
            "GBSIBG", 0.0
        );

        mockMvc.perform(patch("/api/gbs/updateBolsa/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(gbsRepository).save(Mockito.<Gbs>any());
    }

    @Test
    void updateBolsa_returnsNotFoundWhenMissing() throws Exception {
        GbsId id = new GbsId(9, "X", "Y", "Z");
        when(gbsRepository.findById(id)).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of(
            "GBSIMP", 1.0,
            "GBSIUS", 1.0,
            "GBSICO", 1.0,
            "GBSFOP", "2026-01-22T12:00:00",
            "GBSIBG", 0.0
        );

        mockMvc.perform(patch("/api/gbs/updateBolsa/9/X/Y/Z")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void updateBolsa_returnsBadRequestOnMissingFields() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("GBSIMP", 1.0);
        payload.put("GBSIUS", null);
        payload.put("GBSICO", 1.0);
        payload.put("GBSFOP", "2026-01-22T12:00:00");
        payload.put("GBSIBG", 0.0);

        mockMvc.perform(patch("/api/gbs/updateBolsa/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateBolsa_returnsBadRequestOnDataAccessException() throws Exception {
        GbsId id = new GbsId(1, "E1", "C1", "REF1");
        when(gbsRepository.findById(id)).thenThrow(new DataAccessResourceFailureException("DB down"));

        Map<String, Object> payload = Map.of(
            "GBSIMP", 1.0,
            "GBSIUS", 1.0,
            "GBSICO", 1.0,
            "GBSFOP", "2026-01-22T12:00:00",
            "GBSIBG", 0.0
        );

        mockMvc.perform(patch("/api/gbs/updateBolsa/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateGbsibg_returnsNoContentOnSuccess() throws Exception {
        GbsId id = new GbsId(1, "E1", "C1", "REF1");
        Gbs existing = new Gbs();
        existing.setGBSIMP(0.0);
        when(gbsRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String, Object> payload = Map.of(
            "GBSIMP", 50.75,
            "GBSIUS", 1.0,
            "GBSICO", 1.0,
            "GBSFOP", "2026-01-22T12:00:00"
        );

        mockMvc.perform(patch("/api/gbs/update-Importe/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(gbsRepository).save(any(Gbs.class));
    }

    @Test
    void updateGbsibg_returnsBadRequestWhenPayloadNull() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("GBSIMP", null);
        payload.put("GBSIUS", 1.0);
        payload.put("GBSICO", 1.0);
        payload.put("GBSFOP", "2026-01-22T12:00:00");

        mockMvc.perform(patch("/api/gbs/update-Importe/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateGbsibg_returnsNotFoundWhenBolsaMissing() throws Exception {
        when(gbsRepository.findById(any(GbsId.class))).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of(
            "GBSIMP", 25.0,
            "GBSIUS", 1.0,
            "GBSICO", 1.0,
            "GBSFOP", "2026-01-22T12:00:00"
        );

        mockMvc.perform(patch("/api/gbs/update-Importe/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void updateGbsibg_returnsBadRequestOnDataAccessException() throws Exception {
        when(gbsRepository.findById(any(GbsId.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        Map<String, Object> payload = Map.of(
            "GBSIMP", 25.0,
            "GBSIUS", 1.0,
            "GBSICO", 1.0,
            "GBSFOP", "2026-01-22T12:00:00"
        );

        mockMvc.perform(patch("/api/gbs/update-Importe/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void transpasar_returnsNoContentOnSuccess() throws Exception {
        GbsId id = new GbsId(1, "E1", "C1", "REF1");
        Gbs existing = new Gbs();
        when(gbsRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String, Object> payload = Map.of(
            "GBSIMP", 300.0,
            "GBSIBG", 75.0,
            "GBSIUS", 10.0,
            "GBSICO", 20.0,
            "GBSFOP", "2026-01-22T12:00:00"
        );

        mockMvc.perform(patch("/api/gbs/transpasar-bolsa/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(gbsRepository).save(any(Gbs.class));
    }

    @Test
    void transpasar_returnsBadRequestWhenFieldsNull() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("GBSIMP", null);
        payload.put("GBSIBG", 75.0);
        payload.put("GBSIUS", 10.0);
        payload.put("GBSICO", 20.0);
        payload.put("GBSFOP", "2026-01-22T12:00:00");

        mockMvc.perform(patch("/api/gbs/transpasar-bolsa/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void transpasar_returnsNotFoundWhenBolsaMissing() throws Exception {
        when(gbsRepository.findById(any(GbsId.class))).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of(
            "GBSIMP", 300.0,
            "GBSIBG", 75.0,
            "GBSIUS", 10.0,
            "GBSICO", 20.0,
            "GBSFOP", "2026-01-22T12:00:00"
        );

        mockMvc.perform(patch("/api/gbs/transpasar-bolsa/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void transpasar_returnsBadRequestOnDataAccessException() throws Exception {
        when(gbsRepository.findById(any(GbsId.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        Map<String, Object> payload = Map.of(
            "GBSIMP", 300.0,
            "GBSIBG", 75.0,
            "GBSIUS", 10.0,
            "GBSICO", 20.0,
            "GBSFOP", "2026-01-22T12:00:00"
        );

        mockMvc.perform(patch("/api/gbs/transpasar-bolsa/1/E1/C1/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void deleteBolsa_returnsNoContentOnSuccess() throws Exception {
        GbsId id = new GbsId(1, "E1", "C1", "REF1");
        Gbs bolsa = new Gbs();
        bolsa.setGBSIUT(0.0);
        when(gbsRepository.findById(id)).thenReturn(Optional.of(bolsa));

        mockMvc.perform(delete("/api/gbs/delete-bolsa/1/E1/C1/REF1"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(gbsRepository).deleteById(id);
    }

    @Test
    void deleteBolsa_returnsNotFoundWhenMissing() throws Exception {
        when(gbsRepository.findById(any(GbsId.class))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/gbs/delete-bolsa/1/E1/C1/REF1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteBolsa_returnsConflictWhenGbsiutNotZero() throws Exception {
        GbsId id = new GbsId(1, "E1", "C1", "REF1");
        Gbs bolsa = new Gbs();
        bolsa.setGBSIUT(50.0);
        when(gbsRepository.findById(id)).thenReturn(Optional.of(bolsa));

        mockMvc.perform(delete("/api/gbs/delete-bolsa/1/E1/C1/REF1"))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString("No se puede eliminar la aplicación")));

        verify(gbsRepository, never()).deleteById(any());
    }

    @Test
    void deleteBolsa_returnsBadRequestOnDataAccessException() throws Exception {
        when(gbsRepository.findById(any(GbsId.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(delete("/api/gbs/delete-bolsa/1/E1/C1/REF1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
void addBolsa_returnsCreatedOnSuccess() throws Exception {
    when(gbsRepository.findByENTAndEJEAndCGECODAndGBSREFAndGBSORGAndGBSFUNAndGBSECO(
            1, "E1", "C1", "REF1", "ORG1", "FUN1", "ECO1"))
        .thenReturn(Optional.empty());

    List<Map<String, Object>> payload = List.of(
        new HashMap<String, Object>() {{
            put("ENT", 1);
            put("EJE", "E1");
            put("CGECOD", "C1");
            put("GBSREF", "REF1");
            put("GBSOPE", "OPE1");
            put("GBSORG", "ORG1");
            put("GBSFUN", "FUN1");
            put("GBSECO", "ECO1");
        }}
    );

    mockMvc.perform(post("/api/gbs/add-Bolsa")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.savedBolsas", hasSize(1)))
        .andExpect(jsonPath("$.savedBolsas[0]").value("REF1"))
        .andExpect(jsonPath("$.unSavedBolsas", hasSize(0)));

    verify(gbsRepository).saveAll(argThat(list -> ((List<?>) list).size() == 1));
}

@Test
void addBolsa_skipsExistingByRef() throws Exception {
    Gbs existing = new Gbs();
    existing.setGBSREF("REF1");
    when(gbsRepository.findByENTAndEJEAndCGECODAndGBSREFAndGBSORGAndGBSFUNAndGBSECO(
            1, "E1", "C1", "REF1", "ORG1", "FUN1", "ECO1"))
        .thenReturn(Optional.of(existing));

    List<Map<String, Object>> payload = List.of(
        new HashMap<String, Object>() {{
            put("ENT", 1);
            put("EJE", "E1");
            put("CGECOD", "C1");
            put("GBSREF", "REF1");
            put("GBSOPE", "OPE1");
            put("GBSORG", "ORG1");
            put("GBSFUN", "FUN1");
            put("GBSECO", "ECO1");
        }}
    );

    mockMvc.perform(post("/api/gbs/add-Bolsa")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.savedBolsas", hasSize(0)))
        .andExpect(jsonPath("$.unSavedBolsas", hasSize(1)))
        .andExpect(jsonPath("$.unSavedBolsas[0]").value("REF1"));

    verify(gbsRepository).saveAll(argThat(list -> ((List<?>) list).isEmpty()));
}

@Test
void addBolsa_returnsBadRequestOnDataAccessException() throws Exception {
    when(gbsRepository.findByENTAndEJEAndCGECODAndGBSREFAndGBSORGAndGBSFUNAndGBSECO(
            anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenThrow(new DataAccessResourceFailureException("DB error"));

    List<Map<String, Object>> payload = List.of(
        new HashMap<String, Object>() {{
            put("ENT", 1);
            put("EJE", "E1");
            put("CGECOD", "C1");
            put("GBSREF", "REF1");
            put("GBSOPE", "OPE1");
            put("GBSORG", "ORG1");
            put("GBSFUN", "FUN1");
            put("GBSECO", "ECO1");
        }}
    );

    mockMvc.perform(post("/api/gbs/add-Bolsa")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Error :")));
}
}