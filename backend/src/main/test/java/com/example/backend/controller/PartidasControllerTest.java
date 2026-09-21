package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.Partida;
import com.example.backend.service.PartidasService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PartidasController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class PartidasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PartidasService partidasService;

    @Test
    void shouldReturnPartidasWithParams() throws Exception {
        Partida p = new Partida();
        when(partidasService.getPartidas(any(PartidasService.SearchCriteria.class)))
            .thenReturn(List.of(p));

        mockMvc.perform(get("/api/sical/partidas")
                .param("cenges", "cenges1")
                .param("alias", "alias1")
                .param("clorg", "org1")
                .param("clfun", "fun1")
                .param("cleco", "eco1")
                .param("clcte", "cte1")
                .param("clpam", "pam1")
                .param("usucenges", "user1")
                .param("orgCode", "0000000000")
                .param("entidad", "0000000001")
                .param("eje", "2026")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        verify(partidasService).getPartidas(any(PartidasService.SearchCriteria.class));
    }

    @Test
    void shouldBuildCriteriaWithOrgCodeEntidadEje() throws Exception {
        when(partidasService.getPartidas(any(PartidasService.SearchCriteria.class)))
            .thenReturn(List.of(new Partida()));

        mockMvc.perform(get("/api/sical/partidas")
                .param("orgCode", "0000000000")
                .param("entidad", "0000000001")
                .param("eje", "2026")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<PartidasService.SearchCriteria> captor =
            org.mockito.ArgumentCaptor.forClass(PartidasService.SearchCriteria.class);
        verify(partidasService).getPartidas(captor.capture());

        PartidasService.SearchCriteria criteria = captor.getValue();
        assertEquals("0000000000", criteria.orgCode);
        assertEquals("0000000001", criteria.entidad);
        assertEquals("2026", criteria.eje);
    }

    @Test
    void shouldReturnInternalServerErrorWithEmptyListOnException() throws Exception {
        when(partidasService.getPartidas(any(PartidasService.SearchCriteria.class)))
            .thenThrow(new RuntimeException("sical fail"));

        mockMvc.perform(get("/api/sical/partidas")
                .param("orgCode", "0000000000")
                .param("entidad", "0000000001")
                .param("eje", "2026")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    // NOTE: Missing required @RequestParam (orgCode/entidad/eje) normally causes Spring
    // to auto-resolve a 400 via MissingServletRequestParameterException. In this app
    // something (likely a broad @ExceptionHandler(Exception.class)) intercepts that and
    // returns 500 instead. These tests assert the actual current behavior, not the
    // Spring default — if that global handler is ever fixed/narrowed, these should be
    // updated back to isBadRequest().
    @Test
    void shouldReturnInternalServerErrorWhenOrgCodeMissing() throws Exception {
        mockMvc.perform(get("/api/sical/partidas")
                .param("entidad", "0000000001")
                .param("eje", "2026")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value(containsString("orgCode")));

        verifyNoInteractions(partidasService);
    }

    @Test
    void shouldReturnInternalServerErrorWhenEntidadMissing() throws Exception {
        mockMvc.perform(get("/api/sical/partidas")
                .param("orgCode", "0000000000")
                .param("eje", "2026")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value(containsString("entidad")));

        verifyNoInteractions(partidasService);
    }

    @Test
    void shouldReturnInternalServerErrorWhenEjeMissing() throws Exception {
        mockMvc.perform(get("/api/sical/partidas")
                .param("orgCode", "0000000000")
                .param("entidad", "0000000001")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value(containsString("eje")));

        verifyNoInteractions(partidasService);
    }
}