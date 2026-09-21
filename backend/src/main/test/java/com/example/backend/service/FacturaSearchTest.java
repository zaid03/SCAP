package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.dto.FacWithTerProjection;
import com.example.backend.sqlserver2.repository.FacRepository;

@ExtendWith(MockitoExtension.class)
public class FacturaSearchTest {

    @Mock
    private FacRepository facRepository;

    @InjectMocks
    private FacturaSearch facturaSearch;

    static class FacProjectionMock implements FacWithTerProjection {
        Integer ent, facnum, tercod, facann, facfac, facoct;
        String eje, cgecod, facobs, factdc, facdoc, facado, factxt, conctp, concpr, conccr, facfpg, facopg, factpg;
        Double facimp, faciec, facidi, facdto;
        LocalDateTime facdat, facfco, facfre;
        String terNom, terNif;

        public FacProjectionMock(Integer third, String tnif, Double fimp, Double fiec, Double fidi, 
                                String fado, LocalDateTime ffre, LocalDateTime fdat, LocalDateTime ffco,
                                Integer fann, String tnom) {
            this.tercod = third;
            this.terNif = tnif;
            this.facimp = fimp;
            this.faciec = fiec;
            this.facidi = fidi;
            this.facado = fado;
            this.facfre = ffre;
            this.facdat = fdat;
            this.facfco = ffco;
            this.facann = fann;
            this.terNom = tnom;
        }

        @Override public Integer getENT() { return ent; }
        @Override public String getEJE() { return eje; }
        @Override public Integer getFACNUM() { return facnum; }
        @Override public Integer getTERCOD() { return tercod; }
        @Override public String getCGECOD() { return cgecod; }
        @Override public String getFACOBS() { return facobs; }
        @Override public Double getFACIMP() { return facimp; }
        @Override public Double getFACIEC() { return faciec; }
        @Override public Double getFACIDI() { return facidi; }
        @Override public String getFACTDC() { return factdc; }
        @Override public Integer getFACANN() { return facann; }
        @Override public Integer getFACFAC() { return facfac; }
        @Override public String getFACDOC() { return facdoc; }
        @Override public LocalDateTime getFACDAT() { return facdat; }
        @Override public LocalDateTime getFACFCO() { return facfco; }
        @Override public String getFACADO() { return facado; }
        @Override public String getFACTXT() { return factxt; }
        @Override public LocalDateTime getFACFRE() { return facfre; }
        @Override public String getCONCTP() { return conctp; }
        @Override public String getCONCPR() { return concpr; }
        @Override public String getCONCCR() { return conccr; }
        @Override public Integer getFACOCT() { return facoct; }
        @Override public String getFACFPG() { return facfpg; }
        @Override public String getFACOPG() { return facopg; }
        @Override public String getFACTPG() { return factpg; }
        @Override public Double getFACDTO() { return facdto; }
        @Override public String getTer_TERNOM() { return terNom; }
        @Override public String getTer_TERNIF() { return terNif; }
    }

    @BeforeEach
    void setUp() {
    }


    @Test
    void searchFactura_filtersBy_TercodWhenNumbersOnly_LessThanOrEqualTo5() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, 
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("100").build());

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTERCOD());
    }

    @Test
    void searchFactura_filtersBy_TernifWhenNumbersOnly_GreaterThan5() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "123456", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "789012", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("123456").build());

        assertEquals(1, result.size());
        assertEquals("123456", result.get(0).getTer_TERNIF());
    }

    @Test
    void searchFactura_filtersBy_TernifOrTernomOrFacdocWhenMixed() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "ABC123", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Acme Corp");
        FacProjectionMock proj2 = new FacProjectionMock(200, "XYZ789", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Other Corp");

        proj1.facdoc = "FAC-2024-ABC";
        proj2.facdoc = "FAC-2024-XYZ";

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("ABC").build());

        assertEquals(1, result.size());
        assertEquals("ABC123", result.get(0).getTer_TERNIF());
    }

    @Test
    void searchFactura_returnsAll_WhenMainFilterIsNull() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").build());

        assertEquals(2, result.size());
    }

    @Test
    void searchFactura_returnsAll_WhenMainFilterIsEmpty() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("").build());

        assertEquals(2, result.size());
    }

    @Test
    void searchFactura_filtersBy_EstadoContabilizadas_WhenFacadoNotNull() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, "2024-01-15",
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("contabilizadas").build());

        assertEquals(1, result.size());
        assertEquals("2024-01-15", result.get(0).getFACADO());
    }

    @Test
    void searchFactura_filtersBy_EstadoNoContabilizadas_WhenFacadoNull() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, "2024-01-15",
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("noContabilizadas").build());

        assertEquals(1, result.size());
        assertNull(result.get(0).getFACADO());
    }

    @Test
    void searchFactura_filtersBy_EstadoPtApplidas_WhenFacadoNullAndEqualsApply() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 150.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("ptApplidas").build());

        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getFACIMP());
    }

    @Test
    void searchFactura_filtersBy_EstadoSinPtApplicar_WhenFacadoNullAndNotEquals() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("sinPtApplicar").build());

        assertEquals(1, result.size());
        assertEquals(200.0, result.get(0).getFACIMP());
    }

    @Test
    void searchFactura_filtersBy_EjFacturaYear() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2025, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").ejFactura(2024).build());

        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).getFACANN());
    }

    @Test
    void searchFactura_returnsEmptyList_WhenEjFacturaNotMatched() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").ejFactura(2025).build());

        assertEquals(0, result.size());
    }

    @Test
    void searchFactura_filtersBy_FechaRegistroBetween() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 2, 10, 10, 0);
        LocalDateTime date3 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, date1, null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null, date2, null, null, 2024, "Supplier B");
        FacProjectionMock proj3 = new FacProjectionMock(300, "NIF003", 300.0, 150.0, 150.0, null, date3, null, null, 2024, "Supplier C");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2, proj3));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 3, 31)).build());

        assertEquals(2, result.size());
    }

    @Test
    void searchFactura_filtersBy_FechaRegistroFrom() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, date1, null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null, date2, null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .fromDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void searchFactura_filtersBy_FechaRegistroTo() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, date1, null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null, date2, null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .toDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void searchFactura_filtersBy_FechaFacturaBetween() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 2, 10, 10, 0);
        LocalDateTime date3 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, null, date1, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null, null, date2, null, 2024, "Supplier B");
        FacProjectionMock proj3 = new FacProjectionMock(300, "NIF003", 300.0, 150.0, 150.0, null, null, date3, null, 2024, "Supplier C");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2, proj3));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("factura")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 3, 31)).build());

        assertEquals(2, result.size());
    }

    @Test
    void searchFactura_filtersBy_FechaFacturaFrom() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, null, date1, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null, null, date2, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("factura")
                .fromDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void searchFactura_filtersBy_FechaFacturaTo() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, null, date1, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null, null, date2, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("factura")
                .toDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void searchFactura_filtersBy_FechaContableBetween() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 2, 10, 10, 0);
        LocalDateTime date3 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, null, null, date1, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null, null, null, date2, 2024, "Supplier B");
        FacProjectionMock proj3 = new FacProjectionMock(300, "NIF003", 300.0, 150.0, 150.0, null, null, null, date3, 2024, "Supplier C");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2, proj3));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("contable")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 3, 31)).build());

        assertEquals(2, result.size());
    }

    @Test
    void searchFactura_filtersBy_FechaContableFrom() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, null, null, date1, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null, null, null, date2, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("contable")
                .fromDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void searchFactura_filtersBy_FechaContableTo() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null, null, null, date1, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null, null, null, date2, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("contable")
                .toDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void searchFactura_combinedFilters_MainFilterAndEjFacturaAndEstado() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, "2024-01-15",
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");
        FacProjectionMock proj3 = new FacProjectionMock(100, "NIF001", 150.0, 75.0, 75.0, null,
            LocalDateTime.now(), null, null, 2025, "Supplier A");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2, proj3));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("100")
                .ejFactura(2024).estado("contabilizadas").build());

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTERCOD());
        assertEquals(2024, result.get(0).getFACANN());
        assertEquals("2024-01-15", result.get(0).getFACADO());
    }

    @Test
    void searchFactura_combinedFilters_AllParameters() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, "2024-01-15",
            date1, LocalDateTime.of(2026, 1, 20, 10, 0), LocalDateTime.of(2026, 1, 25, 10, 0), 2024, "Supplier A");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1")
                .mainFilter("100").ejFactura(2024).estado("contabilizadas")
                .fecha("registro")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 2, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void searchFactura_returnsEmptyList_WhenRepositoryReturnsEmpty() {
        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of());

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").build());

        assertEquals(0, result.size());
    }

    @Test
    void searchFactura_returnsEmptyList_WhenRepositoryReturnsNull() {
        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(null);

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").build());

        assertNull(result);
    }

    @Test
    void searchFactura_filtersIgnore_WhenEstadoIsEmpty() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, "2024-01-15",
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("").build());

        assertEquals(2, result.size());
    }

    @Test
    void searchFactura_filtersIgnore_WhenFechaIsEmpty() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), LocalDateTime.now(), null, 2024, "Supplier A");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 12, 31)).build());

        assertEquals(1, result.size());
    }

    @Test
    void searchFactura_noFiltersApplied_WhenAllParametersNull() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, "2024-01-15",
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").build());

        assertEquals(2, result.size());
        verify(facRepository).findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1");
    }

    @Test
    void searchFactura_handlesNullTercodGracefully() {
        FacProjectionMock proj1 = new FacProjectionMock(null, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("100").build());

        assertEquals(0, result.size());
    }

    @Test
    void searchFactura_handlesNullTernifGracefully() {
        FacProjectionMock proj1 = new FacProjectionMock(100, null, 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("ABC123").build());

        assertEquals(0, result.size());
    }

    @Test
    void searchFactura_handlesNullDateFieldsGracefully() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, null, null, 2024, "Supplier A");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 12, 31)).build());

        assertEquals(0, result.size());
    }

    @Test
    void searchFactura_returnsMultipleResults_WhenMultipleMatch() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(100, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");
        FacProjectionMock proj3 = new FacProjectionMock(100, "NIF003", 300.0, 150.0, 150.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier C");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2, proj3));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("100").build());

        assertEquals(3, result.size());
    }

    @Test
    void searchFactura_isCaseInsensitive_WhenSearchingTernom() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "ACME CORP");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Other Corp");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("acme").build());

        assertEquals(1, result.size());
        assertEquals("ACME CORP", result.get(0).getTer_TERNOM());
    }

    @Test
    void filterByTernif_exactMatch_returnsSingleResult() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "123456", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "789012", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        List<FacWithTerProjection> list = List.of(proj1, proj2);
        
        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(list);

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("123456").build());

        assertEquals(1, result.size());
        assertEquals("123456", result.get(0).getTer_TERNIF());
    }

    @Test
    void filterByTernif_noMatch_returnsEmptyList() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "123456", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("999999").build());

        assertEquals(0, result.size());
    }

    @Test
    void filterByTernif_multipleMatches_returnsAllMatches() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "123456", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "123456", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("123456").build());

        assertEquals(2, result.size());
    }

    @Test
    void filterByTernifOrTernomOrFacdoc_filterByTernif_returnsMatch() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "ABC123", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Acme Corp");
        FacProjectionMock proj2 = new FacProjectionMock(200, "XYZ789", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Other Corp");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("ABC").build());

        assertEquals(1, result.size());
        assertEquals("ABC123", result.get(0).getTer_TERNIF());
    }

    @Test
    void filterByTernifOrTernomOrFacdoc_filterByTernom_returnsMatch() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Acme Corporation");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Other Corp");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("Acme").build());

        assertEquals(1, result.size());
        assertEquals("Acme Corporation", result.get(0).getTer_TERNOM());
    }

    @Test
    void filterByTernifOrTernomOrFacdoc_caseInsensitive() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "SUPPLIER COMPANY");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("supplier").build());

        assertEquals(1, result.size());
    }

    @Test
    void filterByTernomOrFacdoc_filterByTernom_returnsMatch() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Acme Corporation");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Other Corp");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("Acme").build());

        assertEquals(1, result.size());
    }

    @Test
    void filterByFacann_exactMatch_returnsSingleResult() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2025, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").ejFactura(2024).build());

        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).getFACANN());
    }

    @Test
    void filterByFacann_noMatch_returnsEmptyList() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").ejFactura(2025).build());

        assertEquals(0, result.size());
    }

    @Test
    void filterByFacadoNNull_filtersNotNull() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, "2024-01-15",
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("contabilizadas").build());

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getFACADO());
    }

    @Test
    void filterByFacadoNull_filtersNull() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, "2024-01-15",
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("noContabilizadas").build());

        assertEquals(1, result.size());
        assertNull(result.get(0).getFACADO());
    }

    @Test
    void filterByFacadoNullAndEMath_exactMatch_returnsMatch() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("ptApplidas").build());

        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getFACIMP());
    }

    @Test
    void filterByFacadoNullAndNotEMath_noMatch_returnsExclude() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 150.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("sinPtApplicar").build());

        assertEquals(1, result.size());
        assertEquals(200.0, result.get(0).getFACIMP());
    }

    @Test
    void filterByFacfreFrom_filtersAfterDate() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 3, 15, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            date1, null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            date2, null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .fromDate(LocalDate.of(2026, 2, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void filterByFacfreTo_filtersBeforeDate() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 3, 15, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            date1, null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            date2, null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .toDate(LocalDate.of(2026, 2, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void filterByFacfreBetween_filtersBothBoundaries() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 2, 15, 10, 0);
        LocalDateTime date3 = LocalDateTime.of(2026, 3, 15, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            date1, null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            date2, null, null, 2024, "Supplier B");
        FacProjectionMock proj3 = new FacProjectionMock(300, "NIF003", 300.0, 150.0, 150.0, null,
            date3, null, null, 2024, "Supplier C");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2, proj3));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(2, result.size());
    }

    @Test
    void filterByFacdatFrom_filtersAfterDate() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 3, 15, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, date1, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, date2, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("factura")
                .fromDate(LocalDate.of(2026, 2, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void filterByFacdatTo_filtersBeforeDate() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 3, 15, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, date1, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, date2, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("factura")
                .toDate(LocalDate.of(2026, 2, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void filterByFacdatBetween_filtersBothBoundaries() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 2, 15, 10, 0);
        LocalDateTime date3 = LocalDateTime.of(2026, 3, 15, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, date1, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, date2, null, 2024, "Supplier B");
        FacProjectionMock proj3 = new FacProjectionMock(300, "NIF003", 300.0, 150.0, 150.0, null,
            null, date3, null, 2024, "Supplier C");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2, proj3));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("factura")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(2, result.size());
    }

    @Test
    void filterByFacfcoFrom_filtersAfterDate() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 3, 15, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, null, date1, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, null, date2, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("contable")
                .fromDate(LocalDate.of(2026, 2, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void filterByFacfcoTo_filtersBeforeDate() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 3, 15, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, null, date1, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, null, date2, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("contable")
                .toDate(LocalDate.of(2026, 2, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void filterByFacfcoBetween_filtersBothBoundaries() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 2, 15, 10, 0);
        LocalDateTime date3 = LocalDateTime.of(2026, 3, 15, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, null, date1, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, null, date2, 2024, "Supplier B");
        FacProjectionMock proj3 = new FacProjectionMock(300, "NIF003", 300.0, 150.0, 150.0, null,
            null, null, date3, 2024, "Supplier C");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2, proj3));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("contable")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(2, result.size());
    }

    @Test
    void filterByTercodOrFacado_filterByTercod_returnsMatch() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("100").build());

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTERCOD());
    }

   @Test
void filterByTernifOrFacado_filterByFacado_returnsMatch() {
    FacProjectionMock proj1 = new FacProjectionMock(
        100, "NIF001", 100.0, 50.0, 50.0,
        "86518964531", LocalDateTime.now(), null, null, 2024, "Supplier A");

    FacProjectionMock proj2 = new FacProjectionMock(
        200, "NIF002", 200.0, 100.0, 100.0,
        "86518964532", LocalDateTime.now(), null, null, 2024, "Supplier B");

    when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
        .thenReturn(List.of(proj1, proj2));

    List<FacWithTerProjection> result = facturaSearch.searchFactura(
        new FacturaSearch.FacturaSearchCriteria.Builder()
            .ent(1)
            .eje("E1")
            .cgecod("C1")
            .mainFilter("86518964531")
            .build());

    assertEquals(1, result.size());
    assertEquals("86518964531", result.get(0).getFACADO());
}
    @Test
    void complexFilter_allFiltersApplied_inCorrectOrder() {
        LocalDateTime regDate = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime facDate = LocalDateTime.of(2026, 1, 20, 10, 0);
        LocalDateTime contDate = LocalDateTime.of(2026, 1, 25, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "ABC123", 100.0, 50.0, 50.0, "2024-01-15",
            regDate, facDate, contDate, 2024, "Acme Corp");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1")
                .mainFilter("ABC").ejFactura(2024).estado("contabilizadas")
                .fecha("registro")
                .fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 2, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void filterByTernif_withNullTernif_excludesNullValues() {
        FacProjectionMock proj1 = new FacProjectionMock(100, null, 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "123456", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("123456").build());

        assertEquals(1, result.size());
        assertEquals("123456", result.get(0).getTer_TERNIF());
    }

    @Test
    void filterByFacfreFrom_withNullFacfre_excludesNullValues() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.of(2026, 3, 15, 10, 0), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .fromDate(LocalDate.of(2026, 2, 1)).build());

        assertEquals(1, result.size());
    }

    @Test
    void searchFactura_withMixedMainFilter_usesTernomOrFacdoc() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "SUPPLIER-2024");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "OTHER-2025");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("SUPPLIER-2024").build());

        assertEquals(1, result.size());
        assertEquals("SUPPLIER-2024", result.get(0).getTer_TERNOM());
    }

    @Test
    void filterByTernomOrFacdoc_filterByFacdoc_returnsMatch() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        proj1.facdoc = "INVOICE-2024-001";
        proj2.facdoc = "INVOICE-2025-002";

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("2024-001").build());

        assertEquals(1, result.size());
        assertEquals("INVOICE-2024-001", result.get(0).getFACDOC());
    }

    @Test
    void filterByTernifOrTernomOrFacdoc_filterByFacdoc_returnsMatch() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        proj1.facdoc = "INVOICE-A-001";
        proj2.facdoc = "INVOICE-B-002";

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("INVOICE-A").build());

        assertEquals(1, result.size());
        assertEquals("INVOICE-A-001", result.get(0).getFACDOC());
    }

    @Test
    void searchFactura_filterByContableFromDate_onlyFromDateProvided() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, null, date1, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, null, date2, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("contable")
                .fromDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
        assertEquals(200.0, result.get(0).getFACIMP());
    }

    @Test
    void searchFactura_filterByContableToDate_onlyToDateProvided() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, null, date1, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, null, date2, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("contable")
                .toDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getFACIMP());
    }

    @Test
    void searchFactura_filterByFacturaFromDate_onlyFromDateProvided() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, date1, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, date2, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("factura")
                .fromDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
        assertEquals(200.0, result.get(0).getFACIMP());
    }

    @Test
    void searchFactura_filterByFacturaToDate_onlyToDateProvided() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            null, date1, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            null, date2, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("factura")
                .toDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getFACIMP());
    }

    @Test
    void searchFactura_filterByRegistroFromDate_onlyFromDateProvided() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            date1, null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            date2, null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .fromDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
        assertEquals(200.0, result.get(0).getFACIMP());
    }

    @Test
    void searchFactura_filterByRegistroToDate_onlyToDateProvided() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 4, 10, 10, 0);

        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            date1, null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            date2, null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").fecha("registro")
                .toDate(LocalDate.of(2026, 3, 1)).build());

        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getFACIMP());
    }

    @Test
    void searchFactura_sinPtApplicarSingleResult_returnsExpected() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 75.0, 75.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("sinPtApplicar").build());

        assertEquals(1, result.size());
        assertEquals(200.0, result.get(0).getFACIMP());
    }

    @Test
    void searchFactura_ptAplicdasMultipleResults_returnsExpected() {
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier A");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier B");
        FacProjectionMock proj3 = new FacProjectionMock(300, "NIF003", 150.0, 75.0, 75.0, null,
            LocalDateTime.now(), null, null, 2024, "Supplier C");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2, proj3));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").estado("ptApplidas").build());

        assertEquals(3, result.size());
    }

    @Test
    void searchFactura_combinedMainFilterAndDate() {
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        FacProjectionMock proj1 = new FacProjectionMock(100, "NIF001", 100.0, 50.0, 50.0, null,
            date1, null, null, 2024, "SupplierX");
        FacProjectionMock proj2 = new FacProjectionMock(200, "NIF002", 200.0, 100.0, 100.0, null,
            LocalDateTime.of(2026, 5, 1, 10, 0), null, null, 2024, "SupplierY");

        when(facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(1, "E1", "C1"))
            .thenReturn(List.of(proj1, proj2));

        List<FacWithTerProjection> result = facturaSearch.searchFactura(
            new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(1).eje("E1").cgecod("C1").mainFilter("X")
                .fecha("registro").fromDate(LocalDate.of(2026, 1, 1)).toDate(LocalDate.of(2026, 4, 30)).build());

        assertEquals(1, result.size());
        assertEquals("SupplierX", result.get(0).getTer_TERNOM());
    }
}