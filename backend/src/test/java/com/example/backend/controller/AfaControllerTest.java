package com.example.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.config.TestExceptionHandler;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.ArticuloArticulo;
import com.example.backend.dto.ArticuloFamilia;
import com.example.backend.dto.ArticuloSubfamilia;
import com.example.backend.sqlserver2.model.Afa;
import com.example.backend.sqlserver2.model.AfaId;
import com.example.backend.sqlserver2.repository.AfaRepository;
import com.example.backend.sqlserver2.repository.ArtRepository;
import com.example.backend.sqlserver2.repository.AsuRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AfaController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class AfaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AfaRepository afaRepository;

    @MockitoBean
    private AsuRepository asuRepository;

    @MockitoBean
    private ArtRepository artRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getByEntAndAfacod_returns200WithList() throws Exception {
        Afa a = new Afa(); a.setAFACOD("AF1"); a.setAFADES("Desc");
        when(afaRepository.findByENTAndAFACOD(1, "AF1")).thenReturn(List.of(a));

        mockMvc.perform(get("/api/afa/by-ent/1/AF1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getByEntAndAfacod_returns404WhenEmpty() throws Exception {
        when(afaRepository.findByENTAndAFACOD(2, "X")).thenReturn(List.of());

        mockMvc.perform(get("/api/afa/by-ent/2/X"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getByEntAndAfacod_returns400OnDataAccessException() throws Exception {
        when(afaRepository.findByENTAndAFACOD(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/afa/by-ent/1/AF"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error")));
    }

    @Test
    void getAfaByEnt_returnsListOr404() throws Exception {
        Afa a = new Afa();
        when(afaRepository.findByENT(1)).thenReturn(List.of(a));

        mockMvc.perform(get("/api/afa/by-ent/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        when(afaRepository.findByENT(2)).thenReturn(List.of());
        mockMvc.perform(get("/api/afa/by-ent/2"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void getAfaByEnt_returns400OnDataAccessException() throws Exception {
        when(afaRepository.findByENT(anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/afa/by-ent/1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error")));
    }

    @Test
    void proveedorFetch_familiaNumericTerm_usesCodeLookup() throws Exception {
        ArticuloFamilia f = new ArticuloFamilia() {
            @Override public String getAFACOD() { return "123"; }
            @Override public String getAFADES() { return "Desc"; }
        };
        when(afaRepository.findAllByENTAndAFACOD(1, "123")).thenReturn(List.of(f));

        mockMvc.perform(get("/api/afa/fetch-articulos-proveedor/1/familia/123")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].afacod").value("123"));
    }

    @Test
    void proveedorFetch_familiaTextTerm_usesDescriptionLookup() throws Exception {
        ArticuloFamilia f = new ArticuloFamilia() {
            @Override public String getAFACOD() { return "AF1"; }
            @Override public String getAFADES() { return "Desc"; }
        };
        when(afaRepository.findByENTAndAFADESContaining(1, "Desc")).thenReturn(List.of(f));

        mockMvc.perform(get("/api/afa/fetch-articulos-proveedor/1/familia/Desc")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].afades").value("Desc"));
    }

    @Test
    void proveedorFetch_subfamiliaNumericTerm_usesCodeLookup() throws Exception {
        ArticuloSubfamilia s = new ArticuloSubfamilia() {
            @Override public String getAFACOD() { return "AF1"; }
            @Override public String getASUCOD() { return "123"; }
            @Override public String getASUDES() { return "Desc"; }
        };
        when(asuRepository.findByENTAndAFACODOrENTAndASUCOD(1, "123", 1, "123"))
            .thenReturn(List.of(s));

        mockMvc.perform(get("/api/afa/fetch-articulos-proveedor/1/subfamilia/123")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].asucod").value("123"));
    }

    @Test
    void proveedorFetch_subfamiliaTextTerm_usesDescriptionLookup() throws Exception {
        ArticuloSubfamilia s = new ArticuloSubfamilia() {
            @Override public String getAFACOD() { return "AF1"; }
            @Override public String getASUCOD() { return "SU1"; }
            @Override public String getASUDES() { return "Desc"; }
        };
        when(asuRepository.findByENTAndASUDESContaining(1, "Desc")).thenReturn(List.of(s));

        mockMvc.perform(get("/api/afa/fetch-articulos-proveedor/1/subfamilia/Desc")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].asudes").value("Desc"));
    }

    @Test
    void proveedorFetch_articuloNumericTerm_usesCodeLookup() throws Exception {
        ArticuloArticulo art = new ArticuloArticulo() {
            @Override public String getAFACOD() { return "AF1"; }
            @Override public String getASUCOD() { return "SU1"; }
            @Override public String getARTCOD() { return "123"; }
            @Override public String getARTDES() { return "Desc"; }
        };
        when(artRepository.findByENTAndAFACODOrENTAndASUCODOrENTAndARTCOD(1, "123", 1, "123", 1, "123"))
            .thenReturn(List.of(art));

        mockMvc.perform(get("/api/afa/fetch-articulos-proveedor/1/articulo/123")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].artcod").value("123"));
    }

    @Test
    void proveedorFetch_articuloTextTerm_usesDescriptionLookup() throws Exception {
        ArticuloArticulo art = new ArticuloArticulo() {
            @Override public String getAFACOD() { return "AF1"; }
            @Override public String getASUCOD() { return "SU1"; }
            @Override public String getARTCOD() { return "ART1"; }
            @Override public String getARTDES() { return "Desc"; }
        };
        when(artRepository.findByENTAndARTDESContaining(1, "Desc")).thenReturn(List.of(art));

        mockMvc.perform(get("/api/afa/fetch-articulos-proveedor/1/articulo/Desc")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].artdes").value("Desc"));
    }

    @Test
    void proveedorFetch_returns200WithEmptyListWhenNoMatches() throws Exception {
        when(afaRepository.findByENTAndAFADESContaining(1, "Nothing")).thenReturn(List.of());

        mockMvc.perform(get("/api/afa/fetch-articulos-proveedor/1/familia/Nothing")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void proveedorFetch_returns404ForUnknownSearchType() throws Exception {
        mockMvc.perform(get("/api/afa/fetch-articulos-proveedor/1/unknown/Term"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void proveedorFetch_returns400OnDataAccessException() throws Exception {
        when(afaRepository.findByENTAndAFADESContaining(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/afa/fetch-articulos-proveedor/1/familia/Desc"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error")));
    }

    @Test
    void updateFamilia_successAndValidation() throws Exception {
        Afa existing = new Afa();
        AfaId id = new AfaId(1, "AF1");
        existing.setAFADES("old");
        when(afaRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String,Object> payload = Map.of("AFADES", "new");

        mockMvc.perform(patch("/api/afa/update-familia/1/AF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        ArgumentCaptor<Afa> cap = ArgumentCaptor.forClass(Afa.class);
        verify(afaRepository).save(cap.capture());
        assertEquals("new", cap.getValue().getAFADES());

        Map<String,Object> bad = Map.of();
        mockMvc.perform(patch("/api/afa/update-familia/1/AF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bad)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateFamilia_notFoundAndDbError() throws Exception {
        AfaId id = new AfaId(2, "NX");
        when(afaRepository.findById(id)).thenReturn(Optional.empty());

        Map<String,Object> payload = Map.of("AFADES", "n");
        mockMvc.perform(patch("/api/afa/update-familia/2/NX")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        when(afaRepository.findById(any(AfaId.class)))
            .thenThrow(new DataAccessResourceFailureException("DB down"));
        mockMvc.perform(patch("/api/afa/update-familia/1/AF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Update failed")));
    }

    @Test
    void updateFamilia_returns400WhenAFADESFieldNull() throws Exception {
        AfaId id = new AfaId(1, "AF1");
        Afa existing = new Afa();
        when(afaRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String, Object> payloadWithNullField = new java.util.HashMap<>();
        payloadWithNullField.put("AFADES", null);

        mockMvc.perform(patch("/api/afa/update-familia/1/AF1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payloadWithNullField)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertFamilia_successValidationAndConflict() throws Exception {
        Map<String,Object> payload = Map.of("ent", 1, "afacod", "A", "afades", "D");
        when(afaRepository.findByENTAndAFACOD(1, "A")).thenReturn(List.of());

        mockMvc.perform(post("/api/afa/Insert-familia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(afaRepository).save(any(Afa.class));

        Map<String,Object> missing = Map.of("ent", 1, "afacod", "A");
        mockMvc.perform(post("/api/afa/Insert-familia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missing)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));

        when(afaRepository.findByENTAndAFACOD(1, "A")).thenReturn(List.of(new Afa()));
        mockMvc.perform(post("/api/afa/Insert-familia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString("Sin resultado")));
    }

    @Test
    void insertFamilia_returns400WhenEntNull() throws Exception {
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("ent", null);
        payload.put("afacod", "A");
        payload.put("afades", "D");

        mockMvc.perform(post("/api/afa/Insert-familia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertFamilia_returns400WhenAfacodNull() throws Exception {
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("ent", 1);
        payload.put("afacod", null);
        payload.put("afades", "D");

        mockMvc.perform(post("/api/afa/Insert-familia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void insertFamilia_returns400WhenAfadesNull() throws Exception {
        Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("ent", 1);
        payload.put("afacod", "A");
        payload.put("afades", null);

        mockMvc.perform(post("/api/afa/Insert-familia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }
}