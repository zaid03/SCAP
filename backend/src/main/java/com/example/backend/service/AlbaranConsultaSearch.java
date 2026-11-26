package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import com.example.backend.sqlserver2.model.Alb;
import com.example.backend.sqlserver2.repository.AlbRepository;
import com.example.backend.dto.AlbaranConsultaDto;
import com.example.backend.dto.AlbaranConsultaDto.TerDto;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AlbaranConsultaSearch {
    @Autowired
    private AlbRepository albRepository;

    private static final int PAGE_SIZE = 20;

    private static final Map<String, String> ALLOWED_SORTS = Map.ofEntries(
        Map.entry("albnum", "ALBNUM"),
        Map.entry("ter.tercod", "ter.TERCOD"),
        Map.entry("ter.ternom", "ter.TERNOM"),
        Map.entry("ter.ternif", "ter.TERNIF"),
        Map.entry("albref", "ALBREF"),
        Map.entry("albdat", "ALBDAT"),
        Map.entry("albbim", "ALBBIM"),
        Map.entry("albfre", "ALBFRE"),
        Map.entry("depcod", "DEPCOD"),
        Map.entry("albcom", "ALBCOM"),
        Map.entry("solnum", "SOLNUM"),
        Map.entry("facnum", "FACNUM")
    );

    private AlbaranConsultaDto toDto(Alb alb) {
        return new AlbaranConsultaDto(
            alb.getALBNUM(),
            alb.getALBREF(),
            alb.getALBDAT(),
            alb.getALBBIM(),
            alb.getALBFRE(),
            alb.getDEPCOD(),
            alb.getALBCOM(),
            alb.getSOLNUM(),
            alb.getSOLSUB(),
            alb.getFACNUM(),
            alb.getALBOBS(),
            alb.getCONCTP(),
            alb.getCONCPR(),
            alb.getCONCCR(),
            alb.getEJE(),
            alb.getTer() == null ? null : new TerDto(
                alb.getTer().getTERCOD(),
                alb.getTer().getTERNOM(),
                alb.getTer().getTERNIF()
            )
        );
    }

    public List<AlbaranConsultaDto> searchAlbaranConsulta(
        Integer ent,
        String main_search,
        String centroDep,
        String comprador,
        String ejeSearch,
        String fechaTipo,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        String estadoTipo,
        int page,
        int size,
        String sort,
        String direction
    ) {
        int safeSize = size > 0 ? Math.min(size, 2000) : PAGE_SIZE;
        int safePage = Math.max(page, 0);

        Pageable pageable = PageRequest.of(
            safePage,
            safeSize,
            Sort.unsorted()
        );

        if (!isBlank(sort)) {
            String property = ALLOWED_SORTS.get(sort);

            if (property != null) {
                Sort.Direction sortDirection =
                    "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

                pageable = PageRequest.of(
                    safePage,
                    safeSize,
                    Sort.by(sortDirection, property)
                );
            }
        }

        boolean noFilters = isBlank(main_search) && isBlank(centroDep) && isBlank(comprador) && isBlank(ejeSearch) && isBlank(fechaTipo) && fromDate == null && toDate == null && isBlank(estadoTipo);

        if (noFilters) {
            return albRepository.findByENT(ent, pageable).stream().map(this::toDto).toList();
        }

        Specification<Alb> specification = AlbaranConsultaSpecification.search(ent, main_search, centroDep, comprador, ejeSearch, fechaTipo, fromDate, toDate, estadoTipo);

        Page<Alb> pageAlbaran = albRepository.findAll(specification, pageable);
        return pageAlbaran.getContent().stream().map(this::toDto).toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public long countAlbaranConsulta(
        Integer ent,
        String mainSearch,
        String centroDep,
        String comprador,
        String ejeSearch,
        String fechaTipo,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        String estadoTipo
    ) {
        boolean noFilters =
            isBlank(mainSearch)
                && isBlank(centroDep)
                && isBlank(comprador)
                && isBlank(ejeSearch)
                && isBlank(fechaTipo)
                && fromDate == null
                && toDate == null
                && isBlank(estadoTipo);

        if (noFilters) {
            return albRepository.countByENT(ent);
        }

        Specification<Alb> specification =
            AlbaranConsultaSpecification.search(
                ent,
                mainSearch,
                centroDep,
                comprador,
                ejeSearch,
                fechaTipo,
                fromDate,
                toDate,
                estadoTipo
            );

        return albRepository.count(specification);
    }
}