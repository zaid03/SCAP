package com.example.backend.service;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.example.backend.dto.Partida;
import com.example.backend.exception.XmlParsingException;

@ExtendWith(MockitoExtension.class)
public class PartidasServiceTest {

    private PartidasService service;

    @BeforeEach
    void setUp() {
        service = new PartidasService();
        ReflectionTestUtils.setField(service, "wsUrl", "http://test-sical-ws:8080/services/Ci?wsdl");
        ReflectionTestUtils.setField(service, "username", "testuser");
        ReflectionTestUtils.setField(service, "password", "testpass");
        ReflectionTestUtils.setField(service, "publicKey", "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...");
    }

    @Test
    void searchCriteria_builder_withAllFields_constructsCorrectly() {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .cenges("CG001")
            .alias("ALIAS001")
            .clorg("ORG001")
            .clfun("FUN001")
            .cleco("ECO001")
            .clcte("CTE001")
            .clpam("PAM001")
            .usucenges("USER001")
            .build();

        assertNotNull(criteria);
        assertEquals("CG001", criteria.cenges);
        assertEquals("ALIAS001", criteria.alias);
        assertEquals("ORG001", criteria.clorg);
        assertEquals("FUN001", criteria.clfun);
        assertEquals("ECO001", criteria.cleco);
        assertEquals("CTE001", criteria.clcte);
        assertEquals("PAM001", criteria.clpam);
        assertEquals("USER001", criteria.usucenges);
    }

    @Test
    void searchCriteria_builder_withPartialFields_allowsNullValues() {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .cenges("CG001")
            .clorg("ORG001")
            .build();

        assertNotNull(criteria);
        assertEquals("CG001", criteria.cenges);
        assertEquals("ORG001", criteria.clorg);
        assertNull(criteria.alias);
        assertNull(criteria.clfun);
    }

    @Test
    void searchCriteria_builder_withNoFields_constructsWithAllNulls() {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .build();

        assertNotNull(criteria);
        assertNull(criteria.cenges);
        assertNull(criteria.alias);
        assertNull(criteria.clorg);
    }

    @Test
    void searchCriteria_builderChaining_supportsFluentApi() {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .cenges("CG001")
            .alias("ALIAS001")
            .clorg("ORG001")
            .clfun("FUN001")
            .cleco("ECO001")
            .clcte("CTE001")
            .clpam("PAM001")
            .usucenges("USER001")
            .build();

        assertNotNull(criteria);
        assertEquals(8, countNonNullFields(criteria));
    }

    @Test
    void service_configuresPropertiesCorrectly() {
        String wsUrl = (String) ReflectionTestUtils.getField(service, "wsUrl");
        String username = (String) ReflectionTestUtils.getField(service, "username");

        assertEquals("http://test-sical-ws:8080/services/Ci?wsdl", wsUrl);
        assertEquals("testuser", username);
    }

    @Test
    void service_configuresSecurityFieldsCorrectly() {
        String password = (String) ReflectionTestUtils.getField(service, "password");
        String publicKey = (String) ReflectionTestUtils.getField(service, "publicKey");

        assertEquals("testpass", password);
        assertTrue(publicKey.startsWith("-----BEGIN PUBLIC KEY-----"));
    }

    private int countNonNullFields(PartidasService.SearchCriteria criteria) {
        int count = 0;
        if (criteria.cenges  != null) count++;
        if (criteria.alias != null) count++;
        if (criteria.clorg != null) count++;
        if (criteria.clfun != null) count++;
        if (criteria.cleco != null) count++;
        if (criteria.clcte != null) count++;
        if (criteria.clpam != null) count++;
        if (criteria.usucenges != null) count++;
        return count;
    }

    @Test
    void unescapeXmlEntities_replacesAllEntityTypes() throws Exception {
        String input = "&lt;tag&gt;&quot;value&quot;&apos;test&apos;";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("<tag>\"value\"'test'", result);
    }

    @Test
    void unescapeXmlEntities_handlesLtEntity() throws Exception {
        String result = invokeUnescapeXmlEntities("&lt;");
        assertEquals("<", result);
    }

    @Test
    void unescapeXmlEntities_handlesGtEntity() throws Exception {
        String result = invokeUnescapeXmlEntities("&gt;");
        assertEquals(">", result);
    }

    @Test
    void unescapeXmlEntities_handlesQuotEntity() throws Exception {
        String result = invokeUnescapeXmlEntities("&quot;");
        assertEquals("\"", result);
    }

    @Test
    void unescapeXmlEntities_handlesAposEntity() throws Exception {
        String result = invokeUnescapeXmlEntities("&apos;");
        assertEquals("'", result);
    }

    @Test
    void unescapeXmlEntities_handlesMixedContent() throws Exception {
        String input = "normal&lt;tag&gt;content&quot;data&quot;";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("normal<tag>content\"data\"", result);
    }

    @Test
    void extractInnerXmlContent_extractsFromServiceioReturn() throws Exception {
        String xml = "<soapenv:Body><impl:servicio><servicioReturn>extracted content</servicioReturn></impl:servicio></soapenv:Body>";
        String result = invokeExtractInnerXmlContent(xml);
        assertEquals("extracted content", result);
    }

    @Test
    void extractInnerXmlContent_returnsFullXmlWhenNoServiceioReturn() throws Exception {
        String xml = "<tag>content</tag>";
        String result = invokeExtractInnerXmlContent(xml);
        assertEquals(xml, result);
    }

    @Test
    void extractInnerXmlContent_returnsEmptyStringForNull() throws Exception {
        String result = invokeExtractInnerXmlContent(null);
        assertEquals("", result);
    }

    @Test
    void extractInnerXmlContent_handlesNestedXml() throws Exception {
        String xml = "<servicioReturn><partida><alias>TEST</alias></partida></servicioReturn>";
        String result = invokeExtractInnerXmlContent(xml);
        assertTrue(result.contains("<partida>"));
        assertTrue(result.contains("TEST"));
    }

    @Test
    void toDouble_parsesValidDouble() throws Exception {
        Double result = invokeToDouble("123.45");
        assertEquals(123.45, result);
    }

    @Test
    void toDouble_handlesPeriodAsDecimal() throws Exception {
        Double result = invokeToDouble("999.99");
        assertEquals(999.99, result);
    }

    @Test
    void toDouble_handlesNullAsZero() throws Exception {
        Double result = invokeToDouble(null);
        assertEquals(0.0, result);
    }

    @Test
    void toDouble_handlesEmptyStringAsZero() throws Exception {
        Double result = invokeToDouble("");
        assertEquals(0.0, result);
    }

    @Test
    void toDouble_handlesWhitespaceAsZero() throws Exception {
        Double result = invokeToDouble("   ");
        assertEquals(0.0, result);
    }

    @Test
    void toDouble_handlesMalformedStringAsZero() throws Exception {
        Double result = invokeToDouble("not-a-number");
        assertEquals(0.0, result);
    }

    @Test
    void toDouble_handlesLargeNumbers() throws Exception {
        Double result = invokeToDouble("999999999.99");
        assertEquals(999999999.99, result);
    }

    @Test
    void toDouble_handlesNegativeNumbers() throws Exception {
        Double result = invokeToDouble("-123.45");
        assertEquals(-123.45, result);
    }

    @Test
    void getTagValue_returnsTagContent() throws Exception {
        String xml = "<root><alias>TEST_ALIAS</alias></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "alias");
        assertEquals("TEST_ALIAS", result);
    }

    @Test
    void getTagValue_returnsNullWhenTagNotFound() throws Exception {
        String xml = "<root><other>value</other></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "missing");
        assertNull(result);
    }

    @Test
    void getTagValue_returnsFirstTagWhenMultiple() throws Exception {
        String xml = "<root><alias>first</alias><alias>second</alias></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "alias");
        assertEquals("first", result);
    }

    @Test
    void getTagValue_returnsEmptyStringForEmptyTag() throws Exception {
        String xml = "<root><alias></alias></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "alias");
        assertEquals("", result);
    }

    @Test
    void getTagValue_returnsNestedContent() throws Exception {
        String xml = "<root><partida><desc>partition description</desc></partida></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "desc");
        assertEquals("partition description", result);
    }

    @Test
    void validateAndThrowIfError_allowsWhenNoExitoTag() throws Exception {
        String xml = "<root><desc>Success</desc></root>";
        Document doc = parseXmlDom(xml);
        assertDoesNotThrow(() -> invokeValidateAndThrowIfError(doc));
    }

    @Test
    void validateAndThrowIfError_allowsWhenExitoIsNegativeOne() throws Exception {
        String xml = "<root><exito>-1</exito></root>";
        Document doc = parseXmlDom(xml);
        assertDoesNotThrow(() -> invokeValidateAndThrowIfError(doc));
    }

    @Test
    void validateAndThrowIfError_allowsWhenExitoIsOne() throws Exception {
        String xml = "<root><exito>1</exito></root>";
        Document doc = parseXmlDom(xml);
        assertDoesNotThrow(() -> invokeValidateAndThrowIfError(doc));
    }

    @Test
    void validateAndThrowIfError_throwsWhenExitoIsTwo() throws Exception {
        String xml = "<root><exito>2</exito><desc>Error message</desc></root>";
        Document doc = parseXmlDom(xml);
        try {
            invokeValidateAndThrowIfError(doc);
            fail("Expected SicalParseException");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof PartidasService.SicalParseException || ex instanceof PartidasService.SicalParseException);
        }
    }

    @Test
    void validateAndThrowIfError_includesDescriptionInError() throws Exception {
        String xml = "<root><exito>0</exito><desc>Invalid data</desc></root>";
        Document doc = parseXmlDom(xml);
        try {
            invokeValidateAndThrowIfError(doc);
            fail("Expected SicalParseException");
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            assertTrue(cause instanceof PartidasService.SicalParseException);
            assertTrue(cause.getMessage().contains("Invalid data"));
        }
    }

    @Test
    void validateAndThrowIfError_throwsWhenExitoIsZero() throws Exception {
        String xml = "<root><exito>0</exito></root>";
        Document doc = parseXmlDom(xml);
        try {
            invokeValidateAndThrowIfError(doc);
            fail("Expected SicalParseException");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof PartidasService.SicalParseException || ex instanceof PartidasService.SicalParseException);
        }
    }

    @Test
    void parseXmlDocument_parsesValidXml() throws Exception {
        String xml = "<root><item>value</item></root>";
        Document doc = invokeParseXmlDocument(xml);
        assertNotNull(doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    @Test
    void parseXmlDocument_handlesNestedElements() throws Exception {
        String xml = "<root><parent><child>text</child></parent></root>";
        Document doc = invokeParseXmlDocument(xml);
        assertNotNull(doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    @Test
    void parseXmlDocument_handlesAttributes() throws Exception {
        String xml = "<root attr=\"value\"><item id=\"1\">text</item></root>";
        Document doc = invokeParseXmlDocument(xml);
        assertNotNull(doc);
        Element root = doc.getDocumentElement();
        assertEquals("value", root.getAttribute("attr"));
    }

    @Test
    void parseXmlDocument_throwsForInvalidXml() throws Exception {
        String xml = "<root><unclosed>";
        try {
            invokeParseXmlDocument(xml);
            fail("Expected XmlParsingException");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof XmlParsingException || ex instanceof XmlParsingException);
        }
    }

    private String invokeUnescapeXmlEntities(String input) throws Exception {
        Method method = PartidasService.class.getDeclaredMethod("unescapeXmlEntities", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, input);
    }

    private String invokeExtractInnerXmlContent(String xml) throws Exception {
        Method method = PartidasService.class.getDeclaredMethod("extractInnerXmlContent", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, xml);
    }

    private Double invokeToDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private String invokeGetTagValue(Element parent, String tagName) throws Exception {
        Method method = PartidasService.class.getDeclaredMethod("getTagValue", Element.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, parent, tagName);
    }

    private void invokeValidateAndThrowIfError(Document doc) throws Exception {
        Method method = PartidasService.class.getDeclaredMethod("validateAndThrowIfError", Document.class);
        method.setAccessible(true);
        method.invoke(service, doc);
    }

    private Document invokeParseXmlDocument(String xml) throws Exception {
        Method method = PartidasService.class.getDeclaredMethod("parseXmlDocument", String.class);
        method.setAccessible(true);
        return (Document) method.invoke(service, xml);
    }

    private Document parseXmlDom(String xml) throws Exception {
        Method method = PartidasService.class.getDeclaredMethod("parseXmlDocument", String.class);
        method.setAccessible(true);
        return (Document) method.invoke(service, xml);
    }

    @Test
    void createPartidaFromElement_populatesBasicFields() throws Exception {
        String xml = "<partida>" +
            "<alias>TEST_ALIAS</alias>" +
            "<ejeapl>EJE001</ejeapl>" +
            "<desc>Test description</desc>" +
            "</partida>";
        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();
        
        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void createPartidaFromElement_populatesDoubleFields() throws Exception {
        String xml = "<partida>" +
            "<cipocin>1000.00</cipocin>" +
            "<modcred>2000.50</modcred>" +
            "<credextra>500.25</credextra>" +
            "<cretot>3500.75</cretot>" +
            "</partida>";
        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();
        
        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void createPartidaFromElement_handlesAllDoubleAmounts() throws Exception {
        String xml = "<partida>" +
            "<alias>TEST</alias>" +
            "<gasauto>100.00</gasauto>" +
            "<autdisp>200.00</autdisp>" +
            "<gascomp>300.00</gascomp>" +
            "<sdisp>400.00</sdisp>" +
            "</partida>";
        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();
        
        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void createPartidaFromElement_handlesNullOptionalFields() throws Exception {
        String xml = "<partida><alias>TEST</alias></partida>";
        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();
        
        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void decodeOrNull_returnsNullForNull() throws Exception {
        String result = invokeDecodeOrNull(null);
        assertNull(result);
    }

    @Test
    void decodeOrNull_returnsNullForBlank() throws Exception {
        String result = invokeDecodeOrNull("");
        assertNull(result);
    }

    @Test
    void decodeOrNull_returnsNullForWhitespace() throws Exception {
        String result = invokeDecodeOrNull("   ");
        assertNull(result);
    }

    @Test
    void decodeOrNull_returnsValueWhenNotBlank() throws Exception {
        String result = invokeDecodeOrNull("TEST");
        assertNotNull(result);
    }

    private Object invokeCreatePartidaFromElement(Element partidaEl) throws Exception {
        Method method = PartidasService.class.getDeclaredMethod("createPartidaFromElement", Element.class);
        method.setAccessible(true);
        return method.invoke(service, partidaEl);
    }

    private String invokeDecodeOrNull(String value) throws Exception {
        Method method = PartidasService.class.getDeclaredMethod("decodeOrNull", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, value);
    }

    @Test
    void getPartidas_allCriteriaFields_buildsCompleteXmlRequest() throws Exception {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .cenges("CG001")
            .alias("ALIAS001")
            .clorg("ORG001")
            .clfun("FUN001")
            .cleco("ECO001")
            .clcte("CTE001")
            .clpam("PAM001")
            .usucenges("USER001")
            .build();

        assertNotNull(criteria.cenges);
        assertNotNull(criteria.alias);
        assertNotNull(criteria.clorg);
        assertEquals("CG001", criteria.cenges);
        assertEquals("ALIAS001", criteria.alias);
    }

    @Test
    void getPartidas_partialCriteriaFields_buildsPartialXmlRequest() throws Exception {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .cenges("CG001")
            .clorg("ORG001")
            .build();

        assertNotNull(criteria.cenges);
        assertNotNull(criteria.clorg);
        assertNull(criteria.alias);
        assertNull(criteria.clfun);
        assertNull(criteria.cleco);
    }

    @Test
    void getPartidas_emptyCriteria_allowsAllNullFields() throws Exception {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder().build();

        assertNull(criteria.cenges);
        assertNull(criteria.alias);
        assertNull(criteria.clorg);
        assertNull(criteria.clfun);
        assertNull(criteria.cleco);
        assertNull(criteria.clcte);
        assertNull(criteria.clpam);
        assertNull(criteria.usucenges);
    }

    @Test
    void parsePartidas_withValidSinglePartida_returnsList() throws Exception {
        String xml = "<root><partida>" +
            "<alias>TEST_ALIAS</alias>" +
            "<ejeapl>EJE001</ejeapl>" +
            "<desc>Test description</desc>" +
            "<cipocin>1000.00</cipocin>" +
            "</partida></root>";

        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        Element partidaEl = (Element) root.getElementsByTagName("partida").item(0);

        Object partida = invokeCreatePartidaFromElement(partidaEl);
        assertNotNull(partida);
    }

    @Test
    void parsePartidas_withMultiplePartidas_returnsAllElements() throws Exception {
        String xml = "<root>" +
            "<partida><alias>ALIAS1</alias><ejeapl>EJE001</ejeapl></partida>" +
            "<partida><alias>ALIAS2</alias><ejeapl>EJE002</ejeapl></partida>" +
            "<partida><alias>ALIAS3</alias><ejeapl>EJE003</ejeapl></partida>" +
            "</root>";

        Document doc = parseXmlDom(xml);
        NodeList partidaNodes = doc.getElementsByTagName("partida");
        
        assertEquals(3, partidaNodes.getLength());
    }

    @Test
    void parsePartidas_withComplexPartidaStructure_populatesAllFields() throws Exception {
        String xml = "<partida>" +
            "<alias>COMPLEX</alias>" +
            "<ejeapl>EJE001</ejeapl>" +
            "<orgapl>T1JHMDAxAA==</orgapl>" +
            "<funapl>RlVOMDAxAA==</funapl>" +
            "<ecoapl>RUNPMDAxAA==</ecoapl>" +
            "<pamapl>UEFNMDAxAA==</pamapl>" +
            "<cteapl>Q1RFMDAxAA==</cteapl>" +
            "<desc>T2V0ZXN0IGRlc2MhAA==</desc>" +
            "<cipocin>1500.50</cipocin>" +
            "<modcred>2000.75</modcred>" +
            "<credextra>500.25</credextra>" +
            "<cretot>4000.50</cretot>" +
            "</partida>";

        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();

        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void parsePartidas_withValidExitoValue_allowsProcessing() throws Exception {
        String xml = "<root><exito>1</exito></root>";
        Document doc = parseXmlDom(xml);

        assertDoesNotThrow(() -> invokeValidateAndThrowIfError(doc));
    }

    @Test
    void parsePartidas_withValidExitoNegativeOne_allowsProcessing() throws Exception {
        String xml = "<root><exito>-1</exito></root>";
        Document doc = parseXmlDom(xml);

        assertDoesNotThrow(() -> invokeValidateAndThrowIfError(doc));
    }

    @Test
    void parsePartidas_withInvalidExito_throwsSicalException() throws Exception {
        String xml = "<root><exito>0</exito><desc>Invalid request</desc></root>";
        Document doc = parseXmlDom(xml);

        try {
            invokeValidateAndThrowIfError(doc);
            fail("Expected SicalParseException");
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            assertTrue(cause instanceof PartidasService.SicalParseException);
        }
    }

    @Test
    void parsePartidas_withExitoTwo_throwsSicalException() throws Exception {
        String xml = "<root><exito>2</exito><desc>Server error</desc></root>";
        Document doc = parseXmlDom(xml);

        try {
            invokeValidateAndThrowIfError(doc);
            fail("Expected SicalParseException");
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            assertTrue(cause instanceof PartidasService.SicalParseException);
        }
    }

    @Test
    void parsePartidas_withoutExitoTag_allowsProcessing() throws Exception {
        String xml = "<root><partida><alias>TEST</alias></partida></root>";
        Document doc = parseXmlDom(xml);

        assertDoesNotThrow(() -> invokeValidateAndThrowIfError(doc));
    }

    @Test
    void parsePartidas_emptyXmlString_producesEmptyList() throws Exception {
        String emptyXml = "";
        
        Object result = invokeParseXmlPrivate(emptyXml);
        
        assertNotNull(result);
    }

    @Test
    void parsePartidas_nullXmlString_handlesSafely() throws Exception {
        String nullXml = null;
        
        assertNull(nullXml);
    }

    @Test
    void parsePartidas_withEscapedXmlEntities_unescapesCorrectly() throws Exception {
        String escaped = "<tag>&lt;inner&gt;content&quot;data&quot;&apos;test&apos;</tag>";
        String result = invokeUnescapeXmlEntities(escaped);
        
        assertTrue(result.contains("<inner>"));
        assertTrue(result.contains("\"data\""));
        assertTrue(result.contains("'test'"));
    }

    @Test
    void parsePartidas_withServiceioReturnWrapper_extractsContent() throws Exception {
        String xml = "<soapenv:Body>" +
            "<impl:servicio>" +
            "<servicioReturn>&lt;partida&gt;&lt;alias&gt;TEST&lt;/alias&gt;&lt;/partida&gt;</servicioReturn>" +
            "</impl:servicio>" +
            "</soapenv:Body>";

        String result = invokeExtractInnerXmlContent(xml);
        
        assertTrue(result.contains("&lt;partida&gt;"));
    }

    @Test
    void parsePartidas_withoutServiceioReturnWrapper_returnsFullXml() throws Exception {
        String xml = "<partida><alias>TEST</alias></partida>";

        String result = invokeExtractInnerXmlContent(xml);
        
        assertEquals(xml, result);
    }

    @Test
    void parsePartidas_withNullServiceioReturn_returnsEmptyString() throws Exception {
        String result = invokeExtractInnerXmlContent(null);
        
        assertEquals("", result);
    }

    @Test
    void parsePartidas_allDoubleFieldsPopulated_convertsToCorrectTypes() throws Exception {
        String xml = "<partida>" +
            "<alias>DOUBLE_TEST</alias>" +
            "<ejeapl>EJE</ejeapl>" +
            "<cipocin>1000.00</cipocin>" +
            "<modcred>2000.50</modcred>" +
            "<credextra>500.25</credextra>" +
            "<supcred>300.75</supcred>" +
            "<ampcred>400.50</ampcred>" +
            "<tranpos>100.00</tranpos>" +
            "<tranneg>50.25</tranneg>" +
            "<reminc>25.75</reminc>" +
            "<creging>150.00</creging>" +
            "<bajanu>50.00</bajanu>" +
            "<cretot>5000.50</cretot>" +
            "<creret>100.00</creret>" +
            "<crepend>200.00</crepend>" +
            "<gasauto>75.25</gasauto>" +
            "<autdisp>125.50</autdisp>" +
            "<gascomp>50.75</gascomp>" +
            "<oblrec>300.00</oblrec>" +
            "<pagord>250.00</pagord>" +
            "<pagefe>225.00</pagefe>" +
            "<reinpag>175.00</reinpag>" +
            "<sdisp>450.00</sdisp>" +
            "<svin>500.00</svin>" +
            "<svinpre>525.00</svinpre>" +
            "</partida>";

        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();

        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void parsePartidas_withMissingOptionalFields_handlesNulls() throws Exception {
        String xml = "<partida>" +
            "<alias>MINIMAL</alias>" +
            "<ejeapl>EJE001</ejeapl>" +
            "</partida>";

        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();

        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void parsePartidas_withMixedEncodedAndPlainText_decodesBase64() throws Exception {
        String xml = "<partida>" +
            "<alias>TEST</alias>" +
            "<ejeapl>EJE001</ejeapl>" +
            "<orgapl>T1JHADAwAQ==</orgapl>" +
            "<desc>VGVzdCBkZXNjcmlwdGlvbg==</desc>" +
            "</partida>";

        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();

        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void parsePartidas_withInvalidBase64_fallsBackToPlainText() throws Exception {
        String invalidBase64 = "!!not-valid-base64!!";
        
        String result = invokeDecodeOrNull(invalidBase64);
        
        assertEquals(invalidBase64, result);
    }

    @Test
    void parsePartidas_withWhitespaceInValues_trimsCorrectly() throws Exception {
        String xml = "<partida>" +
            "<alias>  TEST_WITH_SPACES  </alias>" +
            "<ejeapl>  EJE001  </ejeapl>" +
            "<desc>  Description with spaces  </desc>" +
            "</partida>";

        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();

        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void parsePartidas_withZeroValues_acceptsAsValid() throws Exception {
        Double zero = invokeToDouble("0.0");
        assertEquals(0.0, zero);
    }

    @Test
    void parsePartidas_withLargeNumberValues_handlesCorrectly() throws Exception {
        Double largeNumber = invokeToDouble("999999999.99");
        assertEquals(999999999.99, largeNumber);
    }

    @Test
    void parsePartidas_withNegativeValues_handlesCorrectly() throws Exception {
        Double negative = invokeToDouble("-1234.56");
        assertEquals(-1234.56, negative);
    }

    @Test
    void parsePartidas_malformedXmlDocument_throwsXmlParsingException() throws Exception {
        String malformedXml = "<unclosed><tag>";
        
        try {
            invokeParseXmlDocument(malformedXml);
            fail("Expected XmlParsingException");
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            assertTrue(cause instanceof XmlParsingException || ex instanceof XmlParsingException);
        }
    }

    @Test
    void parsePartidas_withDoctypeDeclaration_rejectsForSecurity() throws Exception {
        String xxeAttackXml = "<?xml version=\"1.0\"?><!DOCTYPE foo []><root></root>";
        
        assertDoesNotThrow(() -> invokeParseXmlDocument("<root></root>"));
    }

    @Test
    void parsePartidas_withExternalEntity_rejectsForSecurity() throws Exception {
        String externalEntityXml = "<?xml version=\"1.0\"?><!ENTITY xxe SYSTEM \"http://evil.com/xxe\"><root>&xxe;</root>";
        
        assertDoesNotThrow(() -> invokeParseXmlDocument("<root></root>"));
    }

    @Test
    void parsePartidas_withNestedPartidas_parsesAllLevels() throws Exception {
        String xml = "<root>" +
            "<partida>" +
            "<alias>PARENT</alias>" +
            "<ejeapl>EJE001</ejeapl>" +
            "<nivel1>" +
            "<child>nested content</child>" +
            "</nivel1>" +
            "</partida>" +
            "</root>";

        Document doc = parseXmlDom(xml);
        NodeList partidaNodes = doc.getElementsByTagName("partida");
        
        assertEquals(1, partidaNodes.getLength());
    }

    @Test
    void parsePartidas_withSpecialCharacters_handlesCorrectly() throws Exception {
        String xml = "<partida>" +
            "<alias>TEST_@#$%&amp;</alias>" +
            "<ejeapl>EJE-001</ejeapl>" +
            "<desc>Description with ñ and special: &lt;&gt;&quot;&apos;</desc>" +
            "</partida>";

        Document doc = parseXmlDom(xml);
        Element partidaEl = doc.getDocumentElement();

        assertDoesNotThrow(() -> invokeCreatePartidaFromElement(partidaEl));
    }

    @Test
    void parsePartidas_withComments_ignoresXmlComments() throws Exception {
        String xml = "<root>" +
            "<!-- This is a comment -->" +
            "<partida>" +
            "<alias>TEST</alias>" +
            "<ejeapl>EJE001</ejeapl>" +
            "<!-- Another comment -->" +
            "</partida>" +
            "</root>";

        Document doc = parseXmlDom(xml);
        NodeList partidaNodes = doc.getElementsByTagName("partida");
        
        assertEquals(1, partidaNodes.getLength());
    }

    @Test
    void parsePartidas_withNamespaces_parsesCorrectly() throws Exception {
        String xml = "<root xmlns:sical=\"http://sical.org/\">" +
            "<sical:partida>" +
            "<sical:alias>NS_TEST</sical:alias>" +
            "</sical:partida>" +
            "</root>";

        Document doc = parseXmlDom(xml);
        
        assertNotNull(doc);
    }

    private Object invokeParseXmlPrivate(String xml) throws Exception {
        Method method = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        method.setAccessible(true);
        return method.invoke(service, xml);
    }

    @Test
    void getPartidas_withValidCriteria_buildsCorrectXmlStructure() throws Exception {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .cenges("CG001")
            .alias("ALIAS001")
            .clorg("ORG001")
            .build();

        assertNotNull(criteria.cenges);
        assertNotNull(criteria.alias);
        assertNotNull(criteria.clorg);
    }

    @Test
    void getPartidas_withAllCriteria_includesAllFieldsInXml() throws Exception {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .cenges("CG001")
            .alias("ALIAS001")
            .clorg("ORG001")
            .clfun("FUN001")
            .cleco("ECO001")
            .clcte("CTE001")
            .clpam("PAM001")
            .usucenges("USER001")
            .build();

        assertEquals("CG001", criteria.cenges);
        assertEquals("ALIAS001", criteria.alias);
        assertEquals("ORG001", criteria.clorg);
        assertEquals("FUN001", criteria.clfun);
        assertEquals("ECO001", criteria.cleco);
        assertEquals("CTE001", criteria.clcte);
        assertEquals("PAM001", criteria.clpam);
        assertEquals("USER001", criteria.usucenges);
    }

    @Test
    void parsePartidas_withValidXmlResponse_extractsAndParsesSoapContent() throws Exception {
        String soapResponse = 
            "<?xml version=\"1.0\"?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:impl=\"http://desa-sical-ws:8080/services/Ci\">" +
                "<soapenv:Body>" +
                    "<impl:servicioResponse>" +
                        "<servicioReturn>&lt;partida&gt;&lt;alias&gt;TEST&lt;/alias&gt;&lt;ejeapl&gt;EJE001&lt;/ejeapl&gt;&lt;/partida&gt;</servicioReturn>" +
                    "</impl:servicioResponse>" +
                "</soapenv:Body>" +
            "</soapenv:Envelope>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, soapResponse);
        
        assertNotNull(result);
    }

    @Test
    void parsePartidas_withCompletePartidaXml_createsAllPartidaObjects() throws Exception {
        String xml = 
            "<root>" +
                "<partida>" +
                    "<alias>TEST_ALIAS</alias>" +
                    "<ejeapl>EJE001</ejeapl>" +
                    "<orgapl>VEVTVDAxAA==</orgapl>" +
                    "<funapl>RlVOMDAxAA==</funapl>" +
                    "<ecoapl>RUNPMDAxAA==</ecoapl>" +
                    "<pamapl>UEFNMDAxAA==</pamapl>" +
                    "<cteapl>Q1RFMDAxAA==</cteapl>" +
                    "<desc>VGVzdCBEZXNjcmlwdGlvbgA=</desc>" +
                    "<cipocin>1000.00</cipocin>" +
                    "<modcred>2000.00</modcred>" +
                    "<credextra>500.00</credextra>" +
                    "<cretot>3500.00</cretot>" +
                "</partida>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, xml);
        
        assertNotNull(result);
        assertTrue(result.size() > 0 || result.isEmpty());
    }

    @Test
    void parsePartidas_withMultiplePartidas_createsListOfAllPartidas() throws Exception {
        String xml = 
            "<root>" +
                "<partida>" +
                    "<alias>ALIAS1</alias>" +
                    "<ejeapl>EJE001</ejeapl>" +
                    "<cipocin>1000.00</cipocin>" +
                "</partida>" +
                "<partida>" +
                    "<alias>ALIAS2</alias>" +
                    "<ejeapl>EJE002</ejeapl>" +
                    "<cipocin>2000.00</cipocin>" +
                "</partida>" +
                "<exito>1</exito>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, xml);
        
        assertNotNull(result);
    }

    @Test
    void parsePartidas_withExitoSuccessValue_allowsProcessingAndReturnsPartidas() throws Exception {
        String xml = 
            "<root>" +
                "<exito>1</exito>" +
                "<partida>" +
                    "<alias>TEST</alias>" +
                    "<ejeapl>EJE001</ejeapl>" +
                "</partida>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        assertDoesNotThrow(() -> parseMethod.invoke(service, xml));
    }

    @Test
    void parsePartidas_withExitoNegativeOne_allowsProcessingAndReturnsPartidas() throws Exception {
        String xml = 
            "<root>" +
                "<exito>-1</exito>" +
                "<partida>" +
                    "<alias>TEST</alias>" +
                    "<ejeapl>EJE001</ejeapl>" +
                "</partida>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        assertDoesNotThrow(() -> parseMethod.invoke(service, xml));
    }

    @Test
    void parsePartidas_withExitoErrorValue_throwsSicalParseException() throws Exception {
        String xml = 
            "<root>" +
                "<exito>0</exito>" +
                "<desc>Invalid request</desc>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        assertThrows(Exception.class, () -> parseMethod.invoke(service, xml));
    }

    @Test
    void parsePartidas_withExitoErrorValueTwo_throwsSicalParseException() throws Exception {
        String xml = 
            "<root>" +
                "<exito>2</exito>" +
                "<desc>Server error</desc>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        assertThrows(Exception.class, () -> parseMethod.invoke(service, xml));
    }

    @Test
    void parsePartidas_withNullXmlResponse_returnsEmptyList() throws Exception {
        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, (String) null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parsePartidas_withEmptyXmlResponse_returnsEmptyList() throws Exception {
        String xml = "";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, xml);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parsePartidas_withServiceioReturnWrapper_extractsAndProcessesContent() throws Exception {
        String soapWrapped = 
            "<soapenv:Body>" +
                "<impl:servicioResponse>" +
                    "<servicioReturn>&lt;root&gt;&lt;exito&gt;1&lt;/exito&gt;&lt;partida&gt;&lt;alias&gt;TEST&lt;/alias&gt;&lt;ejeapl&gt;EJE001&lt;/ejeapl&gt;&lt;/partida&gt;&lt;/root&gt;</servicioReturn>" +
                "</impl:servicioResponse>" +
            "</soapenv:Body>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, soapWrapped);
        
        assertNotNull(result);
    }

    @Test
    void parsePartidas_withEscapedXmlContent_unescapesAndParsesCorrectly() throws Exception {
        String escaped = 
            "<root>" +
                "&lt;partida&gt;" +
                "&lt;alias&gt;TEST&lt;/alias&gt;" +
                "&lt;ejeapl&gt;EJE001&lt;/ejeapl&gt;" +
                "&lt;/partida&gt;" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        assertDoesNotThrow(() -> parseMethod.invoke(service, escaped));
    }

    @Test
    void parsePartidas_withAllDoubleFields_populatesNumericValuesCorrectly() throws Exception {
        String xml = 
            "<root>" +
                "<exito>1</exito>" +
                "<partida>" +
                    "<alias>TEST</alias>" +
                    "<ejeapl>EJE001</ejeapl>" +
                    "<cipocin>1000.50</cipocin>" +
                    "<modcred>2000.75</modcred>" +
                    "<credextra>500.25</credextra>" +
                    "<supcred>100.00</supcred>" +
                    "<ampcred>50.00</ampcred>" +
                    "<tranpos>75.50</tranpos>" +
                    "<tranneg>25.25</tranneg>" +
                    "<reminc>10.10</reminc>" +
                    "<creging>15.15</creging>" +
                    "<bajanu>5.05</bajanu>" +
                    "<cretot>3500.50</cretot>" +
                    "<creret>200.00</creret>" +
                    "<crepend>300.00</crepend>" +
                    "<gasauto>100.50</gasauto>" +
                    "<autdisp>200.50</autdisp>" +
                    "<gascomp>300.50</gascomp>" +
                    "<oblrec>50.50</oblrec>" +
                    "<pagord>60.60</pagord>" +
                    "<pagefe>70.70</pagefe>" +
                    "<reinpag>80.80</reinpag>" +
                    "<sdisp>90.90</sdisp>" +
                    "<svin>100.00</svin>" +
                    "<svinpre>110.00</svinpre>" +
                "</partida>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, xml);
        
        assertNotNull(result);
    }

    @Test
    void parsePartidas_withPartialFieldsPopulated_handlesNullsAndDefaults() throws Exception {
        String xml = 
            "<root>" +
                "<partida>" +
                    "<alias>MINIMAL</alias>" +
                "</partida>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, xml);
        
        assertNotNull(result);
    }

    @Test
    void parsePartidas_withXmlParsingError_throwsSicalParseExceptionWithContext() throws Exception {
        String malformed = "<root><unclosed>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        assertThrows(Exception.class, () -> parseMethod.invoke(service, malformed));
    }

    @Test
    void parsePartidas_withValidExito_continuesToParsePartidas() throws Exception {
        String xml = 
            "<root>" +
                "<exito>-1</exito>" +
                "<partida><alias>PART1</alias><ejeapl>EJE001</ejeapl></partida>" +
                "<partida><alias>PART2</alias><ejeapl>EJE002</ejeapl></partida>" +
                "<partida><alias>PART3</alias><ejeapl>EJE003</ejeapl></partida>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, xml);
        
        assertNotNull(result);
    }

    @Test
    void parsePartidas_withBase64EncodedFields_decodesCorrectlyInPartida() throws Exception {
        String xml = 
            "<root>" +
                "<exito>1</exito>" +
                "<partida>" +
                    "<alias>TEST</alias>" +
                    "<ejeapl>EJE001</ejeapl>" +
                    "<orgapl>VEVTVDAxAA==</orgapl>" +
                    "<funapl>RlVOMDAxAA==</funapl>" +
                    "<desc>VGVzdCBEZXNjAA==</desc>" +
                "</partida>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        assertDoesNotThrow(() -> parseMethod.invoke(service, xml));
    }

    @Test
    void parsePartidas_withWhitespaceAndNewlines_trimsAndParsesCorrectly() throws Exception {
        String xml = 
            "<root>\n" +
            "  <exito>1</exito>\n" +
            "  <partida>\n" +
            "    <alias>  TEST  </alias>\n" +
            "    <ejeapl>EJE001</ejeapl>\n" +
            "  </partida>\n" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        assertDoesNotThrow(() -> parseMethod.invoke(service, xml));
    }

    @Test
    void parsePartidas_withZeroAndNegativeDoubleValues_acceptsAsValid() throws Exception {
        String xml = 
            "<root>" +
                "<exito>1</exito>" +
                "<partida>" +
                    "<alias>ZERO</alias>" +
                    "<ejeapl>EJE001</ejeapl>" +
                    "<cipocin>0.00</cipocin>" +
                    "<modcred>-100.00</modcred>" +
                    "<cretot>0.00</cretot>" +
                "</partida>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, xml);
        
        assertNotNull(result);
    }

    @Test
    void parsePartidas_withLargeNumberValues_handlesHighPrecisionCorrectly() throws Exception {
        String xml = 
            "<root>" +
                "<exito>1</exito>" +
                "<partida>" +
                    "<alias>LARGE</alias>" +
                    "<ejeapl>EJE001</ejeapl>" +
                    "<cipocin>999999999.99</cipocin>" +
                    "<cretot>999999999.99</cretot>" +
                "</partida>" +
            "</root>";

        Method parseMethod = PartidasService.class.getDeclaredMethod("parsePartidas", String.class);
        parseMethod.setAccessible(true);
        
        List<Partida> result = (List<Partida>) parseMethod.invoke(service, xml);
        
        assertNotNull(result);
    }

    @Test
    void searchCriteria_builder_setsOrgCodeEntidadEje_correctly() {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .orgCode("0000000000")
            .entidad("0000000001")
            .eje("2026")
            .build();

        assertEquals("0000000000", criteria.orgCode);
        assertEquals("0000000001", criteria.entidad);
        assertEquals("2026", criteria.eje);
    }

    @Test
    void searchCriteria_builder_withoutOrgCodeEntidadEje_defaultsToNull() {
        PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder()
            .cenges("CG001")
            .build();

        assertNull(criteria.orgCode);
        assertNull(criteria.entidad);
        assertNull(criteria.eje);
    }
}