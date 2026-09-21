package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.dto.COGAIPOnlyDto;
import com.example.backend.dto.CogCgeProjection;
import com.example.backend.dto.CogSaveDto;
import com.example.backend.sqlserver2.model.Cog;
import com.example.backend.sqlserver2.model.CogId;
import com.example.backend.sqlserver2.repository.CogRepository;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CogController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class CogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CogRepository cogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fetchCentroGestores_returns404WhenEmpty() throws Exception {
        when(cogRepository.findAllByENTAndEJEAndCONCOD(1, "E1", 100))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/cog/fetch-centros/1/E1/100"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchCentroGestores_returns500OnDataAccessException() throws Exception {
        when(cogRepository.findAllByENTAndEJEAndCONCOD(anyInt(), anyString(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/cog/fetch-centros/1/E1/100"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void deleteCentroGestore_returns204OnSuccess() throws Exception {
        COGAIPOnlyDto centro = new COGAIPOnlyDto() {
            @Override
            public Double getCOGIAP() {
                return 0.0;
            }
        };
        when(cogRepository.findByENTAndEJEAndCONCODAndCGECOD(1, "E1", 100, "C1"))
            .thenReturn(Optional.of(centro));

        mockMvc.perform(delete("/api/cog/delete-centro/1/E1/100/C1"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(cogRepository).deleteById(any(CogId.class));
    }

    @Test
    void deleteCentroGestore_returns400WhenCogaipGreaterThanZero() throws Exception {
        COGAIPOnlyDto centro = new COGAIPOnlyDto() {
            @Override
            public Double getCOGIAP() {
                return 5.0;
            }
        };
        when(cogRepository.findByENTAndEJEAndCONCODAndCGECOD(1, "E1", 100, "C1"))
            .thenReturn(Optional.of(centro));

        mockMvc.perform(delete("/api/cog/delete-centro/1/E1/100/C1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("No se puede quitar")));
    }

    @Test
    void deleteCentroGestore_returns404WhenNotFound() throws Exception {
        when(cogRepository.findByENTAndEJEAndCONCODAndCGECOD(1, "E1", 100, "C1"))
            .thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/cog/delete-centro/1/E1/100/C1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteCentroGestore_returns404WhenCogaipIsNull() throws Exception {
        COGAIPOnlyDto centro = new COGAIPOnlyDto() {
            @Override
            public Double getCOGIAP() {
                return null;
            }
        };
        when(cogRepository.findByENTAndEJEAndCONCODAndCGECOD(1, "E1", 100, "C1"))
            .thenReturn(Optional.of(centro));

        mockMvc.perform(delete("/api/cog/delete-centro/1/E1/100/C1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteCentroGestore_returns500OnDataAccessException() throws Exception {
        when(cogRepository.findByENTAndEJEAndCONCODAndCGECOD(anyInt(), anyString(), anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(delete("/api/cog/delete-centro/1/E1/100/C1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void saveCentros_returns204OnSuccess() throws Exception {
        when(cogRepository.existsByENTAndEJEAndCONCODAndCGECOD(1, "E1", 100, "C1"))
            .thenReturn(false);

        List<Map<String, Object>> payload = List.of(
            Map.of(
                "ent", 1,
                "eje", "E1",
                "concod", 100,
                "cgecod", "C1",
                "cogimp", 100.0,
                "cogaip", 50.0
            )
        );

        mockMvc.perform(post("/api/cog/save-centroGestores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(cogRepository).saveAll(any());
    }

    @Test
    void saveCentros_skipsExistingCentros() throws Exception {
        when(cogRepository.existsByENTAndEJEAndCONCODAndCGECOD(1, "E1", 100, "C1"))
            .thenReturn(true);

        List<Map<String, Object>> payload = List.of(
            Map.of(
                "ent", 1,
                "eje", "E1",
                "concod", 100,
                "cgecod", "C1",
                "cogimp", 100.0,
                "cogaip", 50.0
            )
        );

        mockMvc.perform(post("/api/cog/save-centroGestores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(cogRepository).saveAll(any());
    }

    @Test
    void saveCentros_returns500OnDataAccessException() throws Exception {
        when(cogRepository.existsByENTAndEJEAndCONCODAndCGECOD(anyInt(), anyString(), anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        List<Map<String, Object>> payload = List.of(
            Map.of(
                "ent", 1,
                "eje", "E1",
                "concod", 100,
                "cgecod", "C1",
                "cogimp", 100.0,
                "cogaip", 50.0
            )
        );

        mockMvc.perform(post("/api/cog/save-centroGestores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void addDCentro_returns204OnSuccess() throws Exception {
        Cog cog = new Cog();
        CogId id = new CogId(1, "E1", 100, "C1");
        when(cogRepository.findById(id)).thenReturn(Optional.of(cog));

        Map<String, Object> payload = Map.of(
            "COGIMP", 150.0,
            "COGOPD", "D"
        );

        mockMvc.perform(patch("/api/cog/update-centro-D/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(cogRepository).save(any(Cog.class));
    }

    @Test
    void addDCentro_returns400WhenPayloadNull() throws Exception {
        mockMvc.perform(patch("/api/cog/update-centro-D/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of())))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void addDCentro_returns400WhenCogimpNull() throws Exception {
        Map<String, Object> payload = Map.of(
            "COGOPD", "D"
        );
        mockMvc.perform(patch("/api/cog/update-centro-D/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void addDCentro_returns400WhenCogopDNull() throws Exception {
        Map<String, Object> payload = Map.of(
            "COGIMP", 150.0
        );
        mockMvc.perform(patch("/api/cog/update-centro-D/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void addDCentro_returns404WhenNotFound() throws Exception {
        CogId id = new CogId(1, "E1", 100, "C1");
        when(cogRepository.findById(id)).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of(
            "COGIMP", 150.0,
            "COGOPD", "D"
        );

        mockMvc.perform(patch("/api/cog/update-centro-D/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void addDCentro_returns500OnDataAccessException() throws Exception {
        CogId id = new CogId(1, "E1", 100, "C1");
        when(cogRepository.findById(id)).thenThrow(new DataAccessResourceFailureException("DB error"));

        Map<String, Object> payload = Map.of(
            "COGIMP", 150.0,
            "COGOPD", "D"
        );

        mockMvc.perform(patch("/api/cog/update-centro-D/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void addDCentro2_returns204OnSuccess() throws Exception {
        CogId id = new CogId(1, "E1", 100, "C1");
        when(cogRepository.findById(id)).thenReturn(Optional.of(new Cog()));

        Map<String, Object> payload = Map.of(
            "COGIM2", 250.0,
            "COGOP2", "D2",
            "COGRF2", "REF2"
        );

        mockMvc.perform(patch("/api/cog/update-centro-D2/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isNoContent());

        verify(cogRepository).save(any(Cog.class));
    }

    @Test
    void addDCentro2_returns400WhenRequiredFieldIsMissing() throws Exception {
        Map<String, Object> payload = Map.of(
            "COGIM2", 250.0,
            "COGOP2", "D2"
        );

        mockMvc.perform(patch("/api/cog/update-centro-D2/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void addDCentro2_returns404WhenNotFound() throws Exception {
        CogId id = new CogId(1, "E1", 100, "C1");
        when(cogRepository.findById(id)).thenReturn(Optional.empty());

        Map<String, Object> payload = Map.of(
            "COGIM2", 250.0,
            "COGOP2", "D2",
            "COGRF2", "REF2"
        );

        mockMvc.perform(patch("/api/cog/update-centro-D2/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteD_returns204AndSavesClearedFirstD() throws Exception {
        Cog cog = new Cog();
        CogId id = new CogId(1, "E1", 100, "C1");
        when(cogRepository.findById(id)).thenReturn(Optional.of(cog));

        mockMvc.perform(delete("/api/cog/delete-D/1/E1/100/C1"))
            .andExpect(status().isNoContent());

        verify(cogRepository).save(cog);
    }

    @Test
    void deleteD2_returns204AndSavesClearedSecondD() throws Exception {
        Cog cog = new Cog();
        CogId id = new CogId(1, "E1", 100, "C1");
        when(cogRepository.findById(id)).thenReturn(Optional.of(cog));

        mockMvc.perform(delete("/api/cog/delete-D2/1/E1/100/C1"))
            .andExpect(status().isNoContent());

        verify(cogRepository).save(cog);
    }

    @Test
    void updateD1_returns204AndSavesBalance() throws Exception {
        CogId id = new CogId(1, "E1", 100, "C1");
        Cog cog = new Cog();
        when(cogRepository.findById(id)).thenReturn(Optional.of(cog));

        mockMvc.perform(patch("/api/cog/updateD1/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"COGIMP\":123.45}"))
            .andExpect(status().isNoContent());

        verify(cogRepository).save(cog);
    }

    @Test
    void updateD1_returns400WhenBalanceIsMissing() throws Exception {
        mockMvc.perform(patch("/api/cog/updateD1/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("COGIMP is required."));
    }

    @Test
    void updateD2_returns204AndSavesBalance() throws Exception {
        CogId id = new CogId(1, "E1", 100, "C1");
        Cog cog = new Cog();
        when(cogRepository.findById(id)).thenReturn(Optional.of(cog));

        mockMvc.perform(patch("/api/cog/updateD2/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"COGIM2\":456.78}"))
            .andExpect(status().isNoContent());

        verify(cogRepository).save(cog);
    }

    @Test
    void updateD2_returns400WhenBalanceIsMissing() throws Exception {
        mockMvc.perform(patch("/api/cog/updateD2/1/E1/100/C1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("COGIM2 is required."));
    }
}