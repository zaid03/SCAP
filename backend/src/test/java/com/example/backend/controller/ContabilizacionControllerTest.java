package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.dto.ContabilizacionRequestDto;
import com.example.backend.dto.ContabilizacionResponseDto;
import com.example.backend.service.ContabilizacionService;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.FacId;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.Fdt;
import com.example.backend.sqlserver2.model.Ter;
import com.example.backend.sqlserver2.repository.FacRepository;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.example.backend.sqlserver2.repository.FdtRepository;
import com.example.backend.sqlserver2.repository.TerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.anyDouble;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContabilizacionController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class ContabilizacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContabilizacionService contabilizacionService;

    @MockitoBean
    private FacRepository facRepository;

    @MockitoBean
    private FdeRepository fdeRepository;

    @MockitoBean
    private FdtRepository fdtRepository;

    @MockitoBean
    private TerRepository terRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generarOperacion_returns400WhenMissingEnt() throws Exception {
        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt(null);
        request.setEje("E1");
        request.setFacnum(50);

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void generarOperacion_returns400WhenMissingEje() throws Exception {
        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(1);
        request.setEje(null);
        request.setFacnum(50);

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void generarOperacion_returns400WhenMissingFacnum() throws Exception {
        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(1);
        request.setEje("E1");
        request.setFacnum(null);

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }

    @Test
    void generarOperacion_returns200OnResponseFailure() throws Exception {
        Fac fac = new Fac();
        fac.setTERCOD(100);
        FacId facId = new FacId(1, "2026", 50);
        when(facRepository.findById(facId)).thenReturn(Optional.of(fac));

            when(terRepository.findByENTAndTERCOD(1, 100)).thenReturn(Optional.empty());
            when(fdeRepository.findByENTAndEJEAndFACNUM(1, "2026", 50)).thenReturn(List.of());
            when(fdtRepository.findByENTAndEJEAndFACNUM(1, "2026", 50)).thenReturn(List.of());
        String smlInput = "<sml>test</sml>";
        String soapResponse = "<response>failure</response>";
        when(contabilizacionService.buildSmlInput(any(), any(), any(), any(), isNull())).thenReturn(smlInput);
        when(contabilizacionService.sendSmlRequest(smlInput, "http://ws.url")).thenReturn(soapResponse);

        ContabilizacionResponseDto responseDto = new ContabilizacionResponseDto();
        responseDto.setExito(false);
        responseDto.setMensaje("Operation failed");
        when(contabilizacionService.parseResponse(soapResponse)).thenReturn(responseDto);

        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(1);
        request.setEje("2026");
        request.setFacnum(50);
        request.setWebserviceUrl("http://ws.url");
        request.setFechaContable("2026-03-21");

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exito").value(false));
    }

    @Test
    void generarOperacion_returns400WhenMissingEntcod() throws Exception {
        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(null);
        request.setEje("2026");
        request.setFacnum(50);

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Faltan datos obligatorios")));
    }
   
    @Test
    void generarOperacion_returns400WhenMissingFechaContable() throws Exception {
        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(1);
        request.setEje("2026");
        request.setFacnum(50);
        request.setFechaContable(null);

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exito").value(false))
            .andExpect(jsonPath("$.mensaje").value("Falta fecha contable"));
    }

    @Test
    void generarOperacion_returns400WhenFechaContableBlank() throws Exception {
        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(1);
        request.setEje("2026");
        request.setFacnum(50);
        request.setFechaContable("   ");

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exito").value(false))
            .andExpect(jsonPath("$.mensaje").value("Falta fecha contable"));
    }

    @Test
    void generarOperacion_returns400WhenFechaContableTooShort() throws Exception {
        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(1);
        request.setEje("2026");
        request.setFacnum(50);
        request.setFechaContable("26");

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exito").value(false))
            .andExpect(jsonPath("$.mensaje").value("Falta fecha contable"));
    }

    @Test
    void generarOperacion_returns400WhenFechaYearDoesNotMatchEje() throws Exception {
        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(1);
        request.setEje("2026");
        request.setFacnum(50);
        request.setFechaContable("2025-03-21");

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exito").value(false))
            .andExpect(jsonPath("$.mensaje")
                .value("La fecha contable debe pertenecer al ejercicio contable"));
    }

    @Test
    void generarOperacion_returns200OnSuccess() throws Exception {
        Fac fac = new Fac();
        fac.setTERCOD(100);

        FacId facId = new FacId(1, "2026", 50);

        when(facRepository.findById(facId))
            .thenReturn(Optional.of(fac));

        when(terRepository.findByENTAndTERCOD(1, 100))
            .thenReturn(Optional.empty());

        when(fdeRepository.findByENTAndEJEAndFACNUM(1, "2026", 50))
            .thenReturn(List.of());

        when(fdtRepository.findByENTAndEJEAndFACNUM(1, "2026", 50))
            .thenReturn(List.of());

        String smlInput = "<sml>test</sml>";
        String soapResponse = "<response>success</response>";

        when(contabilizacionService.buildSmlInput(
                any(), any(), any(), any(), isNull()))
            .thenReturn(smlInput);

        when(contabilizacionService.sendSmlRequest(
                smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        ContabilizacionResponseDto responseDto = new ContabilizacionResponseDto();
        responseDto.setExito(true);
        responseDto.setMensaje("Operation successful");
        responseDto.setOpesical("OP123");

        when(contabilizacionService.parseResponse(soapResponse))
            .thenReturn(responseDto);

        when(facRepository.save(any(Fac.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(1);
        request.setEje("2026");
        request.setFacnum(50);
        request.setWebserviceUrl("http://ws.url");
        request.setFechaContable("2026-03-21");

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exito").value(true))
            .andExpect(jsonPath("$.opesical").value("OP123"));

        verify(facRepository).save(any(Fac.class));
        verify(contabilizacionService)
            .actualizarAcumulados(any(), any(), any(), eq(0.0));
    }

    @Test
    void generarOperacion_returns200WhenActualizarAcumuladosFails() throws Exception {
        Fac fac = new Fac();
        fac.setTERCOD(null);

        FacId facId = new FacId(1, "2026", 50);

        when(facRepository.findById(facId))
            .thenReturn(Optional.of(fac));

        when(fdeRepository.findByENTAndEJEAndFACNUM(1, "2026", 50))
            .thenReturn(List.of());

        when(fdtRepository.findByENTAndEJEAndFACNUM(1, "2026", 50))
            .thenReturn(List.of());

        String smlInput = "<sml>test</sml>";
        String soapResponse = "<response>success</response>";

        when(contabilizacionService.buildSmlInput(
                any(), any(), any(), any(), isNull()))
            .thenReturn(smlInput);

        when(contabilizacionService.sendSmlRequest(
                smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        ContabilizacionResponseDto responseDto = new ContabilizacionResponseDto();
        responseDto.setExito(true);
        responseDto.setMensaje("Operation successful");

        when(contabilizacionService.parseResponse(soapResponse))
            .thenReturn(responseDto);

        when(facRepository.save(any(Fac.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new RuntimeException("acumulados error"))
            .when(contabilizacionService)
            .actualizarAcumulados(any(), any(), any(), anyDouble());

        ContabilizacionRequestDto request = new ContabilizacionRequestDto();
        request.setEnt("1");
        request.setEntcod(1);
        request.setEje("2026");
        request.setFacnum(50);
        request.setWebserviceUrl("http://ws.url");
        request.setFechaContable("2026-03-21");

        mockMvc.perform(post("/api/contabilizacion/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exito").value(true))
            .andExpect(jsonPath("$.mensaje")
                .value(containsString("acumulados error")));
    }
}
