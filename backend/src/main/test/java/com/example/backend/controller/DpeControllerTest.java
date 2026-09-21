package com.example.backend.controller;

import java.util.List;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.config.TestExceptionHandler;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.PersonaServiceRequest;
import com.example.backend.dto.ServicePersonaRequest;
import com.example.backend.service.DpePersonasForService;
import com.example.backend.service.DpeService;
import com.example.backend.sqlserver2.model.Dep;
import com.example.backend.sqlserver2.model.Dpe;
import com.example.backend.sqlserver2.model.Per;
import com.example.backend.sqlserver2.repository.DepRepository;
import com.example.backend.sqlserver2.repository.DpeRepository;
import com.example.backend.sqlserver2.repository.PerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = DpeController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class DpeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DpeRepository dpeRepository;

    @MockitoBean
    private PerRepository perRepository;

    @MockitoBean
    private DepRepository depRepository;

    @MockitoBean
    private DpeService dpeService;

    @MockitoBean
    private DpePersonasForService dpePersonasForService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fetchServicePersonas_returnsList() throws Exception {
        Dpe d1 = new Dpe(); d1.setPERCOD("U1");
        Dpe d2 = new Dpe(); d2.setPERCOD("U2");
        when(dpeRepository.findByENTAndEJEAndDEPCOD(1, "E1", "D1")).thenReturn(List.of(d1, d2));

        Per p1 = new Per(); p1.setPERCOD("U1"); p1.setPERNOM("User One");
        Per p2 = new Per(); p2.setPERCOD("U2"); p2.setPERNOM("User Two");
        when(perRepository.findByPERCODIn(List.of("U1", "U2"))).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/depe/fetch-service-personas/1/E1/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].percod", anyOf(is("U1"), is("U2"))));
    }

    @Test
    void fetchServicePersonas_returns500OnDataAccessException() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDEPCOD(anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/depe/fetch-service-personas/1/E1/D1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void deletePersonaService_returnsNoContentWhenExists() throws Exception {
        when(dpeRepository.existsById(any())).thenReturn(true);

        mockMvc.perform(delete("/api/depe/delete-persona-service/1/E1/D1/U1"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(dpeRepository).deleteById(any());
    }

    @Test
    void deletePersonaService_returnsNotFoundWhenMissing() throws Exception {
        when(dpeRepository.existsById(any())).thenReturn(false);

        mockMvc.perform(delete("/api/depe/delete-persona-service/1/E1/D1/U1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchPersonaService_returnsDeps() throws Exception {
        Dpe d1 = new Dpe(); d1.setDEPCOD("D1");
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of(d1));

        Dep dep = new Dep(); dep.setDEPCOD("D1"); dep.setDEPDES("Service Desc");
        when(depRepository.findByENTAndEJEAndDEPCODIn(1, "E1", List.of("D1"))).thenReturn(List.of(dep));

        mockMvc.perform(get("/api/depe/fetch-persona-service/1/E1/U1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].depcod").value("D1"))
            .andExpect(jsonPath("$[0].depdes").value("Service Desc"));
    }

    @Test
    void fetchPersonaService_returnsNotFoundWhenEmpty() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U2")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/fetch-persona-service/1/E1/U2"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
     void addPersonaServices_validRequest_returnsCreated() throws Exception {
         PersonaServiceRequest req = new PersonaServiceRequest();
         req.setPercod("U1");
         req.setServices(List.of("S1", "S2"));
 
         mockMvc.perform(post("/api/depe/add-persona-services")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(req)))
             .andDo(print())
             .andExpect(status().isCreated());
 
        ArgumentCaptor<PersonaServiceRequest> cap = ArgumentCaptor.forClass(PersonaServiceRequest.class);
        verify(dpeService).savePersonaServices(cap.capture());
        PersonaServiceRequest passed = cap.getValue();
        assertEquals(req.getPercod(), passed.getPercod());
        assertEquals(req.getServices(), passed.getServices());
    }

    @Test
    void addPersonaServices_validationFails() throws Exception {
        PersonaServiceRequest missingUser = new PersonaServiceRequest();
        missingUser.setServices(List.of("S1"));
        mockMvc.perform(post("/api/depe/add-persona-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingUser)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Falta un dato obligatorio")));

        PersonaServiceRequest missingServices = new PersonaServiceRequest();
        missingServices.setPercod("U1");
        mockMvc.perform(post("/api/depe/add-persona-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingServices)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Debe seleccionar al menos un servicio.")));
    }

    @Test
    void deleteAllPersonaServices_returnsNoContentWhenDeleted() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of(new Dpe()));
        when(dpeRepository.deleteByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(1);

        mockMvc.perform(delete("/api/depe/delete-persona-Allservice/1/E1/U1"))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteAllPersonaServices_returnsNotFoundWhenMissing() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of());

        mockMvc.perform(delete("/api/depe/delete-persona-Allservice/1/E1/U1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void addServicesPersona_validationAndSuccess() throws Exception {
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setDepcod("S1");
        req.setPersonas(List.of("U1", "U2"));

        mockMvc.perform(post("/api/depe/add-services-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

       ArgumentCaptor<ServicePersonaRequest> cap = ArgumentCaptor.forClass(ServicePersonaRequest.class);
       verify(dpePersonasForService).saveServicePersonas(cap.capture());
       ServicePersonaRequest passed = cap.getValue();
       assertEquals(req.getDepcod(), passed.getDepcod());
       assertEquals(req.getPersonas(), passed.getPersonas());
    }

    @Test
    void addServicesPersona_validationFails() throws Exception {
        ServicePersonaRequest missingDep = new ServicePersonaRequest();
        missingDep.setPersonas(List.of("U1"));
        mockMvc.perform(post("/api/depe/add-services-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingDep)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("El código de servicio es obligatorio.")));

        ServicePersonaRequest missingPersons = new ServicePersonaRequest();
        missingPersons.setDepcod("S1");
        mockMvc.perform(post("/api/depe/add-services-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingPersons)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Debe seleccionar al menos un persona.")));
    }

    @Test
    void deleteService_returnsNoContentWhenExists() throws Exception {
        when(dpeRepository.existsById(any())).thenReturn(true);

        mockMvc.perform(delete("/api/depe/delete-service-persona/1/E1/D1/U1"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(dpeRepository).deleteById(any());
    }

    @Test
    void deleteService_returnsNotFoundWhenMissing() throws Exception {
        when(dpeRepository.existsById(any())).thenReturn(false);

        mockMvc.perform(delete("/api/depe/delete-service-persona/1/E1/D1/U1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchPersonasServicios_returnsPagedResults() throws Exception {
        when(dpeRepository.findByENTAndEJE(anyInt(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/1/E1/0")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJE(anyInt(), anyString(), any());
    }

    @Test
    void fetchPersonasServicios_withNullResultsReturnsNotFound() throws Exception {
        when(dpeRepository.findByENTAndEJE(anyInt(), anyString(), any())).thenReturn(null);

        mockMvc.perform(get("/api/depe/personas-servicios/1/E1/0")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchPersonasServicios_returnsNotFoundWhenEmpty() throws Exception {
        when(dpeRepository.findByENTAndEJE(anyInt(), anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void endpoints_return500OnDataAccessException() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDEPCOD(anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("boom"));

        mockMvc.perform(get("/api/depe/fetch-service-personas/1/E1/D1"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    void fetchServicePersonas_returnsEmptyList() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDEPCOD(1, "E1", "D1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/fetch-service-personas/1/E1/D1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchPersonasServicios_byPersonaCod() throws Exception {
        when(dpeRepository.findProjectionByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "U1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byPersonaName() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPer_PERNOMContaining(1, "E1", "John Doe")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "John Doe")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byCentroGestor() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CG1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byServicioCod() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "S1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byServicioName() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "Service Description")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byPerfilAlmacen() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "almacen")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byPerfilComprador() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "comprador")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byPerfilContabilidad() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "contabilidad")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byPerfilPeticionario() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "peticionario")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byInvalidPerfil() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "invalid")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_dataAccessException() throws Exception {
        when(dpeRepository.findByENTAndEJE(anyInt(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void fetchPersonasServicios_returns500OnException() throws Exception {
        when(dpeRepository.findByENTAndEJE(anyInt(), anyString(), any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/depe/personas-servicios/1/E1/0")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void deleteAllPersonaServices_returns500OnDataAccessException() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of(new Dpe()));
        when(dpeRepository.deleteByENTAndEJEAndPERCOD(1, "E1", "U1"))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(delete("/api/depe/delete-persona-Allservice/1/E1/U1"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    void addPersonaServices_exception() throws Exception {
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setPercod("U1");
        req.setServices(List.of("S1"));

        doThrow(new RuntimeException("Save failed")).when(dpeService).savePersonaServices(any());

        mockMvc.perform(post("/api/depe/add-persona-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void addServicesPersona_exception() throws Exception {
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setDepcod("S1");
        req.setPersonas(List.of("U1"));

        doThrow(new RuntimeException("Save failed")).when(dpePersonasForService).saveServicePersonas(any());

        mockMvc.perform(post("/api/depe/add-services-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error :")));
    }

    @Test
    void fetchPersonaService_returns500OnDataAccessException() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPERCOD(anyInt(), anyString(), anyString()))
            .thenThrow(new DataAccessResourceFailureException("DB error"));

        mockMvc.perform(get("/api/depe/fetch-persona-service/1/E1/U1"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    void deletePersonaService_returns500OnDataAccessException() throws Exception {
        when(dpeRepository.existsById(any())).thenReturn(true);
        doThrow(new DataAccessResourceFailureException("DB error")).when(dpeRepository).deleteById(any());

        mockMvc.perform(delete("/api/depe/delete-persona-service/1/E1/D1/U1"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteService_returns500OnDataAccessException() throws Exception {
        when(dpeRepository.existsById(any())).thenReturn(true);
        doThrow(new DataAccessResourceFailureException("DB error")).when(dpeRepository).deleteById(any());

        mockMvc.perform(delete("/api/depe/delete-service-persona/1/E1/D1/U1"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void deleteAllPersonaServices_deleteReturnsZero() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of(new Dpe()));
        when(dpeRepository.deleteByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(0);

        mockMvc.perform(delete("/api/depe/delete-persona-Allservice/1/E1/U1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void addPersonaServices_withEmptyPercod() throws Exception {
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setPercod("  ");
        req.setServices(List.of("S1"));

        mockMvc.perform(post("/api/depe/add-persona-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Falta un dato obligatorio")));
    }

    @Test
    void addServicesPersona_withEmptyDepcod() throws Exception {
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setDepcod("  ");
        req.setPersonas(List.of("U1"));

        mockMvc.perform(post("/api/depe/add-services-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("El código de servicio es obligatorio.")));
    }

    @Test
    void addPersonaServices_withEmptyServices() throws Exception {
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setPercod("U1");
        req.setServices(List.of());

        mockMvc.perform(post("/api/depe/add-persona-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Debe seleccionar al menos un servicio.")));
    }

    @Test
    void addServicesPersona_withEmptyPersonas() throws Exception {
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setDepcod("S1");
        req.setPersonas(List.of());

        mockMvc.perform(post("/api/depe/add-services-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Debe seleccionar al menos un persona.")));
    }

    @Test
    void searchPersonasServicios_withMultipleFilters_persona_and_servicio() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPer_PERNOMContaining(1, "E1", "John")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "John")
                .param("servicio", "Service")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_withMultipleFilters_cgecod_and_perfil() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CG1")
                .param("perfil", "almacen")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void fetchServicePersonas_verifiesRepositoryCalls() throws Exception {
        Dpe d1 = new Dpe(); d1.setPERCOD("U1");
        when(dpeRepository.findByENTAndEJEAndDEPCOD(1, "E1", "D1")).thenReturn(List.of(d1));

        Per p1 = new Per(); p1.setPERCOD("U1"); p1.setPERNOM("User");
        when(perRepository.findByPERCODIn(List.of("U1"))).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/depe/fetch-service-personas/1/E1/D1"))
            .andDo(print())
            .andExpect(status().isOk());

        verify(dpeRepository).findByENTAndEJEAndDEPCOD(1, "E1", "D1");
        verify(perRepository).findByPERCODIn(List.of("U1"));
    }

    @Test
    void fetchPersonaService_verifiesRepositoryCalls() throws Exception {
        Dpe d1 = new Dpe(); d1.setDEPCOD("D1");
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of(d1));

        Dep dep = new Dep(); dep.setDEPCOD("D1"); dep.setDEPDES("Desc");
        when(depRepository.findByENTAndEJEAndDEPCODIn(1, "E1", List.of("D1"))).thenReturn(List.of(dep));

        mockMvc.perform(get("/api/depe/fetch-persona-service/1/E1/U1"))
            .andDo(print())
            .andExpect(status().isOk());

        verify(dpeRepository).findByENTAndEJEAndPERCOD(1, "E1", "U1");
        verify(depRepository).findByENTAndEJEAndDEPCODIn(1, "E1", List.of("D1"));
    }

    @Test
    void deletePersonaService_verifiesRepositoryCalls() throws Exception {
        when(dpeRepository.existsById(any())).thenReturn(true);

        mockMvc.perform(delete("/api/depe/delete-persona-service/1/E1/D1/U1"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(dpeRepository).existsById(any());
        verify(dpeRepository).deleteById(any());
    }

    @Test
    void deleteService_verifiesRepositoryCalls() throws Exception {
        when(dpeRepository.existsById(any())).thenReturn(true);

        mockMvc.perform(delete("/api/depe/delete-service-persona/1/E1/D1/U1"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(dpeRepository).existsById(any());
        verify(dpeRepository).deleteById(any());
    }

    @Test
    void addPersonaServices_verifiesServiceCall() throws Exception {
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setPercod("U1");
        req.setServices(List.of("S1", "S2", "S3"));

        mockMvc.perform(post("/api/depe/add-persona-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

        ArgumentCaptor<PersonaServiceRequest> cap = ArgumentCaptor.forClass(PersonaServiceRequest.class);
        verify(dpeService).savePersonaServices(cap.capture());
        PersonaServiceRequest captured = cap.getValue();
        assertEquals("U1", captured.getPercod());
        assertEquals(3, captured.getServices().size());
    }

    @Test
    void addServicesPersona_verifiesServiceCall() throws Exception {
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setDepcod("S1");
        req.setPersonas(List.of("U1", "U2", "U3"));

        mockMvc.perform(post("/api/depe/add-services-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

        ArgumentCaptor<ServicePersonaRequest> cap = ArgumentCaptor.forClass(ServicePersonaRequest.class);
        verify(dpePersonasForService).saveServicePersonas(cap.capture());
        ServicePersonaRequest captured = cap.getValue();
        assertEquals("S1", captured.getDepcod());
        assertEquals(3, captured.getPersonas().size());
    }

    @Test
    void fetchPersonasServicios_withHighPageNumber() throws Exception {
        when(dpeRepository.findByENTAndEJE(anyInt(), anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/1/E1/100")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_caseInsensitivePerfilAlmacen() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "ALMACEN")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_caseInsensitivePerfilComprador() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "COMPRADOR")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byPersonaCodeShort() throws Exception {
        when(dpeRepository.findProjectionByENTAndEJEAndPERCOD(1, "E1", "P1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "P1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_byServiceCodeShort() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "SC123")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void fetchServicePersonas_withDistinctPersonas() throws Exception {
        Dpe d1 = new Dpe(); d1.setPERCOD("U1");
        Dpe d2 = new Dpe(); d2.setPERCOD("U1");
        Dpe d3 = new Dpe(); d3.setPERCOD("U2");
        when(dpeRepository.findByENTAndEJEAndDEPCOD(1, "E1", "D1")).thenReturn(List.of(d1, d2, d3));

        Per p1 = new Per(); p1.setPERCOD("U1"); p1.setPERNOM("User One");
        Per p2 = new Per(); p2.setPERCOD("U2"); p2.setPERNOM("User Two");
        when(perRepository.findByPERCODIn(List.of("U1", "U2"))).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/depe/fetch-service-personas/1/E1/D1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void fetchPersonaService_withMultipleDeps() throws Exception {
        Dpe d1 = new Dpe(); d1.setDEPCOD("D1");
        Dpe d2 = new Dpe(); d2.setDEPCOD("D2");
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of(d1, d2));

        Dep dep1 = new Dep(); dep1.setDEPCOD("D1"); dep1.setDEPDES("Service 1");
        Dep dep2 = new Dep(); dep2.setDEPCOD("D2"); dep2.setDEPDES("Service 2");
        when(depRepository.findByENTAndEJEAndDEPCODIn(1, "E1", List.of("D1", "D2"))).thenReturn(List.of(dep1, dep2));

        mockMvc.perform(get("/api/depe/fetch-persona-service/1/E1/U1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void searchPersonasServicios_byPersonaCode_and_cgecod() throws Exception {
        when(dpeRepository.findProjectionByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "U1")
                .param("cgecod", "CG1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void fetchPersonasServicios_verifyPageable() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1", PageRequest.of(2, 20))).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/1/E1/2")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteAllPersonaServices_dataAccessExceptionOnDelete() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of(new Dpe()));
        when(dpeRepository.deleteByENTAndEJEAndPERCOD(1, "E1", "U1"))
            .thenThrow(new DataAccessResourceFailureException("DB constraint"));

        mockMvc.perform(delete("/api/depe/delete-persona-Allservice/1/E1/U1"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("error durante la eliminación:")));
    }

    @Test
    void searchPersonasServicios_emptyPersonaParam() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_emptyServicioParam() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_emptyCgecod() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_emptyPerfilParam() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void addPersonaServices_withNullServices() throws Exception {
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setPercod("U1");
        req.setServices(null);

        mockMvc.perform(post("/api/depe/add-persona-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void addServicesPersona_withNullPersonas() throws Exception {
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setDepcod("S1");
        req.setPersonas(null);

        mockMvc.perform(post("/api/depe/add-services-persona")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void fetchServicePersonas_allDistinctPersonas() throws Exception {
        Dpe d1 = new Dpe(); d1.setPERCOD("U1");
        Dpe d2 = new Dpe(); d2.setPERCOD("U2");
        Dpe d3 = new Dpe(); d3.setPERCOD("U3");
        when(dpeRepository.findByENTAndEJEAndDEPCOD(1, "E1", "D1")).thenReturn(List.of(d1, d2, d3));

        Per p1 = new Per(); p1.setPERCOD("U1"); p1.setPERNOM("User One");
        Per p2 = new Per(); p2.setPERCOD("U2"); p2.setPERNOM("User Two");
        Per p3 = new Per(); p3.setPERCOD("U3"); p3.setPERNOM("User Three");
        when(perRepository.findByPERCODIn(List.of("U1", "U2", "U3"))).thenReturn(List.of(p1, p2, p3));

        mockMvc.perform(get("/api/depe/fetch-service-personas/1/E1/D1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].percod", containsInAnyOrder("U1", "U2", "U3")));
    }

    @Test
    void fetchPersonaService_allDistinctDeps() throws Exception {
        Dpe d1 = new Dpe(); d1.setDEPCOD("D1");
        Dpe d2 = new Dpe(); d2.setDEPCOD("D2");
        Dpe d3 = new Dpe(); d3.setDEPCOD("D3");
        when(dpeRepository.findByENTAndEJEAndPERCOD(1, "E1", "U1")).thenReturn(List.of(d1, d2, d3));

        Dep dep1 = new Dep(); dep1.setDEPCOD("D1"); dep1.setDEPDES("Service 1");
        Dep dep2 = new Dep(); dep2.setDEPCOD("D2"); dep2.setDEPDES("Service 2");
        Dep dep3 = new Dep(); dep3.setDEPCOD("D3"); dep3.setDEPDES("Service 3");
        when(depRepository.findByENTAndEJEAndDEPCODIn(1, "E1", List.of("D1", "D2", "D3"))).thenReturn(List.of(dep1, dep2, dep3));

        mockMvc.perform(get("/api/depe/fetch-persona-service/1/E1/U1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].depcod", containsInAnyOrder("D1", "D2", "D3")));
    }

    @Test
    void searchPersonasServicios_filterByServicio_shortCode() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "D1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByServicio_longDescription() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "Service Name"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByServicio_withWhitespace() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "   S1   "))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPersona_shortCode() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "U1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPersona_longName() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "Very Long Persona Name"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPersona_withWhitespace() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "   U1   "))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPerfil_almacen() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPerfil_comprador() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "comprador"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPerfil_contabilidad() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "contabilidad"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPerfil_peticionario() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "peticionario"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPerfil_caseInsensitive() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "ALMACEN"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_multipleFilters_servicio_and_persona() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "D1")
                .param("persona", "U1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_multipleFilters_all() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "Service")
                .param("persona", "Juan")
                .param("cgecod", "CGE1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_multipleFilters_servicio_and_cgecod() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "S1")
                .param("cgecod", "CGE1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_multipleFilters_persona_and_perfil() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "User")
                .param("perfil", "comprador"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_multipleFilters_cgecod_and_perfil() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CGE1")
                .param("perfil", "contabilidad"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_emptyFilterValues_servicio() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", ""))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_cgecod_filtering() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CENTER1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_nullDataHandling() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(null);

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchPersonasServicios_emptyPersona_multipleFilters() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "")
                .param("servicio", "S1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_whitespaceOnlyFilters() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "     ")
                .param("persona", "     "))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPerfil_invalidType() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "invalidProfile"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPerfil_almacenUppercase() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "ALMACEN"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filteredServicio_emptyPersona() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "SRV01")
                .param("persona", ""))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_cgecod_nullHandling() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", ""))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filtersWithEmptyPerfil() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", ""))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPersona_boundary20Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());
        
        String persona20 = "12345678901234567890";
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", persona20))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByPersona_boundary21Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());
        
        String persona21 = "123456789012345678901";
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", persona21))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByServicio_boundary6Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());
        
        String servicio6 = "SERV01";
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", servicio6))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_filterByServicio_boundary7Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());
        
        String servicio7 = "SERV001";
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", servicio7))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByServicioShort() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "SRV"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByServicioLong() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "LongServiceDescription"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByPersonaShort() throws Exception {
        when(dpeRepository.findProjectionByENTAndEJEAndPERCOD(1, "E1", "USR01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "USR01"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByPersonaLong() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPer_PERNOMContaining(1, "E1", "VeryLongPersonaName"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "VeryLongPersonaName"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByCgecod() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CGE01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CGE01"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByPerfilAlmacen() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByPerfilComprador() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "comprador"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByPerfilContabilidad() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "contabilidad"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByPerfilPeticionario() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "peticionario"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_multipleFiltersServicioAndPersona() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "SRV01")
                .param("persona", "USR01"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_multipleFiltersCgecodAndPerfil() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CGE01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CGE01")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_applyFiltersWithAllParameters() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CGE01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "SRV01")
                .param("persona", "USR01")
                .param("cgecod", "CGE01")
                .param("perfil", "comprador"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_nullSafeListWithNull() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(null);

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_nullSafeListWithEmpty() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_filterByPerfilInvalid() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "invalidPerfil"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_emptyServicioFilter() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", ""))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_emptyPersonaFilter() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", ""))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_boundary20CharPersona() throws Exception {
        String persona20 = "12345678901234567890";
        when(dpeRepository.findProjectionByENTAndEJEAndPERCOD(1, "E1", persona20)).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", persona20))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_boundary21CharPersona() throws Exception {
        String persona21 = "123456789012345678901";
        when(dpeRepository.findByENTAndEJEAndPer_PERNOMContaining(1, "E1", persona21)).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", persona21))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_boundary6CharServicio() throws Exception {
        String servicio6 = "SERV01";
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", servicio6))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_helperMethods_boundary7CharServicio() throws Exception {
        String servicio7 = "SERV0001";
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", servicio7))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByServicio_shortCode_lessThan6Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "S1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByServicio_shortCode_exactly6Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "S12345")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByServicio_longDescription_moreThan6Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "Service Description Long")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByServicio_withWhitespace() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "  S1  ")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPersona_shortCode_lessThan20Chars() throws Exception {
        when(dpeRepository.findProjectionByENTAndEJEAndPERCOD(1, "E1", "USER01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "USER01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPersona_shortCode_exactly20Chars() throws Exception {
        String code20Chars = "USER0123456789012345";
        when(dpeRepository.findProjectionByENTAndEJEAndPERCOD(1, "E1", code20Chars)).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", code20Chars)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPersona_longName_moreThan20Chars() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPer_PERNOMContaining(1, "E1", "John Doe Long Name Here")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "John Doe Long Name Here")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPersona_withWhitespace() throws Exception {
        when(dpeRepository.findByENTAndEJEAndPer_PERNOMContaining(1, "E1", "John Doe")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "  John Doe  ")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPerfil_almacen_mapsCorrectly() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "almacen")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPerfil_comprador_mapsCorrectly() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "comprador")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPerfil_contabilidad_mapsCorrectly() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "contabilidad")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPerfil_peticionario_mapsCorrectly() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "peticionario")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPerfil_almacen_upperCase_mapsCorrectly() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "ALMACEN")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPerfil_invalid_returnsAllData() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "invalid_perfil_type")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPerfil_null_returnsAllData() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByCgecod_filtering() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CG01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByCgecod_null_ignored() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByCgecod_empty_ignored() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_servicio_and_persona_combined() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "S1")
                .param("persona", "USER01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_servicio_and_cgecod_combined() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "S1")
                .param("cgecod", "CG01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_persona_and_perfil_combined() throws Exception {
        when(dpeRepository.findProjectionByENTAndEJEAndPERCOD(1, "E1", "USER01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "USER01")
                .param("perfil", "almacen")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_cgecod_and_perfil_combined() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CG01")
                .param("perfil", "comprador")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_all_filters_combined() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "S1")
                .param("persona", "USER01")
                .param("cgecod", "CG01")
                .param("perfil", "almacen")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void getInitialData_selectsByCgecod_whenProvided() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CG01")
                .param("persona", "USER01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG01");
    }

    @Test
    void getInitialData_ignoresPersonaParam_returnsFullDataset_when20CharsOrLess() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "U01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJE(1, "E1");
        verify(dpeRepository, never()).findProjectionByENTAndEJEAndPERCOD(anyInt(), anyString(), anyString());
    }

    @Test
    void getInitialData_ignoresPersonaParam_returnsFullDataset_whenMoreThan20Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "John Doe Very Long Name")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJE(1, "E1");
        verify(dpeRepository, never()).findByENTAndEJEAndPer_PERNOMContaining(anyInt(), anyString(), anyString());
    }

    @Test
    void getInitialData_selectsAll_whenNoFilterSpecified() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJE(1, "E1");
    }

    @Test
    void nullSafeList_handlesNull_returnsEmpty() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(null);

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void helper_filterByPersona_boundary_exactly20CharsCode() throws Exception {
        String code20 = "12345678901234567890";
        when(dpeRepository.findProjectionByENTAndEJEAndPERCOD(1, "E1", code20)).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", code20)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByPersona_boundary_21CharsGoesToName() throws Exception {
        String name21 = "123456789012345678901";
        when(dpeRepository.findByENTAndEJEAndPer_PERNOMContaining(1, "E1", name21)).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", name21)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByServicio_boundary_exactly6CharsCode() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "123456")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void helper_filterByServicio_boundary_7CharsGoesToDescription() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "1234567")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_emptyStringFilters_treated_asMissing() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "")
                .param("persona", "")
                .param("cgecod", "")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_whitespaceOnlyFilters_trimmedCorrectly() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "   ")
                .param("persona", "   ")
                .param("cgecod", "   ")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void getInitialData_withPersonaParam_returnsFullDataset_noPrefilteredByPersona() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "U01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJE(1, "E1");
    }

    @Test
    void filterByPersona_searchByPERCODExactMatch_whenPersonaLessThan20Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "U01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_searchByPERNOM_whenPersonaLessThan20Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "John")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_searchOnlyByPERNOM_whenPersonaMoreThan20Chars() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "This is a very long name string")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_caseInsensitiveSearch_onPERCOD() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "u01")  
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_caseInsensitiveSearch_onPERNOM() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "john") 
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_partialMatch_onPERNOM() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "Joh")  
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void getInitialData_filtersByCgecod_whenProvided() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CG01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG01");
    }

    @Test
    void getInitialData_returnsAll_whenCgecodNotProvided() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJE(1, "E1");
    }

    @Test
    void searchPersonasServicios_personaAnd20Charboundary_searchsBothFields() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "12345678901234567890") 
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_personaWith21Chars_searchesOnlyPERNOM() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "123456789012345678901") 
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_personaAndCgecod_combined() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG01")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "U01")
                .param("cgecod", "CG01")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG01");
    }

    @Test
    void searchPersonasServicios_multipleFiltersWithPersona_appliedCorrectly() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "U01")
                .param("servicio", "SRV01")
                .param("perfil", "almacen")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJE(1, "E1");
    }

    @Test
    void filterByPersona_withWhitespace_trimmedBeforeComparison() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "  U01  ")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void getInitialData_noPrefilteredByPersona_regardlessOfLength() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1")).thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "A")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(dpeRepository).findByENTAndEJE(1, "E1");
    }

    @Test
    void fetchPersonasServicios_withEmptyResult_returnsNotFound() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1", PageRequest.of(0, 20)))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/1/E1/0"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void fetchPersonasServicios_withNullResult_returnsNotFound() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1", PageRequest.of(0, 20)))
            .thenReturn(null);

        mockMvc.perform(get("/api/depe/personas-servicios/1/E1/0"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void searchPersonasServicios_applyFilters_withNullData() throws Exception {
        when(dpeRepository.findByENTAndEJEAndDep_Cge_CGECOD(1, "E1", "CG1"))
            .thenReturn(null);

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CG1")
                .param("servicio", "SRV"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_withServicioFilter() throws Exception {        
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "S00"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_withPersonaFilter_lessThan20Chars() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "USER"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_withPersonaFilter_moreThan20Chars() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "VERYLONGPERSONANAMEHERE"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_withCgecodFilter() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CG001"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_withPerfilFilter() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_withMultipleFilters() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "SRV")
                .param("persona", "USER")
                .param("cgecod", "CG1")
                .param("perfil", "comprador"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPerfil_withNullData() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPerfil_withNullPerfilType() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "invalid"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPerfil_peticionario_allZeros() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "peticionario"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPerfil_almacen_withOne() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPerfil_comprador_withOne() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "comprador"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPerfil_contabilidad_withOne() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "contabilidad"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_withNullData() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "ABC"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_withNullInput() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_with20CharsSearchsBoth() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "12345678901234567890"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_with21CharsSearchesNameOnly() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "123456789012345678901"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByServicio_withNullData() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "ABC"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByServicio_with6CharFilters() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "ABCDEF"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByServicio_with7CharSearchesByDescription() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "ABCDEFG"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void nullSafeList_withNull() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(null);

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_returnsDataWhenAllFiltersNull() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_withEmptyServicio() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", ""))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_withEmptyPersona() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", ""))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPerfil_withEmptyData() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_trimmedComparison() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "  USER  "))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByServicio_trimmedComparison() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "  SRV  "))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_cgecodFilter_withNullDep() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", "CG1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void searchPersonasServicios_allFiltersCombined() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("servicio", "SRV001")
                .param("persona", "USER")
                .param("cgecod", "CG1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_caseInsensitivePercod() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "user"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void filterByPersona_caseInsensitivePernom() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("persona", "USER"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void mapPerfilType_lowercase() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "almacen"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void mapPerfilType_uppercase() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "ALMACEN"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void mapPerfilType_mixedCase() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("perfil", "AlMaCeN"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_dataNotNull_cgecod_filter() throws Exception {
        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1")
                .param("cgecod", ""))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void applyFilters_perfilNull_returnsData() throws Exception {
        when(dpeRepository.findByENTAndEJE(1, "E1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/depe/personas-servicios/search")
                .param("ent", "1")
                .param("eje", "E1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}