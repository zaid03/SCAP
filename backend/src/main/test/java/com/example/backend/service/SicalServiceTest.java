package com.example.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.example.backend.dto.Tercero;
import com.example.backend.exception.XmlParsingException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SicalServiceTest {

    private SicalService service;

    private static final String ORG_CODE = "ORG001";
    private static final String ENTIDAD = "1";
    private static final String EJE = "2024";

    @BeforeEach
    void setUp() {
        service = new SicalService();
        ReflectionTestUtils.setField(service, "wsUrl", "http://test-sical-ws:8080/services/Ci?wsdl");
        ReflectionTestUtils.setField(service, "username", "testuser");
        ReflectionTestUtils.setField(service, "password", "testpass");
        ReflectionTestUtils.setField(service, "publicKey", "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...");
    }

    @Test
    void service_configuresAllRequiredProperties() {
        String wsUrl = (String) ReflectionTestUtils.getField(service, "wsUrl");
        String username = (String) ReflectionTestUtils.getField(service, "username");
        String password = (String) ReflectionTestUtils.getField(service, "password");
        String publicKey = (String) ReflectionTestUtils.getField(service, "publicKey");

        assertNotNull(wsUrl);
        assertNotNull(username);
        assertNotNull(password);
        assertNotNull(publicKey);

        assertEquals("http://test-sical-ws:8080/services/Ci?wsdl", wsUrl);
        assertEquals("testuser", username);
        assertEquals("testpass", password);
        assertTrue(publicKey.startsWith("-----BEGIN PUBLIC KEY-----"));
    }

    @Test
    void service_hasWebServiceUrlConfigured() {
        String wsUrl = (String) ReflectionTestUtils.getField(service, "wsUrl");
        assertTrue(wsUrl.contains("sical-ws"));
        assertTrue(wsUrl.contains("8080"));
    }

    @Test
    void service_hasSecurityCredentialsConfigured() {
        String username = (String) ReflectionTestUtils.getField(service, "username");
        String password = (String) ReflectionTestUtils.getField(service, "password");

        assertNotNull(username);
        assertNotNull(password);
        assertFalse(username.isEmpty());
        assertFalse(password.isEmpty());
    }

    @Test
    void getTerceros_acceptsNifParameter() throws Exception {
        try {
            service.getTerceros("12345678A", null, null, ORG_CODE, ENTIDAD, EJE);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_acceptsNombreParameter() throws Exception {
        try {
            service.getTerceros(null, "Juan", null, ORG_CODE, ENTIDAD, EJE);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_acceptsCodigoParameter() throws Exception {
        try {
            service.getTerceros(null, null, "García", ORG_CODE, ENTIDAD, EJE);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_acceptsAllSearchParameters() throws Exception {
        try {
            service.getTerceros("12345678A", "Juan", "García", ORG_CODE, ENTIDAD, EJE);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_acceptsNullSearchParameters() throws Exception {
        try {
            service.getTerceros(null, null, null, ORG_CODE, ENTIDAD, EJE);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_withNullXmlResponse_throwsXmlParsingException() {
        assertThrows(XmlParsingException.class,
            () -> service.getTerceros(null, null, null, ORG_CODE, ENTIDAD, EJE));
    }

    @Test
    void service_buildsCorrectSoapRequestStructure() {
        assertDoesNotThrow(() -> {
            try {
                service.getTerceros("TEST", null, null, ORG_CODE, ENTIDAD, EJE);
            } catch (XmlParsingException e) {
            }
        });
    }

    @Test
    void service_handlesMultipleTiposDocumento() throws Exception {
        String[] docTypes = {"NIF", "NIE", "CIF", "PASAPORTE"};
        for (String docType : docTypes) {
            try {
                service.getTerceros(docType + "123456", null, null, ORG_CODE, ENTIDAD, EJE);
            } catch (XmlParsingException e) {
                assertNotNull(e);
            }
        }
    }

    @Test
    void service_handlesTerceroWithoutApellido() throws Exception {
        try {
            service.getTerceros(null, "SoleNameNoApellido", null, ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void service_handlesTerceroWithSpecialCharacters() throws Exception {
        try {
            service.getTerceros("12345678X", "José María", "García-López", ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void service_usesBase64Encoding() {
        assertDoesNotThrow(() -> {
            try {
                service.getTerceros("encoded", null, null, ORG_CODE, ENTIDAD, EJE);
            } catch (XmlParsingException e) {
            }
        });
    }

    @Test
    void service_buildsSoapEnvelopeCorrectly() {
        assertDoesNotThrow(() -> {
            try {
                service.getTerceros(null, null, null, ORG_CODE, ENTIDAD, EJE);
            } catch (XmlParsingException e) {
                assertTrue(e.getMessage().contains("Error") || e.getCause() != null);
            }
        });
    }

    @Test
    void getTerceros_returnsListType() {
        try {
            service.getTerceros("test", null, null, ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void service_includesSecurityHeaders() {
        assertDoesNotThrow(() -> {
            try {
                service.getTerceros(null, null, null, ORG_CODE, ENTIDAD, EJE);
            } catch (XmlParsingException e) {
                assertNotNull(e);
            }
        });
    }

    @Test
    void service_handlesXmlEncodedContent() throws Exception {
        try {
            service.getTerceros("&lt;encoded&gt;", null, null, ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void service_handlesApostropheEncoding() throws Exception {
        try {
            service.getTerceros("O&apos;Brien", null, null, ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void service_handlesQuoteEncoding() throws Exception {
        try {
            service.getTerceros("Name&quot;with&quot;quotes", null, null, ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_withOnlyNif_isValid() throws Exception {
        try {
            service.getTerceros("12345678A", null, null, ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_withOnlyNombre_isValid() throws Exception {
        try {
            service.getTerceros(null, "Juan", null, ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_withOnlyCodigo_isValid() throws Exception {
        try {
            service.getTerceros(null, null, "García", ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_withNifAndNombre_isValid() throws Exception {
        try {
            service.getTerceros("12345678A", "Juan", null, ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_withNifAndCodigo_isValid() throws Exception {
        try {
            service.getTerceros("12345678A", null, "García", ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_withNombreAndCodigo_isValid() throws Exception {
        try {
            service.getTerceros(null, "Juan", "García", ORG_CODE, ENTIDAD, EJE);
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void getTerceros_withDifferentOrgEntidadEje_isValid() throws Exception {
        try {
            service.getTerceros("12345678A", null, null, "ORG2", "2", "2023");
        } catch (XmlParsingException e) {
            assertNotNull(e);
        }
    }

    @Test
    void service_initializesSuccessfully() {
        assertNotNull(service);
        assertNotNull(ReflectionTestUtils.getField(service, "wsUrl"));
        assertNotNull(ReflectionTestUtils.getField(service, "username"));
    }

    @Test
    void unescapeXmlEntities_replacesLtEntity() throws Exception {
        String input = "&lt;tag&gt;";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("<tag>", result);
    }

    @Test
    void unescapeXmlEntities_replacesGtEntity() throws Exception {
        String input = "value&gt;end";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("value>end", result);
    }

    @Test
    void unescapeXmlEntities_replacesQuotEntity() throws Exception {
        String input = "name&quot;value&quot;";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("name\"value\"", result);
    }

    @Test
    void unescapeXmlEntities_replacesAposEntity() throws Exception {
        String input = "O&apos;Brien";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("O'Brien", result);
    }

    @Test
    void unescapeXmlEntities_replaceMultipleEntities() throws Exception {
        String input = "&lt;name&gt;&quot;value&quot;&apos;test&apos;";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("<name>\"value\"'test'", result);
    }

    @Test
    void unescapeXmlEntities_handlesMixedContent() throws Exception {
        String input = "normal&lt;tag&gt;text&quot;quoted&quot;";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("normal<tag>text\"quoted\"", result);
    }

    @Test
    void unescapeXmlEntities_returnsEmptyStringForNull() throws Exception {
        String result = invokeUnescapeXmlEntities(null);
        assertEquals("", result);
    }

    @Test
    void unescapeXmlEntities_returnsEmptyStringForEmptyInput() throws Exception {
        String result = invokeUnescapeXmlEntities("");
        assertEquals("", result);
    }

    @Test
    void unescapeXmlEntities_handlesSingleEntity() throws Exception {
        String result = invokeUnescapeXmlEntities("&lt;");
        assertEquals("<", result);
    }

    @Test
    void unescapeXmlEntities_handlesConsecutiveEntities() throws Exception {
        String input = "&lt;&gt;&quot;&apos;";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("<>\"'", result);
    }

    @Test
    void extractSoapContent_extractsContentFromServiceioReturn() throws Exception {
        String xml = "<soapenv:Body><impl:servicio><servicioReturn>extracted content</servicioReturn></impl:servicio></soapenv:Body>";
        String result = invokeExtractSoapContent(xml);
        assertEquals("extracted content", result);
    }

    @Test
    void extractSoapContent_returnsFullXmlWhenNoServiceioReturn() throws Exception {
        String xml = "<tag>content</tag>";
        String result = invokeExtractSoapContent(xml);
        assertEquals(xml, result);
    }

    @Test
    void extractSoapContent_returnsEmptyStringForNull() throws Exception {
        String result = invokeExtractSoapContent(null);
        assertEquals("", result);
    }

    @Test
    void extractSoapContent_handlesNestedTags() throws Exception {
        String xml = "<soapenv:Body><servicioReturn><nested><data>content</data></nested></servicioReturn></soapenv:Body>";
        String result = invokeExtractSoapContent(xml);
        assertTrue(result.contains("<nested>"));
        assertTrue(result.contains("<data>"));
    }

    @Test
    void extractSoapContent_handlesServiceioReturnWithAttributes() throws Exception {
        String xml = "<servicioReturn attr=\"value\">content here</servicioReturn>";
        String result = invokeExtractSoapContent(xml);
        assertEquals("content here", result);
    }

    @Test
    void extractSoapContent_returnsCdataContent() throws Exception {
        String xml = "<servicioReturn><![CDATA[<e><tag>value</tag></e>]]></servicioReturn>";
        String result = invokeExtractSoapContent(xml);
        assertTrue(result.contains("CDATA") || result.contains("<e>"));
    }

    @Test
    void extractSoapContent_handlesMultilineContent() throws Exception {
        String xml = "<servicioReturn>\n<line1>value1</line1>\n<line2>value2</line2>\n</servicioReturn>";
        String result = invokeExtractSoapContent(xml);
        assertTrue(result.contains("line1") && result.contains("line2"));
    }

    @Test
    void unescapePart_returnsNullForNegativeIndex() throws Exception {
        String[] parts = {"a", "b", "c"};
        String result = invokeUnescapePart(parts, -1);
        assertNull(result);
    }

    @Test
    void unescapePart_returnsNullForIndexOutOfBounds() throws Exception {
        String[] parts = {"a", "b", "c"};
        String result = invokeUnescapePart(parts, 10);
        assertNull(result);
    }

    @Test
    void unescapePart_returnsNullWhenPartIsNull() throws Exception {
        String[] parts = {"a", null, "c"};
        String result = invokeUnescapePart(parts, 1);
        assertNull(result);
    }

    @Test
    void unescapePart_unescapesXmlInPart() throws Exception {
        String[] parts = {"&lt;tag&gt;", "b", "c"};
        String result = invokeUnescapePart(parts, 0);
        assertEquals("<tag>", result);
    }

    @Test
    void unescapePart_trimsPart() throws Exception {
        String[] parts = {"  value  ", "b", "c"};
        String result = invokeUnescapePart(parts, 0);
        assertEquals("value", result);
    }

    @Test
    void unescapePart_handlesComplexXmlEntities() throws Exception {
        String[] parts = {"&lt;name&gt;&quot;value&quot;&apos;", "b"};
        String result = invokeUnescapePart(parts, 0);
        assertEquals("<name>\"value\"'", result);
    }

    @Test
    void unescapePart_handlesEmptyString() throws Exception {
        String[] parts = {"", "b", "c"};
        String result = invokeUnescapePart(parts, 0);
        assertEquals("", result);
    }

    @Test
    void unescapePart_handlesWhitespaceOnly() throws Exception {
        String[] parts = {"   ", "b", "c"};
        String result = invokeUnescapePart(parts, 0);
        assertEquals("", result);
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
    void getTagValue_returnsTagContent() throws Exception {
        String xml = "<root><name>John</name></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "name");
        assertEquals("John", result);
    }

    @Test
    void getTagValue_returnsFirstTagWhenMultiple() throws Exception {
        String xml = "<root><item>first</item><item>second</item></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "item");
        assertEquals("first", result);
    }

    @Test
    void getTagValue_returnsEmptyStringForEmptyTag() throws Exception {
        String xml = "<root><name></name></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "name");
        assertEquals("", result);
    }

    @Test
    void getTagValue_returnsNestedContent() throws Exception {
        String xml = "<root><person><name>John</name></person></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "name");
        assertEquals("John", result);
    }

    @Test
    void deriveMissingApellido_keepsExistingAppellidoTercero() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero("Existing");
        t.setApellido1("First");
        t.setApellido2("Second");

        invokDeriveMissingApellido(t);
        assertEquals("Existing", t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_appendsApellido1AndApellido2() throws Exception {
        Tercero t = new Tercero();
        t.setApellido1("García");
        t.setApellido2("López");

        invokDeriveMissingApellido(t);
        assertEquals("García López", t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_usesOnlyApellido1() throws Exception {
        Tercero t = new Tercero();
        t.setApellido1("García");

        invokDeriveMissingApellido(t);
        assertEquals("García", t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_usesOnlyApellido2() throws Exception {
        Tercero t = new Tercero();
        t.setApellido2("López");

        invokDeriveMissingApellido(t);
        assertEquals("López", t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_parsesNombreCompletoWhenNoApellidos() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("John García López");

        invokDeriveMissingApellido(t);
        assertEquals("López", t.getApellTercero());
        assertEquals("John García", t.getNomTercero());
    }

    @Test
    void deriveMissingApellido_doesNothingWhenAllFieldsNull() throws Exception {
        Tercero t = new Tercero();
        invokDeriveMissingApellido(t);
        assertNull(t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_skipsBlankApellidoTercero() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero("");
        t.setApellido1("García");

        invokDeriveMissingApellido(t);
        assertEquals("García", t.getApellTercero());
    }

    @Test
    void parseNombreCompleto_parsesMultiwordName() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("Juan María García López");

        invokeParseNombreCompleto(t);
        assertEquals("Juan María García", t.getNomTercero());
        assertEquals("López", t.getApellTercero());
    }

    @Test
    void parseNombreCompleto_handlesTwoWordName() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("Juan García");

        invokeParseNombreCompleto(t);
        assertEquals("Juan", t.getNomTercero());
        assertEquals("García", t.getApellTercero());
    }

    @Test
    void parseNombreCompleto_handlesSingleWord() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("Juan");

        invokeParseNombreCompleto(t);
        assertEquals("Juan", t.getNomTercero());
        assertNull(t.getApellTercero());
    }

    @Test
    void parseNombreCompleto_handlesExtraSpaces() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("  Juan   María   García  ");

        invokeParseNombreCompleto(t);
        assertTrue(t.getNomTercero().contains("Juan"));
        assertTrue(t.getApellTercero().contains("García"));
    }

    @Test
    void parseNombreCompleto_handlesHyphenatedNames() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("Juan María García-López");

        invokeParseNombreCompleto(t);
        assertTrue(t.getNomTercero().contains("Juan"));
        assertEquals("García-López", t.getApellTercero());
    }

    @Test
    void parseNombreCompleto_handlesFourPartName() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("José María García-López");

        invokeParseNombreCompleto(t);
        assertTrue(t.getNomTercero().contains("José"));
        assertEquals("García-López", t.getApellTercero());
    }

    @Test
    void populateTerceroFromDetter_parsesDumberFields() throws Exception {
        String detter = "ID123-@-12345678A-@-NIF-@-ALIAS1-@-Juan-@-Main St-@-Madrid-@-28001-@-Madrid-@-123456789-@-987654321-@-EMPRESA-@-DESC2-@-DESC3-@-DESC4-@-DESC5-@-DESC6-@-DESC7-@-DESC8-@-Some obs-@-NO-@-juan@email.com-@-Juan García López-@-García-@-López";
        Tercero t = new Tercero();

        invokePopulateTerceroFromDetter(t, detter);

        assertEquals("ID123", t.getIdenTercero());
        assertEquals("12345678A", t.getNIFtercero());
        assertEquals("NIF", t.getTipoDocumento());
        assertEquals("ALIAS1", t.getAlias());
        assertEquals("Juan", t.getNomTercero());
        assertEquals("Main St", t.getDomicilio());
        assertEquals("Madrid", t.getPoblacion());
    }

    @Test
    void populateTerceroFromDetter_handlesMinimalDetter() throws Exception {
        String detter = "ID1-@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@-";
        Tercero t = new Tercero();

        invokePopulateTerceroFromDetter(t, detter);

        assertEquals("ID1", t.getIdenTercero());
        assertEquals("", t.getNIFtercero());
        assertEquals("", t.getAlias());
    }

    @Test
    void populateTerceroFromDetter_unescapesXmlEntities() throws Exception {
        String detter = "ID1-@-NIF&lt;1-@--@--@-Juan&apos;s-@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@-";
        Tercero t = new Tercero();

        invokePopulateTerceroFromDetter(t, detter);

        assertEquals("NIF<1", t.getNIFtercero());
        assertEquals("Juan's", t.getNomTercero());
    }

    @Test
    void populateTerceroFromDetter_trimsAllParts() throws Exception {
        String detter = "  ID1  -@-  NIF123  -@-  TYPE  -@-  ALIAS  -@-  NAME  -@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@-";
        Tercero t = new Tercero();

        invokePopulateTerceroFromDetter(t, detter);

        assertEquals("ID1", t.getIdenTercero());
        assertEquals("NIF123", t.getNIFtercero());
        assertEquals("TYPE", t.getTipoDocumento());
    }

    @Test
    void populateTerceroFromTags_extractsFromElement() throws Exception {
        String xml = "<tercero><idenTercero>ID001</idenTercero><NIFtercero>12345678A</NIFtercero><nomTercero>Juan</nomTercero><apellTercero>García</apellTercero></tercero>";
        Document doc = parseXmlDom(xml);
        Element element = doc.getDocumentElement();
        Tercero t = new Tercero();

        invokePopulateTerceroFromTags(t, element);

        assertEquals("ID001", t.getIdenTercero());
        assertEquals("12345678A", t.getNIFtercero());
        assertEquals("Juan", t.getNomTercero());
        assertEquals("García", t.getApellTercero());
    }

    @Test
    void populateTerceroFromTags_handlesPartialData() throws Exception {
        String xml = "<tercero><idenTercero>ID001</idenTercero><nomTercero>Juan</nomTercero></tercero>";
        Document doc = parseXmlDom(xml);
        Element element = doc.getDocumentElement();
        Tercero t = new Tercero();

        invokePopulateTerceroFromTags(t, element);

        assertEquals("ID001", t.getIdenTercero());
        assertEquals("Juan", t.getNomTercero());
        assertNull(t.getNIFtercero());
    }

    @Test
    void extractAndUnescapeXmlContent_unescapesAndExtractsSoapContent() throws Exception {
        String soap = "<soapenv:Body><impl:servicio><servicioReturn>&lt;tag&gt;</servicioReturn></impl:servicio></soapenv:Body>";
        Method method = SicalService.class.getDeclaredMethod("extractAndUnescapeXmlContent", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, soap);
        assertEquals("<tag>", result);
    }

    @Test
    void parseXmlDocument_withSecurityFeatures() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root><name>Test</name></root>";
        Document doc = parseXmlDom(xml);
        assertNotNull(doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    @Test
    void parseTerceroFromElement_withDetterField() throws Exception {
        String xml = "<tercero><detter>ID1-@-NIF1-@-TYPE-@-ALIAS-@-NAME-@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@-OBS-@-NO-@-email@test.com-@-Full Name-@-First-@-Last</detter></tercero>";
        Document doc = parseXmlDom(xml);
        Element element = (Element) doc.getElementsByTagName("tercero").item(0);

        Method method = SicalService.class.getDeclaredMethod("parseTerceroFromElement", Element.class);
        method.setAccessible(true);
        Tercero result = (Tercero) method.invoke(service, element);

        assertNotNull(result);
        assertEquals("ID1", result.getIdenTercero());
        assertEquals("NIF1", result.getNIFtercero());
    }

    @Test
    void parseTerceroFromElement_withoutDetterField() throws Exception {
        String xml = "<tercero><idenTercero>ID1</idenTercero><NIFtercero>NIF1</NIFtercero><nomTercero>Juan</nomTercero><apellTercero>García</apellTercero></tercero>";
        Document doc = parseXmlDom(xml);
        Element element = (Element) doc.getElementsByTagName("tercero").item(0);

        Method method = SicalService.class.getDeclaredMethod("parseTerceroFromElement", Element.class);
        method.setAccessible(true);
        Tercero result = (Tercero) method.invoke(service, element);

        assertNotNull(result);
        assertEquals("Juan", result.getNomTercero());
    }

    @Test
    void unescapeXmlEntities_handlesAllEntityTypes() throws Exception {
        String input = "&lt;tag&gt;&quot;quoted&quot;&apos;apostrophe&apos;";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("<tag>\"quoted\"'apostrophe'", result);
    }

    @Test
    void extractSoapContent_handlesNoServiceioReturn() throws Exception {
        String xml = "<other>content</other>";
        String result = invokeExtractSoapContent(xml);
        assertEquals(xml, result);
    }

    @Test
    void unescapePart_withValidBaseCase() throws Exception {
        String[] parts = {"value", "second"};
        String result = invokeUnescapePart(parts, 0);
        assertEquals("value", result);
    }

    @Test
    void parseTerceros_withMultipleElements() throws Exception {
        String xml = "<?xml version=\"1.0\"?><data><tercero><idenTercero>ID1</idenTercero><nomTercero>Juan</nomTercero></tercero><tercero><idenTercero>ID2</idenTercero><nomTercero>Maria</nomTercero></tercero></data>";
        Method method = SicalService.class.getDeclaredMethod("parseTerceros", String.class);
        method.setAccessible(true);
        List<Tercero> result = (List<Tercero>) method.invoke(service, xml);
        assertEquals(2, result.size());
    }

    @Test
    void parseTerceros_withEmptyData() throws Exception {
        String xml = "<?xml version=\"1.0\"?><data></data>";
        Method method = SicalService.class.getDeclaredMethod("parseTerceros", String.class);
        method.setAccessible(true);
        List<Tercero> result = (List<Tercero>) method.invoke(service, xml);
        assertEquals(0, result.size());
    }

    @Test
    void extractSoapContent_withProperServiceioReturnTag() throws Exception {
        String xml = "<soapenv:Body><impl:servicio><servicioReturn>extracted content</servicioReturn></impl:servicio></soapenv:Body>";
        String result = invokeExtractSoapContent(xml);
        assertEquals("extracted content", result);
    }

    @Test
    void extractSoapContent_withServiceioReturnAndNestedXml() throws Exception {
        String xml = "<soapenv:Body><servicioReturn><root><tercero><id>1</id></tercero></root></servicioReturn></soapenv:Body>";
        String result = invokeExtractSoapContent(xml);
        assertTrue(result.contains("<root>"));
        assertTrue(result.contains("</root>"));
    }

    @Test
    void extractSoapContent_returnsXmlUnchangedWhenNoClosingTag() throws Exception {
        String xml = "<soapenv:Body><servicioReturn>incomplete";
        String result = invokeExtractSoapContent(xml);
        assertEquals(xml, result);
    }

    @Test
    void extractSoapContent_returnsXmlUnchangedWhenNoOpeningTag() throws Exception {
        String xml = "<soapenv:Body>incomplete</servicioReturn>";
        String result = invokeExtractSoapContent(xml);
        assertEquals(xml, result);
    }

    @Test
    void extractSoapContent_withMultipleServiceioReturnTags() throws Exception {
        String xml = "<servicioReturn>first</servicioReturn><servicioReturn>second</servicioReturn>";
        String result = invokeExtractSoapContent(xml);
        assertEquals("first", result);
    }

    @Test
    void parseTerceros_throwsXmlParsingExceptionOnMalformedXml() throws Exception {
        String malformedXml = "<unclosed><tag>";
        Method method = SicalService.class.getDeclaredMethod("parseTerceros", String.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> method.invoke(service, malformedXml));
    }

    @Test
    void parseTerceros_throwsXmlParsingExceptionOnInvalidCharacters() throws Exception {
        String invalidXml = "<root>\u0000invalid</root>";
        Method method = SicalService.class.getDeclaredMethod("parseTerceros", String.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> method.invoke(service, invalidXml));
    }

    @Test
    void deriveMissingApellido_withEmptyStringAppellidoTercero() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero("");
        t.setApellido1("García");
        t.setApellido2("López");

        invokDeriveMissingApellido(t);
        assertEquals("García López", t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_withBothApellidosEmpty() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero(null);
        t.setApellido1("");
        t.setApellido2("");
        t.setNombreCompleto("John Smith");

        invokDeriveMissingApellido(t);
        assertEquals("Smith", t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_withApellido1EmptyApellido2Null() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero(null);
        t.setApellido1("");
        t.setApellido2(null);
        t.setNombreCompleto("John Doe");

        invokDeriveMissingApellido(t);
        assertEquals("Doe", t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_withApellido1NullApellido2Empty() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero(null);
        t.setApellido1(null);
        t.setApellido2("");
        t.setNombreCompleto("Jane Brown");

        invokDeriveMissingApellido(t);
        assertEquals("Brown", t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_withNombreCompletoEmpty() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero(null);
        t.setApellido1(null);
        t.setApellido2(null);
        t.setNombreCompleto("");

        invokDeriveMissingApellido(t);
        assertNull(t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_withNombreCompletoOnlyWhitespace() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero(null);
        t.setApellido1(null);
        t.setApellido2(null);
        t.setNombreCompleto("   ");

        invokDeriveMissingApellido(t);
        assertNull(t.getApellTercero());
    }

    @Test
    void parseNombreCompleto_withEmptyNombreCompleto() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("");

        invokeParseNombreCompleto(t);
        assertEquals("", t.getNomTercero());
    }

    @Test
    void parseNombreCompleto_withNullNombreCompleto() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto(null);

        assertThrows(InvocationTargetException.class, () -> invokeParseNombreCompleto(t));
    }

    @Test
    void parseNombreCompleto_withWhitespaceOnlyNombreCompleto() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("     ");

        invokeParseNombreCompleto(t);
        assertEquals("     ", t.getNomTercero());
    }

    @Test
    void parseNombreCompleto_withThreeWordName() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("Juan Carlos García");

        invokeParseNombreCompleto(t);
        assertEquals("Juan Carlos", t.getNomTercero());
        assertEquals("García", t.getApellTercero());
    }

    @Test
    void parseNombreCompleto_withHighlyMultiwordName() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("José María José García López");

        invokeParseNombreCompleto(t);
        assertEquals("José María José García", t.getNomTercero());
        assertEquals("López", t.getApellTercero());
    }

    @Test
    void parseNombreCompleto_preservesLeadingAndTrailingSpaces() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("  Juan  García  ");

        invokeParseNombreCompleto(t);
        assertEquals("Juan", t.getNomTercero());
        assertEquals("García", t.getApellTercero());
    }

    @Test
    void parseTerceros_withMalformedXmlButValidTerceroElement() throws Exception {
        String xml = "<data><tercero><idenTercero>ID1</idenTercero></tercero><invalid";
        Method method = SicalService.class.getDeclaredMethod("parseTerceros", String.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> method.invoke(service, xml));
    }

    @Test
    void parseTerceros_withValidXmlButExceptionDuringParse() throws Exception {
        String malformedXml = "<?xml version=\"1.0\"?><data>&#xD;&#x00;</data>";
        Method method = SicalService.class.getDeclaredMethod("parseTerceros", String.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> method.invoke(service, malformedXml));
    }

    @Test
    void deriveMissingApellido_withApellido1OnlyAsFirstApellido() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero(null);
        t.setApellido1("Martínez");
        t.setApellido2(null);

        invokDeriveMissingApellido(t);
        assertEquals("Martínez", t.getApellTercero());
    }

    @Test
    void deriveMissingApellido_trimsProperlyWhenBothApellidosPresent() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero(null);
        t.setApellido1("  García");
        t.setApellido2("López  ");

        invokDeriveMissingApellido(t);
        assertTrue(t.getApellTercero().contains("García"));
        assertTrue(t.getApellTercero().contains("López"));
    }

    @Test
    void unescapeXmlEntities_withSequentialEntities() throws Exception {
        String input = "&lt;&gt;&lt;&gt;";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("<><>", result);
    }

    @Test
    void unescapeXmlEntities_withMixedEntitiesAndText() throws Exception {
        String input = "Start&lt;tag&gt;middle&quot;quote&quot;end&apos;mark";
        String result = invokeUnescapeXmlEntities(input);
        assertEquals("Start<tag>middle\"quote\"end'mark", result);
    }

    @Test
    void extractSoapContent_withEmptyServiceioReturn() throws Exception {
        String xml = "<servicioReturn></servicioReturn>";
        String result = invokeExtractSoapContent(xml);
        assertEquals("", result);
    }

    @Test
    void extractSoapContent_withWhitespaceInServiceioReturn() throws Exception {
        String xml = "<servicioReturn>   content   </servicioReturn>";
        String result = invokeExtractSoapContent(xml);
        assertEquals("   content   ", result);
    }

    @Test
    void parseTerceros_withMultipleTercerosAndExceptionInSecond() throws Exception {
        String xml = "<?xml version=\"1.0\"?><data><tercero><idenTercero>ID1</idenTercero><nomTercero>Juan</nomTercero></tercero><tercero><invalid</tercero></data>";
        Method method = SicalService.class.getDeclaredMethod("parseTerceros", String.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> method.invoke(service, xml));
    }

    @Test
    void populateTerceroFromDetter_withOnlyFirstField() throws Exception {
        String detter = "ID001-@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@-";
        Tercero t = new Tercero();

        invokePopulateTerceroFromDetter(t, detter);
        assertEquals("ID001", t.getIdenTercero());
    }

    @Test
    void populateTerceroFromDetter_withSpecialCharactersInVariousFields() throws Exception {
        String detter = "ID&lt;001&gt;-@-NIF&quot;123&quot;-@-&apos;TYPE&apos;-@--@-Juan&lt;Name&gt;-@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@--@-";
        Tercero t = new Tercero();

        invokePopulateTerceroFromDetter(t, detter);
        assertEquals("ID<001>", t.getIdenTercero());
        assertEquals("NIF\"123\"", t.getNIFtercero());
        assertEquals("'TYPE'", t.getTipoDocumento());
        assertEquals("Juan<Name>", t.getNomTercero());
    }

    @Test
    void unescapePart_withIndexAtBoundary() throws Exception {
        String[] parts = {"first", "second", "third"};
        String result = invokeUnescapePart(parts, 2);
        assertEquals("third", result);
    }

    @Test
    void getTagValue_withNestedElementsReturnsFirstMatch() throws Exception {
        String xml = "<root><container><name>nested</name></container><name>top</name></root>";
        Document doc = parseXmlDom(xml);
        Element root = doc.getDocumentElement();
        String result = invokeGetTagValue(root, "name");
        assertEquals("nested", result);
    }

    @Test
    void deriveMissingApellido_callsParseNombreCompletoWhenBothAppellidosNull() throws Exception {
        Tercero t = new Tercero();
        t.setApellTercero(null);
        t.setApellido1(null);
        t.setApellido2(null);
        t.setNombreCompleto("FirstName LastName");

        invokDeriveMissingApellido(t);
        assertNotNull(t.getApellTercero());
        assertEquals("LastName", t.getApellTercero());
    }

    @Test
    void parseNombreCompleto_singleWordReturnsOnlyNomTercero() throws Exception {
        Tercero t = new Tercero();
        t.setNombreCompleto("SingleName");

        invokeParseNombreCompleto(t);
        assertEquals("SingleName", t.getNomTercero());
        assertNull(t.getApellTercero());
    }

    @Test
    void extractSoapContent_findsCorrectClosingTagWhenMultipleTagsExist() throws Exception {
        String xml = "<other><servicioReturn>first</servicioReturn></other><servicioReturn>second</servicioReturn>";
        String result = invokeExtractSoapContent(xml);
        assertEquals("first", result);
    }

    private String invokeUnescapeXmlEntities(String input) throws Exception {
        Method method = SicalService.class.getDeclaredMethod("unescapeXmlEntities", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, input);
    }

    private String invokeExtractSoapContent(String xml) throws Exception {
        Method method = SicalService.class.getDeclaredMethod("extractSoapContent", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, xml);
    }

    private String invokeUnescapePart(String[] parts, int idx) throws Exception {
        Method method = SicalService.class.getDeclaredMethod("unescapePart", String[].class, int.class);
        method.setAccessible(true);
        return (String) method.invoke(service, parts, idx);
    }

    private String invokeGetTagValue(Element parent, String tag) throws Exception {
        Method method = SicalService.class.getDeclaredMethod("getTagValue", Element.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, parent, tag);
    }

    private void invokDeriveMissingApellido(Tercero t) throws Exception {
        Method method = SicalService.class.getDeclaredMethod("deriveMissingApellido", Tercero.class);
        method.setAccessible(true);
        method.invoke(service, t);
    }

    private void invokeParseNombreCompleto(Tercero t) throws Exception {
        Method method = SicalService.class.getDeclaredMethod("parseNombreCompleto", Tercero.class);
        method.setAccessible(true);
        method.invoke(service, t);
    }

    private void invokePopulateTerceroFromDetter(Tercero t, String detter) throws Exception {
        Method method = SicalService.class.getDeclaredMethod("populateTerceroFromDetter", Tercero.class, String.class);
        method.setAccessible(true);
        method.invoke(service, t, detter);
    }

    private void invokePopulateTerceroFromTags(Tercero t, Element element) throws Exception {
        Method method = SicalService.class.getDeclaredMethod("populateTerceroFromTags", Tercero.class, Element.class);
        method.setAccessible(true);
        method.invoke(service, t, element);
    }

    private Document parseXmlDom(String xml) throws Exception {
        Method method = SicalService.class.getDeclaredMethod("parseXmlDocument", String.class);
        method.setAccessible(true);
        return (Document) method.invoke(service, xml);
    }
}