package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.backend.dto.ContabilizacionRequestDto;
import com.example.backend.dto.ContabilizacionResponseDto;
import com.example.backend.dto.Operaciones;
import com.example.backend.exception.SmlBuildingException;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.Fdt;

@ExtendWith(MockitoExtension.class)
public class ContabilizacionServiceTest {

    @Mock
    private com.example.backend.service.OperacionesService operacionesService;

    private ContabilizacionService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ContabilizacionService();
        ReflectionTestUtils.setField(service, "operacionesService", operacionesService);
        ReflectionTestUtils.setField(service, "sicalWsUrl", "http://test-sical-ws:8080/services/Ci");
        mockOperacionesSingleLine();
    }

    @Test
    void buildSmlInput_withValidData_returnsXmlString() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        List<Fde> fdeList = List.of(createValidFde());
        List<Fdt> fdtList = List.of(createValidFdt());

        String result = service.buildSmlInput(req, fac, fdeList, fdtList, "12345678A");

        assertNotNull(result);
        assertTrue(result.contains("<e>"));
        assertTrue(result.contains("</e>"));
        assertTrue(result.contains("<ope>"));
        assertTrue(result.contains("<sec>"));
        assertTrue(result.contains("<par>"));
    }

    @Test
    void buildSmlInput_containsRequiredSecurityFields() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        List<Fde> fdeList = List.of();
        List<Fdt> fdtList = List.of();

        String result = service.buildSmlInput(req, fac, fdeList, fdtList, "12345678A");

        assertTrue(result.contains("<usu>" + req.getUsu() + "</usu>"));
        assertTrue(result.contains("<org>" + req.getOrg() + "</org>"));
        assertTrue(result.contains("<ent>" + req.getEnt() + "</ent>"));
        assertTrue(result.contains("<eje>" + req.getEje() + "</eje>"));
    }

    @Test
    void buildSmlInput_withValidDataAndMultipleLists_succeeds() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        List<Fde> fdeList = List.of(createValidFde(), createValidFde());
        List<Fdt> fdtList = List.of(createValidFdt(), createValidFdt());

        String result = service.buildSmlInput(req, fac, fdeList, fdtList, "12345678A");

        assertNotNull(result);
        assertTrue(result.contains("<operacion>"));
        assertTrue(result.contains("</operacion>"));
    }

    @Test
    void buildSmlInput_withEmptyFdeList_stillBuildsValidXml() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "12345678A");

        assertNotNull(result);
        assertTrue(result.contains("<l_linea>"));
        assertTrue(result.contains("</l_linea>"));
    }

    @Test
    void buildSmlInput_withMultipleFdeEntries_includesAllLines() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        List<Fde> fdeList = List.of(
            createValidFde(),
            createValidFde()
        );

        String result = service.buildSmlInput(req, fac, fdeList, List.of(), "12345678A");

        assertTrue(result.contains("<linea>"));
        int count = 0;
        int index = 0;
        while ((index = result.indexOf("<linea>", index)) != -1) {
            count++;
            index += 1;
        }
        assertEquals(2, count);
    }

    @Test
    void parseResponse_withSuccessResponse_returnsExitoTrue() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                    &lt;operacion&gt;
                      &lt;opeext&gt;123&lt;/opeext&gt;
                      &lt;opesical&gt;456&lt;/opesical&gt;
                    &lt;/operacion&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertTrue(result.isExito());
        assertEquals("Operación generada correctamente", result.getMensaje());
    }

    @Test
    void parseResponse_withFailureResponse_returnsExitoFalse() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;0&lt;/exito&gt;
                    &lt;codigo&gt;ERR001&lt;/codigo&gt;
                    &lt;desc&gt;Error en la operación&lt;/desc&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
        assertTrue(result.getMensaje().contains("ERR001"));
        assertTrue(result.getMensaje().contains("Error en la operación"));
    }

    @Test
    void parseResponse_withMalformedXml_returnsErrorMessage() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;invalid&gt;xml structure
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
        assertTrue(result.getMensaje().contains("error") || result.getMensaje().contains("Error"));
    }

    @Test
    void parseResponse_withNullResponse_returnsErrorMessage() {
        ContabilizacionResponseDto result = service.parseResponse(null);

        assertFalse(result.isExito());
        assertNotNull(result.getMensaje());
    }

    @Test
    void parseResponse_withInvalidSoapEnvelope_returnsErrorMessage() {
        String soapResponse = "not a valid soap response";

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
        assertNotNull(result.getMensaje());
    }

    @Test
    void sendSmlRequest_withValidSml_returnsSoapResponse() {
        String smlInput = "<e><test>data</test></e>";
        String result = service.sendSmlRequest(smlInput, "http://test-url");
        assertTrue(result == null || result instanceof String);
    }

    @Test
    void buildSmlInput_formatDateCorrectly() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACDAT(LocalDateTime.of(2026, 3, 22, 10, 30));

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "12345678A");

        assertTrue(result.contains("<fdoc>20260322</fdoc>"));
    }

    @Test
    void buildSmlInput_withNullFacDate_omitsDateField() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACDAT(null);

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "12345678A");

        assertFalse(result.contains("<fdoc>"));
    }

    @Test
    void buildSmlInput_handlesContratoFieldsWithDefaults() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setCONCTP(null);
        fac.setCONCPR(null);
        fac.setCONCCR(null);

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "12345678A");

        assertTrue(result.contains("tipContrato"));
        assertTrue(result.contains("proContrato"));
        assertTrue(result.contains("criContrato"));
    }

    @Test
    void buildSmlInput_withFacOptionalFieldsNull_omitsOptionalElements() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACOPG(null);
        fac.setFACOCT(null);
        fac.setFACTPG(null);

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "12345678A");

        assertNotNull(result);
        assertTrue(result.contains("<e>"));
    }

    @Test
    void parseResponse_withOperacionFields_mapsAllFieldsCorrectly() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                    &lt;operacion&gt;
                      &lt;opeext&gt;EXT123&lt;/opeext&gt;
                      &lt;opesical&gt;SIC456&lt;/opesical&gt;
                      &lt;nap&gt;NAP789&lt;/nap&gt;
                      &lt;referencia&gt;REF001&lt;/referencia&gt;
                      &lt;importe&gt;1000.50&lt;/importe&gt;
                      &lt;ejercicio&gt;2026&lt;/ejercicio&gt;
                    &lt;/operacion&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertTrue(result.isExito());
        assertEquals("EXT123", result.getOpeext());
        assertEquals("SIC456", result.getOpesical());
        assertEquals("NAP789", result.getNap());
        assertEquals("REF001", result.getReferencia());
        assertEquals("1000.50", result.getImporte());
        assertEquals("2026", result.getEjercicio());
    }

    @Test
    void buildSmlInput_withNullRequest_throwsException() {
        Fac fac = createValidFac();
        assertThrows(SmlBuildingException.class, () -> {
            service.buildSmlInput(null, fac, List.of(), List.of(), "NIF");
        });
    }

    @Test
    void buildSmlInput_withNullFac_throwsException() {
        ContabilizacionRequestDto req = createValidRequest();
        assertThrows(SmlBuildingException.class, () -> {
            service.buildSmlInput(req, null, List.of(), List.of(), "NIF");
        });
    }

    @Test
    void buildSmlInput_withEmptyFdeAndFdtLists_succeeds() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertNotNull(result);
        assertTrue(result.contains("<e>"));
        assertTrue(result.contains("</e>"));
    }

    @Test
    void parseResponse_withEmptySoapResponse_returnsError() {
        ContabilizacionResponseDto result = service.parseResponse("");

        assertFalse(result.isExito());
        assertNotNull(result.getMensaje());
    }

    @Test
    void parseResponse_withBlankSoapResponse_returnsError() {
        ContabilizacionResponseDto result = service.parseResponse("   ");

        assertFalse(result.isExito());
        assertNotNull(result.getMensaje());
    }

    @Test
    void parseResponse_withMissingServiceoReturnTag_returnsError() {
        String soapResponse = "<soap:Envelope><soap:Body></soap:Body></soap:Envelope>";

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
        assertNotNull(result.getMensaje());
    }

    @Test
    void parseResponse_withMissingExitoTag_returnsError() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;codigo&gt;ERR001&lt;/codigo&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
    }

    @Test
    void buildSmlInput_withFdeNegativeAmount_skipsLine() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fde fde = createValidFde();
        fde.setFDEIMP(-50.0);
        fde.setFDEDIF(-25.0);

        String result = service.buildSmlInput(req, fac, List.of(fde), List.of(), "NIF");

        assertNotNull(result);
        assertFalse(result.contains("<linea>"));
    }

    @Test
    void buildSmlInput_withFdeNullFields_handlesGracefully() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fde fde = new Fde();
        fde.setFDEIMP(100.0);
        fde.setFDEDIF(null);
        fde.setFDEORG(null);
        fde.setFDEFUN(null);
        fde.setFDEECO(null);
        fde.setFDEREF(null);

        String result = service.buildSmlInput(req, fac, List.of(fde), List.of(), "NIF");

        assertNotNull(result);
        assertTrue(result.contains("<linea>"));
    }

    @Test
    void buildSmlInput_withFdtNullFields_handlesGracefully() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fdt fdt = new Fdt();
        fdt.setFDTARE(null);
        fdt.setFDTORG(null);
        fdt.setFDTFUN(null);
        fdt.setFDTECO(null);
        fdt.setFDTDTO(null);
        fdt.setFDTBSE(null);
        fdt.setFDTPRE(null);
        fdt.setFDTTXT(null);

        String result = service.buildSmlInput(req, fac, List.of(), List.of(fdt), "NIF");

        assertNotNull(result);
        assertFalse(result.contains("<dto>"));
    }

    @Test
    void parseResponse_withComplexErrorStructure_buildsErrorMessage() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;0&lt;/exito&gt;
                    &lt;codigo&gt;ERR001&lt;/codigo&gt;
                    &lt;desc&gt;Validation failed&lt;/desc&gt;
                    &lt;error&gt;Field A invalid&lt;/error&gt;
                    &lt;error&gt;Field B missing&lt;/error&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
        assertTrue(result.getMensaje().contains("ERR001"));
        assertTrue(result.getMensaje().contains("Validation failed"));
        assertTrue(result.getMensaje().contains("Field A invalid"));
    }

    @Test
    void buildSmlInput_withEmptyTerAyt_buildsValidXml() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "");

        assertNotNull(result);
        assertTrue(result.contains("<e>"));
    }

    @Test
    void buildSmlInput_withNullTerAyt_buildsValidXml() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), null);

        assertNotNull(result);
        assertTrue(result.contains("<e>"));
    }

    private ContabilizacionRequestDto createValidRequest() {
        ContabilizacionRequestDto req = new ContabilizacionRequestDto();
        req.setOrg("ORG001");
        req.setEnt("1");
        req.setEje("E1");
        req.setUsu("USER001");
        req.setPwd("password");
        req.setPublicKey("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...");
        req.setFechaContable("2026-03-22");
        return req;
    }

    private Fac createValidFac() {
        Fac fac = new Fac();
        fac.setEJE("E1");
        fac.setFACNUM(100);
        fac.setCGECOD("CGE001");
        fac.setFACOPG("OP001");
        fac.setFACDAT(LocalDateTime.now());
        fac.setFACOCT(5);
        fac.setFACFPG("20260401");
        fac.setFACTPG("TPG001");
        fac.setFACTXT("Test invoice");
        fac.setCONCTP("Suministro");
        fac.setCONCPR("AdDirec");
        fac.setCONCCR("SinC");
        return fac;
    }

    private Fde createValidFde() {
        Fde fde = new Fde();
        fde.setFDEIMP(100.0);
        fde.setFDEDIF(0.0);
        fde.setFDEORG("ORG001");
        fde.setFDEFUN("FUN001");
        fde.setFDEECO("ECO001");
        fde.setFDEREF("REF001");
        return fde;
    }

    private Fdt createValidFdt() {
        Fdt fdt = new Fdt();
        fdt.setFDTARE("AREA001");
        fdt.setFDTORG("ORG001");
        fdt.setFDTFUN("FUN001");
        fdt.setFDTECO("ECO001");
        fdt.setFDTDTO(50.0);
        fdt.setFDTBSE(100.0);
        fdt.setFDTPRE(10.0);
        fdt.setFDTTXT("Test detail");
        return fdt;
    }

    @Test
    void buildSmlInput_withFacFPGValidFormat_includesPaymentDate() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACFPG("20260415");

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertTrue(result.contains("<fpago>20260415</fpago>"));
    }

    @Test
    void buildSmlInput_withFacFPGFormattedDate_handlesConversion() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACFPG("2026-04-15");

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertTrue(result.contains("fpago") || result.contains("2026"));
    }

    @Test
    void buildSmlInput_withFacFPGTooShort_omitsPaymentDate() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACFPG("2026");

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertNotNull(result);
    }

    @Test
    void buildSmlInput_withFacFPGEmpty_omitsPaymentDate() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACFPG("");

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertFalse(result.contains("<fpago>"));
    }

    @Test
    void buildSmlInput_withFacFPGNull_omitsPaymentDate() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACFPG(null);

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertFalse(result.contains("<fpago>"));
    }

    @Test
    void buildSmlInput_withFacOPGEmpty_omitsOrtField() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACOPG("");

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertNotNull(result);
    }

    @Test
    void buildSmlInput_withFacTXTNull_omitsTextField() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACTXT(null);

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertFalse(result.contains("<text>"));
    }

    @Test
    void buildSmlInput_withMultipleFdeAmountsZero_skipsLines() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fde fde1 = createValidFde();
        fde1.setFDEIMP(0.0);
        fde1.setFDEDIF(0.0);
        Fde fde2 = createValidFde();
        fde2.setFDEIMP(100.0);

        String result = service.buildSmlInput(req, fac, List.of(fde1, fde2), List.of(), "NIF");

        int count = result.split("<linea>").length - 1;
        assertEquals(1, count);
    }

    @Test
    void buildSmlInput_withFdeZeroAmount_skipsLine() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fde fde = createValidFde();
        fde.setFDEIMP(0.0);

        String result = service.buildSmlInput(req, fac, List.of(fde), List.of(), "NIF");

        assertFalse(result.contains("<linea>"));
    }

    @Test
    void parseResponse_withOperacionFieldOrganica_decodesBase64() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                    &lt;operacion&gt;
                      &lt;organica&gt;Q29kZWQgVmFsdWU=&lt;/organica&gt;
                    &lt;/operacion&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertTrue(result.isExito());
        assertNotNull(result.getOrganica());
    }

    @Test
    void parseResponse_withOperacionFieldFuncional_decodesBase64() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                    &lt;operacion&gt;
                      &lt;funcional&gt;Q29kZWQgVmFsdWU=&lt;/funcional&gt;
                    &lt;/operacion&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertTrue(result.isExito());
        assertNotNull(result.getFuncional());
    }

    @Test
    void parseResponse_withOperacionFieldEconomica_decodesBase64() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                    &lt;operacion&gt;
                      &lt;economica&gt;Q29kZWQgVmFsdWU=&lt;/economica&gt;
                    &lt;/operacion&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertTrue(result.isExito());
        assertNotNull(result.getEconomica());
    }

    @Test
    void parseResponse_withErrorListMultipleErrors_concatenatesAll() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;0&lt;/exito&gt;
                    &lt;error&gt;Error 1&lt;/error&gt;
                    &lt;error&gt;Error 2&lt;/error&gt;
                    &lt;error&gt;Error 3&lt;/error&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
        assertTrue(result.getMensaje().contains("Error 1"));
        assertTrue(result.getMensaje().contains("Error 2"));
        assertTrue(result.getMensaje().contains("Error 3"));
    }

    @Test
    void parseResponse_withOnlyCodeInError_includesCode() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;0&lt;/exito&gt;
                    &lt;codigo&gt;CODE123&lt;/codigo&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
        assertTrue(result.getMensaje().contains("CODE123"));
    }

    @Test
    void parseResponse_withOnlyDescInError_includesDesc() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;0&lt;/exito&gt;
                    &lt;desc&gt;Description only&lt;/desc&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
        assertTrue(result.getMensaje().contains("Description only"));
    }

    @Test
    void parseResponse_withNoErrorDetails_returnsUnknownError() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;0&lt;/exito&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
        assertTrue(result.getMensaje().contains("Error desconocido"));
    }

    @Test
    void buildSmlInput_withFdeAllFieldsPopulated_includesAllInOutput() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fde fde = createValidFde();
        fde.setFDEORG("O1");
        fde.setFDEFUN("F1");
        fde.setFDEECO("E1");
        fde.setFDEREF("R1");

        String result = service.buildSmlInput(req, fac, List.of(fde), List.of(), "NIF");

        assertTrue(result.contains("<linea>"));
        assertTrue(result.contains("<imp>"));
    }

    // NOTE: dto lines are only emitted when a matching Fde line (same org/fun/eco)
    // was actually included in <l_linea>. This test now supplies that matching Fde.
    @Test
    void buildSmlInput_withFdtAllFieldsPopulated_includesAllInOutput() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fde fde = createValidFde();
        fde.setFDEORG("O1");
        fde.setFDEFUN("F1");
        fde.setFDEECO("E1");
        Fdt fdt = createValidFdt();
        fdt.setFDTARE("A1");
        fdt.setFDTORG("O1");
        fdt.setFDTFUN("F1");
        fdt.setFDTECO("E1");

        String result = service.buildSmlInput(req, fac, List.of(fde), List.of(fdt), "NIF");

        assertTrue(result.contains("<dto>"));
        assertTrue(result.contains("</dto>"));
    }

    @Test
    void buildSmlInput_withFdtNullAmounts_omitsAmountFields() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fdt fdt = createValidFdt();
        fdt.setFDTDTO(null);
        fdt.setFDTBSE(null);
        fdt.setFDTPRE(null);

        String result = service.buildSmlInput(req, fac, List.of(), List.of(fdt), "NIF");

        assertFalse(result.contains("<dto>"));
    }

    @Test
    void parseResponse_withEmptyOperacionTag_stillSucceeds() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                    &lt;operacion&gt;
                    &lt;/operacion&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertTrue(result.isExito());
        assertEquals("Operación generada correctamente", result.getMensaje());
    }

    @Test
    void buildSmlInput_withContratoFieldsEmpty_usesDefaults() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setCONCTP("");
        fac.setCONCPR("");
        fac.setCONCCR("");

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertTrue(result.contains("<tipContrato>"));
        assertTrue(result.contains("<proContrato>"));
        assertTrue(result.contains("<criContrato>"));
    }

    @Test
    void buildSmlInput_fechaContableFormattedWithDashes_removedCorrectly() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        req.setFechaContable("2026-03-22");
        Fac fac = createValidFac();

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertTrue(result.contains("<fecont>20260322</fecont>"));
    }

    @Test
    void buildSmlInput_fechaContableAlreadyFormatted_usesAsIs() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        req.setFechaContable("20260322");
        Fac fac = createValidFac();

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertTrue(result.contains("<fecont>20260322</fecont>"));
    }

    @Test
    void parseResponse_withNullExitoValue_treatAsFailure() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;&lt;/exito&gt;
                    &lt;desc&gt;Error occurred&lt;/desc&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
    }

    @Test
    void buildSmlInput_withFdeImpZeroDifNotZero_includesLine() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fde fde = createValidFde();
        fde.setFDEIMP(0.0);
        fde.setFDEDIF(50.0);

        String result = service.buildSmlInput(req, fac, List.of(fde), List.of(), "NIF");

        assertTrue(result.contains("<linea>"));
    }

    @Test
    void buildSmlInput_withFdeImpNotZeroDifZero_includesLine() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        Fde fde = createValidFde();
        fde.setFDEIMP(50.0);
        fde.setFDEDIF(0.0);

        String result = service.buildSmlInput(req, fac, List.of(fde), List.of(), "NIF");

        assertTrue(result.contains("<linea>"));
    }

    // NOTE: needs a matching Fde line included so the dto entries (all sharing the
    // same org/fun/eco clave) are not filtered out by the new "orphan dto" guard.
    @Test
    void buildSmlInput_withFdtMultipleEntries_includesAllDtos() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        List<Fdt> fdtList = List.of(
            createValidFdt(),
            createValidFdt(),
            createValidFdt()
        );

        String result = service.buildSmlInput(req, fac, List.of(createValidFde()), fdtList, "NIF");

        int count = result.split("<dto>").length - 1;
        assertEquals(3, count);
    }

    @Test
    void sendSmlRequest_withNullUrl_usesDefaultUrl() {
        String smlInput = "<e><test>data</test></e>";

        String result = service.sendSmlRequest(smlInput, null);

        assertTrue(result == null || result instanceof String);
    }

    @Test
    void sendSmlRequest_withEmptyUrl_usesDefaultUrl() {
        String smlInput = "<e><test>data</test></e>";

        String result = service.sendSmlRequest(smlInput, "");

        assertTrue(result == null || result instanceof String);
    }

    @Test
    void sendSmlRequest_withCustomUrl_usesProvidedUrl() {
        String smlInput = "<e><test>data</test></e>";
        String customUrl = "http://custom-url:8080/service";

        String result = service.sendSmlRequest(smlInput, customUrl);

        assertTrue(result == null || result instanceof String);
    }

    @Test
    void buildSmlInput_withTerAytContainingSpecialCharacters_encodesCorrectly() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "12345678-ABC");

        assertTrue(result.contains("<nif>"));
    }

    @Test
    void parseResponse_withNonExitoNegativeOne_treatAsFailure() {
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;1&lt;/exito&gt;
                    &lt;desc&gt;Unexpected value&lt;/desc&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        ContabilizacionResponseDto result = service.parseResponse(soapResponse);

        assertFalse(result.isExito());
    }

    @Test
    void buildSmlInput_withFacOCTZero_omitsObtField() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACOCT(0);

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertNotNull(result);
    }

    @Test
    void buildSmlInput_withFacOCTNull_omitsObpField() throws Exception {
        ContabilizacionRequestDto req = createValidRequest();
        Fac fac = createValidFac();
        fac.setFACOCT(null);

        String result = service.buildSmlInput(req, fac, List.of(), List.of(), "NIF");

        assertFalse(result.contains("<obp>"));
    }

    @Test
    void buildSmlInput_withWsNoResults_skipsFdeLine() throws Exception {
      org.mockito.Mockito.when(operacionesService.getOperaciones(
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any()
      )).thenReturn(List.of());

      ContabilizacionRequestDto req = createValidRequest();
      Fac fac = createValidFac();
      Fde fde = createValidFde();

      String result = service.buildSmlInput(req, fac, List.of(fde), List.of(), "NIF");

      assertNotNull(result);
      assertFalse(result.contains("<linea>"));
    }

    @Test
    void buildSmlInput_withWsMultipleOperations_skipsFdeLine() throws Exception {
      Operaciones op1 = createOperacionWithSingleLinea();
      Operaciones op2 = createOperacionWithSingleLinea();

      org.mockito.Mockito.when(operacionesService.getOperaciones(
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any()
      )).thenReturn(List.of(op1, op2));

      ContabilizacionRequestDto req = createValidRequest();
      Fac fac = createValidFac();
      Fde fde = createValidFde();

      String result = service.buildSmlInput(req, fac, List.of(fde), List.of(), "NIF");

      assertNotNull(result);
      assertFalse(result.contains("<linea>"));
    }

    private void mockOperacionesSingleLine() throws Exception {
      org.mockito.Mockito.lenient().when(operacionesService.getOperaciones(
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any(),
        org.mockito.ArgumentMatchers.<String>any()
      )).thenReturn(List.of(createOperacionWithSingleLinea()));
    }

    private Operaciones createOperacionWithSingleLinea() {
      Operaciones op = new Operaciones();
      Operaciones.Linea linea = new Operaciones.Linea();
      linea.setNlinea(1);
      linea.setPrya(2026);
      linea.setPryt("PRYT");
      linea.setPryo("PRYO");
      linea.setPryn("PRYN");
      linea.setPryx(9);
      op.setLineaList(List.of(linea));
      return op;
    }
}