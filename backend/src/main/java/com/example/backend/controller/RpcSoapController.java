package com.example.backend.controller;

import com.example.backend.dto.RpcResult;
import com.example.backend.service.RpcSoapService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rpc")
@CrossOrigin(origins = "http://localhost:4200")
public class RpcSoapController {

    private final RpcSoapService rpcSoapService;

    public RpcSoapController(RpcSoapService rpcSoapService) {
        this.rpcSoapService = rpcSoapService;
    }

    @PostMapping("/call")
    public ResponseEntity<?> callRpc(
        @RequestBody Map<String, 
        Object> params
    ) {
        try {
            String endpoint = (String) params.get("endpoint");
            String operation = (String) params.get("operation");
            String namespace = (String) params.get("namespace");
            String soapAction = params.getOrDefault("soapAction", "").toString();
            String payload = params.get("payload") != null ? params.get("payload").toString() : null;

            Map<String, String> paramsMap = null;
                if (payload == null && params.get("paramsMap") instanceof Map) {
                    Map<?, ?> rawMap = (Map<?, ?>) params.get("paramsMap");
                    paramsMap = new java.util.HashMap<>();
                    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                        paramsMap.put(entry.getKey().toString(), entry.getValue() != null ? entry.getValue().toString() : "");
                    }
            }   

            RpcResult result = rpcSoapService.callRpc(
                endpoint,
                operation,
                namespace,
                soapAction,
                payload,
                paramsMap
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}