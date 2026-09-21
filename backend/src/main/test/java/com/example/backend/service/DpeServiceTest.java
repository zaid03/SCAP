package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.backend.dto.PersonaServiceRequest;
import com.example.backend.sqlserver2.model.Dpe;
import com.example.backend.sqlserver2.model.DpeId;
import com.example.backend.sqlserver2.repository.DpeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DpeServiceTest {

    private DpeService service;
    
    @Mock
    private DpeRepository dpeRepository;
    
    @BeforeEach
    void setUp() {
        service = new DpeService(dpeRepository);
    }
    
    @Test
    void savePersonaServices_withValidSingleService_succeeds() {
        PersonaServiceRequest req = createRequest(1, "2024", "PER001", "DEP001");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        verify(dpeRepository, times(1)).findById(any(DpeId.class));
        verify(dpeRepository, times(1)).save(any(Dpe.class));
    }

    @Test
    void savePersonaServices_withMultipleServices_savesAll() {
        PersonaServiceRequest req = createRequest(1, "2024", "PER001", "DEP001", "DEP002", "DEP003");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        verify(dpeRepository, times(3)).findById(any(DpeId.class));
        verify(dpeRepository, times(3)).save(any(Dpe.class));
    }

    @Test
    void savePersonaServices_withDuplicateService_skipsExisting() {
        PersonaServiceRequest req = createRequest(1, "2024", "PER001", "DEP001", "DEP002");
        
        when(dpeRepository.findById(any(DpeId.class)))
            .thenReturn(Optional.of(existingDpe("DEP001")))
            .thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        verify(dpeRepository, times(2)).findById(any(DpeId.class));
        verify(dpeRepository, times(1)).save(any(Dpe.class));
    }

    @Test
    void savePersonaServices_savesWithCorrectFields() {
        PersonaServiceRequest req = createRequest(1, "2024", "PER001", "DEP001");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        ArgumentCaptor<Dpe> captor = ArgumentCaptor.forClass(Dpe.class);
        verify(dpeRepository).save(captor.capture());
        Dpe savedDpe = captor.getValue();
        
        assertEquals(1, savedDpe.getENT());
        assertEquals("2024", savedDpe.getEJE());
        assertEquals("DEP001", savedDpe.getDEPCOD());
        assertEquals("PER001", savedDpe.getPERCOD());
    }

    @Test
    void savePersonaServices_withAllDuplicates_savesNone() {
        PersonaServiceRequest req = createRequest(1, "2024", "PER001", "DEP001", "DEP002", "DEP003");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.of(existingDpe("DEP001")));
        
        service.savePersonaServices(req);
        
        verify(dpeRepository, times(3)).findById(any(DpeId.class));
        verify(dpeRepository, never()).save(any(Dpe.class));
    }
    
    @Test
    void savePersonaServices_withNullServicesList_returnsWithoutProcessing() {
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setEnt(1);
        req.setEje("2024");
        req.setPercod("PER001");
        req.setServices(null);
        
        service.savePersonaServices(req);
        
        verify(dpeRepository, never()).findById(any(DpeId.class));
        verify(dpeRepository, never()).save(any(Dpe.class));
    }

    @Test
    void savePersonaServices_withEmptyServicesList_returnsWithoutProcessing() {
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setEnt(1);
        req.setEje("2024");
        req.setPercod("PER001");
        req.setServices(new ArrayList<>());
        
        service.savePersonaServices(req);
        
        verify(dpeRepository, never()).findById(any(DpeId.class));
        verify(dpeRepository, never()).save(any(Dpe.class));
    }
    
    @Test
    void savePersonaServices_withMixedDuplicateAndNew_savesOnlyNew() {
        PersonaServiceRequest req = createRequest(1, "2024", "PER001", "EXIST", "NEW", "EXIST2", "NEW2");
        
        when(dpeRepository.findById(any(DpeId.class)))
            .thenReturn(Optional.of(existingDpe("EXIST")))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingDpe("EXIST2")))
            .thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        verify(dpeRepository, times(4)).findById(any(DpeId.class));
        verify(dpeRepository, times(2)).save(any(Dpe.class));
    }

    @Test
    void savePersonaServices_savesWithCorrectCompositeIds() {
        PersonaServiceRequest req = createRequest(5, "2025", "PERSONA05", "DEPT1", "DEPT2");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        ArgumentCaptor<DpeId> idCaptor = ArgumentCaptor.forClass(DpeId.class);
        verify(dpeRepository, times(2)).findById(idCaptor.capture());
        
        List<DpeId> capturedIds = idCaptor.getAllValues();
        assertEquals(2, capturedIds.size());
    }

    @Test
    void savePersonaServices_withLargeServiceList_processesAll() {
        List<String> services = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            services.add("DEP" + String.format("%03d", i));
        }
        
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setEnt(1);
        req.setEje("2024");
        req.setPercod("PER001");
        req.setServices(services);
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        verify(dpeRepository, times(100)).findById(any(DpeId.class));
        verify(dpeRepository, times(100)).save(any(Dpe.class));
    }

    @Test
    void savePersonaServices_withDifferentEntAndEje_createsCorrectIds() {
        PersonaServiceRequest req1 = createRequest(1, "2024", "PER001", "DEP001");
        PersonaServiceRequest req2 = createRequest(2, "2025", "PER002", "DEP002");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req1);
        service.savePersonaServices(req2);
        
        ArgumentCaptor<Dpe> dpeCaptor = ArgumentCaptor.forClass(Dpe.class);
        verify(dpeRepository, times(2)).save(dpeCaptor.capture());
        
        List<Dpe> savedDpes = dpeCaptor.getAllValues();
        assertEquals(1, savedDpes.get(0).getENT());
        assertEquals("2024", savedDpes.get(0).getEJE());
        assertEquals(2, savedDpes.get(1).getENT());
        assertEquals("2025", savedDpes.get(1).getEJE());
    }

    @Test
    void savePersonaServices_persistsAllServicesInSingleRequest() {
        PersonaServiceRequest req = createRequest(1, "2024", "PER001", "S1", "S2", "S3", "S4", "S5");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        ArgumentCaptor<Dpe> captor = ArgumentCaptor.forClass(Dpe.class);
        verify(dpeRepository, times(5)).save(captor.capture());
        
        List<Dpe> savedDpes = captor.getAllValues();
        assertEquals("S1", savedDpes.get(0).getDEPCOD());
        assertEquals("S2", savedDpes.get(1).getDEPCOD());
        assertEquals("S3", savedDpes.get(2).getDEPCOD());
        assertEquals("S4", savedDpes.get(3).getDEPCOD());
        assertEquals("S5", savedDpes.get(4).getDEPCOD());
    }

    @Test
    void savePersonaServices_withDifferentPersonaCodes_createsCorrectRecords() {
        List<String> services = Arrays.asList("DEP1", "DEP2", "DEP3");
        
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setEnt(1);
        req.setEje("2024");
        req.setPercod("PERSONA_SPECIAL");
        req.setServices(services);
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        ArgumentCaptor<Dpe> captor = ArgumentCaptor.forClass(Dpe.class);
        verify(dpeRepository, times(3)).save(captor.capture());
        
        List<Dpe> savedDpes = captor.getAllValues();
        for (Dpe dpe : savedDpes) {
            assertEquals("PERSONA_SPECIAL", dpe.getPERCOD());
        }
    }

    @Test
    void savePersonaServices_alternatingExistingAndNew_skipsAndSavesCorrectly() {
        PersonaServiceRequest req = createRequest(1, "2024", "PER001", "S1", "S2", "S3", "S4", "S5", "S6");
        
        when(dpeRepository.findById(any(DpeId.class)))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingDpe("S2")))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingDpe("S4")))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingDpe("S6")));
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.savePersonaServices(req);
        
        verify(dpeRepository, times(3)).save(any(Dpe.class));
        
        ArgumentCaptor<Dpe> captor = ArgumentCaptor.forClass(Dpe.class);
        verify(dpeRepository, times(3)).save(captor.capture());
        
        List<Dpe> savedDpes = captor.getAllValues();
        assertEquals("S1", savedDpes.get(0).getDEPCOD());
        assertEquals("S3", savedDpes.get(1).getDEPCOD());
        assertEquals("S5", savedDpes.get(2).getDEPCOD());
    }
    
    private PersonaServiceRequest createRequest(Integer ent, String eje, String percod, String... services) {
        PersonaServiceRequest req = new PersonaServiceRequest();
        req.setEnt(ent);
        req.setEje(eje);
        req.setPercod(percod);
        req.setServices(Arrays.asList(services));
        return req;
    }

    private Dpe existingDpe(String depcod) {
        Dpe dpe = new Dpe();
        dpe.setDEPCOD(depcod);
        return dpe;
    }
}
