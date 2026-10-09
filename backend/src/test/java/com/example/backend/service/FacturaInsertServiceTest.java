package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.backend.dto.FacturaInsertDto;
import com.example.backend.sqlserver2.model.Cfg;
import com.example.backend.sqlserver2.model.Cot;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.Ter;
import com.example.backend.sqlserver2.model.Gbs;
import com.example.backend.sqlserver2.repository.CfgRepository;
import com.example.backend.sqlserver2.repository.FacRepository;
import com.example.backend.sqlserver2.repository.TerRepository;
import com.example.backend.sqlserver2.repository.GbsRepository;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.example.backend.dto.CuentaBancaria;
import com.example.backend.dto.Tercero;
import com.example.backend.exception.XmlParsingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.example.backend.sqlserver2.repository.CotRepository;
import com.example.backend.sqlserver2.repository.CogRepository;

@ExtendWith(MockitoExtension.class)
class FacturaInsertServiceTest {

    private FacturaInsertService service;
    
    @Mock
    private TerRepository terRepository;
    @Mock
    private CfgRepository cfgRepository;
    @Mock
    private FacRepository facRepository;
    @Mock
    private GbsRepository gbsRepository;
    @Mock
    private FdeRepository fdeRepository;
    
    @Mock
    private SicalService sicalService;    
    @Mock
    private CotRepository cotRepository;
    @Mock
    private CogRepository cogRepository;

    @BeforeEach
    void setUp() {
        service = new FacturaInsertService();
        ReflectionTestUtils.setField(service, "terRepository", terRepository);
        ReflectionTestUtils.setField(service, "cfgRepository", cfgRepository);
        ReflectionTestUtils.setField(service, "facRepository", facRepository);
        ReflectionTestUtils.setField(service, "gbsRepository", gbsRepository);
        ReflectionTestUtils.setField(service, "fdeRepository", fdeRepository);
        ReflectionTestUtils.setField(service, "sicalService", sicalService);  
        ReflectionTestUtils.setField(service, "cotRepository", cotRepository);
        ReflectionTestUtils.setField(service, "cogRepositroy", cogRepository);
    }
    
    @Test
    void insertFacturas_withValidSingleFactura_succeeds() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        List<Gbs> gbsList = List.of(createGbs("REF001", "OPE001", "ORG001"));
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1)).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001")).thenReturn(gbsList);
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));
        when(fdeRepository.save(any(Fde.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertDto dto = createValidFacturaDto("PROV001");
        List<FacturaInsertDto> facturas = List.of(dto);
        
        FacturaInsertService.NamesResponse response = service.insertFacturas(facturas);
        
        assertEquals(1, response.savedNames().size());
        assertEquals(0, response.unsavedNames().size());
        assertEquals(0, response.missingProviders().size());
        verify(facRepository, times(1)).save(any(Fac.class));
        verify(fdeRepository, times(gbsList.size())).save(any(Fde.class));
    }

    @Test
    void insertFacturas_withMultipleValidFacturas_succeedsForAll() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        List<Gbs> gbsList = List.of(createGbs("REF001", "OPE001", "ORG001"));
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC(anyString(), anyInt(), anyInt())).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100, 101);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001")).thenReturn(gbsList);
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));
        when(fdeRepository.save(any(Fde.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertDto dto1 = createValidFacturaDto("PROV001");
        FacturaInsertDto dto2 = createValidFacturaDto("PROV001");
        List<FacturaInsertDto> facturas = List.of(dto1, dto2);
        
        FacturaInsertService.NamesResponse response = service.insertFacturas(facturas);
        
        assertEquals(2, response.savedNames().size());
        assertEquals(0, response.unsavedNames().size());
        assertEquals(0, response.missingProviders().size());
        verify(facRepository, times(2)).save(any(Fac.class));
        verify(fdeRepository, times(2 * gbsList.size())).save(any(Fde.class));
    }

    @Test
    void insertFacturas_withMatchingContractCge_setsContractMessageWithoutFde() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        Cot cot = new Cot();
        cot.setCONCOD(25);

        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1)).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001"))
            .thenReturn(List.of(createGbs("REF001", "OPE001", "ORG001")));
        when(cotRepository.findByENTAndEJEAndTERCODAndConn_CONBLOAndConn_CONTIP(
                1, "2024", 1, 0, 3))
            .thenReturn(List.of(cot));
        when(cogRepository.existsByENTAndEJEAndCONCODAndCGECOD(1, "2024", 25, "CGE001"))
            .thenReturn(true);
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertService.NamesResponse response =
            service.insertFacturas(List.of(createValidFacturaDto("PROV001")));

        assertEquals("PROVEEDOR CON CONTRATO AD", response.savedNames().get(0).message());
        verify(fdeRepository, never()).save(any(Fde.class));
    }

    @Test
    void insertFacturas_withContractWithoutMatchingCge_createsFde() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        Cot cot = new Cot();
        cot.setCONCOD(25);
        Gbs gbs = createGbs("REF001", "OPE001", "ORG001");

        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1)).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001")).thenReturn(List.of(gbs));
        when(cotRepository.findByENTAndEJEAndTERCODAndConn_CONBLOAndConn_CONTIP(
                1, "2024", 1, 0, 3))
            .thenReturn(List.of(cot));
        when(cogRepository.existsByENTAndEJEAndCONCODAndCGECOD(1, "2024", 25, "CGE001"))
            .thenReturn(false);
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertService.NamesResponse response =
            service.insertFacturas(List.of(createValidFacturaDto("PROV001")));

        assertEquals("", response.savedNames().get(0).message());
        verify(fdeRepository).save(any(Fde.class));
    }

    @Test
    void insertFacturas_withMultipleGbsRows_createsMultipleFdeRecords() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        List<Gbs> gbsList = List.of(
            createGbs("REF001", "OPE001", "ORG001"),
            createGbs("REF002", "OPE002", "ORG002"),
            createGbs("REF003", "OPE003", "ORG003")
        );
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1)).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001")).thenReturn(gbsList);
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));
        when(fdeRepository.save(any(Fde.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertDto dto = createValidFacturaDto("PROV001");
        
        FacturaInsertService.NamesResponse response = service.insertFacturas(List.of(dto));
        
        assertEquals(1, response.savedNames().size());
        assertEquals(0, response.unsavedNames().size());
        assertEquals(0, response.missingProviders().size());
        verify(fdeRepository, times(3)).save(any(Fde.class));
    }

    @Test
    void insertFacturas_facNumIncrementsCorrectly() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC(anyString(), anyInt(), anyInt())).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(999);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001")).thenReturn(new ArrayList<>());
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertDto dto = createValidFacturaDto("PROV001");
        service.insertFacturas(List.of(dto));
        
        ArgumentCaptor<Fac> captor = ArgumentCaptor.forClass(Fac.class);
        verify(facRepository).save(captor.capture());
        Fac savedFac = captor.getValue();
        
        assertEquals(1000, savedFac.getFACNUM());
    }
    
    @Test
    void insertFacturas_withProviderNotFound_returnsMessage() {
        when(terRepository.findByENTAndTERNIF(1, "INVALID")).thenReturn(null);
        
        FacturaInsertDto dto = createValidFacturaDto("INVALID");
        FacturaInsertService.NamesResponse response = service.insertFacturas(List.of(dto));
        
        assertEquals(0, response.savedNames().size());
        assertEquals(0, response.unsavedNames().size());
        assertEquals(1, response.missingProviders().size());
        assertEquals("INVALID", response.missingProviders().get(0));
        verify(facRepository, never()).save(any());
    }

    @Test
    void insertFacturas_withDuplicateFactura_returnsMessage() {
        Ter ter = createTer(1, "PROV001");
        Fac existing = new Fac();
        existing.setFACTDC("F");
        existing.setFACANN(2024);
        existing.setFACFAC(1);
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1)).thenReturn(List.of(existing));
        
        FacturaInsertDto dto = createValidFacturaDto("PROV001");
        FacturaInsertService.NamesResponse response = service.insertFacturas(List.of(dto));
        
        assertEquals(0, response.savedNames().size());
        assertEquals(1, response.unsavedNames().size());
        assertEquals(0, response.missingProviders().size());
        verify(facRepository, never()).save(any(Fac.class));
    }

    @Test
    void insertFacturas_withNoConfig_skipsInsertion() {
        Ter ter = createTer(1, "PROV001");
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1)).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(new ArrayList<>());
        
        FacturaInsertDto dto = createValidFacturaDto("PROV001");
        FacturaInsertService.NamesResponse response = service.insertFacturas(List.of(dto));
        
        assertEquals(0, response.savedNames().size());
        assertEquals(0, response.unsavedNames().size());
        assertEquals(0, response.missingProviders().size());
        verify(facRepository, never()).save(any(Fac.class));
    }

    @Test
    void insertFacturas_withNullMaxFacNum_startsAt1() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1)).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(null);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001")).thenReturn(new ArrayList<>());
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertDto dto = createValidFacturaDto("PROV001");
        service.insertFacturas(List.of(dto));
        
        ArgumentCaptor<Fac> captor = ArgumentCaptor.forClass(Fac.class);
        verify(facRepository).save(captor.capture());
        Fac savedFac = captor.getValue();
        
        assertEquals(1, savedFac.getFACNUM());
    }
    
    @Test
    void insertFacturas_withMixedValidAndInvalid_processesIndependently() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(terRepository.findByENTAndTERNIF(1, "INVALID")).thenReturn(null);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC(anyString(), anyInt(), anyInt())).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001")).thenReturn(new ArrayList<>());
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertDto validDto = createValidFacturaDto("PROV001");
        FacturaInsertDto invalidDto = createValidFacturaDto("INVALID");
        
        FacturaInsertService.NamesResponse response = service.insertFacturas(List.of(validDto, invalidDto));
        
        assertEquals(1, response.savedNames().size());
        assertEquals(0, response.unsavedNames().size());
        assertEquals(1, response.missingProviders().size());
        assertEquals("INVALID", response.missingProviders().get(0));
        verify(facRepository, times(1)).save(any(Fac.class));
    }

    @Test
    void insertFacturas_withEmptyList_returnsEmptyMessages() {
        FacturaInsertService.NamesResponse response = service.insertFacturas(new ArrayList<>());
        
        assertEquals(0, response.savedNames().size());
        assertEquals(0, response.unsavedNames().size());
        assertEquals(0, response.missingProviders().size());
        verify(terRepository, never()).findByENTAndTERNIF(anyInt(), anyString());
    }

    @Test
    void insertFacturas_savesFacturaWithCorrectFields() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1)).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001")).thenReturn(new ArrayList<>());
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertDto dto = createValidFacturaDto("PROV001");
        service.insertFacturas(List.of(dto));
        
        ArgumentCaptor<Fac> captor = ArgumentCaptor.forClass(Fac.class);
        verify(facRepository).save(captor.capture());
        Fac savedFac = captor.getValue();
        
        assertEquals(1, savedFac.getENT());
        assertEquals("2024", savedFac.getEJE());
        assertEquals(101, savedFac.getFACNUM());
        assertEquals(1, savedFac.getTERCOD());
        assertEquals("CGE001", savedFac.getCGECOD());
    }

    @Test
    void insertFacturas_savesFdeWithCorrectFields() {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");
        Gbs gbs = createGbs("REF001", "OPE001", "ORG001");
        
        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1)).thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001")).thenReturn(List.of(gbs));
        when(facRepository.save(any(Fac.class))).thenAnswer(i -> i.getArgument(0));
        when(fdeRepository.save(any(Fde.class))).thenAnswer(i -> i.getArgument(0));

        FacturaInsertDto dto = createValidFacturaDto("PROV001");
        service.insertFacturas(List.of(dto));
        
        ArgumentCaptor<Fde> captor = ArgumentCaptor.forClass(Fde.class);
        verify(fdeRepository).save(captor.capture());
        Fde savedFde = captor.getValue();
        
        assertEquals(1, savedFde.getENT());
        assertEquals("2024", savedFde.getEJE());
        assertEquals(101, savedFde.getFACNUM());
        assertEquals("REF001", savedFde.getFDEREF());
        assertEquals(0.0, savedFde.getFDEIMP());
        assertEquals(0.0, savedFde.getFDEDIF());
    }
    
    private FacturaInsertDto createValidFacturaDto(String tercero) {
        FacturaInsertDto dto = new FacturaInsertDto();
        dto.ENT = 1;
        dto.EJE = "2024";
        dto.tercero = tercero;
        dto.CGECOD = "CGE001";
        dto.FACTDC = "F";
        dto.FACANN = 2024;
        dto.FACFAC = 1;
        dto.FACIMP = 1.0;
        dto.FACIEC = 0.0;
        dto.FACIDI = 0.0;
        dto.FACDOC = "DOC001";
        dto.FACDAT = LocalDateTime.of(2024, 1, 15, 0, 0, 0);
        dto.FACTXT = "Invoice text";
        dto.FACDTO = 0.0;
        dto.FACFRE = LocalDateTime.of(2024, 1, 31, 0, 0, 0);
        return dto;
    }
    
    private Ter createTer(Integer terCod, String terNif) {
        Ter ter = new Ter();
        ter.setTERCOD(terCod);
        ter.setTERNIF(terNif);
        return ter;
    }
    
    private Cfg createCfg(String code, String fpg, String opg, String tpg) {
        Cfg cfg = new Cfg();
        cfg.setCFGFPG(fpg);
        cfg.setCFGOPG(opg);
        cfg.setCFGTPG(tpg);
        return cfg;
    }
    
    private Gbs createGbs(String ref, String ope, String org) {
        Gbs gbs = new Gbs();
        gbs.setGBSREF(ref);
        gbs.setGBSOPE(ope);
        gbs.setGBSORG(org);
        gbs.setGBSFUN("FUN001");
        gbs.setGBSECO("ECO001");
        gbs.setGBSSUB("SUB001");
        return gbs;
    }

    @Test
    void insertFacturas_withValidBankAccount_setsFacoct() throws Exception {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");

        CuentaBancaria cuenta = mock(CuentaBancaria.class);
        when(cuenta.TDB_SIT()).thenReturn("A");
        when(cuenta.TDB_ORD()).thenReturn("7");

        Tercero tercero = mock(Tercero.class);
        when(tercero.getCuentasBancarias()).thenReturn(List.of(cuenta));

        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1))
            .thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(sicalService.getTerceros(
            isNull(), isNull(), eq("1"), any(), any(), any()))
            .thenReturn(List.of(tercero));
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001"))
            .thenReturn(new ArrayList<>());
        when(cotRepository.findByENTAndEJEAndTERCODAndConn_CONBLOAndConn_CONTIP(
            1, "2024", 1, 0, 3))
            .thenReturn(new ArrayList<>());
        when(facRepository.save(any(Fac.class)))
            .thenAnswer(i -> i.getArgument(0));

        service.insertFacturas(List.of(createValidFacturaDto("PROV001")));

        ArgumentCaptor<Fac> captor = ArgumentCaptor.forClass(Fac.class);
        verify(facRepository).save(captor.capture());

        assertEquals(7, captor.getValue().getFACOCT());
    }

    @Test
    void insertFacturas_withXmlParsingException_setsFacoctToZero() throws Exception {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");

        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1))
            .thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(sicalService.getTerceros(
            isNull(), isNull(), eq("1"), any(), any(), any()))
            .thenThrow(new XmlParsingException("Invalid XML"));
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001"))
            .thenReturn(new ArrayList<>());
        when(cotRepository.findByENTAndEJEAndTERCODAndConn_CONBLOAndConn_CONTIP(
            1, "2024", 1, 0, 3))
            .thenReturn(new ArrayList<>());
        when(facRepository.save(any(Fac.class)))
            .thenAnswer(i -> i.getArgument(0));

        service.insertFacturas(List.of(createValidFacturaDto("PROV001")));

        ArgumentCaptor<Fac> captor = ArgumentCaptor.forClass(Fac.class);
        verify(facRepository).save(captor.capture());

        assertEquals(0, captor.getValue().getFACOCT());
    }

    @Test
    void insertFacturas_withInvalidBankAccountOrder_setsFacoctToZero() throws Exception {
        Ter ter = createTer(1, "PROV001");
        Cfg cfg = createCfg("CFG001", "TPG001", "OPG001", "FPG001");

        CuentaBancaria cuenta = mock(CuentaBancaria.class);
        when(cuenta.TDB_SIT()).thenReturn("A");
        when(cuenta.TDB_ORD()).thenReturn("NOT_A_NUMBER");

        Tercero tercero = mock(Tercero.class);
        when(tercero.getCuentasBancarias()).thenReturn(List.of(cuenta));

        when(terRepository.findByENTAndTERNIF(1, "PROV001")).thenReturn(ter);
        when(facRepository.findByFACTDCAndFACANNAndFACFAC("F", 2024, 1))
            .thenReturn(new ArrayList<>());
        when(cfgRepository.findByENTAndEJE(1, "2024")).thenReturn(List.of(cfg));
        when(facRepository.findMaxFACNUMByENTAndEJE(1, "2024")).thenReturn(100);
        when(sicalService.getTerceros(
            isNull(), isNull(), eq("1"), any(), any(), any()))
            .thenReturn(List.of(tercero));
        when(gbsRepository.findByENTAndEJEAndCGECOD(1, "2024", "CGE001"))
            .thenReturn(new ArrayList<>());
        when(cotRepository.findByENTAndEJEAndTERCODAndConn_CONBLOAndConn_CONTIP(
            1, "2024", 1, 0, 3))
            .thenReturn(new ArrayList<>());
        when(facRepository.save(any(Fac.class)))
            .thenAnswer(i -> i.getArgument(0));

        service.insertFacturas(List.of(createValidFacturaDto("PROV001")));

        ArgumentCaptor<Fac> captor = ArgumentCaptor.forClass(Fac.class);
        verify(facRepository).save(captor.capture());

        assertEquals(0, captor.getValue().getFACOCT());
    }
}
