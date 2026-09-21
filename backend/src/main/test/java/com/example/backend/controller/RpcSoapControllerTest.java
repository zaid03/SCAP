package com.example.backend.controller;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.config.TestExceptionHandler;
import com.example.backend.config.TestSecurityConfig;
import com.example.backend.dto.RpcResult;
import com.example.backend.service.RpcSoapService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = RpcSoapController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class RpcSoapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RpcSoapService rpcSoapService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCallRpcWithPayloadAndReturn200() throws Exception {
        RpcResult fake = mock(RpcResult.class);
        when(rpcSoapService.callRpc(eq("http://e"), eq("op"), eq("ns"), eq("action"), eq("payload"),
                isNull()))
            .thenReturn(fake);

        Map<String, Object> request = Map.of(
            "endpoint", "http://e",
            "operation", "op",
            "namespace", "ns",
            "soapAction", "action",
            "payload", "payload"
        );

        mockMvc.perform(post("/api/rpc/call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(rpcSoapService).callRpc(eq("http://e"), eq("op"), eq("ns"), eq("action"),
            eq("payload"), isNull());
    }

    @Test
    void shouldConvertParamsMapToStringMapAndPassToService() throws Exception {
        RpcResult fake = mock(RpcResult.class);
        when(rpcSoapService.callRpc(anyString(), anyString(), anyString(), anyString(),
                any(), isNull()))
             .thenThrow(new RuntimeException("rpc failure"));

        Map<String, Object> paramsMap = Map.of("p1", "one", "p2", 2);
        Map<String, Object> request = Map.of(
            "endpoint", "e",
            "operation", "op",
            "namespace", "ns",
            "paramsMap", paramsMap
        );

        mockMvc.perform(post("/api/rpc/call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(rpcSoapService).callRpc(eq("e"), eq("op"), eq("ns"), eq(""),
            isNull(), captor.capture());

        Map<String, String> passed = captor.getValue();
        assertThat(passed.get("p1"), equalTo("one"));
        assertThat(passed.get("p2"), equalTo("2")); 
    }

    @Test
    void shouldConvertNullParamsMapValueToEmptyString() throws Exception {
        RpcResult fake = mock(RpcResult.class);
        when(rpcSoapService.callRpc(anyString(), anyString(), anyString(), anyString(),
                any(), isNull()))
            .thenReturn(fake);

        Map<String, Object> paramsMap = new java.util.HashMap<>();
        paramsMap.put("p1", "one");
        paramsMap.put("p2", null);
        
        Map<String, Object> request = Map.of(
            "endpoint", "e",
            "operation", "op",
            "namespace", "ns",
            "paramsMap", paramsMap
        );

        mockMvc.perform(post("/api/rpc/call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(rpcSoapService).callRpc(eq("e"), eq("op"), eq("ns"), eq(""),
            isNull(), captor.capture());

        Map<String, String> passed = captor.getValue();
        assertThat(passed.get("p1"), equalTo("one"));
        assertThat(passed.get("p2"), equalTo(""));
    }

    @Test
    void shouldReturn500WhenServiceThrows() throws Exception {
        when(rpcSoapService.callRpc(anyString(), anyString(), anyString(), anyString(),
                any(), isNull()))
            .thenThrow(new RuntimeException("rpc failure"));

        Map<String, Object> request = Map.of("endpoint", "e", "operation", "op", "namespace", "ns");

        mockMvc.perform(post("/api/rpc/call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error", containsString("rpc failure")));

        verify(rpcSoapService).callRpc(eq("e"), eq("op"), eq("ns"), eq(""),
            isNull(), isNull());
    }
}