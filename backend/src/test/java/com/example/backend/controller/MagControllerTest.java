package com.example.backend.controller;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.config.TestExceptionHandler;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.magcodOnly;
import com.example.backend.sqlserver2.repository.MagRepository;

@WebMvcTest(controllers = MagController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class MagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MagRepository magRepository;

    @Test
    void shouldReturnNotFoundWhenNoMag() throws Exception {
        when(magRepository.findByENTAndDEPCOD(2, "X")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mag/fetch-almacen-nombre/2/X")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("No resultado"));

        verify(magRepository).findByENTAndDEPCOD(2, "X");
    }

    @Test
    void shouldReturn500OnDataAccessException() throws Exception {
        when(magRepository.findByENTAndDEPCOD(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/mag/fetch-almacen-nombre/1/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }

    @Test
    void shouldReturnAlmacenName_whenFound() throws Exception {
        magcodOnly mag = mock(magcodOnly.class);

        when(mag.getMAGCOD()).thenReturn(7);
        when(mag.getMAGNOM()).thenReturn("Almacen X");

        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenReturn(Optional.of(mag));

        mockMvc.perform(get("/api/mag/fetch-almacen-nombre/1/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.magcod").value(7))
            .andExpect(jsonPath("$.magnom").value("Almacen X"));

        verify(magRepository).findByENTAndDEPCOD(1, "D1");
        verify(mag).getMAGCOD();
        verify(mag).getMAGNOM();
    }
}