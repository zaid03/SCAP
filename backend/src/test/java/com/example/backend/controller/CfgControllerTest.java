package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Cfg;
import com.example.backend.sqlserver2.repository.CfgRepository;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CfgController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class CfgControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CfgRepository cfgRepository;

    @Test
    void getEJE_returns200WithList() throws Exception {
        Cfg c = new Cfg();
        when(cfgRepository.findEjeByENTAndCFGEST(1, 0)).thenReturn(List.of(c));

        mockMvc.perform(get("/api/cfg/by-ent/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getEJE_returns404WhenEmpty() throws Exception {
        when(cfgRepository.findEjeByENTAndCFGEST(2, 0)).thenReturn(List.of());

        mockMvc.perform(get("/api/cfg/by-ent/2"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getEJE_returns500OnDataAccessException() throws Exception {
        when(cfgRepository.findEjeByENTAndCFGEST(anyInt(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/cfg/by-ent/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("DB")));
    }

    @Test
    void fetchAll_returns200WithList() throws Exception {
        Cfg c = new Cfg();
        when(cfgRepository.findByENT(1)).thenReturn(List.of(c));

        mockMvc.perform(get("/api/cfg/fetch-Eje/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void fetchAll_returns404WhenEmpty() throws Exception {
        when(cfgRepository.findByENT(2)).thenReturn(List.of());

        mockMvc.perform(get("/api/cfg/fetch-Eje/2"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchAll_returns500OnDataAccessException() throws Exception {
        when(cfgRepository.findByENT(anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/cfg/fetch-Eje/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void searchEjercicios_returns200WithList() throws Exception {
        Cfg c = new Cfg();
        when(cfgRepository.findByENTAndEJE(1, "2026")).thenReturn(List.of(c));

        mockMvc.perform(get("/api/cfg/search-Eje/1/2026")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void searchEjercicios_returns404WhenEmpty() throws Exception {
        when(cfgRepository.findByENTAndEJE(1, "X")).thenReturn(List.of());

        mockMvc.perform(get("/api/cfg/search-Eje/1/X"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchEjercicios_returns500OnDataAccessException() throws Exception {
        when(cfgRepository.findByENTAndEJE(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/cfg/search-Eje/1/2026"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }
}