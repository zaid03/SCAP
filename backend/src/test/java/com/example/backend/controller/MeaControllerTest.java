package com.example.backend.controller;

import com.example.backend.config.TestExceptionHandler;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.ArticulosPorAlmcenProjection;
import com.example.backend.dto.ExistenciasMeaProjection;
import com.example.backend.dto.ServiceMagsProjection;
import com.example.backend.dto.existenciasProjection;
import com.example.backend.dto.magcodOnly;
import com.example.backend.service.existenciaAlmacenFetches;
import com.example.backend.sqlserver2.model.Mea;
import com.example.backend.sqlserver2.repository.MagRepository;
import com.example.backend.sqlserver2.repository.MeaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MeaController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class MeaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeaRepository meaRepository;

    @MockitoBean
    private existenciaAlmacenFetches ExistenciaAlmacenFetches;

    @MockitoBean
    private MagRepository magRepository;

    ArticulosPorAlmcenProjection projection = new ArticulosPorAlmcenProjection() {
        @Override public String getArt_Afa_AFACOD() { return "A1"; }
        @Override public String getArt_Afa_AFADES() { return "Familia"; }
        @Override public String getArt_Asu_ASUCOD() { return "S1"; }
        @Override public String getArt_Asu_ASUDES() { return "Subfamilia"; }
        @Override public String getArt_ARTCOD() { return "ART1"; }
        @Override public String getArt_ARTDES() { return "Articulo"; }
        @Override public String getArt_ARTREF() { return "REF1"; }
        @Override public Integer getArt_ARTBLO() { return 0; }
        @Override public Double getArt_ARTUNI() { return 1.0; }
        @Override public Double getMEAUNI() { return 5.0; }
        @Override public Double getMEASOL() { return 2.0; }
        @Override public Double getMEAREC() { return 3.0; }
        @Override public String getArt_Aun_AUNDES() { return "Unidad"; }
        @Override public Double getArt_ARTUCO() { return 1.0; }
        @Override public Double getArt_ARTUEM() { return 1.0; }
        @Override public Double getArt_ARTPMI() { return 1.0; }
        @Override public Double getMEAPMP() { return 1.0; }
        @Override public Double getMEAMIN() { return 1.0; }
        @Override public Double getMEAOPT() { return 1.0; }
    };

    @Test
    void fetchArticulosPorAlmacen_returns200WithResults() throws Exception {

        when(meaRepository.findByENT(eq(1), any()))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/mea/fetch-articulos-por-almacen/1")
                .param("page", "0")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void fetchArticulosPorAlmacen_returns404WhenEmpty() throws Exception {
        when(meaRepository.findByENT(eq(1), any()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/mea/fetch-articulos-por-almacen/1")
                .param("page", "0"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchArticulosPorAlmacen_returns500OnException() throws Exception {
        when(meaRepository.findByENT(eq(1), any()))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/mea/fetch-articulos-por-almacen/1")
                .param("page", "0"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }

    @Test
    void exportArticulosPorAlmacen_returns200WithResults() throws Exception {

        when(meaRepository.findAllByENT(1))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/mea/export/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void exportArticulosPorAlmacen_returns404WhenEmpty() throws Exception {
        when(meaRepository.findAllByENT(1))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/mea/export/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void exportArticulosPorAlmacen_returns500OnException() throws Exception {
        when(meaRepository.findAllByENT(1))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/mea/export/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }
    
        @Test
    void getPag_returns200WithCount() throws Exception {
        when(meaRepository.countByENT(1))
            .thenReturn(25);

        mockMvc.perform(get("/api/mea/get-pag/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("25"));
    }

    @Test
    void getPag_returns404WhenCountIsZero() throws Exception {
        when(meaRepository.countByENT(1))
            .thenReturn(0);

        mockMvc.perform(get("/api/mea/get-pag/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getPag_returns500OnException() throws Exception {
        when(meaRepository.countByENT(1))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/mea/get-pag/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }

        @Test
    void searchArticulos_returns200WithResults() throws Exception {

        when(meaRepository.searchArticulos(
                1, "test", "A1", "S1", "Todos", "10"))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/mea/search-articulos/1")
                .param("mainSearch", "test")
                .param("afaCod", "A1")
                .param("asuCod", "S1")
                .param("bloqueado", "Todos")
                .param("almacen", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void searchArticulos_returns404WhenEmpty() throws Exception {
        when(meaRepository.searchArticulos(
                1, null, null, null, "Todos", null))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/mea/search-articulos/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchArticulos_returns500OnException() throws Exception {
        when(meaRepository.searchArticulos(
                1, null, null, null, "Todos", null))
            .thenThrow(new RuntimeException("Search error"));

        mockMvc.perform(get("/api/mea/search-articulos/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :Search error"));
    }

        @Test
    void existenciasPorArticulo_returns200WithResults() throws Exception {
        Mea mea = new Mea();
        mea.setMAGCOD(10);
        mea.setMEAUNI(5.0);
        mea.setMEALOC("2");

        when(meaRepository.findByENTAndAFACODAndASUCODAndARTCOD(
                1, "A1", "S1", "ART1"))
            .thenReturn(List.of(mea));

        mockMvc.perform(get(
                "/api/mea/existencias-por-articulo/1/A1/S1/ART1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].magcod").value(10));
    }

    @Test
    void existenciasPorArticulo_returns404WhenEmpty() throws Exception {
        when(meaRepository.findByENTAndAFACODAndASUCODAndARTCOD(
                1, "A1", "S1", "ART1"))
            .thenReturn(List.of());

        mockMvc.perform(get(
                "/api/mea/existencias-por-articulo/1/A1/S1/ART1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void existenciasPorArticulo_returns500OnException() throws Exception {
        when(meaRepository.findByENTAndAFACODAndASUCODAndARTCOD(
                1, "A1", "S1", "ART1"))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get(
                "/api/mea/existencias-por-articulo/1/A1/S1/ART1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }

        @Test
    void existenciasAlmacen_returns200() throws Exception {
        existenciaAlmacenFetches.NamesResponse response =
            mock(existenciaAlmacenFetches.NamesResponse.class);

        when(ExistenciaAlmacenFetches.existenciasService(
                1, null, null, null, null, null, null, null, 0))
            .thenReturn(response);

        mockMvc.perform(get("/api/mea/existencias-almacen/1"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void existenciasAlmacen_returns400OnIllegalArgumentException() throws Exception {
        when(ExistenciaAlmacenFetches.existenciasService(
                1, null, null, null, null, null, null, null, 0))
            .thenThrow(new IllegalArgumentException("Invalid parameters"));

        mockMvc.perform(get("/api/mea/existencias-almacen/1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid parameters"));
    }

    @Test
    void existenciasAlmacen_returns500OnException() throws Exception {
        when(ExistenciaAlmacenFetches.existenciasService(
                1, null, null, null, null, null, null, null, 0))
            .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/mea/existencias-almacen/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :Service error"));
    }

        @Test
    void paginationExistencias_returns200WithTotalPages() throws Exception {
        magcodOnly projection = mock(magcodOnly.class);

        when(projection.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenReturn(Optional.of(projection));

        when(meaRepository.countByMAGCODAndArt_ARTBLONot(10, 0))
            .thenReturn(5L);

        mockMvc.perform(get("/api/mea/get-pag/1/D1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("5"));
    }

    @Test
    void paginationExistencias_returns500WhenAlmacenNotFound() throws Exception {
        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mea/get-pag/1/D1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :Almacen sin resultado."));
    }

    @Test
    void paginationExistencias_returns500OnException() throws Exception {
        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/mea/get-pag/1/D1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }

    @Test
    void dataExport_returns200WithEmptyList() throws Exception {
        magcodOnly projection = mock(magcodOnly.class);
        when(projection.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenReturn(Optional.of(projection));

        when(meaRepository.findAllByENTAndMAGCODAndArt_ARTBLONot(
                1, 10, 0))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/mea/export/1/D1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void dataExport_returns500WhenAlmacenNotFound() throws Exception {
        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mea/export/1/D1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :Almacen sin resultado."));
    }

    @Test
    void dataExport_returns500OnException() throws Exception {
        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/mea/export/1/D1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }
}
