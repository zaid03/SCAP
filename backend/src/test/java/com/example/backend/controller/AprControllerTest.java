package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Apr;
import com.example.backend.sqlserver2.model.AprId;
import com.example.backend.sqlserver2.repository.AprRepository;
import com.example.backend.dto.ProveedoresArticleProjection;
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
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AprController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class AprControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AprRepository aprRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getApr_returns200WithList() throws Exception {
        Apr apr = new Apr();
        apr.setAPRREF("ART001");
        apr.setAPRPRE(100.0);
        when(aprRepository.findByENTAndTERCOD(1, 100)).thenReturn(List.of(apr));

        mockMvc.perform(get("/api/more/by-apr/1/100")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getApr_returns404WhenEmpty() throws Exception {
        when(aprRepository.findByENTAndTERCOD(1, 100)).thenReturn(List.of());

        mockMvc.perform(get("/api/more/by-apr/1/100"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getApr_returns400OnDataAccessException() throws Exception {
        when(aprRepository.findByENTAndTERCOD(anyInt(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/more/by-apr/1/100"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateArticulo_returns204OnSuccess() throws Exception {
        Apr apr = new Apr();
        apr.setAPRREF("ART001");
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");
        when(aprRepository.findById(id)).thenReturn(Optional.of(apr));

        Map<String, Object> payload = Map.of(
            "aprref", "ART001_UPDATED",
            "aprpre", 150.0,
            "apruem", 2.0,
            "aprobs", "updated observation",
            "apracu", 5
        );

        mockMvc.perform(patch("/api/more/update-apr/1/100/FAM/SUB/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(aprRepository).save(any(Apr.class));
    }

    @Test
    void updateArticulo_returns404WhenNotFound() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");
        when(aprRepository.findById(id)).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of(
            "aprref", "ART001",
            "aprpre", 150.0,
            "apruem", 2.0,
            "aprobs", "observation",
            "apracu", 5
        );

        mockMvc.perform(patch("/api/more/update-apr/1/100/FAM/SUB/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void updateArticulo_returns400OnDataAccessException() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");
        when(aprRepository.findById(id)).thenThrow(new DataAccessResourceFailureException("DB error"));

        Map<String, Object> payload = Map.of(
            "aprref", "ART001",
            "aprpre", 150.0,
            "apruem", 2.0,
            "aprobs", "observation",
            "apracu", 5
        );

        mockMvc.perform(patch("/api/more/update-apr/1/100/FAM/SUB/ART")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error:")));
    }

    @Test
    void deleteApr_returns200OnSuccess() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");
        when(aprRepository.existsById(id)).thenReturn(true);

        mockMvc.perform(delete("/api/more/delete-apr")
                .param("ent", "1")
                .param("tercod", "100")
                .param("afacod", "FAM")
                .param("asucod", "SUB")
                .param("artcod", "ART"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("articulo eliminado exitosamente"));

        verify(aprRepository).deleteById(id);
    }

    @Test
    void deleteApr_returns404WhenNotFound() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");
        when(aprRepository.existsById(id)).thenReturn(false);

        mockMvc.perform(delete("/api/more/delete-apr")
                .param("ent", "1")
                .param("tercod", "100")
                .param("afacod", "FAM")
                .param("asucod", "SUB")
                .param("artcod", "ART"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteApr_returns400OnDataAccessException() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");
        when(aprRepository.existsById(id)).thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(delete("/api/more/delete-apr")
                .param("ent", "1")
                .param("tercod", "100")
                .param("afacod", "FAM")
                .param("asucod", "SUB")
                .param("artcod", "ART"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void addApr_savesNewArticulo_returnsInSavedList() throws Exception {
        Apr apr = new Apr();
        apr.setENT(1);
        apr.setTERCOD(100);
        apr.setAFACOD("FAM");
        apr.setASUCOD("SUB");
        apr.setARTCOD("ART");

        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");
        when(aprRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/more/add-apr")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(apr))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.savedArticulos", hasSize(1)))
            .andExpect(jsonPath("$.savedArticulos[0].afacod").value("FAM"))
            .andExpect(jsonPath("$.unsavedArticulos", hasSize(0)));

        verify(aprRepository).save(any(Apr.class));
    }

    @Test
    void addApr_skipsExistingArticulo_returnsInUnsavedList() throws Exception {
        Apr apr = new Apr();
        apr.setENT(1);
        apr.setTERCOD(100);
        apr.setAFACOD("FAM");
        apr.setASUCOD("SUB");
        apr.setARTCOD("ART");

        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");
        Apr existing = new Apr();
        existing.setAFACOD("FAM");
        existing.setASUCOD("SUB");
        existing.setARTCOD("ART");
        when(aprRepository.findById(id)).thenReturn(Optional.of(existing));

        mockMvc.perform(post("/api/more/add-apr")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(apr))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unsavedArticulos", hasSize(1)))
            .andExpect(jsonPath("$.unsavedArticulos[0].artcod").value("ART"))
            .andExpect(jsonPath("$.savedArticulos", hasSize(0)));

        verify(aprRepository, org.mockito.Mockito.never()).save(any(Apr.class));
    }

    @Test
    void addApr_returns400OnDataAccessException() throws Exception {
        Apr apr = new Apr();
        apr.setENT(1);
        apr.setTERCOD(100);
        apr.setAFACOD("FAM");
        apr.setASUCOD("SUB");
        apr.setARTCOD("ART");

        when(aprRepository.findById(any(AprId.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(post("/api/more/add-apr")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(apr))))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void proveedoresPorArticulo_debug() throws Exception {
        ProveedoresArticleProjection provider = new ProveedoresArticleProjection() {
            @Override
            public String getAPRREF() {
                return "REF001";
            }

            @Override
            public Double getAPRUEM() {
                return 10.5;
            }

            @Override
            public String getAPROBS() {
                return "Observacion";
            }

            @Override
            public Integer getTERCOD() {
                return 123;
            }

            @Override
            public String getTer_TERNOM() {
                return "Proveedor Test";
            }
        };

        when(aprRepository.findByENTAndAFACODAndASUCODAndARTCOD(
                1, "AFA", "ASU", "ART"))
            .thenReturn(List.of(provider));

        mockMvc.perform(get("/api/more/proveedores-por-articulo/1/AFA/ASU/ART")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print());
    }

    @Test
    void proveedoresPorArticulo_returns404WhenEmpty() throws Exception {
        when(aprRepository.findByENTAndAFACODAndASUCODAndARTCOD(
                1, "FAM", "SUB", "ART"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/more/proveedores-por-articulo/1/FAM/SUB/ART"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void proveedoresPorArticulo_returns500OnException() throws Exception {
        when(aprRepository.findByENTAndAFACODAndASUCODAndARTCOD(
                anyInt(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/more/proveedores-por-articulo/1/FAM/SUB/ART"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }

    @Test
    void proveedorInfoUpdate_returns204OnSuccess() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");

        Apr apr = new Apr();

        when(aprRepository.findById(id)).thenReturn(Optional.of(apr));

        Map<String, Object> payload = Map.of(
            "APRREF", "REF001",
            "APROBS", "Updated observation",
            "APRUEM", 25.5
        );

        mockMvc.perform(patch("/api/more/update-prov-info/1/FAM/SUB/ART/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(aprRepository).save(apr);
    }

    @Test
    void proveedorInfoUpdate_returns404WhenNotFound() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");

        when(aprRepository.findById(id)).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of(
            "APRREF", "REF001",
            "APROBS", "Observation",
            "APRUEM", 25.5
        );

        mockMvc.perform(patch("/api/more/update-prov-info/1/FAM/SUB/ART/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void proveedorInfoUpdate_returns500OnException() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");

        when(aprRepository.findById(id))
            .thenThrow(new RuntimeException("DB error"));

        Map<String, Object> payload = Map.of(
            "APRREF", "REF001",
            "APROBS", "Observation",
            "APRUEM", 25.5
        );

        mockMvc.perform(patch("/api/more/update-prov-info/1/FAM/SUB/ART/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }

    @Test
    void artProveedorDelete_returns204OnSuccess() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");

        when(aprRepository.existsById(id)).thenReturn(true);

        mockMvc.perform(delete("/api/more/delete-proveedor-art/1/FAM/SUB/ART/100"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(aprRepository).deleteById(id);
    }

    @Test
    void artProveedorDelete_returns400WhenNotExists() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");

        when(aprRepository.existsById(id)).thenReturn(false);

        mockMvc.perform(delete("/api/more/delete-proveedor-art/1/FAM/SUB/ART/100"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Proveedor no existe"));
    }

    @Test
    void artProveedorDelete_returns500OnException() throws Exception {
        AprId id = new AprId(1, 100, "FAM", "SUB", "ART");

        when(aprRepository.existsById(id))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(delete("/api/more/delete-proveedor-art/1/FAM/SUB/ART/100"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }

    @Test
    void tercerodAdd_addsNewProviders() throws Exception {
        when(aprRepository.existsById(any(AprId.class))).thenReturn(false);

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "AFACOD", "FAM",
            "ASUCOD", "SUB",
            "ARTCOD", "ART",
            "tercods", List.of(100, 200)
        );

        mockMvc.perform(post("/api/more/add-terceros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("Se guardaron 2 artículos"));

        verify(aprRepository, times(2)).save(any(Apr.class));
    }

    @Test
    void tercerodAdd_skipsExistingProviders() throws Exception {
        when(aprRepository.existsById(any(AprId.class))).thenReturn(true);

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "AFACOD", "FAM",
            "ASUCOD", "SUB",
            "ARTCOD", "ART",
            "tercods", List.of(100, 200)
        );

        mockMvc.perform(post("/api/more/add-terceros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("Se guardaron 0 artículos"));

        verify(aprRepository, never()).save(any(Apr.class));
    }

    @Test
    void tercerodAdd_returns400WhenPayloadIsInvalid() throws Exception {
        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "AFACOD", "FAM",
            "ASUCOD", "SUB",
            "ARTCOD", "ART"
        );

        mockMvc.perform(post("/api/more/add-terceros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void tercerodAdd_returns500OnException() throws Exception {
        when(aprRepository.existsById(any(AprId.class)))
            .thenThrow(new RuntimeException("DB error"));

        Map<String, Object> payload = Map.of(
            "ENT", 1,
            "AFACOD", "FAM",
            "ASUCOD", "SUB",
            "ARTCOD", "ART",
            "tercods", List.of(100)
        );

        mockMvc.perform(post("/api/more/add-terceros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error :DB error"));
    }
}