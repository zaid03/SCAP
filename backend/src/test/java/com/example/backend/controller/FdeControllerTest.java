package com.example.backend.controller;

import com.example.backend.dto.CogCgeProjection;
import com.example.backend.dto.FdeFacTerProjection;
import com.example.backend.dto.ProjectionContabilizar;
import com.example.backend.dto.CogCgeProjection.CogCge;
import com.example.backend.dto.FdeFacTerProjection.FacInfo;
import com.example.backend.dto.FdeFacTerProjection.TerInfo;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.FacId;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.FdeId;
import com.example.backend.sqlserver2.model.Gbs;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.example.backend.sqlserver2.repository.FacRepository;
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
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.backend.service.ContabilizadoSearch;
import com.example.backend.service.ContabilizarSearch;
import com.example.backend.sqlserver2.repository.CogRepository;
import com.example.backend.sqlserver2.repository.GbsRepository;


@WebMvcTest(controllers = FdeController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class FdeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FdeRepository fdeRepository;

    @MockitoBean
    private FacRepository facRepository;
    @MockitoBean
    private ContabilizarSearch contabilizarSearch;     

    @MockitoBean
    private ContabilizadoSearch contabilizadoSearch;   

    @MockitoBean
    private GbsRepository gbsRepository;               

    @MockitoBean
    private CogRepository cogRepository;    

    @Test
    void getFde_returnsListWhenFound() throws Exception {
        Fde f = new Fde();
        f.setFDEREF("REF1");
        f.setFDEECO("ECO1");
        f.setFDEIMP(55.5);
        f.setFDEDIF(5.0);
        when(fdeRepository.findByENTAndEJEAndFACNUM(1, "E1", 123)).thenReturn(List.of(f));

        mockMvc.perform(get("/api/fde/1/E1/123")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(content().string(containsString("REF1")))
            .andExpect(content().string(containsString("ECO1")))
            .andExpect(content().string(containsString("55.5")));
    }

    @Test
    void getFde_returns404WhenEmpty() throws Exception {
        when(fdeRepository.findByENTAndEJEAndFACNUM(2, "E2", 1)).thenReturn(List.of());

        mockMvc.perform(get("/api/fde/2/E2/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getFde_returns400OnDataAccessException() throws Exception {
        when(fdeRepository.findByENTAndEJEAndFACNUM(anyInt(), anyString(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/fde/1/E1/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateDiferencias_returns204OnSuccess() throws Exception {
        FdeId fdeId = new FdeId(1, "E1", 100, "REF1");
        Fde fde = new Fde();
        fde.setFDEREF("REF1");
        fde.setFDEDIF(0.0);
        
        FacId facId = new FacId(1, "E1", 100);
        Fac fac = new Fac();
        fac.setFACIDI(0.0);

        when(fdeRepository.findById(any(FdeId.class))).thenReturn(Optional.of(fde));
        when(facRepository.findById(any(FacId.class))).thenReturn(Optional.of(fac));

        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(fdeRepository).save(any(Fde.class));
        verify(facRepository).save(any(Fac.class));
    }

    @Test
    void updateDiferencias_returns400WhenFDEDIFNull() throws Exception {
        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", null);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("faltan datos obligatorios")));
    }

    @Test
    void updateDiferencias_returns400WhenFACIDINull() throws Exception {
        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", null);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("faltan datos obligatorios")));
    }

    @Test
    void updateDiferencias_returns404WhenFdeNotFound() throws Exception {
        when(fdeRepository.findById(any(FdeId.class))).thenReturn(Optional.empty());

        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(fdeRepository, never()).save(any());
        verify(facRepository, never()).save(any());
    }

    @Test
    void updateDiferencias_returns404WhenFacNotFound() throws Exception {
        FdeId fdeId = new FdeId(1, "E1", 100, "REF1");
        Fde fde = new Fde();
        fde.setFDEREF("REF1");

        when(fdeRepository.findById(any(FdeId.class))).thenReturn(Optional.of(fde));
        when(facRepository.findById(any(FacId.class))).thenReturn(Optional.empty());

        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(fdeRepository).save(any(Fde.class));
        verify(facRepository, never()).save(any());
    }

    @Test
    void updateDiferencias_returns400OnDataAccessException() throws Exception {
        when(fdeRepository.findById(any(FdeId.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("FDEDIF", 10.5);
                put("FACIDI", 5.25);
            }}
        );

        mockMvc.perform(patch("/api/fde/update-diferencias/1/E1/100/REF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void fetchContabilizado_returns200WithList() throws Exception {
        ProjectionContabilizar projection = new ProjectionContabilizar() {
            @Override public Integer getFACNUM() { return 100; }
            @Override public String getFDEREF() { return "REF1"; }
            @Override public String getFDEOPE() { return "OPE1"; }
            @Override public String getFDEORG() { return "ORG1"; }
            @Override public String getFDEFUN() { return "FUN1"; }
            @Override public String getFDEECO() { return "ECO1"; }
            @Override public String getFDESUB() { return "SUB1"; }
            @Override public Double getFDEIMP() { return 100.0; }
            @Override public Double getFDEDIF() { return 5.0; }
            @Override public String getCGECOD() { return "CGE1"; }
            @Override public String getFACDOC() { return "DOC1"; }
            @Override public java.time.LocalDateTime getFACFCO() { return null; }
            @Override public Integer getTERCOD() { return 10; }
            @Override public Double getFACIMP() { return 100.0; }
            @Override public Integer getFACANN() { return 2026; }
            @Override public Integer getFACFAC() { return 1; }
            @Override public java.time.LocalDateTime getFACDAT() { return null; }
            @Override public String getTERNOM() { return "Proveedor"; }
            @Override public String getTERNIF() { return "NIF"; }
        };

        when(fdeRepository.findPendienteContabilizar(1, "E1"))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/1/E1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fderef").value("REF1"))
            .andExpect(jsonPath("$[0].facnum").value(100));
    }

    @Test
    void fetchContabilizado_returns404WhenEmpty() throws Exception {
        when(fdeRepository.findPendienteContabilizar(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/1/E1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchContabilizado_returns400OnGenericException() throws Exception {
        when(fdeRepository.findPendienteContabilizar(1, "E1"))
            .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/1/E1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error :Unexpected error"));
    }

    @Test
    void fetchContabilizado_returns400OnDataAccessException() throws Exception {
        when(fdeRepository.findPendienteContabilizar(1, "E1"))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/1/E1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void searchContabilizado_returns200WithList() throws Exception {
        ProjectionContabilizar projection = new ProjectionContabilizar() {
            @Override public Integer getFACNUM() { return 100; }
            @Override public String getFDEREF() { return "REF1"; }
            @Override public String getFDEOPE() { return "OPE1"; }
            @Override public String getFDEORG() { return "ORG1"; }
            @Override public String getFDEFUN() { return "FUN1"; }
            @Override public String getFDEECO() { return "ECO1"; }
            @Override public String getFDESUB() { return "SUB1"; }
            @Override public Double getFDEIMP() { return 100.0; }
            @Override public Double getFDEDIF() { return 5.0; }
            @Override public String getCGECOD() { return "CGE1"; }
            @Override public String getFACDOC() { return "DOC1"; }
            @Override public java.time.LocalDateTime getFACFCO() { return null; }
            @Override public Integer getTERCOD() { return 10; }
            @Override public Double getFACIMP() { return 100.0; }
            @Override public Integer getFACANN() { return 2026; }
            @Override public Integer getFACFAC() { return 1; }
            @Override public java.time.LocalDateTime getFACDAT() { return null; }
            @Override public String getTERNOM() { return "Proveedor"; }
            @Override public String getTERNIF() { return "NIF"; }
        };

        when(contabilizarSearch.searchContabilizado(
                1, "E1", "PROV", "CGE", "ECO", 2026))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "E1")
                .param("proveedor", "PROV")
                .param("centroGestor", "CGE")
                .param("economica", "ECO")
                .param("ano", "2026")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fderef").value("REF1"));
    }

    @Test
    void searchContabilizado_returns404WhenEmpty() throws Exception {
        when(contabilizarSearch.searchContabilizado(
                1, "E1", "PROV", "CGE", "ECO", 2026))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "E1")
                .param("proveedor", "PROV")
                .param("centroGestor", "CGE")
                .param("economica", "ECO")
                .param("ano", "2026"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchContabilizado_returns400OnDataAccessException() throws Exception {
        when(contabilizarSearch.searchContabilizado(
                1, "E1", "PROV", "CGE", "ECO", 2026))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "E1")
                .param("proveedor", "PROV")
                .param("centroGestor", "CGE")
                .param("economica", "ECO")
                .param("ano", "2026"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void fetchContabilizadoSecond_returns200WithList() throws Exception {
        FdeFacTerProjection projection = new FdeFacTerProjection() {
            @Override public Integer getFACNUM() { return 100; }
            @Override public String getFDEREF() { return "REF1"; }
            @Override public String getFDEOPE() { return "OPE1"; }
            @Override public String getFDEORG() { return "ORG1"; }
            @Override public String getFDEFUN() { return "FUN1"; }
            @Override public String getFDEECO() { return "ECO1"; }
            @Override public String getFDESUB() { return "SUB1"; }
            @Override public Double getFDEIMP() { return 100.0; }
            @Override public Double getFDEDIF() { return 5.0; }

            @Override
            public FacInfo getFac() {
                return new FacInfo() {
                    @Override public String getCGECOD() { return "CGE1"; }
                    @Override public String getFACDOC() { return "DOC1"; }
                    @Override public java.time.LocalDateTime getFACFCO() { return null; }
                    @Override public Integer getTERCOD() { return 10; }
                    @Override public Double getFACIMP() { return 100.0; }
                    @Override public Integer getFACANN() { return 2026; }
                    @Override public Integer getFACFAC() { return 1; }
                    @Override public java.time.LocalDateTime getFACDAT() { return null; }

                    @Override
                    public TerInfo getTer() {
                        return new TerInfo() {
                            @Override public String getTERNOM() { return "Proveedor"; }
                            @Override public String getTERNIF() { return "NIF"; }
                        };
                    }
                };
            }
        };

        when(fdeRepository
                .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                    1, "E1", 0.0, 1, "E1", 0.0))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/fde/fetch-contabilizado/1/E1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fderef").value("REF1"))
            .andExpect(jsonPath("$[0].fac.ter.ternom").value("Proveedor"));
    }

    @Test
    void fetchContabilizadoSecond_returns400OnDataAccessException() throws Exception {
        when(fdeRepository
                .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                    anyInt(), anyString(), anyDouble(), anyInt(), anyString(), anyDouble()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/fde/fetch-contabilizado/1/E1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void fetchContabilizadoSecond_returns400OnGenericException() throws Exception {
        when(fdeRepository
                .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                    anyInt(), anyString(), anyDouble(), anyInt(), anyString(), anyDouble()))
            .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/fde/fetch-contabilizado/1/E1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error :Unexpected error"));
    }

    @Test
    void searchContabilizadoSecond_returns200WithList() throws Exception {
        FdeFacTerProjection projection = new FdeFacTerProjection() {
            @Override public Integer getFACNUM() { return 100; }
            @Override public String getFDEREF() { return "REF1"; }
            @Override public String getFDEOPE() { return "OPE1"; }
            @Override public String getFDEORG() { return "ORG1"; }
            @Override public String getFDEFUN() { return "FUN1"; }
            @Override public String getFDEECO() { return "ECO1"; }
            @Override public String getFDESUB() { return "SUB1"; }
            @Override public Double getFDEIMP() { return 100.0; }
            @Override public Double getFDEDIF() { return 5.0; }

            @Override
            public FacInfo getFac() {
                return new FacInfo() {
                    @Override public String getCGECOD() { return "CGE1"; }
                    @Override public String getFACDOC() { return "DOC1"; }
                    @Override public java.time.LocalDateTime getFACFCO() { return null; }
                    @Override public Integer getTERCOD() { return 10; }
                    @Override public Double getFACIMP() { return 100.0; }
                    @Override public Integer getFACANN() { return 2026; }
                    @Override public Integer getFACFAC() { return 1; }
                    @Override public java.time.LocalDateTime getFACDAT() { return null; }

                    @Override
                    public TerInfo getTer() {
                        return new TerInfo() {
                            @Override public String getTERNOM() { return "Proveedor"; }
                            @Override public String getTERNIF() { return "NIF"; }
                        };
                    }
                };
            }
        };

        when(contabilizadoSearch.searchContabilizado(
                1, "E1", "PROV", "CGE", "ECO", 2026))
            .thenReturn(List.of(projection));

        mockMvc.perform(get("/api/fde/search-contabilizado")
                .param("ent", "1")
                .param("eje", "E1")
                .param("proveedor", "PROV")
                .param("centroGestor", "CGE")
                .param("economica", "ECO")
                .param("ano", "2026")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fderef").value("REF1"))
            .andExpect(jsonPath("$[0].fac.ter.ternom").value("Proveedor"));
    }

    @Test
    void contSinAD_returns400WhenRequiredDataMissing() throws Exception {
        String payload = objectMapper.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("ENT", null);
                put("EJE", "E1");
                put("CGECOD", "CGE1");
                put("FACNUM", 100);
            }}
        );

        mockMvc.perform(patch("/api/fde/AD-sin-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void contSinAD_returns404WhenNoBolsas() throws Exception {
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "E1", "CGE1"))
            .thenReturn(List.of());

        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatch(1, "E1", "CGE1", 100)
        );

        mockMvc.perform(patch("/api/fde/AD-sin-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Eliminado pero Bolsas Sin resultado"));
    }

    @Test
    void contSinAD_returns204OnSuccess() throws Exception {
        Gbs gbs = new Gbs();
        gbs.setGBSREF("REF1");
        gbs.setGBSOPE("OPE1");
        gbs.setGBSORG("ORG1");
        gbs.setGBSFUN("FUN1");
        gbs.setGBSECO("ECO1");

        Fac fac = new Fac();

        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "E1", "CGE1"))
            .thenReturn(List.of(gbs));

        when(facRepository.findById(any(FacId.class)))
            .thenReturn(Optional.of(fac));

        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatch(1, "E1", "CGE1", 100)
        );

        mockMvc.perform(patch("/api/fde/AD-sin-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(facRepository).save(fac);
        verify(fdeRepository).deleteByENTAndEJEAndFACNUM(1, "E1", 100);
        verify(fdeRepository).save(any(Fde.class));

        assert fac.getCONCOD() == 0;
    }

    @Test
    void contSinAD_returns500OnDataAccessException() throws Exception {
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "E1", "CGE1"))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatch(1, "E1", "CGE1", 100)
        );

        mockMvc.perform(patch("/api/fde/AD-sin-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void contConAD_returns400WhenRequiredDataMissing() throws Exception {
        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatchCon(
                1, "E1", null, 10, "ECO1", 100
            )
        );

        mockMvc.perform(patch("/api/fde/AD-con-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void contConAD_returns404WhenFacturaNotFound() throws Exception {
        when(facRepository.findById(any(FacId.class)))
            .thenReturn(Optional.empty());

        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatchCon(
                1, "E1", "CGE1", 10, "ECO1", 100
            )
        );

        mockMvc.perform(patch("/api/fde/AD-con-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void contConAD_returns404WhenNoCogs() throws Exception {
        when(facRepository.findById(any(FacId.class)))
            .thenReturn(Optional.of(new Fac()));

        when(cogRepository.findAllByENTAndEJEAndCONCODAndCGECOD(
                1, "E1", 10, "CGE1"))
            .thenReturn(List.of());

        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatchCon(
                1, "E1", "CGE1", 10, "ECO1", 100
            )
        );

        mockMvc.perform(patch("/api/fde/AD-con-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Registros para llenar la tabla Sin resultado"));
    }

    @Test
    void contConAD_returns400WhenReferenceIsBlank() throws Exception {
        Fac fac = new Fac();

        CogCgeProjection cog = mock(CogCgeProjection.class);

        when(facRepository.findById(any(FacId.class)))
            .thenReturn(Optional.of(fac));

        when(cogRepository.findAllByENTAndEJEAndCONCODAndCGECOD(
                1, "E1", 10, "CGE1"))
            .thenReturn(List.of(cog));

        when(cog.getCOGRFD()).thenReturn("   ");

        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatchCon(
                1, "E1", "CGE1", 10, "ECO1", 100
            )
        );

        mockMvc.perform(patch("/api/fde/AD-con-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Referencia Sin resultado"));
    }

    @Test
    void contConAD_returns204WithoutSecondFde() throws Exception {
        Fac fac = new Fac();

        CogCgeProjection cog = mock(CogCgeProjection.class);
        CogCgeProjection.CogCge cge = mock(CogCgeProjection.CogCge.class);

        when(facRepository.findById(any(FacId.class)))
            .thenReturn(Optional.of(fac));

        when(cogRepository.findAllByENTAndEJEAndCONCODAndCGECOD(
                1, "E1", 10, "CGE1"))
            .thenReturn(List.of(cog));

        when(cog.getCOGRFD()).thenReturn("REF1");
        when(cog.getCOGOPD()).thenReturn("OPE1");
        when(cog.getCOGRF2()).thenReturn(null);
        when(cog.getCge()).thenReturn(cge);
        when(cge.getCGEORG()).thenReturn("ORG1");
        when(cge.getCGEFUN()).thenReturn("FUN1");

        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatchCon(
                1, "E1", "CGE1", 10, "ECO1", 100
            )
        );

        mockMvc.perform(patch("/api/fde/AD-con-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(facRepository).save(fac);
        verify(fdeRepository).deleteByENTAndEJEAndFACNUM(1, "E1", 100);
        verify(fdeRepository, times(1)).save(any(Fde.class));

        org.junit.jupiter.api.Assertions.assertEquals(10, fac.getCONCOD());
    }

    @Test
    void contConAD_returns204WithSecondFde() throws Exception {
        Fac fac = new Fac();

        CogCgeProjection cog = mock(CogCgeProjection.class);
        CogCgeProjection.CogCge cge = mock(CogCgeProjection.CogCge.class);

        when(facRepository.findById(any(FacId.class)))
            .thenReturn(Optional.of(fac));

        when(cogRepository.findAllByENTAndEJEAndCONCODAndCGECOD(
                1, "E1", 10, "CGE1"))
            .thenReturn(List.of(cog));

        when(cog.getCOGRFD()).thenReturn("REF1");
        when(cog.getCOGOPD()).thenReturn("OPE1");
        when(cog.getCOGRF2()).thenReturn("REF2");
        when(cog.getCOGOP2()).thenReturn("OPE2");
        when(cog.getCge()).thenReturn(cge);
        when(cge.getCGEORG()).thenReturn("ORG1");
        when(cge.getCGEFUN()).thenReturn("FUN1");

        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatchCon(
                1, "E1", "CGE1", 10, "ECO1", 100
            )
        );

        mockMvc.perform(patch("/api/fde/AD-con-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(facRepository).save(fac);
        verify(fdeRepository).deleteByENTAndEJEAndFACNUM(1, "E1", 100);
        verify(fdeRepository, times(2)).save(any(Fde.class));
    }

    @Test
    void contConAD_returns500OnDataAccessException() throws Exception {
        when(facRepository.findById(any(FacId.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        String payload = objectMapper.writeValueAsString(
            new FdeController.CPatchCon(
                1, "E1", "CGE1", 10, "ECO1", 100
            )
        );

        mockMvc.perform(patch("/api/fde/AD-con-Cont")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }
}