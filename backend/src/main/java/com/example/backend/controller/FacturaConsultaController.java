package com.example.backend.controller;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.example.backend.exception.XmlParsingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.backend.dto.FacturaConsultaRequestDto;
import com.example.backend.service.FacturaConsultaService;

@RestController
@RequestMapping("/api/facturas")
public class FacturaConsultaController {
    @Autowired
    private FacturaConsultaService facturaConsultaService;

    @PostMapping("/consulta")
    public ResponseEntity<?> consultaFacturas(@RequestBody FacturaConsultaRequestDto request) {
        try {
            String smlInput = facturaConsultaService.buildSmlInput(request);
            String smlResponse = facturaConsultaService.sendSmlRequest(smlInput, request.getWebserviceUrl());

            String sml = extractSmlFromSoap(smlResponse);
            if (sml == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Respuesta SOAP inválida");
            }

            Document doc = parseXmlResponse(sml);
            String exito = getTagValue(doc, "exito");
            String desc = getTagValue(doc, "desc");

            return handleResponse(exito, desc, doc);
            
        } catch (XmlParsingException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("XML parsing error: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    private DocumentBuilder initializeDocumentBuilder() throws XmlParsingException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbFactory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return dbFactory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new XmlParsingException("Failed to initialize XML document builder with security configuration", ex);
        }
    }

    private Document parseXmlResponse(String sml) throws XmlParsingException {
        try {
            DocumentBuilder dBuilder = initializeDocumentBuilder();
            return dBuilder.parse(new java.io.ByteArrayInputStream(sml.getBytes()));
        } catch (org.xml.sax.SAXException | java.io.IOException ex) {
            throw new XmlParsingException("Failed to parse XML response", ex);
        }
    }

    private ResponseEntity<?> handleResponse(String exito, String desc, Document doc) {
        if ("-1".equals(exito)) {
            java.util.List<java.util.Map<String, String>> facturas = extractFacturasFromDocument(doc);
            if (facturas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }
            return ResponseEntity.ok(facturas);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(desc != null ? desc : "Error desconocido");
        }
    }

    private java.util.List<java.util.Map<String, String>> extractFacturasFromDocument(Document doc) {
        java.util.List<java.util.Map<String, String>> facturas = new java.util.ArrayList<>();
        NodeList facturaNodes = doc.getElementsByTagName("factura");
        
        for (int i = 0; i < facturaNodes.getLength(); i++) {
            Node facturaNode = facturaNodes.item(i);
            java.util.Map<String, String> facturaMap = new java.util.HashMap<>();
            NodeList children = facturaNode.getChildNodes();
            
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    facturaMap.put(child.getNodeName(), child.getTextContent());
                }
            }
            facturas.add(facturaMap);
        }
        return facturas;
    }

    private String extractSmlFromSoap(String soap) {
        if (soap == null || soap.isBlank()) {
            return null;
        }

        int start = soap.indexOf("<servicioReturn");
        if (start < 0) {
            return null;
        }

        int openTagEnd = soap.indexOf(">", start);
        if (openTagEnd < 0) {
            return null;
        }

        int end = soap.indexOf("</servicioReturn>", openTagEnd + 1);
        if (end <= openTagEnd) {
            return null;
        }

        return soap.substring(openTagEnd + 1, end)
            .replace("&lt;", "<")
            .replace("&gt;", ">");
    }

    private String getTagValue(Document doc, String tag) {
        NodeList nodes = doc.getElementsByTagName(tag);
        if (nodes.getLength() > 0 && nodes.item(0).getFirstChild() != null) {
            return nodes.item(0).getFirstChild().getNodeValue();
        }
        return null;
    }
}