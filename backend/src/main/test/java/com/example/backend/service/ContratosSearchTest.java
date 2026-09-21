package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.dto.ContratoDto;
import com.example.backend.service.CotContratoProjection.ConnInfo;
import com.example.backend.service.CotContratoProjection.TerInfo;
import com.example.backend.sqlserver2.repository.CotRepository;

@ExtendWith(MockitoExtension.class)
public class ContratosSearchTest {

    @Mock
    private CotRepository cotRepository;

    @InjectMocks
    private ContratosSearch contratosSearch;

    @Test
    void testSearchContratos_TodosMode_NullTerm() {
        Integer ent = 1;
        String eje = "2024";

        CotContratoProjection projection = createProjection(
            100,
            "LOT001",
            "Description",
            5,
            "Supplier",
            0
        );

        when(cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getConcod());
        assertEquals("LOT001", result.get(0).getConlot());
        assertEquals("Description", result.get(0).getCondes());

        verify(cotRepository).findAllProjectedByConnCONTIPAndConnENTAndConnEJE(
            3, ent, eje
        );
    }

    @Test
    void testSearchContratos_TodosMode_EmptyTerm() {
        Integer ent = 1;
        String eje = "2024";

        CotContratoProjection projection = createProjection(
            200,
            "LOT002",
            "Desc",
            10,
            "Provider",
            0
        );

        when(cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", "");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(cotRepository).findAllProjectedByConnCONTIPAndConnENTAndConnEJE(
            3, ent, eje
        );
    }

    @Test
    void testSearchContratos_TodosMode_NumericTerm() {
        Integer ent = 1;
        String eje = "2024";
        String term = "12345";

        CotContratoProjection projection = createProjection(
            12345,
            "LOT003",
            "Numeric search",
            15,
            "Vendor",
            0
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(
                3, ent, eje, 12345))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", term);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(12345, result.get(0).getConcod());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(
                3, ent, eje, 12345
            );
    }

    @Test
    void testSearchContratos_TodosMode_TextTerm_UsesContaining() {
        Integer ent = 1;
        String eje = "2024";
        String term = "Search";

        CotContratoProjection projection = createProjection(
            300,
            "LOT004",
            "Description containing Search text",
            20,
            "Company",
            0
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContaining(
                3, ent, eje, term))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", term);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Description containing Search text",
            result.get(0).getCondes());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContaining(
                3, ent, eje, term
            );
    }

    @Test
    void testSearchContratos_BloqueMode_NullTerm() {
        Integer ent = 1;
        String eje = "2024";

        CotContratoProjection projection = createProjection(
            700,
            "LOT009",
            "Bloque Null",
            45,
            "Supplier5",
            1
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLONot(
                3, ent, eje, 0))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "bloque", null);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLONot(
                3, ent, eje, 0
            );
    }

    @Test
    void testSearchContratos_BloqueMode_EmptyTerm() {
        Integer ent = 1;
        String eje = "2024";

        CotContratoProjection projection = createProjection(
            800,
            "LOT010",
            "Bloque Empty",
            50,
            "Supplier6",
            1
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLONot(
                3, ent, eje, 0))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "bloque", "");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLONot(
                3, ent, eje, 0
            );
    }

    @Test
    void testSearchContratos_BloqueMode_NumericTerm() {
        Integer ent = 1;
        String eje = "2024";
        String term = "99999";

        CotContratoProjection projection = createProjection(
            99999,
            "LOT011",
            "Bloque Numeric",
            55,
            "Supplier7",
            1
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCODAndConnCONBLONot(
                3, ent, eje, 99999, 0))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "bloque", term);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(99999, result.get(0).getConcod());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCODAndConnCONBLONot(
                3, ent, eje, 99999, 0
            );
    }

    @Test
    void testSearchContratos_BloqueMode_TextTerm_UsesContaining() {
        Integer ent = 1;
        String eje = "2024";
        String term = "Bloque";

        CotContratoProjection projection = createProjection(
            900,
            "LOT012",
            "Contrato Bloque Text",
            60,
            "Supplier8",
            1
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContainingAndConnCONBLONot(
                3, ent, eje, term, 0))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "bloque", term);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Contrato Bloque Text", result.get(0).getCondes());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContainingAndConnCONBLONot(
                3, ent, eje, term, 0
            );
    }

    @Test
    void testSearchContratos_NoBloqueMode_NullTerm() {
        Integer ent = 1;
        String eje = "2024";

        CotContratoProjection projection = createProjection(
            400,
            "LOT005",
            "NoBloque Null",
            25,
            "Supplier1",
            0
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLO(
                3, ent, eje, 0))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "noBloque", null);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLO(
                3, ent, eje, 0
            );
    }

    @Test
    void testSearchContratos_NoBloqueMode_EmptyTerm() {
        Integer ent = 1;
        String eje = "2024";

        CotContratoProjection projection = createProjection(
            500,
            "LOT006",
            "NoBloque Empty",
            30,
            "Supplier2",
            0
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLO(
                3, ent, eje, 0))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "noBloque", "");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLO(
                3, ent, eje, 0
            );
    }

    @Test
    void testSearchContratos_NoBloqueMode_NumericTerm() {
        Integer ent = 1;
        String eje = "2024";
        String term = "54321";

        CotContratoProjection projection = createProjection(
            54321,
            "LOT007",
            "NoBloque Numeric",
            35,
            "Supplier3",
            0
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCODAndConnCONBLO(
                3, ent, eje, 54321, 0))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "noBloque", term);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(54321, result.get(0).getConcod());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCODAndConnCONBLO(
                3, ent, eje, 54321, 0
            );
    }

    @Test
    void testSearchContratos_NoBloqueMode_TextTerm_UsesContaining() {
        Integer ent = 1;
        String eje = "2024";
        String term = "Text";

        CotContratoProjection projection = createProjection(
            600,
            "LOT008",
            "Some TextSearch Value",
            40,
            "Supplier4",
            0
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContainingAndConnCONBLO(
                3, ent, eje, term, 0))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "noBloque", term);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Some TextSearch Value",
            result.get(0).getCondes());

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContainingAndConnCONBLO(
                3, ent, eje, term, 0
            );
    }

    @Test
    void testSearchContratos_EmptyResult() {
        Integer ent = 1;
        String eje = "2024";

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(new ArrayList<>());

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchContratos_NullRepositoryResult() {
        Integer ent = 1;
        String eje = "2024";

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(null);

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchContratos_MultipleResults() {
        Integer ent = 1;
        String eje = "2024";

        CotContratoProjection projection1 = createProjection(
            1000, "LOT_A", "ContA", 100, "Vendor A", 0
        );

        CotContratoProjection projection2 = createProjection(
            2000, "LOT_B", "ContB", 200, "Vendor B", 1
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(List.of(projection1, projection2));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", null);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1000, result.get(0).getConcod());
        assertEquals("Vendor A", result.get(0).getTernom());

        assertEquals(2000, result.get(1).getConcod());
        assertEquals("Vendor B", result.get(1).getTernom());
    }

    @Test
    void testBuildContratoDto_AllFieldsPopulated() {
        Integer ent = 1;
        String eje = "2024";

        CotContratoProjection projection = createProjection(
            5555,
            "ECONOMIC_CODE",
            "Comprehensive Description",
            777,
            "Complete Supplier Name",
            1
        );

        LocalDateTime startDate =
            LocalDateTime.of(2024, 3, 15, 10, 30, 45);

        LocalDateTime endDate =
            LocalDateTime.of(2024, 9, 20, 14, 45, 30);

        ConnInfo conn = projection.getConn();

        when(conn.getCONFIN()).thenReturn(startDate);
        when(conn.getCONFFI()).thenReturn(endDate);

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", null);

        assertEquals(1, result.size());

        ContratoDto dto = result.get(0);

        assertEquals(5555, dto.getConcod());
        assertEquals("ECONOMIC_CODE", dto.getConlot());
        assertEquals("Comprehensive Description", dto.getCondes());
        assertEquals(startDate, dto.getConfin());
        assertEquals(endDate, dto.getConffi());
        assertEquals(1, dto.getConblo());
        assertEquals(777, dto.getTercod());
        assertEquals("Complete Supplier Name", dto.getTernom());
    }

    @Test
    void testBuildContratoDto_WithZeroValues() {
        Integer ent = 1;
        String eje = "2024";

        CotContratoProjection projection = createProjection(
            0,
            "0",
            "",
            0,
            "",
            0
        );

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(List.of(projection));

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", null);

        assertEquals(1, result.size());

        ContratoDto dto = result.get(0);

        assertEquals(0, dto.getConcod());
        assertEquals("0", dto.getConlot());
        assertEquals("", dto.getCondes());
        assertEquals(0, dto.getConblo());
        assertEquals(0, dto.getTercod());
        assertEquals("", dto.getTernom());
    }

    @Test
    void testSearchContratos_DifferentEntity() {
        Integer ent = 999;
        String eje = "2025";

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(new ArrayList<>());

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", null);

        assertNotNull(result);

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje);
    }

    @Test
    void testSearchContratos_LargeResultSet() {
        Integer ent = 1;
        String eje = "2024";

        List<CotContratoProjection> mockResults = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            mockResults.add(createProjection(
                i,
                "LOT_" + i,
                "Desc_" + i,
                i,
                "Supplier_" + i,
                i % 2
            ));
        }

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(mockResults);

        List<ContratoDto> result =
            contratosSearch.searchContrtos(ent, eje, "todos", null);

        assertNotNull(result);
        assertEquals(1000, result.size());
        assertEquals(0, result.get(0).getConcod());
        assertEquals(999, result.get(999).getConcod());
    }

    @Test
    void testSearchContratos_RepositoryCalledWithCorrectParameters() {
        Integer ent = 42;
        String eje = "2024";

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(new ArrayList<>());

        contratosSearch.searchContrtos(ent, eje, "todos", null);

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje);
    }

    @Test
    void testSearchContratos_NumericTermWithOnlyDigits() {
        Integer ent = 1;
        String eje = "2024";
        String term = "123456";

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(
                3, ent, eje, 123456))
            .thenReturn(new ArrayList<>());

        contratosSearch.searchContrtos(ent, eje, "todos", term);

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(
                3, ent, eje, 123456
            );
    }

    @Test
    void testSearchContratos_TermWithLettersUsesContaining() {
        Integer ent = 1;
        String eje = "2024";
        String term = "12AB34";

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContaining(
                3, ent, eje, term))
            .thenReturn(new ArrayList<>());

        contratosSearch.searchContrtos(ent, eje, "todos", term);

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContaining(
                3, ent, eje, term
            );
    }

    @Test
    void testSearchContratos_TermWithSpecialCharsUsesContaining() {
        Integer ent = 1;
        String eje = "2024";
        String term = "123@456";

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContaining(
                3, ent, eje, term))
            .thenReturn(new ArrayList<>());

        contratosSearch.searchContrtos(ent, eje, "todos", term);

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContaining(
                3, ent, eje, term
            );
    }

    @Test
    void testSearchContratos_EmptyStringDoesNotUseContaining() {
        Integer ent = 1;
        String eje = "2024";

        when(cotRepository
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje))
            .thenReturn(new ArrayList<>());

        contratosSearch.searchContrtos(ent, eje, "todos", "");

        verify(cotRepository)
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje);

        verify(cotRepository, never())
            .findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContaining(
                anyInt(), anyInt(), anyString(), anyString()
            );
    }

    private CotContratoProjection createProjection(
        int concod,
        String conlot,
        String condes,
        int tercod,
        String ternom,
        int conblo
    ) {
        CotContratoProjection projection =
            mock(CotContratoProjection.class);

        ConnInfo conn = mock(ConnInfo.class);
        TerInfo ter = mock(TerInfo.class);

        when(projection.getConn()).thenReturn(conn);
        when(projection.getTer()).thenReturn(ter);

        when(conn.getCONCOD()).thenReturn(concod);
        when(conn.getCONLOT()).thenReturn(conlot);
        when(conn.getCONDES()).thenReturn(condes);
        when(conn.getCONFIN())
            .thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));
        when(conn.getCONFFI())
            .thenReturn(LocalDateTime.of(2024, 12, 31, 0, 0));
        when(conn.getCONBLO()).thenReturn(conblo);

        when(ter.getTERCOD()).thenReturn(tercod);
        when(ter.getTERNOM()).thenReturn(ternom);

        return projection;
    }
}