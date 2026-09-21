package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.Operaciones;
import com.example.backend.service.OperacionesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OperacionesController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class OperacionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OperacionesService operacionesService;

    @Test
    void shouldReturnOperacionesWithParams() throws Exception {

        Operaciones o = new Operaciones();

        when(operacionesService.getOperaciones(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any()))
                .thenReturn(List.of(o));

        mockMvc.perform(get("/api/sical/operaciones")
                        .param("orgCode", "0000000000")
                        .param("entidad", "0000000001")
                        .param("eje", "2026")
                        .param("numeroOperDesde", "1")
                        .param("numeroOperHasta", "2")
                        .param("codigoOperacion", "cod")
                        .param("organica", "org")
                        .param("funcional", "fun")
                        .param("economica", "eco")
                        .param("expediente", "exp")
                        .param("grupoApunte", "grp")
                        .param("oficina", "ofi")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(operacionesService).getOperaciones(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void shouldReturnEmptyListWhenNoResults() throws Exception {

        when(operacionesService.getOperaciones(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sical/operaciones")
                        .param("orgCode", "0000000000")
                        .param("entidad", "0000000001")
                        .param("eje", "2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(operacionesService).getOperaciones(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void shouldReturn500WhenServiceThrowsException() throws Exception {

        when(operacionesService.getOperaciones(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("sical fail"));

        mockMvc.perform(get("/api/sical/operaciones")
                        .param("orgCode", "0000000000")
                        .param("entidad", "0000000001")
                        .param("eje", "2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("sical fail"));

        verify(operacionesService).getOperaciones(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void shouldReturnExceptionMessage() throws Exception {

        when(operacionesService.getOperaciones(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any()))
                .thenThrow(new Exception("boom"));

        mockMvc.perform(get("/api/sical/operaciones")
                        .param("orgCode", "0000000000")
                        .param("entidad", "0000000001")
                        .param("eje", "2026"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("boom"));
    }
}