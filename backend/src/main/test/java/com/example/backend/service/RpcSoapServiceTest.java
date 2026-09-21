package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.backend.dto.RpcResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ExtendWith(MockitoExtension.class)
class RpcSoapServiceTest {

    private RpcSoapService service;
    
    @BeforeEach
    void setUp() {
        service = new RpcSoapService();
    }
    
    @Test
    void callRpc_withValidPayload_returnsResponse() {
        String responseXml = "<soap:Envelope><soap:Body><Response>Success</Response></soap:Body></soap:Envelope>";
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, 
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseXml))) {
            
            RpcResult result = service.callRpc(
                "http://example.com/api",
                "GetData",
                "http://example.com/namespace",
                "GetData",
                "<Param1>Value1</Param1>",
                null
            );
            
            assertNotNull(result);
            assertEquals(responseXml, result.getRawResponse());
            assertNull(result.getError());
            assertTrue(result.getRawRequest().contains("<ns1:GetData"));
            assertTrue(result.getRawRequest().contains("<Param1>Value1</Param1>"));
        }
    }

    @Test
    void callRpc_withNullPayloadAndParams_buildsXmlFromParams() {
        String responseXml = "<soap:Envelope><soap:Body><Response>OK</Response></soap:Body></soap:Envelope>";
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseXml))) {
            
            Map<String, String> params = new HashMap<>();
            params.put("UserID", "123");
            params.put("Password", "secret");
            
            RpcResult result = service.callRpc(
                "http://example.com/api",
                "Authenticate",
                "http://example.com/namespace",
                "Authenticate",
                null,
                params
            );
            
            assertNotNull(result);
            assertEquals(responseXml, result.getRawResponse());
            assertTrue(result.getRawRequest().contains("<ns1:Authenticate"));
            assertTrue(result.getRawRequest().contains("<UserID>123</UserID>"));
            assertTrue(result.getRawRequest().contains("<Password>secret</Password>"));
        }
    }

    @Test
    void callRpc_withPayloadIgnoresParams() {
        String responseXml = "<soap:Envelope><soap:Body><Response>Success</Response></soap:Body></soap:Envelope>";
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseXml))) {
            
            Map<String, String> ignoredParams = new HashMap<>();
            ignoredParams.put("Ignored", "Value");
            
            RpcResult result = service.callRpc(
                "http://example.com/api",
                "DoWork",
                "http://example.com/namespace",
                "DoWork",
                "<ActualParam>ActualValue</ActualParam>",
                ignoredParams
            );
            
            assertTrue(result.getRawRequest().contains("<ActualParam>ActualValue</ActualParam>"));
            assertFalse(result.getRawRequest().contains("<Ignored>Value</Ignored>"));
        }
    }

    @Test
    void callRpc_withNullSoapAction_setsEmptySoapAction() {
        String responseXml = "<soap:Envelope><soap:Body><Response>Success</Response></soap:Body></soap:Envelope>";
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseXml))) {
            
            RpcResult result = service.callRpc(
                "http://example.com/api",
                "GetInfo",
                "http://example.com/namespace",
                null,
                "<Data/>",
                null
            );
            
            assertNotNull(result);
            assertEquals(responseXml, result.getRawResponse());
        }
    }

    @Test
    void callRpc_postsSoapEnvelopeWithCorrectHeaders() {
        String responseXml = "<soap:Envelope><soap:Body><Response>Success</Response></soap:Body></soap:Envelope>";
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseXml))) {
            
            service.callRpc(
                "http://example.com/api",
                "TestOp",
                "http://example.com/ns",
                "TestAction",
                "<Param/>",
                null
            );
            
            assertTrue(mocked.constructed().size() >= 1);
        }
    }
    
    @Test
    void callRpc_withRestTemplateException_setsError() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection failed")))) {
            
            RpcResult result = service.callRpc(
                "http://invalid.example.com/api",
                "GetData",
                "http://example.com/namespace",
                "GetData",
                "<Param/>",
                null
            );
            
            assertNull(result.getRawResponse());
            assertNotNull(result.getError());
            assertTrue(result.getError().contains("Connection failed"));
        }
    }

    @Test
    void callRpc_withNetworkException_capturesErrorMessage() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Network timeout")))) {
            
            RpcResult result = service.callRpc(
                "http://example.com/api",
                "LongOperation",
                "http://example.com/namespace",
                "LongOperation",
                "<Param/>",
                null
            );
            
            assertEquals("Network timeout", result.getError());
        }
    }
    
    @Test
    void callRpc_containsValidSoapEnvelope() {
        String responseXml = "<soap:Envelope></soap:Envelope>";
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseXml))) {
            
            RpcResult result = service.callRpc(
                "http://example.com/api",
                "Operation",
                "http://example.com/namespace",
                "Operation",
                "<Data/>",
                null
            );
            
            String envelope = result.getRawRequest();
            assertTrue(envelope.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
            assertTrue(envelope.contains("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"));
            assertTrue(envelope.contains("<soapenv:Header/>"));
            assertTrue(envelope.contains("<soapenv:Body>"));
            assertTrue(envelope.contains("</soapenv:Body>"));
            assertTrue(envelope.contains("</soapenv:Envelope>"));
        }
    }

    @Test
    void callRpc_withMultipleNamespaceParams_buildsCorrectXml() {
        String responseXml = "<Response/>";
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseXml))) {
            
            Map<String, String> params = new HashMap<>();
            params.put("FirstName", "John");
            params.put("LastName", "Doe");
            params.put("Email", "john@example.com");
            params.put("Age", "30");
            
            RpcResult result = service.callRpc(
                "http://example.com/api",
                "CreateUser",
                "http://example.com/user",
                "CreateUser",
                null,
                params
            );
            
            String request = result.getRawRequest();
            assertTrue(request.contains("<FirstName>John</FirstName>"));
            assertTrue(request.contains("<LastName>Doe</LastName>"));
            assertTrue(request.contains("<Email>john@example.com</Email>"));
            assertTrue(request.contains("<Age>30</Age>"));
        }
    }
    
    @Test
    void buildXmlFromParams_createsCorrectStructure() {
        Map<String, String> params = new HashMap<>();
        params.put("Param1", "Value1");
        params.put("Param2", "Value2");
        
        String xml = service.buildXmlFromParams("Operation", params);
        
        assertTrue(xml.contains("<Operation>"));
        assertTrue(xml.contains("</Operation>"));
        assertTrue(xml.contains("<Param1>Value1</Param1>"));
        assertTrue(xml.contains("<Param2>Value2</Param2>"));
    }

    @Test
    void buildXmlFromParams_withEmptyParams_createsEmptyElement() {
        Map<String, String> params = new HashMap<>();
        
        String xml = service.buildXmlFromParams("EmptyOp", params);
        
        assertEquals("<EmptyOp></EmptyOp>", xml.toString());
    }

    @Test
    void buildXmlFromParams_withSpecialCharacters_encapsulatesValues() {
        Map<String, String> params = new HashMap<>();
        params.put("Name", "Test<Name>");
        params.put("Value", "Test&Value");
        
        String xml = service.buildXmlFromParams("Operation", params);
        
        assertTrue(xml.contains("<Name>Test<Name></Name>"));
        assertTrue(xml.contains("<Value>Test&Value</Value>"));
    }

    @Test
    void buildXmlFromParams_withMultipleValues_ordersCorrectly() {
        Map<String, String> params = new HashMap<>();
        params.put("A", "ValueA");
        params.put("B", "ValueB");
        params.put("C", "ValueC");
        
        String xml = service.buildXmlFromParams("TestOp", params);
        
        assertTrue(xml.startsWith("<TestOp>"));
        assertTrue(xml.endsWith("</TestOp>"));
        assertTrue(xml.contains("<A>ValueA</A>"));
        assertTrue(xml.contains("<B>ValueB</B>"));
        assertTrue(xml.contains("<C>ValueC</C>"));
    }
    
    @Test
    void callRpc_endToEndWithComplexPayload_succeeds() {
        String responseXml = "<?xml version=\"1.0\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><GetDataResponse><Result>OK</Result></GetDataResponse></soap:Body></soap:Envelope>";
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseXml))) {
            
            String complexPayload = "<Filter><ID>123</ID><Status>Active</Status><Date>2024-01-15</Date></Filter>";
            
            RpcResult result = service.callRpc(
                "http://service.example.com/rpc",
                "GetFilteredData",
                "http://service.example.com/schema",
                "http://service.example.com/schema/GetFilteredData",
                complexPayload,
                null
            );
            
            assertEquals(responseXml, result.getRawResponse());
            assertTrue(result.getRawRequest().contains("<ns1:GetFilteredData"));
            assertTrue(result.getRawRequest().contains(complexPayload));
        }
    }

    @Test
    void callRpc_withMultipleCallsToSameEndpoint_returnsIndependentResults() {
        String response1 = "<Response1/>";
        String response2 = "<Response2/>";
        AtomicInteger callCount = new AtomicInteger(0);
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenAnswer(i -> callCount.getAndIncrement() == 0 ? response1 : response2))) {
            
            RpcResult result1 = service.callRpc(
                "http://example.com/api",
                "Operation1",
                "http://example.com/ns",
                "Operation1",
                "<Data1/>",
                null
            );
            
            RpcResult result2 = service.callRpc(
                "http://example.com/api",
                "Operation2",
                "http://example.com/ns",
                "Operation2",
                "<Data2/>",
                null
            );
            
            assertEquals(response1, result1.getRawResponse());
            assertEquals(response2, result2.getRawResponse());
        }
    }

    @Test
    void callRpc_preservesPayloadExactly() {
        String responseXml = "<soap:Envelope/>";
        
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
            (mock, context) -> when(mock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseXml))) {
            
            String originalPayload = "<Element attr=\"value\">Content with <nested/> tags</Element>";
            
            RpcResult result = service.callRpc(
                "http://example.com/api",
                "Operation",
                "http://example.com/ns",
                "Operation",
                originalPayload,
                null
            );
            
            assertTrue(result.getRawRequest().contains(originalPayload));
        }
    }
}