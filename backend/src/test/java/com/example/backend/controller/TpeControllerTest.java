package com.example.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.config.TestExceptionHandler;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.sqlserver2.model.Tpe;
import com.example.backend.sqlserver2.model.TpeId;
import com.example.backend.sqlserver2.repository.TpeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.never;

@WebMvcTest(controllers = TpeController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class TpeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TpeRepository tpeRepository;

    private Tpe createTpe(int ent, int tercod, int tpecod, String name) {
        Tpe t = new Tpe();
        t.setENT(ent);
        t.setTERCOD(tercod);
        t.setTPECOD(tpecod);
        t.setTPENOM(name);
        t.setTPETEL("100200300");
        t.setTPETMO("600700800");
        t.setTPECOE("COE");
        t.setTPEOBS("OBS");
        return t;
    }

    @Test
    void shouldGetByEntAndTercod_returns200() throws Exception {
        Tpe a = createTpe(1, 50, 1, "Alice");
        Tpe b = createTpe(1, 50, 2, "Bob");
        when(tpeRepository.findByENTAndTERCOD(1, 50)).thenReturn(List.of(a, b));

        mockMvc.perform(get("/api/more/by-tpe/1/50")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturn404WhenNoTpeFound() throws Exception {
        when(tpeRepository.findByENTAndTERCOD(999999, 999999)).thenReturn(List.of());

        mockMvc.perform(get("/api/more/by-tpe/999999/999999")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

   
    @Test
    void shouldAddTpe_returns201_and_createEntity() throws Exception {
        int ent = 2;
        int tercod = 77;

        when(tpeRepository.findFirstByENTAndTERCODOrderByTPECODDesc(ent, tercod))
            .thenReturn(null);
        when(tpeRepository.save(any(Tpe.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> payload = Map.of(
            "tpenom", "NewContact",
            "tpetel", "111222333",
            "tpetmo", "999000111",
            "tpecoe", "contact@example.com",
            "tpeobs", "OBS"
        );

        mockMvc.perform(post("/api/more/add/" + ent + "/" + tercod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(content().string(
                containsString("Persona de contacto agregada")));

        ArgumentCaptor<Tpe> captor = ArgumentCaptor.forClass(Tpe.class);
        verify(tpeRepository).save(captor.capture());

        Tpe saved = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(ent, saved.getENT());
        org.junit.jupiter.api.Assertions.assertEquals(tercod, saved.getTERCOD());
        org.junit.jupiter.api.Assertions.assertEquals(1, saved.getTPECOD());
        org.junit.jupiter.api.Assertions.assertEquals("NewContact", saved.getTPENOM());
    }

    @Test
    void shouldAddTpe_generatesNextTpecod() throws Exception {
        int ent = 2;
        int tercod = 77;

        Tpe last = createTpe(ent, tercod, 5, "Previous");

        when(tpeRepository.findFirstByENTAndTERCODOrderByTPECODDesc(ent, tercod))
            .thenReturn(last);
        when(tpeRepository.save(any(Tpe.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        String payload = """
            {
                "tpenom": "NewContact",
                "tpetel": "111",
                "tpetmo": "",
                "tpecoe": "",
                "tpeobs": ""
            }
            """;

        mockMvc.perform(post("/api/more/add/" + ent + "/" + tercod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        ArgumentCaptor<Tpe> captor = ArgumentCaptor.forClass(Tpe.class);
        verify(tpeRepository).save(captor.capture());

        org.junit.jupiter.api.Assertions.assertEquals(
            6, captor.getValue().getTPECOD());
    }

    @Test
    void shouldAddTpe_returns409WhenTelephoneExists() throws Exception {
        int ent = 2;
        int tercod = 77;

        when(tpeRepository.existsByENTAndTERCODAndTPETEL(ent, tercod, "111"))
            .thenReturn(true);

        String payload = """
            {
                "tpenom": "Duplicate",
                "tpetel": "111",
                "tpetmo": "",
                "tpecoe": "",
                "tpeobs": ""
            }
            """;

        mockMvc.perform(post("/api/more/add/" + ent + "/" + tercod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isConflict())
            .andExpect(content().string("Este persona ya existe"));

        verify(tpeRepository, never()).save(any(Tpe.class));
    }

    @Test
    void shouldAddTpe_returns409WhenMobileExists() throws Exception {
        int ent = 2;
        int tercod = 77;

        when(tpeRepository.existsByENTAndTERCODAndTPETMO(ent, tercod, "222"))
            .thenReturn(true);

        String payload = """
            {
                "tpenom": "Duplicate",
                "tpetel": "",
                "tpetmo": "222",
                "tpecoe": "",
                "tpeobs": ""
            }
            """;

        mockMvc.perform(post("/api/more/add/" + ent + "/" + tercod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isConflict());

        verify(tpeRepository, never()).save(any(Tpe.class));
    }

    @Test
    void shouldAddTpe_returns409WhenEmailExists() throws Exception {
        int ent = 2;
        int tercod = 77;

        when(tpeRepository.existsByENTAndTERCODAndTPECOE(
                ent, tercod, "contact@example.com"))
            .thenReturn(true);

        String payload = """
            {
                "tpenom": "Duplicate",
                "tpetel": "",
                "tpetmo": "",
                "tpecoe": "contact@example.com",
                "tpeobs": ""
            }
            """;

        mockMvc.perform(post("/api/more/add/" + ent + "/" + tercod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isConflict());

        verify(tpeRepository, never()).save(any(Tpe.class));
    }

    @Test
    void shouldAddTpe_ignoresBlankContactFields() throws Exception {
        int ent = 2;
        int tercod = 77;

        when(tpeRepository.findFirstByENTAndTERCODOrderByTPECODDesc(ent, tercod))
            .thenReturn(null);
        when(tpeRepository.save(any(Tpe.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        String payload = """
            {
                "tpenom": "NoContactInfo",
                "tpetel": "",
                "tpetmo": "   ",
                "tpecoe": "",
                "tpeobs": ""
            }
            """;

        mockMvc.perform(post("/api/more/add/" + ent + "/" + tercod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        verify(tpeRepository, never())
            .existsByENTAndTERCODAndTPETEL(anyInt(), anyInt(), any(String.class));
        verify(tpeRepository, never())
            .existsByENTAndTERCODAndTPETMO(anyInt(), anyInt(), any(String.class));
        verify(tpeRepository, never())
            .existsByENTAndTERCODAndTPECOE(anyInt(), anyInt(), any(String.class));

        verify(tpeRepository).save(any(Tpe.class));
    }

    @Test
    void shouldAddTpe_returns400OnDataAccessException() throws Exception {
        int ent = 2;
        int tercod = 77;

        when(tpeRepository.findFirstByENTAndTERCODOrderByTPECODDesc(ent, tercod))
            .thenReturn(null);
        when(tpeRepository.save(any(Tpe.class)))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        String payload = """
            {
                "tpenom": "NewContact",
                "tpetel": "111",
                "tpetmo": "",
                "tpecoe": "",
                "tpeobs": ""
            }
            """;

        mockMvc.perform(post("/api/more/add/" + ent + "/" + tercod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("La inserción falló")));
    }

    @Test
    void shouldModifyTpe_returns204_and_updateEntity() throws Exception {
        int ent = 4;
        int tercod = 99;
        int tpecod = 5;
        Tpe existing = createTpe(ent, tercod, tpecod, "ToModify");

        when(tpeRepository.findById(new TpeId(ent, tercod, tpecod))).thenReturn(Optional.of(existing));
        when(tpeRepository.save(any(Tpe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> payload = Map.of(
            "tpenom", "ModifiedName",
            "tpetel", "555",
            "tpetmo", "666",
            "tpecoe", "COE2",
            "tpeobs", "OBS2"
        );

        mockMvc.perform(put("/api/more/modify/" + ent + "/" + tercod + "/" + tpecod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(tpeRepository).save(any(Tpe.class));
    }

    @Test
    void shouldModifyTpe_returns400WhenNullTpenom() throws Exception {
        int ent = 4;
        int tercod = 99;
        int tpecod = 5;

        Map<String, String> payload = Map.of(
            "tpenom", "",
            "tpetel", "555",
            "tpetmo", "666",
            "tpecoe", "COE2",
            "tpeobs", "OBS2"
        );

        mockMvc.perform(put("/api/more/modify/" + ent + "/" + tercod + "/" + tpecod)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tpetel\": \"555\"}"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("nombre requerido")));
    }

    @Test
    void shouldModifyTpe_returns404WhenNotFound() throws Exception {
        int ent = 4;
        int tercod = 99;
        int tpecod = 999;

        when(tpeRepository.findById(new TpeId(ent, tercod, tpecod))).thenReturn(Optional.empty());

        Map<String, String> payload = Map.of(
            "tpenom", "ModifiedName",
            "tpetel", "555",
            "tpetmo", "666",
            "tpecoe", "COE2",
            "tpeobs", "OBS2"
        );

        mockMvc.perform(put("/api/more/modify/" + ent + "/" + tercod + "/" + tpecod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void shouldModifyTpe_returns400OnDataAccessException() throws Exception {
        int ent = 4;
        int tercod = 99;
        int tpecod = 5;
        Tpe existing = createTpe(ent, tercod, tpecod, "ToModify");

        when(tpeRepository.findById(new TpeId(ent, tercod, tpecod))).thenReturn(Optional.of(existing));
        when(tpeRepository.save(any(Tpe.class))).thenThrow(new DataAccessResourceFailureException("DB error"));

        Map<String, String> payload = Map.of(
            "tpenom", "ModifiedName",
            "tpetel", "555",
            "tpetmo", "666",
            "tpecoe", "COE2",
            "tpeobs", "OBS2"
        );

        mockMvc.perform(put("/api/more/modify/" + ent + "/" + tercod + "/" + tpecod)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void shouldDeleteTpe_returns200_and_deleteEntity() throws Exception {
        int ent = 5;
        int tercod = 101;
        int tpecod = 10;

        when(tpeRepository.existsById(new TpeId(ent, tercod, tpecod))).thenReturn(true);
        doNothing().when(tpeRepository).deleteById(new TpeId(ent, tercod, tpecod));

        mockMvc.perform(delete("/api/more/delete/" + ent + "/" + tercod + "/" + tpecod))
            .andDo(print())
            .andExpect(status().isOk());

        verify(tpeRepository).deleteById(new TpeId(ent, tercod, tpecod));
    }

    @Test
    void shouldDeleteTpe_returns404WhenNotFound() throws Exception {
        int ent = 5;
        int tercod = 101;
        int tpecod = 999;

        when(tpeRepository.existsById(new TpeId(ent, tercod, tpecod))).thenReturn(false);

        mockMvc.perform(delete("/api/more/delete/" + ent + "/" + tercod + "/" + tpecod))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void shouldDeleteTpe_returns400OnDataAccessException() throws Exception {
        int ent = 5;
        int tercod = 101;
        int tpecod = 10;

        when(tpeRepository.existsById(new TpeId(ent, tercod, tpecod))).thenReturn(true);
        doThrow(new DataAccessResourceFailureException("DB error"))
            .when(tpeRepository).deleteById(new TpeId(ent, tercod, tpecod));

        mockMvc.perform(delete("/api/more/delete/" + ent + "/" + tercod + "/" + tpecod))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void shouldGetByEntAndTercod_returns400OnDataAccessException() throws Exception {
        when(tpeRepository.findByENTAndTERCOD(anyInt(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/more/by-tpe/1/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error")));
    }
}