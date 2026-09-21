package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Len;
import com.example.backend.sqlserver2.repository.LenRepository;
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
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentCaptor;

@WebMvcTest(controllers = LenController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
@AutoConfigureJsonTesters
public class LenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LenRepository lenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fetchAll_returns200WhenPresent() throws Exception {
        Len l = new Len();
        l.setLENCOD(1);
        l.setLENDES("Lugar");
        when(lenRepository.findAll()).thenReturn(List.of(l));

        mockMvc.perform(get("/api/Len/fetch-all")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].lencod", is(1)))
            .andExpect(jsonPath("$[0].lendes", is("Lugar")));
    }

    @Test
    void fetchAll_returns404WhenEmpty() throws Exception {
        when(lenRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/Len/fetch-all")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchAll_returns500OnDataAccessException() throws Exception {
        when(lenRepository.findAll()).thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/Len/fetch-all")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void addLugar_createsWithCodeOneWhenNoExistingRecords() throws Exception {
        when(lenRepository.findFirstByOrderByLENCODDesc()).thenReturn(null);

        Len payload = new Len();
        payload.setLENDES("Primer");
        payload.setLENTXT("Txt");

        mockMvc.perform(post("/api/Len/add-lugar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isCreated());

        ArgumentCaptor<Len> cap = ArgumentCaptor.forClass(Len.class);
        verify(lenRepository).save(cap.capture());
        assert cap.getValue().getLENCOD() == 1;
        assert "Primer".equals(cap.getValue().getLENDES());
    }

    @Test
    void addLugar_createsAndReturns201() throws Exception {
        Len last = new Len();
        last.setLENCOD(5);
        when(lenRepository.findFirstByOrderByLENCODDesc()).thenReturn(last);

        Len payload = new Len();
        payload.setLENDES("Nuevo");
        payload.setLENTXT("Txt");

        mockMvc.perform(post("/api/Len/add-lugar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isCreated());

        ArgumentCaptor<Len> cap = ArgumentCaptor.forClass(Len.class);
        verify(lenRepository).save(cap.capture());
        assert cap.getValue().getLENCOD() == 6;
        assert "Nuevo".equals(cap.getValue().getLENDES());
    }

    @Test
    void addLugar_returns500OnDataAccessException() throws Exception {
        when(lenRepository.findFirstByOrderByLENCODDesc()).thenThrow(new DataAccessResourceFailureException("DB down"));

        Len payload = new Len();
        payload.setLENDES("X");

        mockMvc.perform(post("/api/Len/add-lugar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void updateLugar_returns204OnSuccess() throws Exception {
        Len existing = new Len();
        existing.setLENCOD(2);
        existing.setLENDES("Old");
        existing.setLENTXT("OldTxt");
        when(lenRepository.findById(2)).thenReturn(Optional.of(existing));

        var payload = new LenController.lugarUpdate("NewDes", "NewTxt");

        mockMvc.perform(patch("/api/Len/update-lugar/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNoContent());

        ArgumentCaptor<Len> cap = ArgumentCaptor.forClass(Len.class);
        verify(lenRepository).save(cap.capture());
        assert "NewDes".equals(cap.getValue().getLENDES());
        assert "NewTxt".equals(cap.getValue().getLENTXT());
    }

    @Test
    void updateLugar_returns404WhenNotFound() throws Exception {
        when(lenRepository.findById(99)).thenReturn(Optional.empty());

        var payload = new LenController.lugarUpdate("A", "B");

        mockMvc.perform(patch("/api/Len/update-lugar/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void updateLugar_returns400WhenMissingFields() throws Exception {
        var payload = new LenController.lugarUpdate(null, "Txt");

        mockMvc.perform(patch("/api/Len/update-lugar/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateLugar_returns400WhenLENTXTIsNull() throws Exception {
        var payload = new LenController.lugarUpdate("Des", null);

        mockMvc.perform(patch("/api/Len/update-lugar/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateLugar_returns400WhenBothFieldsNull() throws Exception {
        var payload = new LenController.lugarUpdate(null, null);

        mockMvc.perform(patch("/api/Len/update-lugar/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void updateLugar_returns400OnDataAccessException() throws Exception {
        when(lenRepository.findById(3)).thenThrow(new DataAccessResourceFailureException("DB down"));
        var payload = new LenController.lugarUpdate("X", "Y");

        mockMvc.perform(patch("/api/Len/update-lugar/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void deleteLugar_returns204WhenExists() throws Exception {
        when(lenRepository.existsById(4)).thenReturn(true);

        mockMvc.perform(delete("/api/Len/delete-lugar/4"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(lenRepository).deleteById(4);
    }

    @Test
    void deleteLugar_returns404WhenNotFound() throws Exception {
        when(lenRepository.existsById(10)).thenReturn(false);

        mockMvc.perform(delete("/api/Len/delete-lugar/10"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void deleteLugar_returns400OnDataAccessException() throws Exception {
        when(lenRepository.existsById(5)).thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(delete("/api/Len/delete-lugar/5"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void filterLencod_returns200WhenFound() throws Exception {
        Len l = new Len();
        l.setLENCOD(7);
        when(lenRepository.findByLENCOD(7)).thenReturn(List.of(l));

        mockMvc.perform(get("/api/Len/filter-lencod/7")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void filterLencod_returns404WhenEmpty() throws Exception {
        when(lenRepository.findByLENCOD(8)).thenReturn(List.of());

        mockMvc.perform(get("/api/Len/filter-lencod/8"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void filterLencod_returns400OnDataAccessException() throws Exception {
        when(lenRepository.findByLENCOD(9)).thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/Len/filter-lencod/9"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void filterLendes_returns200WhenFound() throws Exception {
        Len l = new Len();
        l.setLENDES("Desc");
        when(lenRepository.findByLENDESContaining("Desc")).thenReturn(List.of(l));

        mockMvc.perform(get("/api/Len/filter-lendes/Desc")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void filterLendes_returns404WhenEmpty() throws Exception {
        when(lenRepository.findByLENDESContaining("X")).thenReturn(List.of());

        mockMvc.perform(get("/api/Len/filter-lendes/X"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void filterLendes_returns400OnDataAccessException() throws Exception {
        when(lenRepository.findByLENDESContaining("Z")).thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/Len/filter-lendes/Z"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }
}