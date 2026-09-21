package com.example.backend.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.backend.dto.RpcResult;

import java.util.Map;

@Service
public class RpcSoapService {

    public RpcResult callRpc(
            String endpoint,
            String operation,
            String namespace,
            String soapAction,
            String payload,
            Map<String, String> paramsMap 
    ) {
        RpcResult result = new RpcResult();

        if (payload == null && paramsMap != null) {
            payload = buildXmlFromParams(operation, paramsMap);
        }

        String nsPrefix = "ns1";
        String methodElement =
            "<" + nsPrefix + ":" + operation + " xmlns:" + nsPrefix + "=\"" + namespace + "\">" +
                payload +
            "</" + nsPrefix + ":" + operation + ">";

        String soapEnvelope =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                    methodElement +
                "</soapenv:Body>" +
            "</soapenv:Envelope>";

        result.setRawRequest(soapEnvelope);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/xml");
        headers.add(HttpHeaders.ACCEPT, "text/xml");
        headers.add("SOAPAction", soapAction != null ? soapAction : "");

        try {
            HttpEntity<String> request = new HttpEntity<>(soapEnvelope, headers);
            RestTemplate restTemplate = new RestTemplate();
            String responseXml = restTemplate.postForObject(endpoint, request, String.class);
            result.setRawResponse(responseXml);
        } catch (Exception e) {
            result.setError(e.getMessage());
        }

        return result;
    }

    public String buildXmlFromParams(String operation, Map<String, String> params) {
        StringBuilder xml = new StringBuilder();
        xml.append("<").append(operation).append(">");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            xml.append("<").append(entry.getKey()).append(">");
            xml.append(entry.getValue());
            xml.append("</").append(entry.getKey()).append(">");
        }
        xml.append("</").append(operation).append(">");
        return xml.toString();
    }
}