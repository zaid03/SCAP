package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Mta;
import com.example.backend.sqlserver2.repository.MtaRepository;
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
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = MtaController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class MtaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MtaRepository mtaRepository;

    @Test
    void shouldReturnAllMtaForEnt() throws Exception {
        Mta m = new Mta();
        m.setMTACOD(10);
        when(mtaRepository.findByENT(1)).thenReturn(List.of(m));

        mockMvc.perform(get("/api/mta/all-mta/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        verify(mtaRepository).findByENT(1);
    }

    @Test
    void shouldFilterAlmacenaje_returns404WhenEmpty() throws Exception {
        when(mtaRepository.findByENTAndMTACOD(2, 99)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/mta/mta-filter/2/99")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(mtaRepository).findByENTAndMTACOD(2, 99);
    }

    @Test
    void shouldFilterAlmacenaje_returns200WithResults() throws Exception {
        Mta m = new Mta();
        m.setMTACOD(5);
        when(mtaRepository.findByENTAndMTACOD(3, 5)).thenReturn(List.of(m));

        mockMvc.perform(get("/api/mta/mta-filter/3/5")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        verify(mtaRepository).findByENTAndMTACOD(3, 5);
    }

    @Test
    void shouldFilterAlmacenaje_returns500OnDataAccessException() throws Exception {
        when(mtaRepository.findByENTAndMTACOD(anyInt(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/mta/mta-filter/1/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }
}