package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Asu;
import com.example.backend.sqlserver2.model.AsuId;
import com.example.backend.sqlserver2.repository.AsuRepository;
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

@WebMvcTest(controllers = AsuController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class AsuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsuRepository asuRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getArtName_returnsListOr404() throws Exception {
        Asu a = new Asu(); a.setASUCOD("C1"); a.setASUDES("desc");
        when(asuRepository.findByENTAndAFACODAndASUCOD(1, "AF", "C1")).thenReturn(List.of(a));

        mockMvc.perform(get("/api/asu/art-name/1/AF/C1").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        when(asuRepository.findByENTAndAFACODAndASUCOD(2, "AF", "XX")).thenReturn(List.of());
        mockMvc.perform(get("/api/asu/art-name/2/AF/XX"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getArtName_returns400OnDataAccessException() throws Exception {
        when(asuRepository.findByENTAndAFACODAndASUCOD(anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/asu/art-name/1/AF/C1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void getSubfamilias_returnsListOr404() throws Exception {
        Asu a = new Asu(); a.setASUCOD("Z"); a.setASUDES("d");
        when(asuRepository.findByENTAndAFACOD(1, "AF")).thenReturn(List.of(a));

        mockMvc.perform(get("/api/asu/by-ent-afacod/1/AF").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        when(asuRepository.findByENTAndAFACOD(2, "XX")).thenReturn(List.of());
        mockMvc.perform(get("/api/asu/by-ent-afacod/2/XX"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getSubfamilias_returns400OnDataAccessException() throws Exception {
        when(asuRepository.findByENTAndAFACOD(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/asu/by-ent-afacod/1/AF"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateSubFamilia_returns400WhenASUDESNull() throws Exception {
        AsuId id = new AsuId(1, "AF", "S1");
        Asu existing = new Asu();
        when(asuRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("ASUDES", null);
        payload.put("ASUECO", "E");
        payload.put("MTACOD", 5);

        mockMvc.perform(patch("/api/asu/update-subfamilia/1/AF/S1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateSubFamilia_returns400WhenASUECONull() throws Exception {
        AsuId id = new AsuId(1, "AF", "S1");
        Asu existing = new Asu();
        when(asuRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("ASUDES", "new");
        payload.put("ASUECO", null);
        payload.put("MTACOD", 5);

        mockMvc.perform(patch("/api/asu/update-subfamilia/1/AF/S1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateSubFamilia_returns400WhenMTACODNull() throws Exception {
        AsuId id = new AsuId(1, "AF", "S1");
        Asu existing = new Asu();
        when(asuRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("ASUDES", "new");
        payload.put("ASUECO", "E");
        payload.put("MTACOD", null);

        mockMvc.perform(patch("/api/asu/update-subfamilia/1/AF/S1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateSubFamilia_successfulUpdate() throws Exception {
        AsuId id = new AsuId(1, "AF", "S1");
        Asu existing = new Asu();
        when(asuRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String,Object> payload = Map.of("ASUDES", "updated desc", "ASUECO", "E", "MTACOD", 10);
        mockMvc.perform(patch("/api/asu/update-subfamilia/1/AF/S1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        ArgumentCaptor<Asu> captor = ArgumentCaptor.forClass(Asu.class);
        verify(asuRepository).save(captor.capture());
        assertEquals("updated desc", captor.getValue().getASUDES());
        assertEquals("E", captor.getValue().getASUECO());
        assertEquals(10, captor.getValue().getMTACOD());
    }

    @Test
    void updateSubFamilia_returns400OnSaveException() throws Exception {
        AsuId id = new AsuId(1, "AF", "S1");
        Asu existing = new Asu();
        when(asuRepository.findById(id)).thenReturn(Optional.of(existing));
        when(asuRepository.save(Mockito.<Asu>any()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        Map<String,Object> payload = Map.of("ASUDES", "new", "ASUECO", "E", "MTACOD", 5);
        mockMvc.perform(patch("/api/asu/update-subfamilia/1/AF/S1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateSubFamilia_notFoundAndDbError() throws Exception {
        AsuId id = new AsuId(2, "AF", "NX");
        when(asuRepository.findById(id)).thenReturn(Optional.empty());

        Map<String,Object> payload = Map.of("ASUDES", "n", "ASUECO", "E", "MTACOD", 1);
        mockMvc.perform(patch("/api/asu/update-subfamilia/2/AF/NX")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        when(asuRepository.findById(Mockito.<AsuId>any()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));
        mockMvc.perform(patch("/api/asu/update-subfamilia/1/AF/S1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void insertSub_returns400WhenEntNull() throws Exception {
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("ent", null);
        payload.put("afacod", "A");
        payload.put("asucod", "X");
        payload.put("asudes", "D");
        payload.put("asueco", "E");
        payload.put("mtacod", 1);

        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertSub_returns400WhenAfacodNull() throws Exception {
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("ent", 1);
        payload.put("afacod", null);
        payload.put("asucod", "X");
        payload.put("asudes", "D");
        payload.put("asueco", "E");
        payload.put("mtacod", 1);

        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertSub_returns400WhenAsucodNull() throws Exception {
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("ent", 1);
        payload.put("afacod", "A");
        payload.put("asucod", null);
        payload.put("asudes", "D");
        payload.put("asueco", "E");
        payload.put("mtacod", 1);

        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertSub_returns400WhenAsudesNull() throws Exception {
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("ent", 1);
        payload.put("afacod", "A");
        payload.put("asucod", "X");
        payload.put("asudes", null);
        payload.put("asueco", "E");
        payload.put("mtacod", 1);

        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertSub_returns400WhenAsuEcoNull() throws Exception {
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("ent", 1);
        payload.put("afacod", "A");
        payload.put("asucod", "X");
        payload.put("asudes", "D");
        payload.put("asueco", null);
        payload.put("mtacod", 1);

        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertSub_returns400WhenMtacodNull() throws Exception {
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("ent", 1);
        payload.put("afacod", "A");
        payload.put("asucod", "X");
        payload.put("asudes", "D");
        payload.put("asueco", "E");
        payload.put("mtacod", null);

        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertSub_successfulInsertAndErrors() throws Exception {
        Map<String,Object> payload = Map.of("ent", 1, "afacod", "A", "asucod", "X", "asudes", "D", "asueco", "E", "mtacod", 1);
        when(asuRepository.findByENTAndAFACODAndASUCOD(1, "A", "X")).thenReturn(List.of());

        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(asuRepository).save(Mockito.<Asu>any());

        Map<String,Object> missing = Map.of("ent", 1, "afacod", "A");
        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missing)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));

        when(asuRepository.findByENTAndAFACODAndASUCOD(1, "A", "X")).thenReturn(List.of(new Asu()));
        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString("Sin resultado")));
    }

    @Test
    void insertSub_returns400OnSaveException() throws Exception {
        Map<String,Object> payload = Map.of("ent", 1, "afacod", "A", "asucod", "X", "asudes", "D", "asueco", "E", "mtacod", 1);
        when(asuRepository.findByENTAndAFACODAndASUCOD(1, "A", "X")).thenReturn(List.of());
        when(asuRepository.save(Mockito.<Asu>any()))
            .thenThrow(new DataAccessResourceFailureException("DB down during insert"));

        mockMvc.perform(post("/api/asu/Insert-Subfamilia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }
}