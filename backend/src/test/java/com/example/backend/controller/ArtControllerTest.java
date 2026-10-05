package com.example.backend.controller;

import com.example.backend.dto.AnaliticaArticulosProjectin.AfaProjection;
import com.example.backend.dto.AnaliticaArticulosProjectin;
import com.example.backend.dto.ArticleProjection;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.dto.ArtAsuContratoProjection;
import com.example.backend.dto.ArtNameProjection;
import com.example.backend.service.ExistenciasSearch;
import com.example.backend.sqlserver2.model.Art;
import com.example.backend.sqlserver2.repository.AfaRepository;
import com.example.backend.sqlserver2.repository.ArtRepository;
import com.example.backend.sqlserver2.repository.AsuRepository;
import com.example.backend.sqlserver2.repository.MeaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
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
    @MockitoBean private MeaRepository meaRepository;             
    @MockitoBean private ExistenciasSearch existenciasSearch;     

    private ArticleProjection articleProjection() {
        return new ArticleProjection() {
            @Override public String getAFACOD() { return "AF"; }
            @Override public String getAfa_AFADES() { return "Family"; }
            @Override public String getASUCOD() { return "ASU"; }
            @Override public String getAsu_ASUDES() { return "Subfamily"; }
            @Override public String getARTCOD() { return "ART"; }
            @Override public String getARTDES() { return "Article"; }
            @Override public String getARTREF() { return "REF"; }
            @Override public Integer getARTBLO() { return 0; }
            @Override public Double getARTUNI() { return 1.0; }
            @Override public Double getARTSOL() { return 2.0; }
            @Override public Double getARTREC() { return 3.0; }
            @Override public String getAun_AUNDES() { return "Unit"; }
            @Override public Double getARTUCO() { return 4.0; }
            @Override public Double getARTUEM() { return 5.0; }
            @Override public Double getARTPMI() { return 6.0; }
            @Override public Double getARTPMP() { return 7.0; }
            @Override public Double getARTMIN() { return 8.0; }
            @Override public Double getARTOPT() { return 9.0; }
        };
    }

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

    @Test
    void fetchAnaliticaArticulos_returns200WithList() throws Exception {
        AnaliticaArticulosProjectin projection = new AnaliticaArticulosProjectin() {
            @Override
            public String getAFACOD() {
                return "AF";
            }

            @Override
            public String getARTCOD() {
                return "ART";
            }

            @Override
            public String getARTDES() {
                return "Test article";
            }

            @Override
            public AfaProjection getAfa() {
                return new AfaProjection() {
                    @Override
                    public String getAFADES() {
                        return "Test family";
                    }
                };
            }
        };

        when(artRepository.findByENT(1))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/art/fetch-analitica-articulos/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].afacod").value("AF"))
            .andExpect(jsonPath("$[0].artcod").value("ART"))
            .andExpect(jsonPath("$[0].artdes").value("Test article"))
            .andExpect(jsonPath("$[0].afa.afades").value("Test family"));
    }

    @Test
    void fetchAnaliticaArticulos_returns404WhenEmpty() throws Exception {
        when(artRepository.findByENT(1))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/art/fetch-analitica-articulos/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchAnaliticaArticulos_returns500OnException() throws Exception {
        when(artRepository.findByENT(1))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/art/fetch-analitica-articulos/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void fetchConsultaGeneral_returns200WithList() throws Exception {
        ArticleProjection projection = new ArticleProjection() {
            @Override public String getAFACOD() { return "AF"; }
            @Override public String getAfa_AFADES() { return "Family"; }
            @Override public String getASUCOD() { return "ASU"; }
            @Override public String getAsu_ASUDES() { return "Subfamily"; }
            @Override public String getARTCOD() { return "ART"; }
            @Override public String getARTDES() { return "Test article"; }
            @Override public String getARTREF() { return "REF"; }
            @Override public Integer getARTBLO() { return 0; }
            @Override public Double getARTUNI() { return 1.0; }
            @Override public Double getARTSOL() { return 2.0; }
            @Override public Double getARTREC() { return 3.0; }
            @Override public String getAun_AUNDES() { return "Unit"; }
            @Override public Double getARTUCO() { return 10.0; }
            @Override public Double getARTUEM() { return 11.0; }
            @Override public Double getARTPMI() { return 12.0; }
            @Override public Double getARTPMP() { return 13.0; }
            @Override public Double getARTMIN() { return 14.0; }
            @Override public Double getARTOPT() { return 15.0; }
        };

        when(artRepository.findByENT(eq(1), any()))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/art/fetch-consulta-general/1")
                .param("page", "2")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].artcod").value("ART"))
            .andExpect(jsonPath("$[0].artdes").value("Test article"));

        verify(artRepository).findByENT(eq(1), argThat(page ->
            page.getPageNumber() == 2 && page.getPageSize() == 20
        ));
    }

    @Test
    void fetchConsultaGeneral_returns404WhenEmpty() throws Exception {
        when(artRepository.findByENT(eq(1), any()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/art/fetch-consulta-general/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchConsultaGeneral_returns500OnException() throws Exception {
        when(artRepository.findByENT(eq(1), any()))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/art/fetch-consulta-general/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void fetchConsultaGeneralExport_returns200WithList() throws Exception {
        ArticleProjection projection = new ArticleProjection() {
            @Override public String getAFACOD() { return "AF"; }
            @Override public String getAfa_AFADES() { return "Family"; }
            @Override public String getASUCOD() { return "ASU"; }
            @Override public String getAsu_ASUDES() { return "Subfamily"; }
            @Override public String getARTCOD() { return "ART"; }
            @Override public String getARTDES() { return "Test article"; }
            @Override public String getARTREF() { return "REF"; }
            @Override public Integer getARTBLO() { return 0; }
            @Override public Double getARTUNI() { return 1.0; }
            @Override public Double getARTSOL() { return 2.0; }
            @Override public Double getARTREC() { return 3.0; }
            @Override public String getAun_AUNDES() { return "Unit"; }
            @Override public Double getARTUCO() { return 10.0; }
            @Override public Double getARTUEM() { return 11.0; }
            @Override public Double getARTPMI() { return 12.0; }
            @Override public Double getARTPMP() { return 13.0; }
            @Override public Double getARTMIN() { return 14.0; }
            @Override public Double getARTOPT() { return 15.0; }
        };

        when(artRepository.findAllByENT(1))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/art/export-consulta-general/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].artcod").value("ART"))
            .andExpect(jsonPath("$[0].artdes").value("Test article"));
    }

    @Test
    void fetchConsultaGeneralExport_returns404WhenEmpty() throws Exception {
        when(artRepository.findAllByENT(1))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/art/export-consulta-general/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchConsultaGeneralExport_returns500OnException() throws Exception {
        when(artRepository.findAllByENT(1))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/art/export-consulta-general/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void getPag_returns200WithPagination() throws Exception {
        when(artRepository.countByENT(1))
            .thenReturn(45);

        mockMvc.perform(get("/api/art/get-pag/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("45"));
    }

    @Test
    void getPag_returns404WhenZero() throws Exception {
        when(artRepository.countByENT(1))
            .thenReturn(0);

        mockMvc.perform(get("/api/art/get-pag/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getPag_returns500OnException() throws Exception {
        when(artRepository.countByENT(1))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/art/get-pag/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void searchArticles_returns200WithList() throws Exception {
        ArticleProjection projection = new ArticleProjection() {
            @Override public String getAFACOD() { return "AF"; }
            @Override public String getAfa_AFADES() { return "Family"; }
            @Override public String getASUCOD() { return "ASU"; }
            @Override public String getAsu_ASUDES() { return "Subfamily"; }
            @Override public String getARTCOD() { return "ART"; }
            @Override public String getARTDES() { return "Test article"; }
            @Override public String getARTREF() { return "REF"; }
            @Override public Integer getARTBLO() { return 0; }
            @Override public Double getARTUNI() { return 1.0; }
            @Override public Double getARTSOL() { return 2.0; }
            @Override public Double getARTREC() { return 3.0; }
            @Override public String getAun_AUNDES() { return "Unit"; }
            @Override public Double getARTUCO() { return 10.0; }
            @Override public Double getARTUEM() { return 11.0; }
            @Override public Double getARTPMI() { return 12.0; }
            @Override public Double getARTPMP() { return 13.0; }
            @Override public Double getARTMIN() { return 14.0; }
            @Override public Double getARTOPT() { return 15.0; }
        };

        when(artRepository.searchArticles(1, "bolt", "AF", "ASU", "todos"))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/art/search/1")
                .param("search", "bolt")
                .param("afacod", "AF")
                .param("asucod", "ASU")
                .param("bloqueado", "todos")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].artcod").value("ART"))
            .andExpect(jsonPath("$[0].artdes").value("Test article"));
    }

    @Test
    void searchArticles_returns404WhenEmpty() throws Exception {
        when(artRepository.searchArticles(1, "bolt", "AF", "ASU", "todos"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/art/search/1")
                .param("search", "bolt")
                .param("afacod", "AF")
                .param("asucod", "ASU")
                .param("bloqueado", "todos"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchArticles_returns500OnException() throws Exception {
        when(artRepository.searchArticles(1, "bolt", "AF", "ASU", "todos"))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/art/search/1")
                .param("search", "bolt")
                .param("afacod", "AF")
                .param("asucod", "ASU")
                .param("bloqueado", "todos"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error: DB error"));
    }

    @Test
    void updateArticle_updatesAllFields() throws Exception {
        Art article = new Art();
        article.setENT(1);
        article.setAFACOD("AF");
        article.setASUCOD("ASU");
        article.setARTCOD("ART");
        when(artRepository.findById(any())).thenReturn(java.util.Optional.of(article));

        mockMvc.perform(patch("/api/art/update-art/1/AF/ASU/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"ARTDES":"Updated","ARTREF":"REF2","ARTBLO":1,"AUNCOD":"U2","ARTUCO":10.5,"ARTUEM":11.5,"ARTMIN":2.5,"ARTOPT":5.5}
                    """))
            .andDo(print())
            .andExpect(status().isNoContent());

        assertThat(article.getARTDES(), is("Updated"));
        assertThat(article.getARTREF(), is("REF2"));
        assertThat(article.getARTBLO(), is(1));
        assertThat(article.getAUNCOD(), is("U2"));
        assertThat(article.getARTUCO(), is(10.5));
        assertThat(article.getARTUEM(), is(11.5));
        assertThat(article.getARTMIN(), is(2.5));
        assertThat(article.getARTOPT(), is(5.5));
        verify(artRepository).save(article);
    }

    @Test
    void updateArticle_returnsNotFoundWhenArticleDoesNotExist() throws Exception {
        when(artRepository.findById(any())).thenReturn(java.util.Optional.empty());

        mockMvc.perform(patch("/api/art/update-art/1/AF/ASU/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ARTDES\":\"Updated\"}"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void updateArticle_returnsBadRequestForNullPayload() throws Exception {
        ResponseEntity<?> response = new ArtController()
            .updateArticle(1, "AF", "ASU", "ART", null);

        assertThat(response.getStatusCode().value(), is(400));
        assertThat(response.getBody(), is("Faltan datos obligatorios."));

        verifyNoInteractions(artRepository);
    }

    @Test
    void updateArticle_returns500OnException() throws Exception {
        when(artRepository.findById(any()))
            .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(patch("/api/art/update-art/1/AF/ASU/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ARTDES\":\"Updated\"}"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error: Update failed"));
    }

    @Test
    void deleteArticle_marksArticleAsBlocked() throws Exception {
        Art article = new Art();
        when(artRepository.findById(any())).thenReturn(java.util.Optional.of(article));

        mockMvc.perform(delete("/api/art/delete-art/1/AF/ASU/ART"))
            .andExpect(status().isNoContent());

        assertThat(article.getARTBLO(), is(1));
        verify(artRepository).save(article);
    }

    @Test
    void deleteArticle_returnsNotFoundWhenArticleDoesNotExist() throws Exception {
        when(artRepository.findById(any())).thenReturn(java.util.Optional.empty());

        mockMvc.perform(delete("/api/art/delete-art/1/AF/ASU/ART"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteArticle_returns500OnException() throws Exception {
        when(artRepository.findById(any()))
            .thenThrow(new RuntimeException("Delete failed"));

        mockMvc.perform(delete("/api/art/delete-art/1/AF/ASU/ART"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error: Delete failed"));
    }

    @Test
    void addArticle_createsArticleWithoutWarehouseEntries() throws Exception {
        when(artRepository.findById(any())).thenReturn(java.util.Optional.empty());
        when(asuRepository.findMagcods(1, "AF", "ASU")).thenReturn(List.of());

        mockMvc.perform(post("/api/art/add-art")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"ENT":1,"AFACOD":"AF","ASUCOD":"ASU","ARTCOD":"ART","ARTDES":"Article","ARTREF":"REF","ARTBLO":0,"ARTUNI":1.0,"ARTSOL":2.0,"ARTREC":3.0,"AUNCOD":"U1","ARTUCO":4.0,"ARTUEM":5.0,"ARTPMP":6.0,"ARTMIN":7.0,"ARTOPT":8.0}
                    """))
            .andExpect(status().isNoContent());

        verify(artRepository).save(any(Art.class));
        verifyNoInteractions(meaRepository);
    }

    @Test
    void addArticle_returnsBadRequestForMissingRequiredData() throws Exception {
        mockMvc.perform(post("/api/art/add-art")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ENT\":1,\"AFACOD\":\"AF\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void addArticle_returnsNotFoundWhenArticleAlreadyExists() throws Exception {
        when(artRepository.findById(any())).thenReturn(java.util.Optional.of(new Art()));

        mockMvc.perform(post("/api/art/add-art")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ENT\":1,\"AFACOD\":\"AF\",\"ASUCOD\":\"ASU\",\"ARTCOD\":\"ART\",\"ARTDES\":\"Article\"}"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("El artículo ya existe."));
    }

    @Test
    void addArticle_returnsBadRequestOnDataAccessException() throws Exception {
        when(artRepository.findById(any()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(post("/api/art/add-art")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ENT\":1,\"AFACOD\":\"AF\",\"ASUCOD\":\"ASU\",\"ARTCOD\":\"ART\",\"ARTDES\":\"Article\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void fetchExistencias_returnsResultsOrNotFound() throws Exception {
        ArticleProjection projection = articleProjection();
        when(artRepository.findByENTAndARTBLONot(1, 0)).thenReturn(List.of(projection));

        mockMvc.perform(get("/api/art/Existencias/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        when(artRepository.findByENTAndARTBLONot(2, 0)).thenReturn(List.of());
        mockMvc.perform(get("/api/art/Existencias/2"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("El artículo ya existe."));
    }

    @Test
    void fetchExistencias_returnsBadRequestOnDataAccessException() throws Exception {
        when(artRepository.findByENTAndARTBLONot(1, 0))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/art/Existencias/1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void searchExistencias_returnsResultsOrNotFound() throws Exception {
        ArticleProjection projection = articleProjection();
        when(existenciasSearch.searchExistencias(1, "bolt", "AF", "ASU"))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/art/existencias/1/search")
                .param("campo", "bolt")
                .param("afacod", "AF")
                .param("asucod", "ASU"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        when(existenciasSearch.searchExistencias(2, null, null, null)).thenReturn(List.of());
        mockMvc.perform(get("/api/art/existencias/2/search"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchExistencias_returnsBadRequestOnDataAccessException() throws Exception {
        when(existenciasSearch.searchExistencias(1, null, null, null))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/art/existencias/1/search"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }
}