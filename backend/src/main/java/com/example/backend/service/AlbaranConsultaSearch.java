package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.backend.dto.albarnesByProveedorProjection;
import com.example.backend.sqlserver2.repository.AlbRepository;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlbaranConsultaSearch {
    @Autowired
    private AlbRepository albRepository;

    private static final int PAGE_SIZE = 20;

    private static final Map<String, String> ALLOWED_SORTS = Map.ofEntries(
        Map.entry("albnum", "ALBNUM"),
        Map.entry("ter.tercod", "ter.TERCOD"),
        Map.entry("ter.ternom", "ter.TERNOM"),
        Map.entry("albref", "ALBREF"),
        Map.entry("albdat", "ALBDAT"),
        Map.entry("albbim", "ALBBIM"),
        Map.entry("albfre", "ALBFRE"),
        Map.entry("depcod", "DEPCOD"),
        Map.entry("albcom", "ALBCOM"),
        Map.entry("solnum", "SOLNUM"),
        Map.entry("facnum", "FACNUM")
    );

    public List<albarnesByProveedorProjection> searchAlbaranConsulta(
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
        String sort,
        String direction
    ) {
        int safePage = Math.max(page, 0);
        List<albarnesByProveedorProjection> albaranes = new ArrayList<>();

        Pageable pageable = PageRequest.of(
            safePage,
            PAGE_SIZE,
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
                    PAGE_SIZE,
                    Sort.by(sortDirection, property)
                );
            }
        }

        boolean noFilters = isBlank(main_search) && isBlank(centroDep) && isBlank(comprador) && isBlank(ejeSearch) && isBlank(fechaTipo) && fromDate == null && toDate == null && isBlank(estadoTipo);

        if (noFilters) {
            albaranes = albRepository.findByENT(ent, pageable);
        }

        
        return albaranes;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}