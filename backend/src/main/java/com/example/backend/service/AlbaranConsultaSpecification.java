package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.backend.sqlserver2.model.Ter;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import com.example.backend.sqlserver2.model.Alb;

public class AlbaranConsultaSpecification {
    private AlbaranConsultaSpecification() {}

    public static Specification<Alb> search(
        Integer ent,
        String main_search,
        String centroDep,
        String comprador,
        String ejeSearch,
        String fechaTipo,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        String estadoTipo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("ENT"), ent));
            Join<Alb, Ter> ter = root.join("ter", JoinType.LEFT);

            if (!isBlank(main_search)) {
                String value = main_search.trim();
                boolean numbersOnly = isNumbersOnly(value);
                boolean mixed = isMixed(value);
                if (numbersOnly) {
                    if (value.length() <= 5) {
                        predicates.add(cb.or(
                            cb.equal(root.get("ALBNUM"), Integer.parseInt(value)),
                            cb.equal(root.get("SOLNUM"), Integer.parseInt(value)),
                            cb.equal(root.get("FACNUM"), Integer.parseInt(value)),
                            cb.equal(root.get("TERCOD"), Integer.parseInt(value)),
                            cb.equal(cb.upper(root.get("ALBREF")), value.toUpperCase())
                        ));
                    } else {
                        predicates.add(cb.or(
                            cb.like(cb.upper(ter.get("TERNIF")), "%" + value.toUpperCase() + "%"),
                            cb.like(cb.upper(root.get("ALBREF")), "%" + value.toUpperCase() + "%")
                        ));
                    }
                } else if (mixed) {
                    if (value.length() > 5) {
                        predicates.add(cb.or(
                            cb.like(cb.upper(ter.get("TERNIF")), "%" + value.toUpperCase() + "%"),
                            cb.like(cb.upper(ter.get("TERNOM")), "%" + value.toUpperCase() + "%"),
                            cb.like(cb.upper(root.get("ALBREF")), "%" + value.toUpperCase() + "%")
                        ));
                    } 
                } else {
                    predicates.add(cb.or(
                        cb.like(cb.upper(ter.get("TERNOM")), "%" + value.toUpperCase() + "%"),
                        cb.like(cb.upper(root.get("ALBREF")), "%" + value.toUpperCase() + "%")
                    ));
                }
            }

            if (!isBlank(centroDep)) {
                String value = centroDep.trim();
                predicates.add(
                    cb.like(cb.upper(root.get("DEPCOD")), "%" + value.toUpperCase() + "%")
                );
            }

            if (!isBlank(comprador)) {
                String value = comprador.trim();
                predicates.add(
                    cb.equal(cb.upper(root.get("DEPCOD")), value.toUpperCase())
                );
            }

            if (!isBlank(ejeSearch)) {
                String value = ejeSearch.trim();
                predicates.add(
                    cb.equal(cb.upper(root.get("EJE")), value.toUpperCase())
                );
            }

            if (!isBlank(fechaTipo)) {
                if ("registro".equalsIgnoreCase(fechaTipo)) {
                    if (fromDate != null && toDate != null) {
                        predicates.add(
                            cb.between(root.get("ALBFRE"), fromDate, toDate)
                        );
                    } else if (fromDate != null) {
                        predicates.add(
                            cb.greaterThanOrEqualTo(root.get("ALBFRE"), fromDate)
                        );
                    } else if (toDate != null) {
                        predicates.add(
                            cb.lessThanOrEqualTo(root.get("ALBFRE"), toDate)
                        );
                    }                    
                }
                if ("albaran".equalsIgnoreCase(fechaTipo)) {
                    if (fromDate != null && toDate != null) {
                        predicates.add(
                            cb.between(root.get("ALBDAT"), fromDate, toDate)
                        );
                    } else if (fromDate != null) {
                        predicates.add(
                            cb.greaterThanOrEqualTo(root.get("ALBDAT"), fromDate)
                        );
                    } else if (toDate != null) {
                        predicates.add(
                            cb.lessThanOrEqualTo(root.get("ALBDAT"), toDate)
                        );
                    }
                }
            }

            if (!isBlank(estadoTipo)) {
                String value = estadoTipo.trim();
                if ("facturado".equalsIgnoreCase(value)) {
                    predicates.add(
                        cb.greaterThan(root.get("FACNUM"), 0)
                    );
                } else if ("sin_factura".equalsIgnoreCase(value)) {
                    predicates.add(
                        cb.equal(root.get("FACNUM"), 0)
                    );
                } else if ("no_facturable".equalsIgnoreCase(value)) {
                    predicates.add(
                        cb.equal(root.get("FACNUM"), -1)
                    );
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean isBlank(String value) {return value == null || value.trim().isEmpty();}
    private static boolean isNumbersOnly(String text) {return text != null && text.matches("\\d+");}
    private static boolean isMixed(String text) { return text != null && !text.isEmpty() && !isNumbersOnly(text);}
}