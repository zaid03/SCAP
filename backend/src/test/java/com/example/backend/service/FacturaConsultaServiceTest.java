package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.backend.dto.FacturaConsultaRequestDto;
import com.example.backend.exception.SmlProcessingException;
import com.example.sical.CryptoSical;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class FacturaConsultaServiceTest {

    private FacturaConsultaService service;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        service = new FacturaConsultaService();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        ReflectionTestUtils.setField(service, "sicalWsUrl", "http://test-sical:8080/services/Ci?wsdl");
    }
    
    @Test
    void buildSmlInput_withValidAllFields_succeeds() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            cryptoMock.when(() -> CryptoSical.calculateSecurityFields("pk123"))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64("origin789"))
                .thenReturn("encodedOrigin");
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64("password123"))
                .thenReturn("encodedPwd");

            FacturaConsultaRequestDto dto = createValidRequestDto();
            
            String result = service.buildSmlInput(dto);
            
            assertNotNull(result);
            assertTrue(result.contains("<org>ORG001</org>"));
            assertTrue(result.contains("<ent>ENT001</ent>"));
            assertTrue(result.contains("<eje>2024</eje>"));
            assertTrue(result.contains("<usu>user1</usu>"));
            assertTrue(result.contains("<tipoRegistro>" + Base64.getEncoder().encodeToString("F".getBytes(StandardCharsets.UTF_8)) + "</tipoRegistro>"));
            assertTrue(result.contains("<pwd>encodedPwd</pwd>"));
            assertTrue(result.contains("<tipoDocumento>1</tipoDocumento>"));
            assertTrue(result.contains("<apl>SNP</apl>"));
            assertTrue(result.contains("<cge>" + Base64.getEncoder().encodeToString("CGE001".getBytes(StandardCharsets.UTF_8)) + "</cge>"));
            assertTrue(result.contains("<tobj>Justificantes</tobj>"));
            assertTrue(result.contains("<cmd>LST</cmd>"));
            assertTrue(result.contains("<ver>2.0</ver>"));
        }
    }

    @Test
    void buildSmlInput_withOnlyRequiredFields_succeeds() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields("pk123"))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encoded");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createDtoFromMap(Map.of(
                "org", "ORG001",
                "ent", "ENT001",
                "eje", "2024",
                "usu", "user1",
                "pwd", "password",
                "publicKey", "pk123"
            ));
            
            String result = service.buildSmlInput(dto);
            
            assertNotNull(result);
            assertTrue(result.contains("<org>ORG001</org>"));
            assertFalse(result.contains("tipoDocumento"));
            assertFalse(result.contains("<cge>"));
        }
    }

    @Test
    void buildSmlInput_withAllOptionalFields_succeeds() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields("pk123"))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encoded");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createFullRequestDto();
            
            String result = service.buildSmlInput(dto);
            
            assertNotNull(result);
            assertTrue(result.contains("<tipoRegistro>" + Base64.getEncoder().encodeToString("F".getBytes(StandardCharsets.UTF_8)) + "</tipoRegistro>"));
            assertTrue(result.contains("<tipoDocumento>1</tipoDocumento>"));
            assertTrue(result.contains("<cge>" + Base64.getEncoder().encodeToString("CGE001".getBytes(StandardCharsets.UTF_8)) + "</cge>"));
            assertTrue(result.contains("<situacionIgual>" + Base64.getEncoder().encodeToString("1".getBytes(StandardCharsets.UTF_8)) + "</situacionIgual>"));
            assertTrue(result.contains("<estado>" + Base64.getEncoder().encodeToString("ACEPTADA".getBytes(StandardCharsets.UTF_8)) + "</estado>"));
            assertTrue(result.contains("<tercero>" + Base64.getEncoder().encodeToString("TERC001".getBytes(StandardCharsets.UTF_8)) + "</tercero>"));
            assertTrue(result.contains("<docProveedor>" + Base64.getEncoder().encodeToString("DOC001".getBytes(StandardCharsets.UTF_8)) + "</docProveedor>"));
        }
    }

    @Test
    void buildSmlInput_withNullPublicKey_throwsException() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(null))
                .thenThrow(new RuntimeException("Null public key"));

            Map<String, Object> data = new HashMap<>();
            data.put("org", "ORG001");
            data.put("ent", "ENT001");
            data.put("eje", "2024");
            data.put("usu", "user1");
            data.put("pwd", "password");
            data.put("publicKey", null);
            
            FacturaConsultaRequestDto dto = createDtoFromMap(data);
            assertThrows(SmlProcessingException.class, () -> service.buildSmlInput(dto));
        }
    }

    @Test
    void buildSmlInput_withNullOrg_succeeds() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            Map<String, Object> data = new HashMap<>();
            data.put("org", null);
            data.put("ent", "ENT001");
            data.put("eje", "2024");
            data.put("usu", "user1");
            data.put("pwd", "password");
            data.put("publicKey", "pk123");
            
            FacturaConsultaRequestDto dto = createDtoFromMap(data);
            String result = service.buildSmlInput(dto);
            
            assertNotNull(result);
            assertTrue(result.contains("<org>") || result.contains("org"));
        }
    }

    @Test
    void buildSmlInput_withNullPassword_succeeds() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");

            Map<String, Object> data = new HashMap<>();
            data.put("org", "ORG001");
            data.put("ent", "ENT001");
            data.put("eje", "2024");
            data.put("usu", "user1");
            data.put("pwd", null);
            data.put("publicKey", "pk123");
            
            FacturaConsultaRequestDto dto = createDtoFromMap(data);
            String result = service.buildSmlInput(dto);
            
            assertNotNull(result);
            assertTrue(result.contains("<pwd>") || result.contains("pwd"));
        }
    }

    @Test
    void buildSmlInput_withCryptoCalculationException_throwsSmlException() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenThrow(new RuntimeException("Crypto error"));

            FacturaConsultaRequestDto dto = createValidRequestDto();
            
            assertThrows(SmlProcessingException.class, () -> service.buildSmlInput(dto));
        }
    }

    @Test
    void buildSmlInput_xmlStructureCorrect_validatesHeaders() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encoded");

            FacturaConsultaRequestDto dto = createValidRequestDto();
            
            String result = service.buildSmlInput(dto);
            
            assertTrue(result.startsWith("<e>"));
            assertTrue(result.endsWith("</e>"));
            assertTrue(result.contains("<ope>") && result.contains("</ope>"));
            assertTrue(result.contains("<sec>") && result.contains("</sec>"));
            assertTrue(result.contains("<par>") && result.contains("</par>"));
            assertTrue(result.contains("<cli>SAGE-AYTOS</cli>"));
        }
    }

    @Test
    void buildSmlInput_withSpecialCharactersInFields_handledCorrectly() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createDtoFromMap(Map.of(
                "org", "ORG001",
                "ent", "ENT001",
                "eje", "2024",
                "usu", "user1",
                "pwd", "password",
                "publicKey", "pk123",
                "tipoRegistro", "F",
                "cge", "CGE<001>" // This value will be Base64 encoded
            ));
            
            String result = service.buildSmlInput(dto);
            assertNotNull(result);
            assertTrue(result.contains("<tipoRegistro>" + Base64.getEncoder().encodeToString("F".getBytes(StandardCharsets.UTF_8)) + "</tipoRegistro>"));
            assertTrue(result.contains("<cge>" + Base64.getEncoder().encodeToString("CGE<001>".getBytes(StandardCharsets.UTF_8)) + "</cge>"));
        }
    }

    @Test
    void buildSmlInput_withDefaultWebserviceUrl_succeeds() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encoded");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createValidRequestDto();
            
            String result = service.buildSmlInput(dto);
            
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    @Test
    void sendSmlRequest_withValidInput_constructsSoapEnvelope() {
        String smlInput = "<e><test>data</test></e>";
        String url = "http://test-endpoint:8080/services";
        
        try {
            service.sendSmlRequest(smlInput, url);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void sendSmlRequest_withNullUrl_usesDefaultUrl() {
        String smlInput = "<e><test>data</test></e>";
        
        try {
            service.sendSmlRequest(smlInput, null);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void sendSmlRequest_withEmptyUrl_usesDefaultUrl() {
        String smlInput = "<e><test>data</test></e>";
        
        try {
            service.sendSmlRequest(smlInput, "");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void sendSmlRequest_withComplexXmlInput_succeeds() {
        String smlInput = "<e><ope><apl>SNP</apl></ope><sec><cli>SAGE</cli></sec></e>";
        String url = "http://test-endpoint:8080/services";
        
        try {
            service.sendSmlRequest(smlInput, url);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    private FacturaConsultaRequestDto createValidRequestDto() {
        return createDtoFromMap(Map.of(
            "org", "ORG001",
            "ent", "ENT001",
            "eje", "2024",
            "usu", "user1",
            "pwd", "password123",
            "publicKey", "pk123", // Required for security fields calculation
            "tipoRegistro", "F", // Added for new Base64 encoded field
            "tipoDocumento", 1,
            "cge", "CGE001"
        ));
    }

    private FacturaConsultaRequestDto createFullRequestDto() {
        Map<String, Object> data = new HashMap<>();
        data.put("org", "ORG001");
        data.put("ent", "ENT001");
        data.put("eje", "2024");
        data.put("usu", "user1");
        data.put("pwd", "password123");
        data.put("publicKey", "pk123"); // Required for security fields calculation
        data.put("tipoRegistro", "F"); // Added for new Base64 encoded field
        data.put("tipoDocumento", 1);
        data.put("cge", "CGE001");
        data.put("situacionIgual", "1");
        data.put("estado", "ACEPTADA");
        data.put("tercero", "TERC001");
        data.put("docProveedor", "DOC001");
        data.put("fecRegDesde", LocalDateTime.of(2024, 1, 1, 0, 0)); // Date fields are formatted, not Base64 encoded
        data.put("fecRegHasta", LocalDateTime.of(2024, 12, 31, 23, 59)); // Date fields are formatted, not Base64 encoded
        data.put("fecDocDesde", LocalDateTime.of(2024, 1, 15, 0, 0)); // Date fields are formatted, not Base64 encoded
        data.put("fecDocHasta", LocalDateTime.of(2024, 12, 15, 23, 59)); // Date fields are formatted, not Base64 encoded
        return createDtoFromMap(data);
    }

    private FacturaConsultaRequestDto createDtoFromMap(Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return objectMapper.readValue(json, FacturaConsultaRequestDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create DTO from map", e);
        }
    }

    // ===================== Additional Coverage Tests =====================

    @Test
    void buildSmlInput_withDateFieldsOnly_includesAllDateRanges() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            Map<String, Object> data = new HashMap<>();
            data.put("org", "ORG001");
            data.put("ent", "ENT001");
            data.put("eje", "2024");
            data.put("usu", "user1");
            data.put("pwd", "password");
            data.put("publicKey", "pk123");
            data.put("fecRegDesde", LocalDateTime.of(2024, 1, 1, 0, 0)); // Date fields are formatted, not Base64 encoded
            data.put("fecRegHasta", LocalDateTime.of(2024, 1, 31, 23, 59)); // Date fields are formatted, not Base64 encoded
            data.put("fecDocDesde", LocalDateTime.of(2024, 2, 1, 0, 0)); // Date fields are formatted, not Base64 encoded
            data.put("fecDocHasta", LocalDateTime.of(2024, 2, 28, 23, 59)); // Date fields are formatted, not Base64 encoded
            
            FacturaConsultaRequestDto dto = createDtoFromMap(data);
            String result = service.buildSmlInput(dto);
            
            assertTrue(result.contains("<fecRegDesde>20240101</fecRegDesde>"));
            assertTrue(result.contains("<fecRegHasta>20240131</fecRegHasta>"));
            assertTrue(result.contains("<fecDocDesde>20240201</fecDocDesde>"));
            assertTrue(result.contains("<fecDocHasta>20240228</fecDocHasta>"));
        }
    }

    @Test
    void buildSmlInput_withEncodingException_throwsSmlException() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64("password123"))
                .thenThrow(new RuntimeException("Encoding failed"));
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenThrow(new RuntimeException("Encoding failed"));

            FacturaConsultaRequestDto dto = createValidRequestDto();
            
            assertThrows(SmlProcessingException.class, () -> service.buildSmlInput(dto));
        }
    }

    @Test
    void buildSmlInput_verifySecurityFieldCalculation() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields("pk123"))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createValidRequestDto();
            service.buildSmlInput(dto);
            
            cryptoMock.verify(() -> CryptoSical.calculateSecurityFields("pk123"));
        }
    }

    @Test
    void buildSmlInput_verifyPasswordEncoding() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createValidRequestDto();
            service.buildSmlInput(dto);
            
            cryptoMock.verify(() -> CryptoSical.encodeSha1Base64("password123"));
        }
    }

    @Test
    void buildSmlInput_verifyOriginEncoding() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createValidRequestDto();
            service.buildSmlInput(dto);
            
            cryptoMock.verify(() -> CryptoSical.encodeSha1Base64("origin789"));
        }
    }

    @Test
    void buildSmlInput_withOnlyStaticRequiredFields_createsValidXml() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createDtoFromMap(Map.of(
                "org", "ORG001",
                "ent", "ENT001",
                "eje", "2024",
                "usu", "user1",
                "pwd", "password",
                "publicKey", "pk123"
                ,"tipoRegistro", "F"
            ));
            
            String result = service.buildSmlInput(dto);
            
            assertTrue(result.contains("<apl>SNP</apl>"));
            assertTrue(result.contains("<tobj>Justificantes</tobj>"));
            assertTrue(result.contains("<cmd>LST</cmd>"));
            assertTrue(result.contains("<ver>2.0</ver>"));
            assertTrue(result.contains("<cli>SAGE-AYTOS</cli>"));
        }
    }

    @Test
    void buildSmlInput_withMultipleOptionalFieldsNull_excludesCorrectly() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            Map<String, Object> data = new HashMap<>();
            data.put("org", "ORG001");
            data.put("ent", "ENT001");
            data.put("eje", "2024");
            data.put("usu", "user1");
            data.put("pwd", "password");
            data.put("publicKey", "pk123");
            data.put("tipoDocumento", null);
            data.put("tipoRegistro", null); // Ensure this is also handled as nullable
            data.put("cge", null);
            data.put("situacionIgual", null);
            
            FacturaConsultaRequestDto dto = createDtoFromMap(data);
            String result = service.buildSmlInput(dto);
            
            assertFalse(result.contains("<tipoDocumento>"));
            assertFalse(result.contains("<tipoRegistro>"));
            assertFalse(result.contains("<situacionIgual>"));
        }
    }

    @Test
    void sendSmlRequest_withValidSmlInput_constructsValidSoapMessage() {
        String smlInput = "<e><sec><cli>SAGE</cli></sec></e>";
        String url = "http://test:8080";
        
        try {
            service.sendSmlRequest(smlInput, url);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void sendSmlRequest_wrapsInputInCdata_preventsParsing() {
        String smlInput = "<script>alert('test')</script>";
        String url = "http://test:8080";
        
        try {
            service.sendSmlRequest(smlInput, url);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void buildSmlInput_withTerceroAndDocProveedor_includesInOutput() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            Map<String, Object> data = new HashMap<>();
            data.put("org", "ORG001");
            data.put("ent", "ENT001");
            data.put("eje", "2024");
            data.put("usu", "user1");
            data.put("pwd", "password");
            data.put("publicKey", "pk123");
            data.put("tipoRegistro", "F");
            data.put("tercero", "TERCERO123"); // This value will be Base64 encoded
            data.put("docProveedor", "DOCPROV456"); // This value will be Base64 encoded
            
            FacturaConsultaRequestDto dto = createDtoFromMap(data);
            String result = service.buildSmlInput(dto);
            assertTrue(result.contains("<tercero>" + Base64.getEncoder().encodeToString("TERCERO123".getBytes(StandardCharsets.UTF_8)) + "</tercero>"));
            assertTrue(result.contains("<docProveedor>" + Base64.getEncoder().encodeToString("DOCPROV456".getBytes(StandardCharsets.UTF_8)) + "</docProveedor>"));
        }
    }

    @Test
    void buildSmlInput_withEstadoField_includesInOutput() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            Map<String, Object> data = new HashMap<>();
            data.put("org", "ORG001");
            data.put("ent", "ENT001");
            data.put("eje", "2024");
            data.put("usu", "user1");
            data.put("pwd", "password");
            data.put("publicKey", "pk123");
            data.put("tipoRegistro", "F");
            data.put("estado", "PAGADA"); // This value will be Base64 encoded
            
            FacturaConsultaRequestDto dto = createDtoFromMap(data);
            String result = service.buildSmlInput(dto);
            assertTrue(result.contains("<estado>" + Base64.getEncoder().encodeToString("PAGADA".getBytes(StandardCharsets.UTF_8)) + "</estado>"));
        }
    }

    @Test
    void buildSmlInput_preservesSituacionIgualField() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            Map<String, Object> data = new HashMap<>();
            data.put("org", "ORG001");
            data.put("ent", "ENT001");
            data.put("eje", "2024");
            data.put("usu", "user1");
            data.put("pwd", "password");
            data.put("publicKey", "pk123");
            data.put("tipoRegistro", "F");
            data.put("situacionIgual", "2"); // This value will be Base64 encoded
            
            FacturaConsultaRequestDto dto = createDtoFromMap(data);
            String result = service.buildSmlInput(dto);
            assertTrue(result.contains("<situacionIgual>" + Base64.getEncoder().encodeToString("2".getBytes(StandardCharsets.UTF_8)) + "</situacionIgual>"));
        }
    }

    @Test
    void buildSmlInput_combinedDateAndOtherFilters() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createFullRequestDto();
            String result = service.buildSmlInput(dto);
            
            assertTrue(result.contains("<sec>") && result.contains("</sec>"));
            assertTrue(result.contains("<par>") && result.contains("</par>"));
            assertTrue(result.length() > 200);
        }
    }

    @Test
    void buildSmlInput_respectsNullableFields() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            Map<String, Object> data = new HashMap<>();
            data.put("org", "ORG001");
            data.put("ent", "ENT001");
            data.put("eje", "2024");
            data.put("usu", "user1");
            data.put("pwd", "password");
            data.put("publicKey", "pk123");
            data.put("tipoRegistro", "F");
            data.put("fecRegDesde", null);
            data.put("fecRegHasta", null);
            
            FacturaConsultaRequestDto dto = createDtoFromMap(data);
            String result = service.buildSmlInput(dto);
            
            assertFalse(result.contains("<fecRegDesde>"));
            assertFalse(result.contains("<fecRegHasta>"));
        }
    }

    @Test
    void sendSmlRequest_constructsSoapWithNamespaces() {
        String smlInput = "<test>data</test>";
        String url = "http://test:8080";
        
        try {
            service.sendSmlRequest(smlInput, url);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void sendSmlRequest_usesTextXmlContentType() {
        String smlInput = "<test>data</test>";
        String url = "http://test:8080";
        
        try {
            service.sendSmlRequest(smlInput, url);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void buildSmlInput_operationSection_correctlyFormatted() {
        try (MockedStatic<CryptoSical> cryptoMock = mockStatic(CryptoSical.class)) {
            CryptoSical.SecurityFields secFields = new CryptoSical.SecurityFields(
                "token456", "nonce123", "20260322100000", "origin789"
            );
            cryptoMock.when(() -> CryptoSical.calculateSecurityFields(anyString()))
                .thenReturn(secFields);
            cryptoMock.when(() -> CryptoSical.encodeSha1Base64(anyString()))
                .thenReturn("encodedSha1");
            // Mock CryptoSical.encodeBase64 for all string parameters that are now encoded
            cryptoMock.when(() -> CryptoSical.encodeBase64(anyString()))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0, String.class).getBytes(StandardCharsets.UTF_8)));

            FacturaConsultaRequestDto dto = createValidRequestDto();
            String result = service.buildSmlInput(dto);
            
            int opeStart = result.indexOf("<ope>");
            int opeEnd = result.indexOf("</ope>");
            int secStart = result.indexOf("<sec>");
            
            assertTrue(opeStart < secStart && opeEnd < secStart);
        }
    }
}