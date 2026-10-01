package com.example.backend.service;

import com.example.backend.dto.ProjectionContabilizar;
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
class ContabilizarSearchTest {

    @Mock
    private FdeRepository fdeRepository;

    @InjectMocks
    private ContabilizarSearch contabilizarSearch;

    @Test
    void searchContabilizado_returnsRepositoryResultWhenNull() {
        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(null);

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", null, null, null, null);

        assertNull(result);
    }

    @Test
    void searchContabilizado_returnsEmptyWhenRepositoryReturnsEmpty() {
        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(List.of());

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", null, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchContabilizado_filtersByNumericProveedorUsingNif() {
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);
        ProjectionContabilizar nonMatching = mock(ProjectionContabilizar.class);

        when(matching.getTERNIF()).thenReturn("123456");
        when(nonMatching.getTERNIF()).thenReturn("999999");

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", "123456", null, null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByNumericProveedorUsingName() {
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);
        ProjectionContabilizar nonMatching = mock(ProjectionContabilizar.class);

        when(matching.getTERNOM()).thenReturn("Proveedor 123456");
        when(nonMatching.getTERNOM()).thenReturn("Proveedor 999999");

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", "123456", null, null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByMixedProveedorName() {
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);
        ProjectionContabilizar nonMatching = mock(ProjectionContabilizar.class);

        when(matching.getTERNOM()).thenReturn("Proveedor ABC");
        when(nonMatching.getTERNOM()).thenReturn("Proveedor XYZ");

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", "abc", null, null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByCentroGestor() {
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);
        ProjectionContabilizar nonMatching = mock(ProjectionContabilizar.class);

        when(matching.getCGECOD()).thenReturn("CG01");
        when(nonMatching.getCGECOD()).thenReturn("CG02");

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", null, "cg01", null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByEconomica() {
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);
        ProjectionContabilizar nonMatching = mock(ProjectionContabilizar.class);

        when(matching.getFDEECO()).thenReturn("EC01");
        when(nonMatching.getFDEECO()).thenReturn("EC02");

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", null, null, "ec01", null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_filtersByAno() {
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);
        ProjectionContabilizar nonMatching = mock(ProjectionContabilizar.class);

        when(matching.getFACANN()).thenReturn(2026);
        when(nonMatching.getFACANN()).thenReturn(2025);

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(matching, nonMatching)));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", null, null, null, 2026);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_handlesNullProveedorFields() {
        ProjectionContabilizar nullFields = mock(ProjectionContabilizar.class);
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);

        when(nullFields.getTERNIF()).thenReturn(null);
        when(nullFields.getTERNOM()).thenReturn(null);

        when(matching.getTERNIF()).thenReturn("123456");

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(
                nullFields,
                matching
            )));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", "123456", null, null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_handlesNullCentroGestor() {
        ProjectionContabilizar nullValue = mock(ProjectionContabilizar.class);
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);

        when(nullValue.getCGECOD()).thenReturn(null);
        when(matching.getCGECOD()).thenReturn("CG01");

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(
                nullValue,
                matching
            )));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", null, "CG01", null, null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_handlesNullEconomica() {
        ProjectionContabilizar nullValue = mock(ProjectionContabilizar.class);
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);

        when(nullValue.getFDEECO()).thenReturn(null);
        when(matching.getFDEECO()).thenReturn("EC01");

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(
                nullValue,
                matching
            )));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", null, null, "EC01", null);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_handlesNullAno() {
        ProjectionContabilizar nullValue = mock(ProjectionContabilizar.class);
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);

        when(nullValue.getFACANN()).thenReturn(null);
        when(matching.getFACANN()).thenReturn(2026);

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(
                nullValue,
                matching
            )));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
                1, "2026", null, null, null, 2026);

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }

    @Test
    void searchContabilizado_appliesAllFiltersTogether() {
        ProjectionContabilizar matching = mock(ProjectionContabilizar.class);
        ProjectionContabilizar wrongProveedor = mock(ProjectionContabilizar.class);
        ProjectionContabilizar wrongCentro = mock(ProjectionContabilizar.class);
        ProjectionContabilizar wrongEconomica = mock(ProjectionContabilizar.class);
        ProjectionContabilizar wrongAno = mock(ProjectionContabilizar.class);

        when(matching.getTERNIF()).thenReturn("123456");
        when(wrongProveedor.getTERNIF()).thenReturn("999999");
        when(wrongCentro.getTERNIF()).thenReturn("123456");
        when(wrongEconomica.getTERNIF()).thenReturn("123456");
        when(wrongAno.getTERNIF()).thenReturn("123456");

        when(matching.getCGECOD()).thenReturn("CG01");
        when(wrongCentro.getCGECOD()).thenReturn("CG02");
        when(wrongEconomica.getCGECOD()).thenReturn("CG01");
        when(wrongAno.getCGECOD()).thenReturn("CG01");

        when(matching.getFDEECO()).thenReturn("EC01");
        when(wrongEconomica.getFDEECO()).thenReturn("EC02");
        when(wrongAno.getFDEECO()).thenReturn("EC01");

        when(matching.getFACANN()).thenReturn(2026);
        when(wrongAno.getFACANN()).thenReturn(2025);

        when(fdeRepository.findPendienteContabilizar(1, "2026"))
            .thenReturn(new ArrayList<>(List.of(
                matching,
                wrongProveedor,
                wrongCentro,
                wrongEconomica,
                wrongAno
            )));

        List<ProjectionContabilizar> result =
            contabilizarSearch.searchContabilizado(
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