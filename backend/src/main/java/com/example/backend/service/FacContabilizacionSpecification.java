package com.example.backend.service;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.example.backend.sqlserver2.model.Fac;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class FacContabilizacionSpecification {

    private static final String TERNOM = "TERNOM";
    private static final String FACDOC = "FACDOC";

    private FacContabilizacionSpecification() {
    }

    public static class SearchCriteria {
        public final Integer ent;
        public final String eje;
        public final String cgecod;
        public final String fechaType;
        public final LocalDateTime desde;
        public final LocalDateTime hasta;
        public final Integer facann;
        public final String search;

        private SearchCriteria(Builder builder) {
            this.ent = builder.ent;
            this.eje = builder.eje;
            this.cgecod = builder.cgecod;
            this.fechaType = builder.fechaType;
            this.desde = builder.desde;
            this.hasta = builder.hasta;
            this.facann = builder.facann;
            this.search = builder.search;
        }

        public static class Builder {
            private Integer ent;
            private String eje;
            private String cgecod;
            private String fechaType;
            private LocalDateTime desde;
            private LocalDateTime hasta;
            private Integer facann;
            private String search;

            public Builder ent(Integer ent) {
                this.ent = ent;
                return this;
            }

            public Builder eje(String eje) {
                this.eje = eje;
                return this;
            }

            public Builder cgecod(String cgecod) {
                this.cgecod = cgecod;
                return this;
            }

            public Builder fechaType(String fechaType) {
                this.fechaType = fechaType;
                return this;
            }

            public Builder desde(LocalDateTime desde) {
                this.desde = desde;
                return this;
            }

            public Builder hasta(LocalDateTime hasta) {
                this.hasta = hasta;
                return this;
            }

            public Builder facann(Integer facann) {
                this.facann = facann;
                return this;
            }

            public Builder search(String search) {
                this.search = search;
                return this;
            }

            public SearchCriteria build() {
                return new SearchCriteria(this);
            }
        }
    }

    public static Specification<Fac> searchContabilizacion(SearchCriteria criteria) {
        return (root, query, cb) -> {
            if (query.getResultType() == Fac.class) {
                root.fetch("ter", JoinType.LEFT);
            }

            Predicate predicate = cb.conjunction();
            predicate = applyBasicFilters(predicate, root, cb, criteria.ent, criteria.eje, criteria.cgecod);
            predicate = applyAmountFilter(predicate, root, cb);
            predicate = applyDateFilters(predicate, root, cb, criteria.fechaType, criteria.desde, criteria.hasta);
            
            if (criteria.facann != null) {
                predicate = cb.and(predicate, cb.equal(root.get("FACANN"), criteria.facann));
            }

            if (criteria.search != null && !criteria.search.trim().isEmpty()) {
                Predicate searchPredicate = buildSearchPredicate(root, cb, criteria.search.trim());
                predicate = cb.and(predicate, searchPredicate);
            }

            return predicate;
        };
    }

    private static Predicate applyBasicFilters(Predicate predicate, Root<Fac> root, CriteriaBuilder cb, Integer ent, String eje, String cgecod) {
        predicate = cb.and(predicate, cb.equal(root.get("ENT"), ent));
        predicate = cb.and(predicate, cb.equal(root.get("EJE"), eje));
        predicate = cb.and(predicate, cb.equal(root.get("CGECOD"), cgecod));
        predicate = cb.and(predicate, cb.isNull(root.get("FACADO")));
        return predicate;
    }

    private static Predicate applyAmountFilter(Predicate predicate, Root<Fac> root, CriteriaBuilder cb) {
        Expression<Double> facimpRounded = cb.function("ROUND", Double.class, root.get("FACIMP"), cb.literal(2));
        Expression<Double> sumRounded = cb.function("ROUND", Double.class, 
            cb.sum(root.get("FACIEC"), root.get("FACIDI")), cb.literal(2));
        return cb.and(predicate, cb.equal(facimpRounded, sumRounded));
    }

    private static Predicate applyDateFilters(Predicate predicate, Root<Fac> root, CriteriaBuilder cb, String fechaType, LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null) {
            if ("factura".equalsIgnoreCase(fechaType)) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("FACDAT"), desde));
            } else {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("FACFRE"), desde));
            }
        }
        
        if (hasta != null) {
            if ("factura".equalsIgnoreCase(fechaType)) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("FACDAT"), hasta));
            } else {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("FACFRE"), hasta));
            }
        }
        
        return predicate;
    }

    private static Predicate buildSearchPredicate(Root<Fac> root, CriteriaBuilder cb, String trimmed) {
        boolean onlyDigits = trimmed.matches("^\\d+$");
        boolean hasLetters = trimmed.chars().anyMatch(Character::isLetter);
        int length = trimmed.length();

        if (onlyDigits && length <= 5) {
            return searchByTercodOrFallback(root, cb, trimmed);
        } else if (onlyDigits && length > 5) {
            return cb.like(root.get("ter").get("TERNIF"), "%" + trimmed + "%");
        } else if (hasLetters && length > 5) {
            return searchByNifNameOrDoc(root, cb, trimmed);
        } else {
            return searchByNameOrDoc(root, cb, trimmed);
        }
    }

    private static Predicate searchByTercodOrFallback(Root<Fac> root, CriteriaBuilder cb, String trimmed) {
        try {
            Integer tercod = Integer.parseInt(trimmed);
            return cb.equal(root.get("TERCOD"), tercod);
        } catch (NumberFormatException e) {
            return searchByNameOrDoc(root, cb, trimmed);
        }
    }

    private static Predicate searchByNifNameOrDoc(Root<Fac> root, CriteriaBuilder cb, String trimmed) {
        return cb.or(
            cb.like(cb.upper(root.get("ter").get("TERNIF")), "%" + trimmed.toUpperCase() + "%"),
            cb.like(cb.upper(root.get("ter").get(TERNOM)), "%" + trimmed.toUpperCase() + "%"),
            cb.like(cb.upper(root.get(FACDOC)), "%" + trimmed.toUpperCase() + "%")
        );
    }

    private static Predicate searchByNameOrDoc(Root<Fac> root, CriteriaBuilder cb, String trimmed) {
        return cb.or(
            cb.like(cb.upper(root.get("ter").get(TERNOM)), "%" + trimmed.toUpperCase() + "%"),
            cb.like(cb.upper(root.get(FACDOC)), "%" + trimmed.toUpperCase() + "%")
        );
    }
}
