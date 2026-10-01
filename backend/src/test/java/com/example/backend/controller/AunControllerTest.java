package com.example.backend.controller;

import com.example.backend.config.TestExceptionHandler;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.sqlserver2.model.Aun;
import com.example.backend.sqlserver2.repository.ArtRepository;
import com.example.backend.sqlserver2.repository.AunRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AunController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class AunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AunRepository aunRepository;

    @MockitoBean
    private ArtRepository artRepository;

    @Test
    void fetchList_returns200WithResults() throws Exception {
        Aun aun = new Aun();
        aun.setENT(1);
        aun.setAUNCOD("U1");
        aun.setAUNDES("Unidad");

        when(aunRepository.findByENT(1))
            .thenReturn(List.of(aun));

        mockMvc.perform(get("/api/aun/fetch-list/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void fetchList_returns404WhenEmpty() throws Exception {
        when(aunRepository.findByENT(1))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/aun/fetch-list/1"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchList_returns400OnDataAccessException() throws Exception {
        when(aunRepository.findByENT(1))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/aun/fetch-list/1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error: DB error"));
    }

    @Test
    void searchByCodigo_returns200WithResults() throws Exception {
        Aun aun = new Aun();
        aun.setENT(1);
        aun.setAUNCOD("U1");
        aun.setAUNDES("Unidad");

        when(aunRepository.findByENTAndAUNCOD(1, "U1"))
            .thenReturn(List.of(aun));

        mockMvc.perform(get("/api/aun/search-codigo/1/U1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void searchByCodigo_returns404WhenEmpty() throws Exception {
        when(aunRepository.findByENTAndAUNCOD(1, "U1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/aun/search-codigo/1/U1"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchByCodigo_returns400OnDataAccessException() throws Exception {
        when(aunRepository.findByENTAndAUNCOD(1, "U1"))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/aun/search-codigo/1/U1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error: DB error"));
    }

    @Test
    void searchByDescripcion_returns200WithResults() throws Exception {
        Aun aun = new Aun();
        aun.setENT(1);
        aun.setAUNCOD("U1");
        aun.setAUNDES("Unidad");

        when(aunRepository.findByENTAndAUNDESContaining(1, "Uni"))
            .thenReturn(List.of(aun));

        mockMvc.perform(get("/api/aun/search-decripcion/1/Uni"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void searchByDescripcion_returns404WhenEmpty() throws Exception {
        when(aunRepository.findByENTAndAUNDESContaining(1, "Uni"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/aun/search-decripcion/1/Uni"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchByDescripcion_returns400OnDataAccessException() throws Exception {
        when(aunRepository.findByENTAndAUNDESContaining(1, "Uni"))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/aun/search-decripcion/1/Uni"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error: DB error"));
    }

    @Test
    void updateUnidad_returns400WhenPayloadIsMissingField() throws Exception {
        mockMvc.perform(patch("/api/aun/update-unidad/1/U1")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void updateUnidad_returns404WhenUnidadDoesNotExist() throws Exception {
        when(aunRepository.findById(any()))
            .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/aun/update-unidad/1/U1")
                .contentType("application/json")
                .content("""
                    {
                        "AUNDES": "Nueva unidad"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void updateUnidad_returns204WhenSuccessful() throws Exception {
        Aun aun = new Aun();
        aun.setENT(1);
        aun.setAUNCOD("U1");
        aun.setAUNDES("Antigua");

        when(aunRepository.findById(any()))
            .thenReturn(Optional.of(aun));

        mockMvc.perform(patch("/api/aun/update-unidad/1/U1")
                .contentType("application/json")
                .content("""
                    {
                        "AUNDES": "Nueva unidad"
                    }
                    """))
            .andExpect(status().isNoContent());

        verify(aunRepository).save(aun);
        org.junit.jupiter.api.Assertions.assertEquals(
            "Nueva unidad",
            aun.getAUNDES()
        );
    }

    @Test
    void updateUnidad_returns400OnDataAccessException() throws Exception {
        when(aunRepository.findById(any()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(patch("/api/aun/update-unidad/1/U1")
                .contentType("application/json")
                .content("""
                    {
                        "AUNDES": "Nueva unidad"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error: DB error"));
    }

    @Test
    void addUnidad_returns400WhenPayloadIsMissing() throws Exception {
        mockMvc.perform(post("/api/aun/add-unidad")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void addUnidad_returns400WhenUnidadAlreadyExists() throws Exception {
        when(aunRepository.existsById(any()))
            .thenReturn(true);

        mockMvc.perform(post("/api/aun/add-unidad")
                .contentType("application/json")
                .content("""
                    {
                        "ENT": 1,
                        "AUNCOD": "U1",
                        "AUNDES": "Unidad"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("El tipo de unidad ya existe"));

        verify(aunRepository, never()).save(any());
    }

    @Test
    void addUnidad_returns204WhenSuccessful() throws Exception {
        when(aunRepository.existsById(any()))
            .thenReturn(false);

        mockMvc.perform(post("/api/aun/add-unidad")
                .contentType("application/json")
                .content("""
                    {
                        "ENT": 1,
                        "AUNCOD": "U1",
                        "AUNDES": "Unidad"
                    }
                    """))
            .andExpect(status().isNoContent());

        verify(aunRepository).save(any(Aun.class));
    }

    @Test
    void addUnidad_returns400OnDataAccessException() throws Exception {
        when(aunRepository.existsById(any()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(post("/api/aun/add-unidad")
                .contentType("application/json")
                .content("""
                    {
                        "ENT": 1,
                        "AUNCOD": "U1",
                        "AUNDES": "Unidad"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error: DB error"));
    }

    @Test
    void deleteUnidad_returns400WhenAssociatedWithArticulo() throws Exception {
        when(artRepository.countByENTAndAUNCOD(1, "U1"))
            .thenReturn(2);

        mockMvc.perform(delete("/api/aun/delete-unidad/1/U1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(
                "No se puede borrar porque está asociado a un artículo"
            ));

        verify(aunRepository, never()).existsById(any());
    }

    @Test
    void deleteUnidad_returns400WhenUnidadDoesNotExist() throws Exception {
        when(artRepository.countByENTAndAUNCOD(1, "U1"))
            .thenReturn(0);

        when(aunRepository.existsById(any()))
            .thenReturn(false);

        mockMvc.perform(delete("/api/aun/delete-unidad/1/U1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("El tipo de unidad no existe"));
    }

    @Test
    void deleteUnidad_returns204WhenSuccessful() throws Exception {
        when(artRepository.countByENTAndAUNCOD(1, "U1"))
            .thenReturn(0);

        when(aunRepository.existsById(any()))
            .thenReturn(true);

        mockMvc.perform(delete("/api/aun/delete-unidad/1/U1"))
            .andExpect(status().isNoContent());

        verify(aunRepository).deleteById(any());
    }

    @Test
    void deleteUnidad_returns400OnDataAccessException() throws Exception {
        when(artRepository.countByENTAndAUNCOD(1, "U1"))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(delete("/api/aun/delete-unidad/1/U1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error: DB error"));
    }

    @Test
    void allGet_returns200WithResults() throws Exception {
        Aun aun = new Aun();
        aun.setENT(1);
        aun.setAUNCOD("U1");
        aun.setAUNDES("Unidad");

        when(aunRepository.findByENT(1))
            .thenReturn(List.of(aun));

        mockMvc.perform(get("/api/aun/get-all/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void allGet_returns404WhenEmpty() throws Exception {
        when(aunRepository.findByENT(1))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/aun/get-all/1"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void allGet_returns500OnException() throws Exception {
        when(aunRepository.findByENT(1))
            .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/aun/get-all/1"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Error: DB error"));
    }
}