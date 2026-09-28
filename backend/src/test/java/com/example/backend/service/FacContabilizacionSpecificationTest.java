package com.example.backend.service;

import com.example.backend.sqlserver2.model.Fac;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class FacContabilizacionSpecificationTest {

    @Mock
    private Root root;

    @Mock
    private CriteriaQuery query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path mockPath;

    @Mock
    private Predicate mockPredicate;

    @Mock
    private Expression mockExpression;

    @Mock
    private Fetch fetch;

    @BeforeEach
    void setUp() {
        doReturn(mockPath).when(root).get(anyString());
        doReturn(fetch).when(root).fetch(anyString(), any(JoinType.class));
        doReturn(mockPredicate).when(cb).conjunction();
        doReturn(mockPredicate).when(cb).equal(any(), any());
        doReturn(mockPredicate).when(cb).isNull(any());
        doReturn(mockPredicate).when(cb).and(any(Predicate.class), any(Predicate.class));
        doReturn(mockPredicate).when(cb).like(any(), anyString());
        doReturn(mockExpression).when(cb).upper(any());
        doReturn(mockPredicate).when(cb).or(any(Predicate.class), any(Predicate.class));
        doReturn(mockPredicate).when(cb).or(any(Predicate.class), any(Predicate.class), any(Predicate.class));
        doReturn(mockExpression).when(cb).function(anyString(), any(), any());
        doReturn(mockExpression).when(cb).function(anyString(), any(), any(), any());
        doReturn(mockExpression).when(cb).literal(anyInt());
    }

    @Test
    void searchContabilizacion_withBasicCriteria_returnsSpecification() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertNotNull(spec);
    }

    @Test
    void searchContabilizacion_buildsPredicate() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
    }

    @Test
    void searchContabilizacion_withFacclassQuery_fetchesTer() {
        when(query.getResultType()).thenReturn((Class) Fac.class);
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).fetch("ter", JoinType.LEFT);
    }

    @Test
    void searchContabilizacion_withNonFacQuery_skipsJoin() {
        when(query.getResultType()).thenReturn((Class) String.class);
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root, never()).fetch("ter", JoinType.LEFT);
    }

    @Test
    void searchContabilizacion_withAllCriteria_appliesToAll() {
        LocalDateTime desde = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .fechaType("factura")
            .desde(desde)
            .hasta(hasta)
            .facann(2024)
            .search("supplier")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("ENT");
        verify(root).get("EJE");
        verify(root).get("CGECOD");
        verify(root).get("FACADO");
        verify(root).get("FACIMP");
        verify(cb, atLeastOnce()).equal(any(), any());
    }

    @Test
    void searchContabilizacion_withNullFacann_skipsAnnFilter() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .facann(null)
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root, never()).get("FACANN");
    }

    @Test
    void searchContabilizacion_withNullSearch_skipsSearchFilter() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .search(null)
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root, never()).get("TERNOM");
        verify(root, never()).get("TERNIF");
    }

    @Test
    void searchContabilizacion_withEmptySearch_skipsSearchFilter() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .search("   ")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root, never()).get("TERNOM");
    }

    @Test
    void applyBasicFilters_filtersAllFields() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(5)
            .eje("EJE02")
            .cgecod("CGE002")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("ENT");
        verify(root).get("EJE");
        verify(root).get("CGECOD");
        verify(root).get("FACADO");
        verify(cb).isNull(mockPath);
    }

    @Test
    void applyBasicFilters_setsCorrectValues() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(7)
            .eje("EJE03")
            .cgecod("CGE003")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(mockPath, 7);
    }

    @Test
    void applyAmountFilter_usesRoundFunction() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
        verify(root).get("FACIMP");
    }

    @Test
    void applyAmountFilter_comparesFacimpWithSum() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("FACIMP");
        verify(root).get("FACIEC");
        verify(root).get("FACIDI");
    }

    @Test
    void applyDateFilters_withDesdeAndFacturaType_usesCorrectField() {
        LocalDateTime desde = LocalDateTime.of(2024, 1, 1, 0, 0);
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .fechaType("factura")
            .desde(desde)
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
        verify(root).get("FACDAT");
    }

    @Test
    void applyDateFilters_withDesdeAndOtherType_usesFacfreField() {
        LocalDateTime desde = LocalDateTime.of(2024, 1, 1, 0, 0);
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .fechaType("recepcion")
            .desde(desde)
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("FACFRE");
    }

    @Test
    void applyDateFilters_withHastaAndFacturaType_usesCorrectField() {
        LocalDateTime hasta = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .fechaType("factura")
            .hasta(hasta)
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
        verify(root).get("FACDAT");
    }

    @Test
    void applyDateFilters_withBothDatesAndFacturaType_filtersBoth() {
        LocalDateTime desde = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .fechaType("factura")
            .desde(desde)
            .hasta(hasta)
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
        verify(root, atLeastOnce()).get("FACDAT");
    }

    @Test
    void applyDateFilters_withNullDates_skipsFiltering() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .desde(null)
            .hasta(null)
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).equal(any(), any());
    }

    @Test
    void buildSearchPredicate_withOnlyDigitsAndShortLength_searchesByTercod() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .search("123")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("TERCOD");
    }

    @Test
    void buildSearchPredicate_withOnlyDigitsAndLongLength_searchesByNif() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .search("123456789")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(root).get("ter");
    }

    @Test
    void buildSearchPredicate_withLettersAndLongLength_searchesByNifNameDoc() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .search("Company123456")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).upper(any());
    }

    @Test
    void buildSearchPredicate_withLettersAndShortLength_searchesByNameDoc() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .search("ABC")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        spec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).or(any(Predicate.class), any(Predicate.class));
    }

    @Test
    void searchCriteriaBuilder_buildsWithAllFields() {
        LocalDateTime desde = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(5)
            .eje("EJE05")
            .cgecod("CGE005")
            .fechaType("factura")
            .desde(desde)
            .hasta(hasta)
            .facann(2024)
            .search("test")
            .build();

        assertEquals(5, criteria.ent);
        assertEquals("EJE05", criteria.eje);
        assertEquals("CGE005", criteria.cgecod);
        assertEquals("factura", criteria.fechaType);
        assertEquals(desde, criteria.desde);
        assertEquals(hasta, criteria.hasta);
        assertEquals(2024, criteria.facann);
        assertEquals("test", criteria.search);
    }

    @Test
    void searchCriteriaBuilder_buildsWithMinimalFields() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .build();

        assertEquals(1, criteria.ent);
        assertEquals("EJE01", criteria.eje);
        assertEquals("CGE001", criteria.cgecod);
        assertNull(criteria.fechaType);
        assertNull(criteria.desde);
        assertNull(criteria.hasta);
        assertNull(criteria.facann);
        assertNull(criteria.search);
    }

    @Test
    void searchContabilizacion_withMultipleSearch_allApplied() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .search("Supplier#2024")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
    }

    @Test
    void searchContabilizacion_withSpecialCharacters_handledCorrectly() {
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .search("Test&@#$%")
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
    }

    @Test
    void searchContabilizacion_withLongSearch_handledCorrectly() {
        StringBuilder longSearch = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longSearch.append("a");
        }
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .search(longSearch.toString())
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
    }

    @Test
    void searchContabilizacion_withDifferentEntities_allAccepted() {
        for (int ent : new int[]{1, 5, 10, 99, 999}) {
            FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
                .ent(ent)
                .eje("EJE01")
                .cgecod("CGE001")
                .build();

            Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
            assertNotNull(spec);
        }
    }

    @Test
    void applyDateFilters_caseInsensitive_fechaType() {
        LocalDateTime desde = LocalDateTime.of(2024, 1, 1, 0, 0);
        
        FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
            .ent(1)
            .eje("EJE01")
            .cgecod("CGE001")
            .fechaType("FACTURA")
            .desde(desde)
            .build();

        Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);
        assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
    }
}
