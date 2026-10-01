package com.example.backend.service;

import com.example.backend.dto.FdeFacTerProjection;
import com.example.backend.sqlserver2.repository.FdeRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContabilizadoSearchTest {

    @Mock
    private FdeRepository fdeRepository;

    @InjectMocks
    private ContabilizadoSearch contabilizadoSearch;

    @Test
    void searchContabilizado_returnsNullWhenRepositoryReturnsNull() {
        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(null);

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", null, null, null, null);

        assertNull(result);
    }

    @Test
    void searchContabilizado_returnsEmptyWhenRepositoryReturnsEmpty() {
        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(List.of());

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", null, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchContabilizado_filtersByNumericProveedorUsingNif() {
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);
        FdeFacTerProjection nonMatching = mock(FdeFacTerProjection.class);

        FdeFacTerProjection.FacInfo matchingFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo nonMatchingFac =
            mock(FdeFacTerProjection.FacInfo.class);

        FdeFacTerProjection.TerInfo matchingTer =
            mock(FdeFacTerProjection.TerInfo.class);
        FdeFacTerProjection.TerInfo nonMatchingTer =
            mock(FdeFacTerProjection.TerInfo.class);

        when(matching.getFac()).thenReturn(matchingFac);
        when(nonMatching.getFac()).thenReturn(nonMatchingFac);

        when(matchingFac.getTer()).thenReturn(matchingTer);
        when(nonMatchingFac.getTer()).thenReturn(nonMatchingTer);

        when(matchingTer.getTERNIF()).thenReturn("123456");
        when(nonMatchingTer.getTERNIF()).thenReturn("999999");

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", "123456", null, null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByNumericProveedorUsingName() {
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);
        FdeFacTerProjection nonMatching = mock(FdeFacTerProjection.class);

        FdeFacTerProjection.FacInfo matchingFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo nonMatchingFac =
            mock(FdeFacTerProjection.FacInfo.class);

        FdeFacTerProjection.TerInfo matchingTer =
            mock(FdeFacTerProjection.TerInfo.class);
        FdeFacTerProjection.TerInfo nonMatchingTer =
            mock(FdeFacTerProjection.TerInfo.class);

        when(matching.getFac()).thenReturn(matchingFac);
        when(nonMatching.getFac()).thenReturn(nonMatchingFac);

        when(matchingFac.getTer()).thenReturn(matchingTer);
        when(nonMatchingFac.getTer()).thenReturn(nonMatchingTer);

        when(matchingTer.getTERNIF()).thenReturn(null);
        when(matchingTer.getTERNOM()).thenReturn("Proveedor 123456");

        when(nonMatchingTer.getTERNIF()).thenReturn(null);
        when(nonMatchingTer.getTERNOM()).thenReturn("Proveedor 999999");

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", "123456", null, null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByMixedProveedorName() {
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);
        FdeFacTerProjection nonMatching = mock(FdeFacTerProjection.class);

        FdeFacTerProjection.FacInfo matchingFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo nonMatchingFac =
            mock(FdeFacTerProjection.FacInfo.class);

        FdeFacTerProjection.TerInfo matchingTer =
            mock(FdeFacTerProjection.TerInfo.class);
        FdeFacTerProjection.TerInfo nonMatchingTer =
            mock(FdeFacTerProjection.TerInfo.class);

        when(matching.getFac()).thenReturn(matchingFac);
        when(nonMatching.getFac()).thenReturn(nonMatchingFac);

        when(matchingFac.getTer()).thenReturn(matchingTer);
        when(nonMatchingFac.getTer()).thenReturn(nonMatchingTer);

        when(matchingTer.getTERNOM()).thenReturn("Proveedor ABC");
        when(nonMatchingTer.getTERNOM()).thenReturn("Proveedor XYZ");

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", "abc", null, null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByCentroGestor() {
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);
        FdeFacTerProjection nonMatching = mock(FdeFacTerProjection.class);

        FdeFacTerProjection.FacInfo matchingFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo nonMatchingFac =
            mock(FdeFacTerProjection.FacInfo.class);

        when(matching.getFac()).thenReturn(matchingFac);
        when(nonMatching.getFac()).thenReturn(nonMatchingFac);

        when(matchingFac.getCGECOD()).thenReturn("CG01");
        when(nonMatchingFac.getCGECOD()).thenReturn("CG02");

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", null, "cg01", null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByEconomica() {
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);
        FdeFacTerProjection nonMatching = mock(FdeFacTerProjection.class);

        when(matching.getFDEECO()).thenReturn("EC01");
        when(nonMatching.getFDEECO()).thenReturn("EC02");

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", null, null, "ec01", null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByAno() {
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);
        FdeFacTerProjection nonMatching = mock(FdeFacTerProjection.class);

        FdeFacTerProjection.FacInfo matchingFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo nonMatchingFac =
            mock(FdeFacTerProjection.FacInfo.class);

        when(matching.getFac()).thenReturn(matchingFac);
        when(nonMatching.getFac()).thenReturn(nonMatchingFac);

        when(matchingFac.getFACANN()).thenReturn(2026);
        when(nonMatchingFac.getFACANN()).thenReturn(2025);

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", null, null, null, 2026);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_handlesNullProveedorStructure() {
        FdeFacTerProjection noFac = mock(FdeFacTerProjection.class);
        FdeFacTerProjection noTer = mock(FdeFacTerProjection.class);
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);

        FdeFacTerProjection.FacInfo facWithoutTer =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo matchingFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.TerInfo matchingTer =
            mock(FdeFacTerProjection.TerInfo.class);

        when(noFac.getFac()).thenReturn(null);

        when(noTer.getFac()).thenReturn(facWithoutTer);
        when(facWithoutTer.getTer()).thenReturn(null);

        when(matching.getFac()).thenReturn(matchingFac);
        when(matchingFac.getTer()).thenReturn(matchingTer);
        when(matchingTer.getTERNIF()).thenReturn("123456");

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(
                noFac,
                noTer,
                matching
            )));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", "123456", null, null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_handlesNullCentroGestor() {
        FdeFacTerProjection noFac = mock(FdeFacTerProjection.class);
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);

        FdeFacTerProjection.FacInfo matchingFac =
            mock(FdeFacTerProjection.FacInfo.class);

        when(noFac.getFac()).thenReturn(null);

        when(matching.getFac()).thenReturn(matchingFac);
        when(matchingFac.getCGECOD()).thenReturn("CG01");

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(noFac, matching)));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", null, "CG01", null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_handlesNullEconomica() {
        FdeFacTerProjection nullValue = mock(FdeFacTerProjection.class);
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);

        when(nullValue.getFDEECO()).thenReturn(null);
        when(matching.getFDEECO()).thenReturn("EC01");

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(
                nullValue,
                matching
            )));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", null, null, "EC01", null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_handlesNullAnoStructure() {
        FdeFacTerProjection noFac = mock(FdeFacTerProjection.class);
        FdeFacTerProjection nullYear = mock(FdeFacTerProjection.class);
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);

        FdeFacTerProjection.FacInfo nullYearFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo matchingFac =
            mock(FdeFacTerProjection.FacInfo.class);

        when(noFac.getFac()).thenReturn(null);

        when(nullYear.getFac()).thenReturn(nullYearFac);
        when(nullYearFac.getFACANN()).thenReturn(null);

        when(matching.getFac()).thenReturn(matchingFac);
        when(matchingFac.getFACANN()).thenReturn(2026);

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(
                noFac,
                nullYear,
                matching
            )));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1, "2026", null, null, null, 2026);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_appliesAllFiltersTogether() {
        FdeFacTerProjection matching = mock(FdeFacTerProjection.class);
        FdeFacTerProjection wrongProveedor = mock(FdeFacTerProjection.class);
        FdeFacTerProjection wrongCentro = mock(FdeFacTerProjection.class);
        FdeFacTerProjection wrongEconomica = mock(FdeFacTerProjection.class);
        FdeFacTerProjection wrongAno = mock(FdeFacTerProjection.class);

        FdeFacTerProjection.FacInfo matchingFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo wrongProveedorFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo wrongCentroFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo wrongEconomicaFac =
            mock(FdeFacTerProjection.FacInfo.class);
        FdeFacTerProjection.FacInfo wrongAnoFac =
            mock(FdeFacTerProjection.FacInfo.class);

        FdeFacTerProjection.TerInfo matchingTer =
            mock(FdeFacTerProjection.TerInfo.class);
        FdeFacTerProjection.TerInfo wrongProveedorTer =
            mock(FdeFacTerProjection.TerInfo.class);
        FdeFacTerProjection.TerInfo wrongCentroTer =
            mock(FdeFacTerProjection.TerInfo.class);
        FdeFacTerProjection.TerInfo wrongEconomicaTer =
            mock(FdeFacTerProjection.TerInfo.class);
        FdeFacTerProjection.TerInfo wrongAnoTer =
            mock(FdeFacTerProjection.TerInfo.class);

        when(matching.getFac()).thenReturn(matchingFac);
        when(wrongProveedor.getFac()).thenReturn(wrongProveedorFac);
        when(wrongCentro.getFac()).thenReturn(wrongCentroFac);
        when(wrongEconomica.getFac()).thenReturn(wrongEconomicaFac);
        when(wrongAno.getFac()).thenReturn(wrongAnoFac);

        when(matchingFac.getTer()).thenReturn(matchingTer);
        when(wrongProveedorFac.getTer()).thenReturn(wrongProveedorTer);
        when(wrongCentroFac.getTer()).thenReturn(wrongCentroTer);
        when(wrongEconomicaFac.getTer()).thenReturn(wrongEconomicaTer);
        when(wrongAnoFac.getTer()).thenReturn(wrongAnoTer);

        when(matchingTer.getTERNIF()).thenReturn("123456");
        when(wrongProveedorTer.getTERNIF()).thenReturn("999999");
        when(wrongCentroTer.getTERNIF()).thenReturn("123456");
        when(wrongEconomicaTer.getTERNIF()).thenReturn("123456");
        when(wrongAnoTer.getTERNIF()).thenReturn("123456");

        when(matchingFac.getCGECOD()).thenReturn("CG01");
        when(wrongCentroFac.getCGECOD()).thenReturn("CG02");
        when(wrongEconomicaFac.getCGECOD()).thenReturn("CG01");
        when(wrongAnoFac.getCGECOD()).thenReturn("CG01");

        when(matching.getFDEECO()).thenReturn("EC01");
        when(wrongEconomica.getFDEECO()).thenReturn("EC02");
        when(wrongAno.getFDEECO()).thenReturn("EC01");

        when(matchingFac.getFACANN()).thenReturn(2026);
        when(wrongAnoFac.getFACANN()).thenReturn(2025);

        when(fdeRepository
            .findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(
                1, "2026", 0.0, 1, "2026", 0.0))
            .thenReturn(new ArrayList<>(List.of(
                matching,
                wrongProveedor,
                wrongCentro,
                wrongEconomica,
                wrongAno
            )));

        List<FdeFacTerProjection> result =
            contabilizadoSearch.searchContabilizado(
                1,
                "2026",
                "123456",
                "CG01",
                "EC01",
                2026
            );

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }
}