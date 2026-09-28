package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.dto.CoaArtProjection;
import com.example.backend.dto.CoaSaveDto;
import com.example.backend.sqlserver2.model.Coa;
import com.example.backend.sqlserver2.model.CoaId;
import com.example.backend.sqlserver2.repository.CoaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CoaController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class CoaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CoaRepository coaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fetchArticulos_returns200WithList() throws Exception {
        when(coaRepository.findAllByENTAndEJEAndConnCONCOD(1, "E1", 100))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/coa/fetch-articulos/1/E1/100")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void fetchArticulos_returns404WhenEmpty() throws Exception {
        when(coaRepository.findAllByENTAndEJEAndConnCONCOD(1, "E1", 100))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/coa/fetch-articulos/1/E1/100"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchArticulos_returns500OnDataAccessException() throws Exception {
        when(coaRepository.findAllByENTAndEJEAndConnCONCOD(anyInt(), anyString(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/coa/fetch-articulos/1/E1/100"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateArticulo_returns204OnSuccess() throws Exception {
        Coa coa = new Coa();
        CoaId id = new CoaId(1, "E1", 100, "FAM", "SUB", "ART");
        when(coaRepository.findById(id)).thenReturn(Optional.of(coa));

        Map<String, Object> payload = Map.of("COAPRE", 150.0);

        mockMvc.perform(patch("/api/coa/update-articulo/1/E1/100/FAM/SUB/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(coaRepository).save(any(Coa.class));
    }

    @Test
    void updateArticulo_returns400WhenPayloadNull() throws Exception {
        mockMvc.perform(patch("/api/coa/update-articulo/1/E1/100/FAM/SUB/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of())))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateArticulo_returns404WhenNotFound() throws Exception {
        CoaId id = new CoaId(1, "E1", 100, "FAM", "SUB", "ART");
        when(coaRepository.findById(id)).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of("COAPRE", 150.0);

        mockMvc.perform(patch("/api/coa/update-articulo/1/E1/100/FAM/SUB/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void updateArticulo_returns500OnDataAccessException() throws Exception {
        CoaId id = new CoaId(1, "E1", 100, "FAM", "SUB", "ART");
        when(coaRepository.findById(id)).thenThrow(new DataAccessResourceFailureException("DB error"));

        Map<String, Object> payload = Map.of("COAPRE", 150.0);

        mockMvc.perform(patch("/api/coa/update-articulo/1/E1/100/FAM/SUB/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void deleteArticulo_returns204OnSuccess() throws Exception {
        CoaId id = new CoaId(1, "E1", 100, "FAM", "SUB", "ART");
        when(coaRepository.existsById(id)).thenReturn(true);

        mockMvc.perform(delete("/api/coa/delete-articulo/1/E1/100/FAM/SUB/ART"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(coaRepository).deleteById(id);
    }

    @Test
    void deleteArticulo_returns404WhenNotFound() throws Exception {
        CoaId id = new CoaId(1, "E1", 100, "FAM", "SUB", "ART");
        when(coaRepository.existsById(id)).thenReturn(false);

        mockMvc.perform(delete("/api/coa/delete-articulo/1/E1/100/FAM/SUB/ART"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteArticulo_returns500OnDataAccessException() throws Exception {
        CoaId id = new CoaId(1, "E1", 100, "FAM", "SUB", "ART");
        when(coaRepository.existsById(id)).thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(delete("/api/coa/delete-articulo/1/E1/100/FAM/SUB/ART"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void saveArticulos_returns201OnSuccess() throws Exception {
        when(coaRepository.findByENTAndCONCODAndAFACODAndASUCODAndARTCOD(
                1, 100, "FAM", "SUB", "ART"))
            .thenReturn(Collections.emptyList());

        CoaSaveDto dto = new CoaSaveDto();
        dto.ent = 1;
        dto.eje = "E1";
        dto.concod = 100;
        dto.afacod = "FAM";
        dto.asucod = "SUB";
        dto.artcod = "ART";
        dto.COAPRE = 100.0;

        mockMvc.perform(post("/api/coa/save-articulos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedNames").isArray())
            .andExpect(jsonPath("$.unsavedNames").isArray())
            .andExpect(jsonPath("$.savedNames.length()").value(1))
            .andExpect(jsonPath("$.unsavedNames.length()").value(0));

        verify(coaRepository).saveAll(any());
    }

    @Test
    void saveArticulos_skipsExistingArticles() throws Exception {
        Coa existing = new Coa();
        existing.setAFACOD("FAM");
        existing.setASUCOD("SUB");
        existing.setARTCOD("ART");

        when(coaRepository.findByENTAndCONCODAndAFACODAndASUCODAndARTCOD(
                1, 100, "FAM", "SUB", "ART"))
            .thenReturn(List.of(existing));

        CoaSaveDto dto = new CoaSaveDto();
        dto.ent = 1;
        dto.eje = "E1";
        dto.concod = 100;
        dto.afacod = "FAM";
        dto.asucod = "SUB";
        dto.artcod = "ART";
        dto.COAPRE = 100.0;

        mockMvc.perform(post("/api/coa/save-articulos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedNames").isArray())
            .andExpect(jsonPath("$.unsavedNames").isArray())
            .andExpect(jsonPath("$.savedNames.length()").value(0))
            .andExpect(jsonPath("$.unsavedNames.length()").value(1));

        verify(coaRepository).saveAll(any());
    }

    @Test
    void saveArticulos_returns500OnDataAccessException() throws Exception {
        when(coaRepository.findByENTAndCONCODAndAFACODAndASUCODAndARTCOD(
                anyInt(), anyInt(), anyString(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        CoaSaveDto dto = new CoaSaveDto();
        dto.ent = 1;
        dto.eje = "E1";
        dto.concod = 100;
        dto.afacod = "FAM";
        dto.asucod = "SUB";
        dto.artcod = "ART";
        dto.COAPRE = 100.0;

        mockMvc.perform(post("/api/coa/save-articulos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));

        verify(coaRepository, never()).saveAll(any());
    }

    @Test
    void saveArticulos_handlesEmptyList() throws Exception {
        mockMvc.perform(post("/api/coa/save-articulos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.emptyList())))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.savedNames").isArray())
            .andExpect(jsonPath("$.unsavedNames").isArray())
            .andExpect(jsonPath("$.savedNames.length()").value(0))
            .andExpect(jsonPath("$.unsavedNames.length()").value(0));

        verify(coaRepository).saveAll(any());
    }
}
