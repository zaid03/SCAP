package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Per;
import com.example.backend.sqlserver2.repository.PerRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = PerController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class PerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PerRepository perRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFetchAll_returns200WithList() throws Exception {
        Per p = new Per();
        p.setPERCOD("X1");
        p.setPERNOM("Name");
        when(perRepository.findAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/Per/fetch-all")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldFetchAll_returns404WhenEmpty() throws Exception {
        when(perRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/Per/fetch-all")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void shouldFetchAll_returns500OnDataAccessException() throws Exception {
        when(perRepository.findAll()).thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/Per/fetch-all")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error :")));
    }

    @Test
    void shouldSearchCodNom_returns200WithResults() throws Exception {
        Per p = new Per();
        p.setPERCOD("C1");
        when(perRepository.findByPERCODOrPERNOMContaining("C1", "C1")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/Per/search-cod-nom/C1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldSearchCodNom_returns404WhenEmpty() throws Exception {
        when(perRepository.findByPERCODOrPERNOMContaining("Z", "Z")).thenReturn(List.of());

        mockMvc.perform(get("/api/Per/search-cod-nom/Z")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void shouldSearchCodNom_returns500OnDataAccessException() throws Exception {
        when(perRepository.findByPERCODOrPERNOMContaining("C1", "C1"))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/Per/search-cod-nom/C1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error :")));
    }

    @Test
    void shouldSearchNom_returns200WithResults() throws Exception {
        Per p = new Per();
        p.setPERNOM("Persona1");
        when(perRepository.findByPERNOMContaining("Persona")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/Per/search-nom/Persona")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldSearchNom_returns404WhenEmpty() throws Exception {
        when(perRepository.findByPERNOMContaining("Z")).thenReturn(List.of());

        mockMvc.perform(get("/api/Per/search-nom/Z")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void shouldSearchNom_returns500OnDataAccessException() throws Exception {
        when(perRepository.findByPERNOMContaining("Persona"))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/Per/search-nom/Persona")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error :")));
    }

    @Test
    void shouldInsertPersona_returns201() throws Exception {
        Map<String,Object> payload = Map.of(
            "PERCOD", "P1",
            "PERNOM", "Persona1",
            "PERCOE", "COE",
            "PERTEL", "111",
            "PERTMO", "222",
            "PERCAR", "ROLE",
            "PEROBS", "OBS"
        );
        when(perRepository.existsById("P1")).thenReturn(false);
        when(perRepository.save(any(Per.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/api/Per/Insert-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(perRepository).save(any(Per.class));
    }

    @Test
    void shouldInsertPersona_returns400WhenMissingFields() throws Exception {
        Map<String,Object> payload = Map.of("PERCOD", "P1"); 

        mockMvc.perform(post("/api/Per/Insert-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios"));
    }

    @Test
    void shouldInsertPersona_returns400WhenExists() throws Exception {
        Map<String,Object> payload = Map.of("PERCOD", "P1", "PERNOM", "Name");
        when(perRepository.existsById("P1")).thenReturn(true);

        mockMvc.perform(post("/api/Per/Insert-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Esta persona ya existe"));
    }

    @Test
    void shouldInsertPersona_returns400OnDataAccessException() throws Exception {
        Map<String,Object> payload = Map.of(
            "PERCOD", "P1",
            "PERNOM", "Persona1",
            "PERCOE", "COE",
            "PERTEL", "111",
            "PERTMO", "222",
            "PERCAR", "ROLE",
            "PEROBS", "OBS"
        );
        when(perRepository.existsById("P1")).thenReturn(false);
        when(perRepository.save(any(Per.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(post("/api/Per/Insert-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error :")));
    }

    @Test
    void shouldUpdatePersona_returns204() throws Exception {
        Map<String,Object> payload = Map.of("PERCOD", "P2", "PERNOM", "Updated");
        Per existing = new Per();
        existing.setPERCOD("P2");
        when(perRepository.findById("P2")).thenReturn(Optional.of(existing));

        mockMvc.perform(patch("/api/Per/update-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(perRepository).save(any(Per.class));
    }

    @Test
    void shouldUpdatePersona_returns404WhenNotFound() throws Exception {
        Map<String,Object> payload = Map.of("PERCOD", "NX", "PERNOM", "X");
        when(perRepository.findById("NX")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/Per/update-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void shouldUpdatePersona_returns400WhenMissingFields() throws Exception {
        Map<String,Object> payload = Map.of("PERCOD", "P2"); 

        mockMvc.perform(patch("/api/Per/update-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios"));
    }

    @Test
    void shouldUpdatePersona_returns400OnDataAccessException() throws Exception {
        Map<String,Object> payload = Map.of("PERCOD", "P2", "PERNOM", "Updated");
        Per existing = new Per();
        existing.setPERCOD("P2");
        when(perRepository.findById("P2")).thenReturn(Optional.of(existing));
        when(perRepository.save(any(Per.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(patch("/api/Per/update-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error :")));
    }
}