package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.backend.dto.ServicePersonaRequest;
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
class DpePersonasForServiceTest {

    private DpePersonasForService service;
    
    @Mock
    private DpeRepository dpeRepository;
    
    @BeforeEach
    void setUp() {
        service = new DpePersonasForService(dpeRepository);
    }
    
    @Test
    void saveServicePersonas_withValidSinglePersona_succeeds() {
        ServicePersonaRequest req = createRequest(1, "2024", "DEP001", "PER001");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.saveServicePersonas(req);
        
        verify(dpeRepository, times(1)).findById(any(DpeId.class));
        verify(dpeRepository, times(1)).save(any(Dpe.class));
    }

    @Test
    void saveServicePersonas_withMultiplePersonas_savesAll() {
        ServicePersonaRequest req = createRequest(1, "2024", "DEP001", "PER001", "PER002", "PER003");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.saveServicePersonas(req);
        
        verify(dpeRepository, times(3)).findById(any(DpeId.class));
        verify(dpeRepository, times(3)).save(any(Dpe.class));
    }

    @Test
    void saveServicePersonas_withDuplicatePersona_skipsExisting() {
        ServicePersonaRequest req = createRequest(1, "2024", "DEP001", "PER001", "PER002");
        
        when(dpeRepository.findById(any(DpeId.class)))
            .thenReturn(Optional.of(existingDpe("PER001")))
            .thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.saveServicePersonas(req);
        
        verify(dpeRepository, times(2)).findById(any(DpeId.class));
        verify(dpeRepository, times(1)).save(any(Dpe.class));
    }

    @Test
    void saveServicePersonas_savesWithCorrectFields() {
        ServicePersonaRequest req = createRequest(1, "2024", "DEP001", "PER001");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.saveServicePersonas(req);
        
        ArgumentCaptor<Dpe> captor = ArgumentCaptor.forClass(Dpe.class);
        verify(dpeRepository).save(captor.capture());
        Dpe savedDpe = captor.getValue();
        
        assertEquals(1, savedDpe.getENT());
        assertEquals("2024", savedDpe.getEJE());
        assertEquals("PER001", savedDpe.getPERCOD());
        assertEquals("DEP001", savedDpe.getDEPCOD());
    }

    @Test
    void saveServicePersonas_withAllDuplicates_savesNone() {
        ServicePersonaRequest req = createRequest(1, "2024", "DEP001", "PER001", "PER002", "PER003");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.of(existingDpe("PER001")));
        
        service.saveServicePersonas(req);
        
        verify(dpeRepository, times(3)).findById(any(DpeId.class));
        verify(dpeRepository, never()).save(any(Dpe.class));
    }
    
    @Test
    void saveServicePersonas_withNullPersonasList_returnsWithoutProcessing() {
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setEnt(1);
        req.setEje("2024");
        req.setDepcod("DEP001");
        req.setPersonas(null);
        
        service.saveServicePersonas(req);
        
        verify(dpeRepository, never()).findById(any(DpeId.class));
        verify(dpeRepository, never()).save(any(Dpe.class));
    }

    @Test
    void saveServicePersonas_withEmptyPersonasList_returnsWithoutProcessing() {
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setEnt(1);
        req.setEje("2024");
        req.setDepcod("DEP001");
        req.setPersonas(new ArrayList<>());
        
        service.saveServicePersonas(req);
        
        verify(dpeRepository, never()).findById(any(DpeId.class));
        verify(dpeRepository, never()).save(any(Dpe.class));
    }
    
    @Test
    void saveServicePersonas_withMixedDuplicateAndNew_savesOnlyNew() {
        ServicePersonaRequest req = createRequest(1, "2024", "DEP001", "EXIST", "NEW", "EXIST2", "NEW2");
        
        when(dpeRepository.findById(any(DpeId.class)))
            .thenReturn(Optional.of(existingDpe("EXIST")))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingDpe("EXIST2")))
            .thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.saveServicePersonas(req);
        
        verify(dpeRepository, times(4)).findById(any(DpeId.class));
        verify(dpeRepository, times(2)).save(any(Dpe.class));
    }

    @Test
    void saveServicePersonas_savesWithCorrectCompositeIds() {
        ServicePersonaRequest req = createRequest(5, "2025", "DEPT05", "PERS1", "PERS2");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.saveServicePersonas(req);
        
        ArgumentCaptor<DpeId> idCaptor = ArgumentCaptor.forClass(DpeId.class);
        verify(dpeRepository, times(2)).findById(idCaptor.capture());
        
        List<DpeId> capturedIds = idCaptor.getAllValues();
        assertEquals(2, capturedIds.size());
    }

    @Test
    void saveServicePersonas_withLargePersonaList_processesAll() {
        List<String> personas = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            personas.add("PER" + String.format("%03d", i));
        }
        
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setEnt(1);
        req.setEje("2024");
        req.setDepcod("DEP001");
        req.setPersonas(personas);
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.saveServicePersonas(req);
        
        verify(dpeRepository, times(100)).findById(any(DpeId.class));
        verify(dpeRepository, times(100)).save(any(Dpe.class));
    }

    @Test
    void saveServicePersonas_withDifferentEntAndEje_createsCorrectIds() {
        ServicePersonaRequest req1 = createRequest(1, "2024", "DEP001", "PER001");
        ServicePersonaRequest req2 = createRequest(2, "2025", "DEP002", "PER002");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.saveServicePersonas(req1);
        service.saveServicePersonas(req2);
        
        ArgumentCaptor<Dpe> dpeCaptor = ArgumentCaptor.forClass(Dpe.class);
        verify(dpeRepository, times(2)).save(dpeCaptor.capture());
        
        List<Dpe> savedDpes = dpeCaptor.getAllValues();
        assertEquals(1, savedDpes.get(0).getENT());
        assertEquals("2024", savedDpes.get(0).getEJE());
        assertEquals(2, savedDpes.get(1).getENT());
        assertEquals("2025", savedDpes.get(1).getEJE());
    }

    @Test
    void saveServicePersonas_persistsAllPersonasInSingleRequest() {
        ServicePersonaRequest req = createRequest(1, "2024", "DEP001", "A", "B", "C", "D", "E");
        
        when(dpeRepository.findById(any(DpeId.class))).thenReturn(Optional.empty());
        when(dpeRepository.save(any(Dpe.class))).thenAnswer(i -> i.getArgument(0));
        
        service.saveServicePersonas(req);
        
        ArgumentCaptor<Dpe> captor = ArgumentCaptor.forClass(Dpe.class);
        verify(dpeRepository, times(5)).save(captor.capture());
        
        List<Dpe> savedDpes = captor.getAllValues();
        assertEquals("A", savedDpes.get(0).getPERCOD());
        assertEquals("B", savedDpes.get(1).getPERCOD());
        assertEquals("C", savedDpes.get(2).getPERCOD());
        assertEquals("D", savedDpes.get(3).getPERCOD());
        assertEquals("E", savedDpes.get(4).getPERCOD());
    }
    
    private ServicePersonaRequest createRequest(Integer ent, String eje, String depcod, String... personas) {
        ServicePersonaRequest req = new ServicePersonaRequest();
        req.setEnt(ent);
        req.setEje(eje);
        req.setDepcod(depcod);
        req.setPersonas(Arrays.asList(personas));
        return req;
    }

    private Dpe existingDpe(String percod) {
        Dpe dpe = new Dpe();
        dpe.setPERCOD(percod);
        return dpe;
    }
}
