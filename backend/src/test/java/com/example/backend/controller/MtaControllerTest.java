package com.example.backend.controller;

import com.example.backend.dto.AlmacenajeAddDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.sqlserver2.model.Mta;
import com.example.backend.sqlserver2.repository.MtaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.example.backend.sqlserver2.repository.AsuRepository;

@WebMvcTest(controllers = MtaController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class MtaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MtaRepository mtaRepository;
    @MockitoBean
    private AsuRepository asuRepository;

    @Test
    void shouldReturnAllMtaForEnt() throws Exception {
        Mta m = new Mta();
        m.setMTACOD(10);
        when(mtaRepository.findByENT(1)).thenReturn(List.of(m));

        mockMvc.perform(get("/api/mta/all-mta/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        verify(mtaRepository).findByENT(1);
    }

    @Test
    void shouldFilterAlmacenaje_returns404WhenEmpty() throws Exception {
        when(mtaRepository.findByENTAndMTACOD(2, 99)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/mta/mta-filter/2/99")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(mtaRepository).findByENTAndMTACOD(2, 99);
    }

    @Test
    void shouldFilterAlmacenaje_returns200WithResults() throws Exception {
        Mta m = new Mta();
        m.setMTACOD(5);
        when(mtaRepository.findByENTAndMTACOD(3, 5)).thenReturn(List.of(m));

        mockMvc.perform(get("/api/mta/mta-filter/3/5")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        verify(mtaRepository).findByENTAndMTACOD(3, 5);
    }

    @Test
    void shouldFilterAlmacenaje_returns500OnDataAccessException() throws Exception {
        when(mtaRepository.findByENTAndMTACOD(anyInt(), anyInt()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/mta/mta-filter/1/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }

    @Test
    void shouldSearchAlmacenaje_returns200WithResults() throws Exception {
        Mta m = new Mta();
        m.setMTACOD(10);
        m.setMTADES("Almacen A");

        when(mtaRepository.findByENTAndMTADESContaining(1, "Almacen"))
            .thenReturn(List.of(m));

        mockMvc.perform(get("/api/mta/search-almacenaje/1/Almacen")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].mtacod").value(10))
            .andExpect(jsonPath("$[0].mtades").value("Almacen A"));

        verify(mtaRepository).findByENTAndMTADESContaining(1, "Almacen");
    }

    @Test
    void shouldSearchAlmacenaje_returns404WhenEmpty() throws Exception {
        when(mtaRepository.findByENTAndMTADESContaining(1, "Missing"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/mta/search-almacenaje/1/Missing")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void shouldSearchAlmacenaje_returns500OnDataAccessException() throws Exception {
        when(mtaRepository.findByENTAndMTADESContaining(anyInt(), any()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/mta/search-almacenaje/1/Almacen")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }

    @Test
    void shouldUpdateAlmacenaje_returns400WhenDataMissing() throws Exception {
        String payload = """
            {"MTADES": null}
            """;

        mockMvc.perform(patch("/api/mta/update-almacenaje/1/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios"));

        verifyNoInteractions(mtaRepository);
    }

    @Test
    void shouldUpdateAlmacenaje_returns404WhenNotFound() throws Exception {
        when(mtaRepository.findById(any()))
            .thenReturn(Optional.empty());

        String payload = """
            {"MTADES": "Almacen Nuevo"}
            """;

        mockMvc.perform(patch("/api/mta/update-almacenaje/1/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void shouldUpdateAlmacenaje_returns204WhenUpdated() throws Exception {
        Mta mta = new Mta();
        mta.setENT(1);
        mta.setMTACOD(10);
        mta.setMTADES("Almacen Viejo");

        when(mtaRepository.findById(any()))
            .thenReturn(Optional.of(mta));

        String payload = """
            {"MTADES": "Almacen Nuevo"}
            """;

        mockMvc.perform(patch("/api/mta/update-almacenaje/1/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(mtaRepository).save(mta);
        org.junit.jupiter.api.Assertions.assertEquals("Almacen Nuevo", mta.getMTADES());
    }

    @Test
    void shouldUpdateAlmacenaje_returns500OnDataAccessException() throws Exception {
        when(mtaRepository.findById(any()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        String payload = """
            {"MTADES": "Almacen Nuevo"}
            """;

        mockMvc.perform(patch("/api/mta/update-almacenaje/1/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }

    @Test
    void shouldAddAlmacenaje_returns400WhenDataMissing() throws Exception {
        String payload = """
            {"ENT": null, "MTADES": "Almacen"}
            """;

        mockMvc.perform(post("/api/mta/add-almacenaje")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios"));

        verifyNoInteractions(mtaRepository);
    }

    @Test
    void shouldAddAlmacenaje_returns204WithNextCode() throws Exception {
        AlmacenajeAddDto dto1 = mock(AlmacenajeAddDto.class);
        AlmacenajeAddDto dto2 = mock(AlmacenajeAddDto.class);

        when(dto1.getMTACOD()).thenReturn(3);
        when(dto2.getMTACOD()).thenReturn(7);

        when(mtaRepository.findDtoByENT(1))
            .thenReturn(List.of(dto1, dto2));

        String payload = """
            {"ENT": 1, "MTADES": "Almacen Nuevo"}
            """;

        mockMvc.perform(post("/api/mta/add-almacenaje")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(mtaRepository).save(argThat(mta ->
            mta.getENT().equals(1)
            && mta.getMTACOD().equals(8)
            && mta.getMTADES().equals("Almacen Nuevo")
        ));
    }

    @Test
    void shouldAddAlmacenaje_returns204WithCodeOneWhenNoExistingCodes() throws Exception {
        when(mtaRepository.findDtoByENT(1))
            .thenReturn(List.of());

        String payload = """
            {"ENT": 1, "MTADES": "Primer Almacen"}
            """;

        mockMvc.perform(post("/api/mta/add-almacenaje")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(mtaRepository).save(argThat(mta ->
            mta.getENT().equals(1)
            && mta.getMTACOD().equals(1)
            && mta.getMTADES().equals("Primer Almacen")
        ));
    }

    @Test
    void shouldAddAlmacenaje_returns500OnDataAccessException() throws Exception {
        when(mtaRepository.findDtoByENT(1))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        String payload = """
            {"ENT": 1, "MTADES": "Almacen"}
            """;

        mockMvc.perform(post("/api/mta/add-almacenaje")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }

    @Test
    void shouldDeleteAlmacenaje_returns400WhenAssociatedWithSubfamilia() throws Exception {
        when(asuRepository.countByENTAndMTACOD(1, 10))
            .thenReturn(2);

        mockMvc.perform(delete("/api/mta/delete-almacenaje/1/10"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(
                "No se puede borrar porque está asociado a una subfamilia"
            ));

        verify(asuRepository).countByENTAndMTACOD(1, 10);
        verifyNoInteractions(mtaRepository);
    }

    @Test
    void shouldDeleteAlmacenaje_returns404WhenNotFound() throws Exception {
        when(asuRepository.countByENTAndMTACOD(1, 10))
            .thenReturn(0);

        when(mtaRepository.existsById(any()))
            .thenReturn(false);

        mockMvc.perform(delete("/api/mta/delete-almacenaje/1/10"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void shouldDeleteAlmacenaje_returns204WhenDeleted() throws Exception {
        when(asuRepository.countByENTAndMTACOD(1, 10))
            .thenReturn(0);

        when(mtaRepository.existsById(any()))
            .thenReturn(true);

        mockMvc.perform(delete("/api/mta/delete-almacenaje/1/10"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(mtaRepository).deleteById(any());
    }

    @Test
    void shouldDeleteAlmacenaje_returns500OnAsuDataAccessException() throws Exception {
        when(asuRepository.countByENTAndMTACOD(1, 10))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(delete("/api/mta/delete-almacenaje/1/10"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }

    @Test
    void shouldDeleteAlmacenaje_returns500OnMtaDataAccessException() throws Exception {
        when(asuRepository.countByENTAndMTACOD(1, 10))
            .thenReturn(0);

        when(mtaRepository.existsById(any()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(delete("/api/mta/delete-almacenaje/1/10"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }
}