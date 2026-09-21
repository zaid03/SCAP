package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.FacId;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.FdeId;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.example.backend.sqlserver2.repository.FacRepository;
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
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FdeController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class FdeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FdeRepository fdeRepository;

    @MockitoBean
    private FacRepository facRepository;

    @Test
    void getFde_returnsListWhenFound() throws Exception {
        Fde f = new Fde();
        f.setFDEREF("REF1");
        f.setFDEECO("ECO1");
        f.setFDEIMP(55.5);
        f.setFDEDIF(5.0);
        when(fdeRepository.findByENTAndEJEAndFACNUM(1, "E1", 123)).thenReturn(List.of(f));

        mockMvc.perform(get("/api/fde/1/E1/123")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(content().string(containsString("REF1")))
            .andExpect(content().string(containsString("ECO1")))
            .andExpect(content().string(containsString("55.5")));
    }

    @Test
    void getFde_returns404WhenEmpty() throws Exception {
        when(fdeRepository.findByENTAndEJEAndFACNUM(2, "E2", 1)).thenReturn(List.of());

        mockMvc.perform(get("/api/fde/2/E2/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getFde_returns400OnDataAccessException() throws Exception {
        when(fdeRepository.findByENTAndEJEAndFACNUM(anyInt(), anyString(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/fde/1/E1/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateDiferencias_returns204OnSuccess() throws Exception {
        FdeId fdeId = new FdeId(1, "E1", 100, "REF1");
        Fde fde = new Fde();
        fde.setFDEREF("REF1");
        fde.setFDEDIF(0.0);
        
        FacId facId = new FacId(1, "E1", 100);
        Fac fac = new Fac();
        fac.setFACIDI(0.0);

        when(fdeRepository.findById(any(FdeId.class))).thenReturn(Optional.of(fde));
        when(facRepository.findById(any(FacId.class))).thenReturn(Optional.of(fac));

        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(fdeRepository).save(any(Fde.class));
        verify(facRepository).save(any(Fac.class));
    }

    @Test
    void updateDiferencias_returns400WhenFDEDIFNull() throws Exception {
        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", null);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("faltan datos obligatorios")));
    }

    @Test
    void updateDiferencias_returns400WhenFACIDINull() throws Exception {
        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", null);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("faltan datos obligatorios")));
    }

    @Test
    void updateDiferencias_returns404WhenFdeNotFound() throws Exception {
        when(fdeRepository.findById(any(FdeId.class))).thenReturn(Optional.empty());

        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(fdeRepository, never()).save(any());
        verify(facRepository, never()).save(any());
    }

    @Test
    void updateDiferencias_returns404WhenFacNotFound() throws Exception {
        FdeId fdeId = new FdeId(1, "E1", 100, "REF1");
        Fde fde = new Fde();
        fde.setFDEREF("REF1");

        when(fdeRepository.findById(any(FdeId.class))).thenReturn(Optional.of(fde));
        when(facRepository.findById(any(FacId.class))).thenReturn(Optional.empty());

        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(fdeRepository).save(any(Fde.class));
        verify(facRepository, never()).save(any());
    }

    @Test
    void updateDiferencias_returns400OnDataAccessException() throws Exception {
        when(fdeRepository.findById(any(FdeId.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }
}