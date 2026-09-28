package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.example.backend.service.SicalService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(controllers = SicalController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class SicalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SicalService sicalService;

    @Test
    void getTerceros_onlyRequiredParams_returnsEmptyList() throws Exception {
        when(sicalService.getTerceros(null, null, null, "ORG1", "ENT1", "2024"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/sical/terceros")
                .param("orgCode", "ORG1")
                .param("entidad", "ENT1")
                .param("eje", "2024")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));

        verify(sicalService).getTerceros(null, null, null, "ORG1", "ENT1", "2024");
    }

    @Test
    void getTerceros_allParams_forwardsToService_andReturnsList() throws Exception {
        @SuppressWarnings("unchecked")
        List<Object> dummy = (List<Object>)(List<?>) List.of(Map.of("nif", "111X", "nombre", "Alice", "codigo", "123"));
        when(sicalService.getTerceros("111X", "Alice", "123", "ORG1", "ENT1", "2024"))
            .thenReturn((List) dummy);

        mockMvc.perform(get("/api/sical/terceros")
                .param("nif", "111X")
                .param("nom", "Alice")
                .param("codigo", "123")
                .param("orgCode", "ORG1")
                .param("entidad", "ENT1")
                .param("eje", "2024")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(sicalService).getTerceros("111X", "Alice", "123", "ORG1", "ENT1", "2024");
    }

    @Test
    void getTerceros_serviceThrows_returns500() throws Exception {
        when(sicalService.getTerceros(any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("SICAL down"));

        mockMvc.perform(get("/api/sical/terceros")
                .param("orgCode", "ORG1")
                .param("entidad", "ENT1")
                .param("eje", "2024")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }
}