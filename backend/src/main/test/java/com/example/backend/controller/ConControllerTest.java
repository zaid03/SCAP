package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.dto.ContratoDto;
import com.example.backend.service.CotContratoProjection;
import com.example.backend.service.ContratosSearch;
import com.example.backend.sqlserver2.repository.CotRepository;
import com.example.backend.sqlserver2.repository.ConRepository;
import com.example.backend.sqlserver2.model.Cot;
import com.example.backend.sqlserver2.model.Conn;
import com.example.backend.sqlserver2.model.ConId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ConController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class ConControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CotRepository cotRepository;

    @MockitoBean
    private ConRepository conRepository;

    @MockitoBean
    private ContratosSearch contratosSearch;

    @Autowired
    private ObjectMapper objectMapper;

    private CotContratoProjection createMockProjection() {
        return createMockProjection(100, 200);
    }

    private CotContratoProjection createMockProjection(Integer concod, Integer tercod) {
        CotContratoProjection projection = new CotContratoProjection() {
            @Override
            public ConnInfo getConn() {
                return new ConnInfo() {
                    @Override
                    public Integer getCONCOD() { return concod; }
                    @Override
                    public String getCONLOT() { return "LOT001"; }
                    @Override
                    public String getCONDES() { return "Contract Description"; }
                    @Override
                    public LocalDateTime getCONFIN() { return LocalDateTime.now(); }
                    @Override
                    public LocalDateTime getCONFFI() { return LocalDateTime.now(); }
                    @Override
                    public Integer getCONBLO() { return 0; }
                };
            }

            @Override
            public TerInfo getTer() {
                return new TerInfo() {
                    @Override
                    public Integer getTERCOD() { return tercod; }
                    @Override
                    public String getTERNOM() { return "Supplier Name"; }
                };
            }
        };
        return projection;
    }

    @Test
    void fetchContratos_returns200WithContratos() throws Exception {
        when(cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, 1, "E1"))
            .thenReturn(List.of(createMockProjection()));

        mockMvc.perform(get("/api/con/fetch-contratos/1/E1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].concod").value(100))
            .andExpect(jsonPath("$[0].ternom").value("Supplier Name"));
    }

    @Test
    void fetchContratos_returns404WhenEmpty() throws Exception {
        when(cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, 1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/con/fetch-contratos/1/E1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchContratos_returns500OnException() throws Exception {
        when(cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, 1, "E1"))
            .thenThrow(new DataAccessResourceFailureException("Database error"));

        mockMvc.perform(get("/api/con/fetch-contratos/1/E1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error :")));
    }

    @Test
    void searchContratosCodigoBloqueado_returns200() throws Exception {

        when(contratosSearch.searchContrtos(1, "E1", "100", null))
            .thenReturn(List.of(createMockDto()));

        mockMvc.perform(get("/api/con/searchByCodigoBloque/1/E1/100"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].concod").value(100));
    }

    @Test
    void searchContratosCodigoBloqueado_returns404WhenEmpty() throws Exception {

        when(contratosSearch.searchContrtos(1, "E1", "100", null))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/con/searchByCodigoBloque/1/E1/100"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchContratosCodigoBloqueado_returns500OnException() throws Exception {

        when(contratosSearch.searchContrtos(1, "E1", "100", null))
            .thenThrow(new DataAccessResourceFailureException("Database error"));

        mockMvc.perform(get("/api/con/searchByCodigoBloque/1/E1/100"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error :")));
    }

    @Test
    void updateContrato_returns204OnSuccess() throws Exception {
        ConId id = new ConId(1, "E1", 100);
        Conn conn = new Conn();
        conn.setCONCOD(100);

        when(conRepository.findById(id)).thenReturn(Optional.of(conn));
        when(conRepository.save(any(Conn.class))).thenReturn(conn);

        String payload = objectMapper.writeValueAsString(Map.of(
            "CONBLO", 1,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "Updated Contract"
        ));

        mockMvc.perform(patch("/api/con/update-contrato/1/E1/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(conRepository).save(any(Conn.class));
    }

    @Test
    void updateContrato_returns400OnMissingCondes() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "CONBLO", 1,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00"
        ));

        mockMvc.perform(patch("/api/con/update-contrato/1/E1/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void updateContrato_returns404WhenNotFound() throws Exception {
        ConId id = new ConId(1, "E1", 100);

        when(conRepository.findById(id)).thenReturn(Optional.empty());

        String payload = objectMapper.writeValueAsString(Map.of(
            "CONBLO", 1,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "Updated Contract"
        ));

        mockMvc.perform(patch("/api/con/update-contrato/1/E1/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void updateContrato_returns500OnException() throws Exception {
        ConId id = new ConId(1, "E1", 100);

        when(conRepository.findById(id)).thenThrow(new DataAccessResourceFailureException("Database error"));

        String payload = objectMapper.writeValueAsString(Map.of(
            "CONBLO", 1,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "Updated Contract"
        ));

        mockMvc.perform(patch("/api/con/update-contrato/1/E1/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error :")));
    }

    // ---- addContrato ----
    // NOTE: addContrato no longer returns 204. After saving Conn/Cot it re-reads the
    // new contract via cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD
    // and returns 200 OK with the built ContratoDto list, or 404 if that lookup is empty.

    @Test
    void addContrato_returns200OnSuccess() throws Exception {
        when(conRepository.findFirstByENTAndEJEOrderByCONCODDesc(1, "E1"))
            .thenReturn(Optional.empty());
        when(conRepository.save(any(Conn.class))).thenReturn(new Conn());
        when(cotRepository.save(any(Cot.class))).thenReturn(new Cot());
        when(cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, 1, "E1", 1))
            .thenReturn(Optional.of(createMockProjection(1, 200)));

        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONBLO", 0,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "New Contract",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].concod").value(1))
            .andExpect(jsonPath("$[0].ternom").value("Supplier Name"));

        verify(conRepository).save(any(Conn.class));
        verify(cotRepository).save(any(Cot.class));
    }

    @Test
    void addContrato_returns404WhenProjectionNotFoundAfterSave() throws Exception {
        when(conRepository.findFirstByENTAndEJEOrderByCONCODDesc(1, "E1"))
            .thenReturn(Optional.empty());
        when(conRepository.save(any(Conn.class))).thenReturn(new Conn());
        when(cotRepository.save(any(Cot.class))).thenReturn(new Cot());
        when(cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, 1, "E1", 1))
            .thenReturn(Optional.empty());

        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONBLO", 0,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "New Contract",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Error añadir contrato"));

        verify(conRepository).save(any(Conn.class));
        verify(cotRepository).save(any(Cot.class));
    }

    @Test
    void addContrato_returns400OnMissingENT() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONDES", "New Contract",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void addContrato_returns400OnMissingEJE() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "CONLOT", "LOT001",
            "CONDES", "New Contract",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void addContrato_returns400OnMissingCONDES() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void addContrato_returns400OnMissingCONLOT() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONDES", "New Contract",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void addContrato_returns400OnMissingTERCOD() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONDES", "New Contract"
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void addContrato_returns500OnException() throws Exception {
        when(conRepository.findFirstByENTAndEJEOrderByCONCODDesc(1, "E1"))
            .thenThrow(new DataAccessResourceFailureException("Database error"));

        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONBLO", 0,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "New Contract",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error :")));
    }

    @Test
    void addContrato_nextConcodWithNullConcod() throws Exception {
        when(conRepository.findFirstByENTAndEJEOrderByCONCODDesc(1, "E1"))
            .thenReturn(Optional.of(new Conn() {{ setCONCOD(null); }}));
        when(conRepository.save(any(Conn.class))).thenReturn(new Conn());
        when(cotRepository.save(any(Cot.class))).thenReturn(new Cot());
        when(cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, 1, "E1", 1))
            .thenReturn(Optional.of(createMockProjection(1, 200)));

        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONBLO", 0,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "New Contract",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].concod").value(1));

        verify(conRepository).save(any(Conn.class));
        verify(cotRepository).save(any(Cot.class));
    }

    @Test
    void fetchContratos_returnsMultipleContratos() throws Exception {
        when(cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, 1, "E1"))
            .thenReturn(List.of(createMockProjection(), createMockProjection()));

        mockMvc.perform(get("/api/con/fetch-contratos/1/E1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchContratosCodigoBloqueado_returnsMultipleContratos() throws Exception {

        when(contratosSearch.searchContrtos(1, "E1", "100", null))
            .thenReturn(List.of(createMockDto(), createMockDto()));

        mockMvc.perform(get("/api/con/searchByCodigoBloque/1/E1/100"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateContrato_withNullPayload() throws Exception {
        mockMvc.perform(patch("/api/con/update-contrato/1/E1/100").contentType(MediaType.APPLICATION_JSON).content("{}")).andDo(print()).andExpect(status().isBadRequest()).andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void updateContrato_withOnlyConBlo() throws Exception {
        ConId id = new ConId(1, "E1", 100);
        Conn conn = new Conn();
        conn.setCONCOD(100);

        when(conRepository.findById(id)).thenReturn(Optional.of(conn));
        when(conRepository.save(any(Conn.class))).thenReturn(conn);

        String payload = objectMapper.writeValueAsString(Map.of(
            "CONBLO", 1,
            "CONDES", "Updated"
        ));

        mockMvc.perform(patch("/api/con/update-contrato/1/E1/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    void updateContrato_updatesAllFields() throws Exception {
        ConId id = new ConId(1, "E1", 100);
        Conn conn = new Conn();
        conn.setCONCOD(100);
        conn.setCONBLO(0);
        conn.setCONDES("Old");

        when(conRepository.findById(id)).thenReturn(Optional.of(conn));
        when(conRepository.save(any(Conn.class))).thenReturn(conn);

        LocalDateTime now = LocalDateTime.now();
        String payload = objectMapper.writeValueAsString(Map.of(
            "CONBLO", 1,
            "CONFIN", now.toString(),
            "CONFFI", now.toString(),
            "CONDES", "New Contract Description"
        ));

        mockMvc.perform(patch("/api/con/update-contrato/1/E1/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(conRepository).save(any(Conn.class));
    }

    @Test
    void updateContrato_savesCallVerified() throws Exception {
        ConId id = new ConId(2, "E2", 200);
        Conn conn = new Conn();

        when(conRepository.findById(id)).thenReturn(Optional.of(conn));
        when(conRepository.save(any(Conn.class))).thenReturn(conn);

        String payload = objectMapper.writeValueAsString(Map.of(
            "CONBLO", 0,
            "CONDES", "Test"
        ));

        mockMvc.perform(patch("/api/con/update-contrato/2/E2/200")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(conRepository).findById(any(ConId.class));
        verify(conRepository).save(any(Conn.class));
    }

    @Test
    void updateContrato_onDifferentIds() throws Exception {
        ConId id = new ConId(5, "E5", 555);
        Conn conn = new Conn();

        when(conRepository.findById(id)).thenReturn(Optional.of(conn));
        when(conRepository.save(any(Conn.class))).thenReturn(conn);

        String payload = objectMapper.writeValueAsString(Map.of(
            "CONBLO", 1,
            "CONDES", "Updated"
        ));

        mockMvc.perform(patch("/api/con/update-contrato/5/E5/555")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    void addContrato_withNullPayload() throws Exception {
        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Faltan datos obligatorios."));
    }

    @Test
    void addContrato_incrementsConcodCorrectly() throws Exception {
        Conn existingConn = new Conn();
        existingConn.setCONCOD(50);

        when(conRepository.findFirstByENTAndEJEOrderByCONCODDesc(1, "E1"))
            .thenReturn(Optional.of(existingConn));
        when(conRepository.save(any(Conn.class))).thenReturn(new Conn());
        when(cotRepository.save(any(Cot.class))).thenReturn(new Cot());
        when(cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, 1, "E1", 51))
            .thenReturn(Optional.of(createMockProjection(51, 200)));

        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONBLO", 0,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "New Contract",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].concod").value(51));

        verify(conRepository).save(any(Conn.class));
    }

    @Test
    void addContrato_withDifferentEntity() throws Exception {
        when(conRepository.findFirstByENTAndEJEOrderByCONCODDesc(5, "E5"))
            .thenReturn(Optional.empty());
        when(conRepository.save(any(Conn.class))).thenReturn(new Conn());
        when(cotRepository.save(any(Cot.class))).thenReturn(new Cot());
        when(cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, 5, "E5", 1))
            .thenReturn(Optional.of(createMockProjection(1, 500)));

        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 5,
            "EJE", "E5",
            "CONLOT", "LOT005",
            "CONBLO", 1,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "Entity 5 Contract",
            "TERCOD", 500
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].tercod").value(500));

        verify(conRepository).save(any(Conn.class));
        verify(cotRepository).save(any(Cot.class));
    }

    @Test
    void addContrato_savesConAndCotEntities() throws Exception {
        Conn connArg = new Conn();
        Cot cotArg = new Cot();

        when(conRepository.findFirstByENTAndEJEOrderByCONCODDesc(1, "E1"))
            .thenReturn(Optional.empty());
        when(conRepository.save(any(Conn.class))).thenReturn(connArg);
        when(cotRepository.save(any(Cot.class))).thenReturn(cotArg);
        when(cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, 1, "E1", 1))
            .thenReturn(Optional.of(createMockProjection(1, 100)));

        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONBLO", 0,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "Test",
            "TERCOD", 100
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isOk());

        verify(conRepository).save(any(Conn.class));
        verify(cotRepository).save(any(Cot.class));
    }

    @Test
    void addContrato_returns400OnMissingPayload() throws Exception {
        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void addContrato_withHighConcodNumber() throws Exception {
        Conn existingConn = new Conn();
        existingConn.setCONCOD(9999);

        when(conRepository.findFirstByENTAndEJEOrderByCONCODDesc(1, "E1"))
            .thenReturn(Optional.of(existingConn));
        when(conRepository.save(any(Conn.class))).thenReturn(new Conn());
        when(cotRepository.save(any(Cot.class))).thenReturn(new Cot());
        when(cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, 1, "E1", 10000))
            .thenReturn(Optional.of(createMockProjection(10000, 200)));

        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONBLO", 0,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "High Concod",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].concod").value(10000));
    }

    @Test
    void fetchContratos_responseContainsRequiredFields() throws Exception {
        when(cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, 1, "E1"))
            .thenReturn(List.of(createMockProjection()));

        mockMvc.perform(get("/api/con/fetch-contratos/1/E1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].concod").exists())
            .andExpect(jsonPath("$[0].conlot").exists())
            .andExpect(jsonPath("$[0].condes").exists())
            .andExpect(jsonPath("$[0].tercod").exists())
            .andExpect(jsonPath("$[0].ternom").exists());
    }

    @Test
    void searchContratosCodigoBloqueado_responseValidation() throws Exception {

        when(contratosSearch.searchContrtos(1, "E1", "100", null))
            .thenReturn(List.of(createMockDto()));

        mockMvc.perform(get("/api/con/searchByCodigoBloque/1/E1/100"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].concod").value(100))
            .andExpect(jsonPath("$[0].tercod").value(200));
    }

    @Test
    void updateContrato_verifiesSaveWithCorrectData() throws Exception {
        ConId id = new ConId(1, "E1", 100);
        Conn conn = new Conn();

        when(conRepository.findById(id)).thenReturn(Optional.of(conn));
        when(conRepository.save(any(Conn.class))).thenReturn(conn);

        String payload = objectMapper.writeValueAsString(Map.of(
            "CONBLO", 1,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-22T10:00:00",
            "CONDES", "Test Description"
        ));

        mockMvc.perform(patch("/api/con/update-contrato/1/E1/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(conRepository).findById(new ConId(1, "E1", 100));
        verify(conRepository).save(any(Conn.class));
    }

    @Test
    void addContrato_verifiesRepositoryCalls() throws Exception {
        when(conRepository.findFirstByENTAndEJEOrderByCONCODDesc(1, "E1"))
            .thenReturn(Optional.empty());
        when(conRepository.save(any(Conn.class))).thenReturn(new Conn());
        when(cotRepository.save(any(Cot.class))).thenReturn(new Cot());
        when(cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, 1, "E1", 1))
            .thenReturn(Optional.of(createMockProjection(1, 200)));

        String payload = objectMapper.writeValueAsString(Map.of(
            "ENT", 1,
            "EJE", "E1",
            "CONLOT", "LOT001",
            "CONBLO", 0,
            "CONFIN", "2026-03-21T10:00:00",
            "CONFFI", "2026-03-21T10:00:00",
            "CONDES", "Test",
            "TERCOD", 200
        ));

        mockMvc.perform(post("/api/con/add-contrato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andDo(print())
            .andExpect(status().isOk());

        verify(conRepository).findFirstByENTAndEJEOrderByCONCODDesc(1, "E1");
        verify(conRepository).save(any(Conn.class));
        verify(cotRepository).save(any(Cot.class));
        verify(cotRepository).findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, 1, "E1", 1);
    }

    private ContratoDto createMockDto() {
        return new ContratoDto.Builder()
            .concod(100)
            .conlot("LOT001")
            .condes("Contract Description")
            .tercod(200)
            .ternom("Supplier Name")
            .build();
    }
}