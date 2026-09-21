package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.dto.ArtAsuContratoProjection;
import com.example.backend.dto.ArtNameProjection;
import com.example.backend.sqlserver2.repository.AfaRepository;
import com.example.backend.sqlserver2.repository.ArtRepository;
import com.example.backend.sqlserver2.repository.AsuRepository;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArtController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class ArtControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ArtRepository artRepository;
    @MockitoBean private AfaRepository afaRepository;
    @MockitoBean private AsuRepository asuRepository;

    @Test
    void getArtName_returnsListOr404() throws Exception {
        ArtNameProjection projection = new ArtNameProjection() {
            @Override
            public String getARTDES() {
                return "Test description";
            }
        };

        when(artRepository.findByENTAndAFACODAndASUCODAndARTCOD(1, "AF", "ASU", "ART"))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/art/art-name/1/AF/ASU/ART").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        when(artRepository.findByENTAndAFACODAndASUCODAndARTCOD(2, "AF", "ASU", "NX")).thenReturn(List.of());
        mockMvc.perform(get("/api/art/art-name/2/AF/ASU/NX"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getArtName_returnsBadRequestOnException() throws Exception {
        when(artRepository.findByENTAndAFACODAndASUCODAndARTCOD(anyInt(), anyString(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/art/art-name/1/AF/ASU/ART"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void deleteFamilia_conflict_whenArticlesExist() throws Exception {
        when(artRepository.countByENTAndAFACOD(1, "AF")).thenReturn(5L);

        mockMvc.perform(delete("/api/art/delete-familia/1/AF"))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString("No se puede borrar una familia")));
    }

    @Test
    void deleteFamilia_notFound_whenNoRemoved_and_noException() throws Exception {
        when(artRepository.countByENTAndAFACOD(1, "AF")).thenReturn(0L);
        when(afaRepository.deleteByENTAndAFACOD(1, "AF")).thenReturn(0);

        mockMvc.perform(delete("/api/art/delete-familia/1/AF"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteFamilia_noContent_onSuccess() throws Exception {
        when(artRepository.countByENTAndAFACOD(1, "AF")).thenReturn(0L);
        when(afaRepository.deleteByENTAndAFACOD(1, "AF")).thenReturn(1);

        mockMvc.perform(delete("/api/art/delete-familia/1/AF"))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteFamilia_returnsBadRequestOnException() throws Exception {
        when(artRepository.countByENTAndAFACOD(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(delete("/api/art/delete-familia/1/AF"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void deleteSubFamilia_conflict_whenArticlesExist() throws Exception {
        when(artRepository.countByENTAndASUCOD(1, "ASU")).thenReturn(2L);

        mockMvc.perform(delete("/api/art/delete-sub-familia/1/AF/ASU"))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString("No se puede borrar una subfamilia")));
    }

    @Test
    void deleteSubFamilia_notFound_and_noContent() throws Exception {
        when(artRepository.countByENTAndASUCOD(1, "ASU")).thenReturn(0L);
        when(asuRepository.deleteByENTAndAFACODAndASUCOD(1, "AF", "ASU")).thenReturn(0);

        mockMvc.perform(delete("/api/art/delete-sub-familia/1/AF/ASU"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        when(asuRepository.deleteByENTAndAFACODAndASUCOD(1, "AF", "ASU")).thenReturn(1);
        mockMvc.perform(delete("/api/art/delete-sub-familia/1/AF/ASU"))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteSubFamilia_returnsBadRequestOnException() throws Exception {
        when(artRepository.countByENTAndASUCOD(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(delete("/api/art/delete-sub-familia/1/AF/ASU"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void getArticulosContratos_returns200WithList() throws Exception {
        ArtAsuContratoProjection p = new ArtAsuContratoProjection() {
            @Override public Integer getAFACOD() { return 1; }
            @Override public Integer getASUCOD() { return 2; }
            @Override public Integer getARTCOD() { return 3; }
            @Override public String getARTDES() { return "Desc"; }
        };
        when(artRepository.findDistinctByENTAndAsuASUECO(1, "C1")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/art/art-cont/1/C1").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].artdes").value("Desc"));
    }

    @Test
    void getArticulosContratos_returnsNotFoundWhenEmpty() throws Exception {
        when(artRepository.findDistinctByENTAndAsuASUECO(1, "C1")).thenReturn(List.of());

        mockMvc.perform(get("/api/art/art-cont/1/C1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("No hay artículos para la económica indicada"));

        verify(artRepository).findDistinctByENTAndAsuASUECO(1, "C1");
    }

    @Test
    void getArticulosContratos_returnsBadRequestOnException() throws Exception {
        when(artRepository.findDistinctByENTAndAsuASUECO(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/art/art-cont/1/C1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void searchArticulosContratosNum_returns200WithList() throws Exception {
        ArtAsuContratoProjection p = new ArtAsuContratoProjection() {
            @Override public Integer getAFACOD() { return 1; }
            @Override public Integer getASUCOD() { return 2; }
            @Override public Integer getARTCOD() { return 3; }
            @Override public String getARTDES() { return "Desc"; }
        };
        when(artRepository.findDistinctByENTAndAsuASUECOAndAFACODOrENTAndAsuASUECOAndASUCOD(1, "C1", "AF", 1, "C1", "AF"))
            .thenReturn(List.of(p));

        mockMvc.perform(get("/api/art/search-art-cont/1/C1/AF").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].artdes").value("Desc"));
    }

    @Test
    void searchArticulosContratosNum_returnsNotFoundWhenEmpty() throws Exception {
        when(artRepository.findDistinctByENTAndAsuASUECOAndAFACODOrENTAndAsuASUECOAndASUCOD(1, "C1", "AF", 1, "C1", "AF"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/art/search-art-cont/1/C1/AF"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(artRepository).findDistinctByENTAndAsuASUECOAndAFACODOrENTAndAsuASUECOAndASUCOD(1, "C1", "AF", 1, "C1", "AF");
    }

    @Test
    void searchArticulosContratosNum_returnsBadRequestOnException() throws Exception {
        when(artRepository.findDistinctByENTAndAsuASUECOAndAFACODOrENTAndAsuASUECOAndASUCOD(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/art/search-art-cont/1/C1/AF"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void searchArticulosContratosDes_returns200WithList() throws Exception {
        ArtAsuContratoProjection p = new ArtAsuContratoProjection() {
            @Override public Integer getAFACOD() { return 1; }
            @Override public Integer getASUCOD() { return 2; }
            @Override public Integer getARTCOD() { return 3; }
            @Override public String getARTDES() { return "Desc"; }
        };
        when(artRepository.findDistinctByENTAndAsuASUECOAndARTDESContaining(1, "C1", "bolt"))
            .thenReturn(List.of(p));

        mockMvc.perform(get("/api/art/search-art-cont-des/1/C1/bolt").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].artdes").value("Desc"));
    }

    @Test
    void searchArticulosContratosDes_returnsNotFoundWhenEmpty() throws Exception {
        when(artRepository.findDistinctByENTAndAsuASUECOAndARTDESContaining(1, "C1", "bolt"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/art/search-art-cont-des/1/C1/bolt"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(artRepository).findDistinctByENTAndAsuASUECOAndARTDESContaining(1, "C1", "bolt");
    }

    @Test
    void searchArticulosContratosDes_returnsBadRequestOnException() throws Exception {
        when(artRepository.findDistinctByENTAndAsuASUECOAndARTDESContaining(anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/art/search-art-cont-des/1/C1/bolt"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }
}