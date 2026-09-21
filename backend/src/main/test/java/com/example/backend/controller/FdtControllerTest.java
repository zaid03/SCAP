package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Fdt;
import com.example.backend.sqlserver2.repository.FdtRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FdtController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class FdtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FdtRepository fdtRepository;

    @Test
    void getFde_returnsListOfDtos() throws Exception {
        Fdt f = new Fdt();
        f.setFDTARE("10");
        f.setFDTORG("ORG1");
        f.setFDTFUN("FUN1");
        f.setFDTECO("ECO1");
        f.setFDTBSE(55.5);
        f.setFDTPRE(5.0);
        f.setFDTDTO(10.0);
        f.setFDTTXT("Texto");

        when(fdtRepository.findByENTAndEJEAndFACNUM(1, "E1", 123)).thenReturn(List.of(f));

        mockMvc.perform(get("/api/fdt/1/E1/123")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(content().string(containsString("ORG1")))
            .andExpect(content().string(containsString("55.5")));
    }

    @Test
    void getFde_returnsNotFoundWhenEmpty() throws Exception {
        when(fdtRepository.findByENTAndEJEAndFACNUM(2, "E2", 1)).thenReturn(List.of());

        mockMvc.perform(get("/api/fdt/2/E2/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getFde_returns500OnDataAccessException() throws Exception {
        when(fdtRepository.findByENTAndEJEAndFACNUM(anyInt(), anyString(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/fdt/1/E1/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }
}