package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Cco;
import com.example.backend.sqlserver2.model.CcoId;
import com.example.backend.sqlserver2.repository.CcoRepository;
import com.example.backend.sqlserver2.repository.DepRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CcoController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class CcoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CcoRepository ccoRepository;
    @MockitoBean private DepRepository depRepository;

    @Test
    void fetchAll_returns200WithList() throws Exception {
        Cco c = new Cco(); c.setCCOCOD("CC1"); c.setCCODES("Desc");
        when(ccoRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of(c));

        mockMvc.perform(get("/api/cco/fetch-all/1/E1").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].ccocod", is("CC1")));
    }

    @Test
    void fetchAll_returns404WhenEmpty() throws Exception {
        when(ccoRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/cco/fetch-all/1/E1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void filterBy_returns404WhenEmpty() throws Exception {
        when(ccoRepository.findByENTAndEJEAndCCOCOD(1, "E1", "X")).thenReturn(List.of());

        mockMvc.perform(get("/api/cco/filter-by/1/E1/X"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void filterBy_returns400OnDataAccessException() throws Exception {
        when(ccoRepository.findByENTAndEJEAndCCOCOD(anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/cco/filter-by/1/E1/X"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void filterByDes_returns200WhenFound() throws Exception {
        Cco c = new Cco(); c.setCCOCOD("CC1"); c.setCCODES("Desc");
        when(ccoRepository.findByENTAndEJEAndCCODESContaining(1, "E1", "Desc")).thenReturn(List.of(c));

        mockMvc.perform(get("/api/cco/filter-by-des/1/E1/Desc").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void filterByDes_returns404WhenEmpty() throws Exception {
        when(ccoRepository.findByENTAndEJEAndCCODESContaining(1, "E1", "X")).thenReturn(List.of());

        mockMvc.perform(get("/api/cco/filter-by-des/1/E1/X"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void filterByDes_returns400OnDataAccessException() throws Exception {
        when(ccoRepository.findByENTAndEJEAndCCODESContaining(anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/cco/filter-by-des/1/E1/X"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void insertCentro_returnsCreatedOnSuccess() throws Exception {
        Map<String,Object> payload = Map.of("ENT", 1, "EJE", "E1", "CCOCOD", "CC1", "CCODES", "Desc");
        when(ccoRepository.findByENTAndEJEAndCCOCOD(1, "E1", "CC1")).thenReturn(List.of());

        mockMvc.perform(post("/api/cco/Insert-centro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(ccoRepository).save(Mockito.<Cco>any());
    }

    @Test
    void insertCentro_returnsBadRequestWhenMissingFields() throws Exception {
        Map<String,Object> payload = Map.of("ENT", 1, "EJE", "E1", "CCOCOD", "CC1"); 

        mockMvc.perform(post("/api/cco/Insert-centro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertCentro_returnsNotFoundWhenExists() throws Exception {
        Map<String,Object> payload = Map.of("ENT", 1, "EJE", "E1", "CCOCOD", "CC1", "CCODES", "Desc");
        when(ccoRepository.findByENTAndEJEAndCCOCOD(1, "E1", "CC1")).thenReturn(List.of(new Cco()));

        mockMvc.perform(post("/api/cco/Insert-centro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Sin resultado")));
    }

    @Test
    void insertCentro_returns400OnDataAccessException() throws Exception {
        Map<String,Object> payload = Map.of("ENT", 1, "EJE", "E1", "CCOCOD", "CC1", "CCODES", "Desc");
        when(ccoRepository.findByENTAndEJEAndCCOCOD(1, "E1", "CC1")).thenReturn(List.of());
        when(ccoRepository.save(any())).thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(post("/api/cco/Insert-centro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateCentro_returnsNoContentOnSuccess() throws Exception {
        Cco existing = new Cco(); existing.setCCOCOD("CC1"); existing.setCCODES("old");
        CcoId id = new CcoId(1, "E1", "CC1");
        when(ccoRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String,Object> payload = Map.of("CCODES", "new");

        mockMvc.perform(patch("/api/cco/update-centro/1/E1/CC1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        ArgumentCaptor<Cco> cap = ArgumentCaptor.forClass(Cco.class);
        verify(ccoRepository).save(cap.capture());
        assertEquals("new", cap.getValue().getCCODES());
    }

    @Test
    void updateCentro_returnsBadRequestWhenMissingFields() throws Exception {
        Map<String,Object> payload = Map.of(); 

        mockMvc.perform(patch("/api/cco/update-centro/1/E1/CC1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateCentro_returnsNotFoundWhenMissing() throws Exception {
        CcoId id = new CcoId(1, "E1", "NX");
        when(ccoRepository.findById(id)).thenReturn(Optional.empty());

        Map<String,Object> payload = Map.of("CCODES", "v");

        mockMvc.perform(patch("/api/cco/update-centro/1/E1/NX")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void updateCentro_returns400OnDataAccessException() throws Exception {
        CcoId id = new CcoId(1, "E1", "CC1");
        Cco existing = new Cco();
        when(ccoRepository.findById(id)).thenReturn(Optional.of(existing));
        when(ccoRepository.save(any())).thenThrow(new DataAccessResourceFailureException("DB error"));

        Map<String,Object> payload = Map.of("CCODES", "new");

        mockMvc.perform(patch("/api/cco/update-centro/1/E1/CC1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void deleteCoste_returnsConflictWhenHasDeps() throws Exception {
        when(ccoRepository.existsById(new CcoId(1, "E1", "CC1"))).thenReturn(true);
        when(depRepository.countByENTAndEJEAndCCOCOD(1, "E1", "CC1")).thenReturn(1L);

        mockMvc.perform(delete("/api/cco/delete-coste/1/E1/CC1"))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString("centros gestores")));
    }

    @Test
    void deleteCoste_returnsNotFoundWhenMissing() throws Exception {
        CcoId id = new CcoId(1, "E1", "CC1");
        when(ccoRepository.existsById(id)).thenReturn(false);

        mockMvc.perform(delete("/api/cco/delete-coste/1/E1/CC1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteCoste_returnsNoContentOnSuccess() throws Exception {
        CcoId id = new CcoId(1, "E1", "CC1");
        when(ccoRepository.existsById(id)).thenReturn(true);
        when(depRepository.countByENTAndEJEAndCCOCOD(1, "E1", "CC1")).thenReturn(0L);

        mockMvc.perform(delete("/api/cco/delete-coste/1/E1/CC1"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(ccoRepository).deleteById(id);
    }

    @Test
    void deleteCoste_returns400OnDataAccessException() throws Exception {
        CcoId id = new CcoId(1, "E1", "CC1");
        when(ccoRepository.existsById(id)).thenReturn(true);
        when(depRepository.countByENTAndEJEAndCCOCOD(1, "E1", "CC1")).thenReturn(0L);
        doThrow(new DataAccessResourceFailureException("DB error")).when(ccoRepository).deleteById(id);

        mockMvc.perform(delete("/api/cco/delete-coste/1/E1/CC1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void endpoints_return400OnDataAccessException() throws Exception {
        when(ccoRepository.findByENTAndEJE(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("boom"));

        mockMvc.perform(get("/api/cco/fetch-all/1/E1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }
}