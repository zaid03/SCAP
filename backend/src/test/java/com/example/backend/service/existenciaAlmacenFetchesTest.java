package com.example.backend.service;

import java.lang.reflect.Method;
import com.example.backend.dto.ServiceMagsProjection;
import com.example.backend.dto.existenciasProjection;
import com.example.backend.dto.magcodOnly;
import com.example.backend.sqlserver2.repository.DpeRepository;
import com.example.backend.sqlserver2.repository.MagRepository;
import com.example.backend.sqlserver2.repository.MeaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class existenciaAlmacenFetchesTest {

    @Mock
    private DpeRepository dpeRepository;

    @Mock
    private MeaRepository meaRepository;

    @Mock
    private MagRepository magRepository;

    @InjectMocks
    private existenciaAlmacenFetches service;

    @Test
    void existenciasService_throwsWhenRequiredDataMissing() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                null, "CG01", "PER01", "2026",
                null, null, null, null, 0
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, null, "PER01", "2026",
                null, null, null, null, 0
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", null, "2026",
                null, null, null, null, 0
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", null,
                null, null, null, null, 0
            )
        );
    }

    @Test
    void existenciasService_throwsWhenMagcodNotFoundWithoutSearch() {
        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", null, null, null, 0
            )
        );
    }

    @Test
    void existenciasService_throwsWhenExistenciasNotFoundWithoutSearch() {
        magcodOnly almacen = mock(magcodOnly.class);

        when(almacen.getMAGCOD()).thenReturn(10);
        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONot(
                eq(1), eq(10), eq(0), any()))
            .thenReturn(List.of());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", null, null, null, 0
            )
        );
    }

    @Test
    void existenciasService_usesProvidedMagcodWithoutSearch() {
        magcodOnly almacen = mock(magcodOnly.class);
        existenciasProjection existencia = mock(existenciasProjection.class);

        when(almacen.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONot(
                eq(1), eq(10), eq(0), any()))
            .thenReturn(List.of(existencia));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", null, null, null, 0
            );

        assertTrue(result.almacenes().isEmpty());
        assertEquals(1, result.existencias().size());
        assertSame(existencia, result.existencias().get(0));
    }

    @Test
    void existenciasService_usesServiceToFindWarehouseWithoutSearch() {
        ServiceMagsProjection serviceProjection =
            mock(ServiceMagsProjection.class);

        magcodOnly almacen = mock(magcodOnly.class);
        existenciasProjection existencia =
            mock(existenciasProjection.class);

        when(serviceProjection.getDEPCOD()).thenReturn("DEP01");
        when(almacen.getMAGCOD()).thenReturn(10);

        when(dpeRepository.findByENTAndEJEAndPERCODAndDep_Cge_CGECODAndDep_DEPALM(
                1, "2026", "PER01", "CG01", 1))
            .thenReturn(List.of(serviceProjection));

        when(magRepository.findByENTAndDEPCOD(1, "DEP01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONot(
                eq(1), eq(10), eq(0), any()))
            .thenReturn(List.of(existencia));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1, "CG01", "PER01", "2026",
                null, null, null, null, 0
            );

        assertEquals(1, result.almacenes().size());
        assertSame(serviceProjection, result.almacenes().get(0));

        assertEquals(1, result.existencias().size());
        assertSame(existencia, result.existencias().get(0));
    }

    @Test
    void existenciasService_throwsWhenServicesEmptyWithoutSearch() {
        when(dpeRepository.findByENTAndEJEAndPERCODAndDep_Cge_CGECODAndDep_DEPALM(
                1, "2026", "PER01", "CG01", 1))
            .thenReturn(List.of());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", "2026",
                null, null, null, null, 0
            )
        );
    }

    @Test
    void existenciasService_throwsWhenWarehouseFromServiceNotFound() {
        ServiceMagsProjection serviceProjection =
            mock(ServiceMagsProjection.class);

        when(serviceProjection.getDEPCOD()).thenReturn("DEP01");

        when(dpeRepository.findByENTAndEJEAndPERCODAndDep_Cge_CGECODAndDep_DEPALM(
                1, "2026", "PER01", "CG01", 1))
            .thenReturn(List.of(serviceProjection));

        when(magRepository.findByENTAndDEPCOD(1, "DEP01"))
            .thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", "2026",
                null, null, null, null, 0
            )
        );
    }

    @Test
    void existenciasService_throwsWhenExistenciasFromServiceEmpty() {
        ServiceMagsProjection serviceProjection =
            mock(ServiceMagsProjection.class);

        magcodOnly almacen = mock(magcodOnly.class);

        when(serviceProjection.getDEPCOD()).thenReturn("DEP01");
        when(almacen.getMAGCOD()).thenReturn(10);

        when(dpeRepository.findByENTAndEJEAndPERCODAndDep_Cge_CGECODAndDep_DEPALM(
                1, "2026", "PER01", "CG01", 1))
            .thenReturn(List.of(serviceProjection));

        when(magRepository.findByENTAndDEPCOD(1, "DEP01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONot(
                eq(1), eq(10), eq(0), any()))
            .thenReturn(List.of());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", "2026",
                null, null, null, null, 0
            )
        );
    }

    @Test
    void existenciasService_throwsWhenSearchHasNoMagcod() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", "2026",
                null, "ABC", null, null, 0
            )
        );
    }

    @Test
    void existenciasService_searchesByAfacod() {
        magcodOnly almacen = mock(magcodOnly.class);
        existenciasProjection existencia =
            mock(existenciasProjection.class);

        when(almacen.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACOD(
                1, 10, 0, "AFA01"))
            .thenReturn(List.of(existencia));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", null, "AFA01", null, 0
            );

        assertEquals(1, result.existencias().size());
        assertSame(existencia, result.existencias().get(0));
    }

    @Test
    void existenciasService_searchesByAsucod() {
        magcodOnly almacen = mock(magcodOnly.class);
        existenciasProjection existencia =
            mock(existenciasProjection.class);

        when(almacen.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONotAndArt_ASUCOD(
                1, 10, 0, "ASU01"))
            .thenReturn(List.of(existencia));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", null, null, "ASU01", 0
            );

        assertEquals(1, result.existencias().size());
        assertSame(existencia, result.existencias().get(0));
    }

    @Test
    void existenciasService_searchesByAfacodAndAsucod() {
        magcodOnly almacen = mock(magcodOnly.class);
        existenciasProjection existencia =
            mock(existenciasProjection.class);

        when(almacen.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACODAndArt_ASUCOD(
                1, 10, 0, "AFA01", "ASU01"))
            .thenReturn(List.of(existencia));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", null, "AFA01", "ASU01", 0
            );

        assertEquals(1, result.existencias().size());
        assertSame(existencia, result.existencias().get(0));
    }

    @Test
    void existenciasService_usesAllExistenciasWhenOnlyMainSearch() {
        magcodOnly mag = mock(magcodOnly.class);
        existenciasProjection matching = mock(existenciasProjection.class);
        existenciasProjection wrong = mock(existenciasProjection.class);

        when(mag.getMAGCOD()).thenReturn(10);

        when(matching.getArt_ARTCOD()).thenReturn("ART001");
        when(wrong.getArt_ARTCOD()).thenReturn("OTHER");

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(mag));

        when(meaRepository
            .findAllByENTAndMAGCODAndArt_ARTBLONot(1, 10, 0))
            .thenReturn(List.of(matching, wrong));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1,
                "CG01",
                "PER01",
                "2026",
                "MAG01",
                "ART001",
                null,
                null,
                0
            );

        assertEquals(1, result.existencias().size());
        assertSame(matching, result.existencias().get(0));
    }

    @Test
    void existenciasService_filtersMainSearchOnAfacodResults() {
        magcodOnly mag = mock(magcodOnly.class);
        existenciasProjection matching = mock(existenciasProjection.class);
        existenciasProjection wrong = mock(existenciasProjection.class);

        when(mag.getMAGCOD()).thenReturn(10);

        when(matching.getArt_ARTCOD()).thenReturn("ART001");
        when(wrong.getArt_ARTCOD()).thenReturn("OTHER");

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(mag));

        when(meaRepository
            .findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACOD(
                1, 10, 0, "AFA01"))
            .thenReturn(List.of(matching, wrong));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1,
                "CG01",
                "PER01",
                "2026",
                "MAG01",
                "ART001",
                "AFA01",
                null,
                0
            );

        assertEquals(1, result.existencias().size());
        assertSame(matching, result.existencias().get(0));
    }

    @Test
    void existenciasService_filtersMainSearchOnAsucodResults() {
        magcodOnly mag = mock(magcodOnly.class);
        existenciasProjection matching = mock(existenciasProjection.class);
        existenciasProjection wrong = mock(existenciasProjection.class);

        when(mag.getMAGCOD()).thenReturn(10);

        when(matching.getArt_ARTCOD()).thenReturn("ART001");
        when(wrong.getArt_ARTCOD()).thenReturn("OTHER");

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(mag));

        when(meaRepository
            .findByENTAndMAGCODAndArt_ARTBLONotAndArt_ASUCOD(
                1, 10, 0, "ASU01"))
            .thenReturn(List.of(matching, wrong));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1,
                "CG01",
                "PER01",
                "2026",
                "MAG01",
                "ART001",
                null,
                "ASU01",
                0
            );

        assertEquals(1, result.existencias().size());
        assertSame(matching, result.existencias().get(0));
    }

    @Test
    void existenciasService_filtersMainSearchOnAfacodAndAsucodResults() {
        magcodOnly mag = mock(magcodOnly.class);
        existenciasProjection matching = mock(existenciasProjection.class);
        existenciasProjection wrong = mock(existenciasProjection.class);

        when(mag.getMAGCOD()).thenReturn(10);

        when(matching.getArt_ARTCOD()).thenReturn("ART001");
        when(wrong.getArt_ARTCOD()).thenReturn("OTHER");

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(mag));

        when(meaRepository
            .findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACODAndArt_ASUCOD(
                1, 10, 0, "AFA01", "ASU01"))
            .thenReturn(List.of(matching, wrong));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1,
                "CG01",
                "PER01",
                "2026",
                "MAG01",
                "ART001",
                "AFA01",
                "ASU01",
                0
            );

        assertEquals(1, result.existencias().size());
        assertSame(matching, result.existencias().get(0));
    }

    @Test
    void existenciasService_throwsWhenAfacodResultsEmpty() {
        magcodOnly almacen = mock(magcodOnly.class);

        when(almacen.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACOD(
                1, 10, 0, "AFA01"))
            .thenReturn(List.of());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", null, "AFA01", null, 0
            )
        );
    }

    @Test
    void existenciasService_throwsWhenAsucodResultsEmpty() {
        magcodOnly almacen = mock(magcodOnly.class);

        when(almacen.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONotAndArt_ASUCOD(
                1, 10, 0, "ASU01"))
            .thenReturn(List.of());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", null, null, "ASU01", 0
            )
        );
    }

    @Test
    void existenciasService_throwsWhenBothFiltersResultsEmpty() {
        magcodOnly almacen = mock(magcodOnly.class);

        when(almacen.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository
            .findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACODAndArt_ASUCOD(
                1, 10, 0, "AFA01", "ASU01"))
            .thenReturn(List.of());

        assertThrows(
            IllegalArgumentException.class,
            () -> service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", null, "AFA01", "ASU01", 0
            )
        );
    }

    @Test
    void existenciasService_mainSearchShortMatchesArticleCode() {
        magcodOnly mag = mock(magcodOnly.class);
        existenciasProjection matching = mock(existenciasProjection.class);
        existenciasProjection wrong = mock(existenciasProjection.class);

        when(mag.getMAGCOD()).thenReturn(10);

        when(matching.getArt_ARTCOD()).thenReturn("ART001");
        when(wrong.getArt_ARTCOD()).thenReturn("OTHER");

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(mag));

        when(meaRepository
            .findAllByENTAndMAGCODAndArt_ARTBLONot(1, 10, 0))
            .thenReturn(List.of(matching, wrong));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1,
                "CG01",
                "PER01",
                "2026",
                "MAG01",
                "ART001",
                null,
                null,
                0
            );

        assertEquals(1, result.existencias().size());
        assertSame(matching, result.existencias().get(0));
    }

    @Test
    void existenciasService_mainSearchShortMatchesArticleReference() {
        magcodOnly mag = mock(magcodOnly.class);
        existenciasProjection matching = mock(existenciasProjection.class);
        existenciasProjection wrong = mock(existenciasProjection.class);

        when(mag.getMAGCOD()).thenReturn(10);

        when(matching.getArt_ARTCOD()).thenReturn(null);
        when(matching.getArt_ARTREF()).thenReturn("REF001");

        when(wrong.getArt_ARTCOD()).thenReturn(null);
        when(wrong.getArt_ARTREF()).thenReturn("OTHER");

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(mag));

        when(meaRepository
            .findAllByENTAndMAGCODAndArt_ARTBLONot(1, 10, 0))
            .thenReturn(List.of(matching, wrong));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1,
                "CG01",
                "PER01",
                "2026",
                "MAG01",
                "REF001",
                null,
                null,
                0
            );

        assertEquals(1, result.existencias().size());
        assertSame(matching, result.existencias().get(0));
    }

    @Test
    void existenciasService_mainSearchShortMatchesDescription() {
        magcodOnly mag = mock(magcodOnly.class);
        existenciasProjection matching = mock(existenciasProjection.class);
        existenciasProjection wrong = mock(existenciasProjection.class);

        when(mag.getMAGCOD()).thenReturn(10);

        when(matching.getArt_ARTCOD()).thenReturn(null);
        when(matching.getArt_ARTREF()).thenReturn(null);
        when(matching.getArt_ARTDES()).thenReturn("Product description");

        when(wrong.getArt_ARTCOD()).thenReturn(null);
        when(wrong.getArt_ARTREF()).thenReturn(null);
        when(wrong.getArt_ARTDES()).thenReturn("Something else");

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(mag));

        when(meaRepository
            .findAllByENTAndMAGCODAndArt_ARTBLONot(1, 10, 0))
            .thenReturn(List.of(matching, wrong));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1,
                "CG01",
                "PER01",
                "2026",
                "MAG01",
                "product",
                null,
                null,
                0
            );

        assertEquals(1, result.existencias().size());
        assertSame(matching, result.existencias().get(0));
    }

    @Test
    void existenciasService_mainSearchLongMatchesReference() {
        magcodOnly mag = mock(magcodOnly.class);
        existenciasProjection matching = mock(existenciasProjection.class);
        existenciasProjection wrong = mock(existenciasProjection.class);

        when(mag.getMAGCOD()).thenReturn(10);

        when(matching.getArt_ARTREF()).thenReturn("ABCDEFGHIJKL");
        when(wrong.getArt_ARTREF()).thenReturn("OTHER");

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(mag));

        when(meaRepository
            .findAllByENTAndMAGCODAndArt_ARTBLONot(1, 10, 0))
            .thenReturn(List.of(matching, wrong));

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1,
                "CG01",
                "PER01",
                "2026",
                "MAG01",
                "ABCDEFGHIJKL",
                null,
                null,
                0
            );

        assertEquals(1, result.existencias().size());
        assertSame(matching, result.existencias().get(0));
    }

    @Test
    void existenciasService_mainSearchLongMatchesDescription() {
        magcodOnly almacen = mock(magcodOnly.class);
        existenciasProjection matching =
            mock(existenciasProjection.class);

        when(almacen.getMAGCOD()).thenReturn(10);

        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(almacen));

        when(meaRepository.findAllByENTAndMAGCODAndArt_ARTBLONot(
                1, 10, 0))
            .thenReturn(List.of(matching));

        when(matching.getArt_ARTREF()).thenReturn(null);
        when(matching.getArt_ARTDES()).thenReturn(
            "Very long product description ABCDEFGHIJKL"
        );

        existenciaAlmacenFetches.NamesResponse result =
            service.existenciasService(
                1, "CG01", "PER01", "2026",
                "MAG01", "ABCDEFGHIJKL", null, null, 0
            );

        assertEquals(1, result.existencias().size());
        assertSame(matching, result.existencias().get(0));
    }

    @Test
    void existenciasService_rejectsBlankRequiredValues() {
        assertThrows(IllegalArgumentException.class, () -> service.existenciasService(
            1, " ", "PER01", "2026", null, null, null, null, 0));
        assertThrows(IllegalArgumentException.class, () -> service.existenciasService(
            1, "CG01", " ", "2026", null, null, null, null, 0));
        assertThrows(IllegalArgumentException.class, () -> service.existenciasService(
            1, "CG01", "PER01", " ", null, null, null, null, 0));
    }

    @Test
    void existenciasService_usesFallbackQueryWhenSearchHasNoArticleFilters() {
        magcodOnly warehouse = mock(magcodOnly.class);
        existenciasProjection existence = mock(existenciasProjection.class);

        when(warehouse.getMAGCOD()).thenReturn(10);
        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(warehouse));
        when(meaRepository.findByENTAndMAGCODAndArt_ARTBLONot(
                eq(1), eq(10), eq(0), any()))
            .thenReturn(List.of(existence));

        existenciaAlmacenFetches.NamesResponse result = service.existenciasService(
            1, "CG01", "PER01", "2026", "MAG01", "   ", " ", " ", 0);

        assertEquals(List.of(existence), result.existencias());
        verify(meaRepository).findByENTAndMAGCODAndArt_ARTBLONot(
            eq(1), eq(10), eq(0), eq(PageRequest.of(0, 20)));
    }

    @Test
    void existenciasService_allowsSearchToReturnNoMatches() {
        magcodOnly warehouse = mock(magcodOnly.class);
        existenciasProjection existence = mock(existenciasProjection.class);

        when(warehouse.getMAGCOD()).thenReturn(10);
        when(existence.getArt_ARTCOD()).thenReturn("OTHER");
        when(magRepository.findByENTAndDEPCOD(1, "MAG01"))
            .thenReturn(Optional.of(warehouse));
        when(meaRepository.findAllByENTAndMAGCODAndArt_ARTBLONot(1, 10, 0))
            .thenReturn(List.of(existence));

        existenciaAlmacenFetches.NamesResponse result = service.existenciasService(
            1, "CG01", "PER01", "2026", "MAG01", "MISSING", null, null, 0);

        assertTrue(result.existencias().isEmpty());
    }

    @Test
    void privateSearchHelpers_handleLongSearchAndNullFields() throws Exception {
        existenciasProjection matching = mock(existenciasProjection.class);
        existenciasProjection excluded = mock(existenciasProjection.class);
        when(matching.getArt_ARTREF()).thenReturn("REF-1234567890");
        when(excluded.getArt_ARTREF()).thenReturn(null);

        Method helper = existenciaAlmacenFetches.class.getDeclaredMethod(
            "filterByArtRefDes", List.class, String.class);
        helper.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<existenciasProjection> result = (List<existenciasProjection>) helper.invoke(
            service, List.of(matching, excluded), " ref-1234567890 ");

        assertEquals(List.of(matching), result);
    }

    @Test
    void privateApplyMainSearch_returnsInputForBlankAndUsesShortThreshold() throws Exception {
        existenciasProjection existence = mock(existenciasProjection.class);
        Method helper = existenciaAlmacenFetches.class.getDeclaredMethod(
            "applyMainSearch", List.class, String.class);
        helper.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<existenciasProjection> blankResult = (List<existenciasProjection>) helper.invoke(
            service, List.of(existence), "   ");

        assertEquals(List.of(existence), blankResult);
    }
}