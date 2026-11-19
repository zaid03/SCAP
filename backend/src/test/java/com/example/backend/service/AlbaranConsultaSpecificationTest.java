package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import com.example.backend.sqlserver2.model.Alb;
import com.example.backend.sqlserver2.model.Ter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({ "rawtypes", "unchecked" })
class AlbaranConsultaSpecificationTest {

    @Mock
    private Root<Alb> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path path;

    @Mock
    private Expression expression;

    @Mock
    private Predicate predicate;

    @Mock
    private Join<Alb, Ter> terJoin;

    @BeforeEach
    void setUp() {
        doReturn(terJoin)
            .when(root)
            .join("ter", JoinType.LEFT);

        doReturn(path)
            .when(root)
            .get(anyString());

        doReturn(path)
            .when(terJoin)
            .get(anyString());

        doReturn(expression)
            .when(cb)
            .upper(any());

        doReturn(predicate)
            .when(cb)
            .equal(any(), any());

        doReturn(predicate)
            .when(cb)
            .like(any(), anyString());

        doReturn(predicate)
            .when(cb)
            .or(any(Predicate[].class));

        doReturn(predicate)
            .when(cb)
            .and(any(Predicate[].class));

        doReturn(predicate)
            .when(cb)
            .between(
                any(Expression.class),
                any(Comparable.class),
                any(Comparable.class)
            );

        doReturn(predicate)
            .when(cb)
            .greaterThanOrEqualTo(
                any(Expression.class),
                any(Comparable.class)
            );

        doReturn(predicate)
            .when(cb)
            .lessThanOrEqualTo(
                any(Expression.class),
                any(Comparable.class)
            );

        doReturn(predicate)
            .when(cb)
            .greaterThan(
                any(Expression.class),
                any(Comparable.class)
            );
    }

    @Test
    void search_alwaysAddsEntityPredicateAndSupplierJoin() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, null, null, null, null, null,
            null, null, null
        );

        assertNotNull(specification);
        assertDoesNotThrow(() ->
            specification.toPredicate(root, query, cb)
        );

        verify(root).get("ENT");
        verify(root).join("ter", JoinType.LEFT);
        verify(cb).equal(path, 1);
    }

    @Test
    void search_withoutOptionalFiltersDoesNotReadOptionalFields() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, null, " ", "", null, null,
            null, null, null
        );

        specification.toPredicate(root, query, cb);

        verify(root, never()).get("DEPCOD");
        verify(root, never()).get("ALBCOM");
        verify(root, never()).get("EJE");
        verify(root, never()).get("ALBFRE");
        verify(root, never()).get("ALBDAT");
        verify(root, never()).get("FACNUM");
    }

    @Test
    void search_numericShortValueChecksNumericFieldsAndReference() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, "12345", null, null, null, null,
            null, null, null
        );

        specification.toPredicate(root, query, cb);

        verify(root).get("ALBNUM");
        verify(root).get("SOLNUM");
        verify(root).get("FACNUM");
        verify(root).get("TERCOD");
        verify(root).get("ALBREF");
    }

    @Test
    void search_numericLongValueChecksSupplierNifAndReference() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, "123456", null, null, null, null,
            null, null, null
        );

        specification.toPredicate(root, query, cb);

        verify(terJoin).get("TERNIF");
        verify(root).get("ALBREF");
    }

    @Test
    void search_longTextChecksSupplierNameNifAndReference() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, "supplier-name", null, null, null, null,
            null, null, null
        );

        specification.toPredicate(root, query, cb);

        verify(terJoin).get("TERNIF");
        verify(terJoin).get("TERNOM");
        verify(root).get("ALBREF");
    }

    @Test
    void search_appliesCentroCompradorAndExerciseFilters() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, null, "DEP01", "BUYER", "2024", null,
            null, null, null
        );

        specification.toPredicate(root, query, cb);

        verify(root).get("DEPCOD");
        verify(root).get("ALBCOM");
        verify(root).get("EJE");

        verify(cb).like(any(), eq("%DEP01%"));
        verify(cb).equal(any(), eq("BUYER"));
        verify(cb).equal(any(), eq("2024"));
    }

    @Test
    void search_appliesRegistrationDateBetweenFilter() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59);

        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, null, null, null, null, "registro",
            from, to, null
        );

        specification.toPredicate(root, query, cb);

        verify(root).get("ALBFRE");
        verify(cb).between(any(), eq(from), eq(to));
    }

    @Test
    void search_appliesAlbaranDateFromFilter() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);

        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, null, null, null, null, "albaran",
            from, null, null
        );

        specification.toPredicate(root, query, cb);

        verify(root).get("ALBDAT");
        verify(cb).greaterThanOrEqualTo(any(), eq(from));
    }

    @Test
    void search_appliesFacturadoState() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, null, null, null, null, null,
            null, null, "facturado"
        );

        specification.toPredicate(root, query, cb);

        verify(root).get("FACNUM");
        verify(cb).greaterThan(any(), eq(0));
    }

    @Test
    void search_appliesSinFacturaState() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, null, null, null, null, null,
            null, null, "sin_factura"
        );

        specification.toPredicate(root, query, cb);

        verify(root).get("FACNUM");
        verify(cb).equal(any(), eq(0));
    }

    @Test
    void search_appliesNoFacturableState() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, null, null, null, null, null,
            null, null, "no_facturable"
        );

        specification.toPredicate(root, query, cb);

        verify(root).get("FACNUM");
        verify(cb).equal(any(), eq(-1));
    }

    @Test
    void search_ignoresUnknownState() {
        Specification<Alb> specification = AlbaranConsultaSpecification.search(
            1, null, null, null, null, null,
            null, null, "unknown"
        );

        specification.toPredicate(root, query, cb);

        verify(root, never()).get("FACNUM");
    }
}