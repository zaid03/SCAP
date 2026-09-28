package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver1.repository.AytRepository;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AytController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class AytControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AytRepository aytRepository;

    @Test
    void fetchAyt_returns200WithList() throws Exception {
        when(aytRepository.findByENTCOD(1)).thenReturn(List.of(Mockito.mock(com.example.backend.sqlserver1.model.Ayt.class)));

        mockMvc.perform(get("/api/ayt/fetch-all/1").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void fetchAyt_returns404WhenEmpty() throws Exception {
        when(aytRepository.findByENTCOD(2)).thenReturn(List.of());

        mockMvc.perform(get("/api/ayt/fetch-all/2"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchAyt_returns404WhenNull() throws Exception {
        when(aytRepository.findByENTCOD(3)).thenReturn(null);

        mockMvc.perform(get("/api/ayt/fetch-all/3"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchAyt_returns400OnDataAccessException() throws Exception {
        when(aytRepository.findByENTCOD(anyInt())).thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/ayt/fetch-all/1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error:")));
    }
}