package com.example.backend.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.config.TestExceptionHandler;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.articulosPorAlbaran;
import com.example.backend.sqlserver2.model.Ade;
import com.example.backend.sqlserver2.repository.AdeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AdeController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
class AdeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdeRepository adeRepository;

    @Test
    void fetchArticulos_returns200WithResults() throws Exception {
        articulosPorAlbaran articulo = new articulosPorAlbaran() {
            public String getADENUM() { return "1"; }
            public String getAFACOD() { return "AFA"; }
            public String getASUCOD() { return "ASU"; }
            public String getARTCOD() { return "ART"; }
            public Double getADEPRE() { return 10.0; }
            public Double getADEUNI() { return 2.0; }
            public String getADEREF() { return "REF"; }
            public String getADEOBS() { return "OBS"; }
        };

        when(adeRepository.findByENTAndALBNUM(1, -65987))
            .thenReturn(List.of(articulo));

        mockMvc.perform(get("/api/ade/fetch-articulos/1/-65987"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void fetchArticulos_returns404WhenEmpty() throws Exception {
        when(adeRepository.findByENTAndALBNUM(1, -65987))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/ade/fetch-articulos/1/-65987"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchArticulos_returns400OnDataAccessException() throws Exception {
        when(adeRepository.findByENTAndALBNUM(1, -65987))
            .thenThrow(new DataAccessResourceFailureException("Database unavailable"));

        mockMvc.perform(get("/api/ade/fetch-articulos/1/-65987"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void articuloUpdate_returns204AndUpdatesArticulo() throws Exception {
        Ade articulo = new Ade();
        articulo.setADEREF("OLD-REF");
        articulo.setADEOBS("Old observation");

        when(adeRepository.findById(any()))
            .thenReturn(Optional.of(articulo));

        AdeController.updateArticulo payload =
            new AdeController.updateArticulo("NEW-REF", "New observation");

        mockMvc.perform(patch("/api/ade/update-articulo/1/-65987/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isNoContent());

        verify(adeRepository).save(articulo);

        org.junit.jupiter.api.Assertions.assertEquals(
            "NEW-REF",
            articulo.getADEREF()
        );
        org.junit.jupiter.api.Assertions.assertEquals(
            "New observation",
            articulo.getADEOBS()
        );
    }

    @Test
    void articuloUpdate_returns500WhenPayloadIsNull() throws Exception {
        mockMvc.perform(patch("/api/ade/update-articulo/1/-65987/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void articuloUpdate_returns404WhenArticuloDoesNotExist() throws Exception {
        when(adeRepository.findById(any()))
            .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/ade/update-articulo/1/-65987/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ADEREF": "NEW-REF",
                      "ADEOBS": "New observation"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void articuloUpdate_returns400WhenFindingArticuloFails() throws Exception {
        when(adeRepository.findById(any()))
            .thenThrow(new DataAccessResourceFailureException("Database unavailable"));

        mockMvc.perform(patch("/api/ade/update-articulo/1/-65987/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ADEREF": "NEW-REF",
                      "ADEOBS": "New observation"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void articuloUpdate_returns400WhenSavingArticuloFails() throws Exception {
        Ade articulo = new Ade();

        when(adeRepository.findById(any()))
            .thenReturn(Optional.of(articulo));

        when(adeRepository.save(any(Ade.class)))
            .thenThrow(new DataAccessResourceFailureException("Database unavailable"));

        mockMvc.perform(patch("/api/ade/update-articulo/1/-65987/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ADEREF": "NEW-REF",
                      "ADEOBS": "New observation"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }
}