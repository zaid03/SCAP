package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.magcodOnly;
import com.example.backend.sqlserver2.model.Mat;
import com.example.backend.sqlserver2.model.Mta;
import com.example.backend.sqlserver2.model.Mag;
import com.example.backend.sqlserver2.repository.MatRepository;
import com.example.backend.sqlserver2.repository.MagRepository;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MatController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class MatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatRepository matRepository;
    
    @MockitoBean
    private MagRepository magRepository;
    
    @MockitoBean
    private MtaRepository mtaRepository;
    
    @Test
    void shouldReturnNotFoundWhenNoMatchingRecords() throws Exception {
        when(magRepository.findByENTAndDEPCOD(2, "D1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mat/fetch-almacenajes/2/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(magRepository).findByENTAndDEPCOD(2, "D1");
    }

    @Test
    void shouldReturnBadRequestOnDataAccessException() throws Exception {
        when(magRepository.findByENTAndDEPCOD(anyInt(), any())).thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/mat/fetch-almacenajes/1/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error:")));
    }

    @Test
    void shouldReturnDistinctAlmacen_whenRecordsMatch() throws Exception {
        magcodOnly magProjection = mock(magcodOnly.class);
        when(magProjection.getMAGCOD()).thenReturn(1);

        Mag mag = new Mag();
        mag.setMAGCOD(1);
        mag.setDEPCOD("D1");

        Mta mta1 = new Mta();
        mta1.setMTACOD(11);
        mta1.setMTADES("Almacen A");

        Mat mat1 = new Mat();
        mat1.setMag(mag);
        mat1.setMta(mta1);

        Mat mat2 = new Mat();
        mat2.setMag(mag);
        mat2.setMta(mta1);

        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenReturn(Optional.of(magProjection));

        when(matRepository.findByENTAndMAGCOD(1, 1))
            .thenReturn(List.of(mat1, mat2));

        when(mtaRepository.findFirstByENTAndMTACOD(1, 11))
            .thenReturn(Optional.of(mta1));

        mockMvc.perform(get("/api/mat/fetch-almacenajes/1/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].mtacod").value(11))
            .andExpect(jsonPath("$[0].mtades").value("Almacen A"));

        verify(magRepository).findByENTAndDEPCOD(1, "D1");
        verify(matRepository).findByENTAndMAGCOD(1, 1);
        verify(mtaRepository, times(2))
            .findFirstByENTAndMTACOD(1, 11);
    }

    @Test
    void shouldReturnNotFoundWhenMatRecordsAreEmpty() throws Exception {
        magcodOnly magProjection = mock(magcodOnly.class);
        when(magProjection.getMAGCOD()).thenReturn(1);

        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenReturn(Optional.of(magProjection));

        when(matRepository.findByENTAndMAGCOD(1, 1))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/mat/fetch-almacenajes/1/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(matRepository).findByENTAndMAGCOD(1, 1);
        verifyNoInteractions(mtaRepository);
    }

    @Test
    void shouldIgnoreMatWhenMtaIsNotFound() throws Exception {
        magcodOnly magProjection = mock(magcodOnly.class);
        when(magProjection.getMAGCOD()).thenReturn(1);

        Mag mag = new Mag();
        mag.setMAGCOD(1);
        mag.setDEPCOD("D1");

        Mta mta = new Mta();
        mta.setMTACOD(11);

        Mat mat = new Mat();
        mat.setMag(mag);
        mat.setMta(mta);

        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenReturn(Optional.of(magProjection));

        when(matRepository.findByENTAndMAGCOD(1, 1))
            .thenReturn(List.of(mat));

        when(mtaRepository.findFirstByENTAndMTACOD(1, 11))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mat/fetch-almacenajes/1/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        verify(mtaRepository).findFirstByENTAndMTACOD(1, 11);
    }

    @Test
    void shouldReturnBadRequestOnMatRepositoryException() throws Exception {
        magcodOnly magProjection = mock(magcodOnly.class);
        when(magProjection.getMAGCOD()).thenReturn(1);

        when(magRepository.findByENTAndDEPCOD(1, "D1"))
            .thenReturn(Optional.of(magProjection));

        when(matRepository.findByENTAndMAGCOD(1, 1))
            .thenThrow(new DataAccessResourceFailureException("DB connection lost"));

        mockMvc.perform(get("/api/mat/fetch-almacenajes/1/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error: DB connection lost")));
    }
}