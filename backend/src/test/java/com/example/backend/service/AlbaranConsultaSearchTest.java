package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.example.backend.dto.AlbaranConsultaDto;
import com.example.backend.sqlserver2.model.Alb;
import com.example.backend.sqlserver2.model.Ter;
import com.example.backend.sqlserver2.repository.AlbRepository;

@ExtendWith(MockitoExtension.class)
class AlbaranConsultaSearchTest {

    @Mock
    private AlbRepository albRepository;

    @InjectMocks
    private AlbaranConsultaSearch service;

    @Test
    void search_withoutFiltersUsesDefaultPageSize() {
        Alb alb = createAlb();
        when(albRepository.findByENT(anyInt(), any(Pageable.class)))
            .thenReturn(List.of(alb));

        List<AlbaranConsultaDto> result = service.searchAlbaranConsulta(
            1, null, null, null, null, null,
            null, null, null,
            0, 0, null, null
        );

        assertEquals(1, result.size());
        assertEquals(123, result.get(0).albnum());
        assertEquals(456, result.get(0).ter().tercod());
        assertEquals("Proveedor", result.get(0).ter().ternom());

        ArgumentCaptor<Pageable> captor =
            ArgumentCaptor.forClass(Pageable.class);

        verify(albRepository).findByENT(eq(1), captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals(Sort.unsorted(), pageable.getSort());
    }

    @Test
    void search_capsPageSizeAt2000() {
        when(albRepository.findByENT(anyInt(), any(Pageable.class)))
            .thenReturn(List.of());

        service.searchAlbaranConsulta(
            1, null, null, null, null, null,
            null, null, null,
            0, 5000, null, null
        );

        ArgumentCaptor<Pageable> captor =
            ArgumentCaptor.forClass(Pageable.class);

        verify(albRepository).findByENT(eq(1), captor.capture());

        assertEquals(2000, captor.getValue().getPageSize());
    }

    @Test
    void search_normalizesNegativePageAndInvalidSize() {
        when(albRepository.findByENT(anyInt(), any(Pageable.class)))
            .thenReturn(List.of());

        service.searchAlbaranConsulta(
            1, null, null, null, null, null,
            null, null, null,
            -3, -10, null, null
        );

        ArgumentCaptor<Pageable> captor =
            ArgumentCaptor.forClass(Pageable.class);

        verify(albRepository).findByENT(eq(1), captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void search_withValidSortUsesDescendingSort() {
        when(albRepository.findByENT(anyInt(), any(Pageable.class)))
            .thenReturn(List.of());

        service.searchAlbaranConsulta(
            1, null, null, null, null, null,
            null, null, null,
            2, 50, "albdat", "desc"
        );

        ArgumentCaptor<Pageable> captor =
            ArgumentCaptor.forClass(Pageable.class);

        verify(albRepository).findByENT(eq(1), captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(50, pageable.getPageSize());
        assertEquals(
            Sort.Direction.DESC,
            pageable.getSort().getOrderFor("ALBDAT").getDirection()
        );
    }

    @Test
    void search_withInvalidSortLeavesSortUnsorted() {
        when(albRepository.findByENT(anyInt(), any(Pageable.class)))
            .thenReturn(List.of());

        service.searchAlbaranConsulta(
            1, null, null, null, null, null,
            null, null, null,
            0, 50, "notAllowed", "desc"
        );

        ArgumentCaptor<Pageable> captor =
            ArgumentCaptor.forClass(Pageable.class);

        verify(albRepository).findByENT(eq(1), captor.capture());

        assertEquals(Sort.unsorted(), captor.getValue().getSort());
    }

    @Test
    void search_withFiltersUsesSpecificationQuery() {
        Alb alb = createAlb();

        when(albRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(alb)));

        List<AlbaranConsultaDto> result = service.searchAlbaranConsulta(
            1, "123", null, null, null, null,
            null, null, null,
            0, 20, null, null
        );

        assertEquals(1, result.size());
        assertEquals(123, result.get(0).albnum());

        verify(albRepository).findAll(
            any(Specification.class),
            any(Pageable.class)
        );
    }

    @Test
    void count_withoutFiltersUsesCountByEnt() {
        when(albRepository.countByENT(1)).thenReturn(125);

        long result = service.countAlbaranConsulta(
            1, null, null, null, null, null,
            null, null, null
        );

        assertEquals(125L, result);
        verify(albRepository).countByENT(1);
    }

    @Test
    void count_withFiltersUsesSpecificationCount() {
        when(albRepository.count(any(Specification.class)))
            .thenReturn(7L);

        long result = service.countAlbaranConsulta(
            1, "123", null, null, null, null,
            null, null, null
        );

        assertEquals(7L, result);
        verify(albRepository).count(any(Specification.class));
    }

    private Alb createAlb() {
        Alb alb = new Alb() {
            @Override
            public Ter getTer() {
                Ter ter = new Ter();
                ter.setTERCOD(456);
                ter.setTERNOM("Proveedor");
                return ter;
            }
        };

        alb.setALBNUM(123);
        alb.setALBREF("REF-123");
        alb.setDEPCOD("DEP");
        alb.setALBCOM("COM");
        alb.setFACNUM(0);

        return alb;
    }
}