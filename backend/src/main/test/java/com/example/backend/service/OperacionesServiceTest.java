package com.example.backend.service;

import com.example.backend.dto.Operaciones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperacionesServiceTest {

    private OperacionesService service;

    @BeforeEach
    void setup() {

        service = new OperacionesService();

        ReflectionTestUtils.setField(service, "wsUrl", "http://localhost/service?wsdl");
        ReflectionTestUtils.setField(service, "username", "user");
        ReflectionTestUtils.setField(service, "password", "pass");
        ReflectionTestUtils.setField(service, "publicKey", "PUBLICKEY");
    }

    @SuppressWarnings("unchecked")
    private List<Operaciones> parse(String xml) throws Exception {

        Method method = OperacionesService.class
                .getDeclaredMethod("parseOperaciones", String.class);

        method.setAccessible(true);

        return (List<Operaciones>) method.invoke(service, xml);
    }

    private Object invoke(String methodName,
                          Class<?> type,
                          Object value) throws Exception {

        Method method = OperacionesService.class
                .getDeclaredMethod(methodName, type);

        method.setAccessible(true);

        return method.invoke(service, value);
    }

    @Test
    void toDouble_shouldParseDecimalPoint() throws Exception {

        Double value = (Double) invoke(
                "toDouble",
                String.class,
                "15.25");

        assertEquals(15.25, value);
    }

    @Test
    void toDouble_shouldParseComma() throws Exception {

        Double value = (Double) invoke(
                "toDouble",
                String.class,
                "15,25");

        assertEquals(15.25, value);
    }

    @Test
    void toDouble_shouldReturnZero_whenInvalid() throws Exception {

        Double value = (Double) invoke(
                "toDouble",
                String.class,
                "abc");

        assertEquals(0.0, value);
    }

    @Test
    void toDouble_shouldReturnZero_whenNull() throws Exception {

        Double value = (Double) invoke(
                "toDouble",
                String.class,
                null);

        assertEquals(0.0, value);
    }

    @Test
    void toLong_shouldParseValue() throws Exception {

        Long value = (Long) invoke(
                "toLong",
                String.class,
                "123");

        assertEquals(123L, value);
    }

    @Test
    void toLong_shouldReturnNull_whenInvalid() throws Exception {

        Long value = (Long) invoke(
                "toLong",
                String.class,
                "abc");

        assertNull(value);
    }

    @Test
    void toInteger_shouldParseValue() throws Exception {

        Integer value = (Integer) invoke(
                "toInteger",
                String.class,
                "55");

        assertEquals(55, value);
    }

    @Test
    void toInteger_shouldReturnNull_whenInvalid() throws Exception {

        Integer value = (Integer) invoke(
                "toInteger",
                String.class,
                "hello");

        assertNull(value);
    }

    @Test
    void decodeOrNull_shouldDecodeBase64() throws Exception {

        String encoded =
                Base64.getEncoder().encodeToString("hello".getBytes(StandardCharsets.UTF_8));

        String value = (String) invoke(
                "decodeOrNull",
                String.class,
                encoded);

        assertEquals("hello", value);
    }

    @Test
    void decodeOrNull_shouldReturnOriginal_whenNotBase64() throws Exception {

        String value = (String) invoke(
                "decodeOrNull",
                String.class,
                "%%%%");

        assertEquals("%%%%", value);
    }

    @Test
    void decodeOrNull_shouldReturnNull_whenNull() throws Exception {

        String value = (String) invoke(
                "decodeOrNull",
                String.class,
                null);

        assertNull(value);
    }

        @Test
    void normalizeTagName_shouldRemoveNamespace() throws Exception {

        Method method = OperacionesService.class
                .getDeclaredMethod("normalizeTagName", String.class);

        method.setAccessible(true);

        assertEquals("operacion", method.invoke(service, "ns:operacion"));
        assertEquals("operacion", method.invoke(service, "OPERACION"));
        assertEquals("", method.invoke(service, (Object) null));
    }

    @Test
    void parseOperaciones_shouldReturnEmptyList_whenNoOperaciones() throws Exception {

        String xml = """
                <respuesta>
                    <exito>1</exito>
                </respuesta>
                """;

        List<Operaciones> list = parse(xml);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void parseOperaciones_shouldAcceptMinusOneSuccess() throws Exception {

        String xml = """
                <respuesta>
                    <exito>-1</exito>
                </respuesta>
                """;

        List<Operaciones> list = parse(xml);

        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    void parseOperaciones_shouldReadSoapWrapper() throws Exception {

        String xml = """
                <soap>
                    <servicioReturn>
                        &lt;respuesta&gt;
                            &lt;exito&gt;1&lt;/exito&gt;
                        &lt;/respuesta&gt;
                    </servicioReturn>
                </soap>
                """;

        List<Operaciones> list = parse(xml);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void parseOperaciones_shouldDecodeEntities() throws Exception {

        String xml = """
                <soap>
                    <servicioReturn>
                        &lt;respuesta&gt;
                            &lt;exito&gt;1&lt;/exito&gt;
                            &lt;operacion&gt;
                                &lt;numope&gt;15&lt;/numope&gt;
                                &lt;texto&gt;SGVsbG8=&lt;/texto&gt;
                            &lt;/operacion&gt;
                        &lt;/respuesta&gt;
                    </servicioReturn>
                </soap>
                """;

        List<Operaciones> list = parse(xml);

        assertEquals(1, list.size());

        Operaciones op = list.get(0);

        assertEquals(15L, op.getNumope());
        assertEquals("Hello", op.getTexto());
    }

    @Test
    void parseOperaciones_shouldReadBasicFields() throws Exception {

        String xml = """
                <respuesta>
                    <exito>1</exito>
                    <operacion>
                        <numope>99</numope>
                        <importe>12.50</importe>
                        <impiva>2.50</impiva>
                        <impdto>1.25</impdto>
                        <texto>VGVzdA==</texto>
                        <codope>Q09ERQ==</codope>
                        <signo>Kw==</signo>
                    </operacion>
                </respuesta>
                """;

        List<Operaciones> list = parse(xml);

        assertEquals(1, list.size());

        Operaciones op = list.get(0);

        assertEquals(99L, op.getNumope());
        assertEquals(12.50, op.getImporte());
        assertEquals(2.50, op.getImpiva());
        assertEquals(1.25, op.getImpdto());
        assertEquals("Test", op.getTexto());
        assertEquals("CODE", op.getCodope());
        assertEquals("+", op.getSigno());
    }

    @Test
    void parseOperaciones_shouldPopulateNestedListsAndFallbackNumope() throws Exception {

        String xml = """
                <respuesta>
                    <operacion>
                        <dto>
                            <numdto>7</numdto>
                            <dtocuenta>Q09ERQ==</dtocuenta>
                            <dtoeje>2025</dtoeje>
                            <dtoimp>10.5</dtoimp>
                        </dto>
                        <iva>
                            <ivabase1>100</ivabase1>
                            <ivaciv1>QkFTRQ==</ivaciv1>
                        </iva>
                        <Relacion>-@-T-@-2024-@-5</Relacion>
                        <linea>321-@-1-@-2-@-3-@-CTA-@-4-@-T-@-O-@-N-@-5-@-6-@-ORG-@-FUN-@-ECO-@-7-@-8.5-@-9.5-@-10.5-@-CTE-@-PAM</linea>
                    </operacion>
                </respuesta>
                """;

        List<Operaciones> list = parse(xml);

        assertEquals(1, list.size());

        Operaciones op = list.getFirst();
        assertEquals(321L, op.getNumope());
        assertNotNull(op.getDtoList());
        assertEquals(1, op.getDtoList().size());
        assertEquals("CODE", op.getDtoList().getFirst().getDtocuenta());
        assertEquals(2025, op.getDtoList().getFirst().getDtoeje());
        assertEquals(10.5, op.getDtoList().getFirst().getDtoimp());
        assertNotNull(op.getIvaList());
        assertEquals(1, op.getIvaList().size());
        assertEquals(100.0, op.getIvaList().getFirst().getIvabase1());
        assertEquals("BASE", op.getIvaList().getFirst().getIvaciv1());
        assertNotNull(op.getRelacionList());
        assertEquals(1, op.getRelacionList().size());
        assertEquals("T", op.getRelacionList().getFirst().getTipoRelacion());
        assertEquals(2024, op.getRelacionList().getFirst().getAnnoRelacion());
        assertEquals(5, op.getRelacionList().getFirst().getOrdenRelacion());
        assertNotNull(op.getLineaList());
        assertEquals(1, op.getLineaList().size());
        assertEquals(1, op.getLineaList().getFirst().getNlinea());
        assertEquals(2L, op.getLineaList().getFirst().getOpeasc());
        assertEquals("CTA", op.getLineaList().getFirst().getLincta());
    }

    @Test
    void parseOperaciones_shouldHandleInvalidNumbers() throws Exception {

        String xml = """
                <respuesta>
                    <exito>1</exito>
                    <operacion>
                        <numope>abc</numope>
                        <importe>xyz</importe>
                    </operacion>
                </respuesta>
                """;

        List<Operaciones> list = parse(xml);

        Operaciones op = list.getFirst();

        assertNull(op.getNumope());
        assertEquals(0.0, op.getImporte());
    }

    @Test
    void getOperaciones_shouldThrowWhenEjeNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getOperaciones(
                    "ORG",
                    "ENT",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null));

        assertEquals("eje is required", ex.getMessage());
    }

    @Test
    void getOperaciones_shouldThrowWhenEjeBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getOperaciones(
                    "ORG",
                    "ENT",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    ""));
    }

    @Test
    void getOperaciones_shouldCallSoapEndpointAndParseResponse() throws Exception {

        String responseXml = """
                <soap>
                    <servicioReturn>
                        &lt;respuesta&gt;
                            &lt;operacion&gt;
                                &lt;numope&gt;15&lt;/numope&gt;
                                &lt;texto&gt;SGVsbG8=&lt;/texto&gt;
                            &lt;/operacion&gt;
                        &lt;/respuesta&gt;
                    </servicioReturn>
                </soap>
                """;

        try (MockedConstruction<RestTemplate> mockedRestTemplate =
                     mockConstruction(RestTemplate.class, (mock, context) ->
                             when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                                     .thenReturn(responseXml))) {

            List<Operaciones> list = service.getOperaciones(
                    "ORG",
                    "ENT",
                    "10",
                    "20",
                    "CODIGO",
                    "ORG-1",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "OFI",
                    "2025");

            assertEquals(1, list.size());
            assertEquals(15L, list.getFirst().getNumope());
            assertEquals("Hello", list.getFirst().getTexto());

            RestTemplate restTemplate = mockedRestTemplate.constructed().getFirst();
            verify(restTemplate).postForObject(
                    eq("http://localhost/service"),
                    any(HttpEntity.class),
                    eq(String.class));
        }
    }
}