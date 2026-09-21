package com.example.backend.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.example.backend.dto.Tercero;
import com.example.backend.exception.XmlParsingException;
import com.example.sical.CryptoSical;

@Service
public class SicalService {

  @Value("${sical.ws.url}")           
    private String wsUrl;
    
    @Value("${sical.username}")
    private String username;
    
    @Value("${sical.password}")
    private String password;
    
    @Value("${sical.public.key}")
    private String publicKey;

    public List<Tercero> getTerceros(String nif, String nom, String codigo, String orgCode, String entidad, String eje) throws XmlParsingException {
      try {
        CryptoSical.SecurityFields sec = CryptoSical.calculateSecurityFields(publicKey);

        String fecha = sec.created;
        String nonce = sec.nonce;
        String token = sec.token;
        String tokenSha1 = CryptoSical.encodeSha1Base64(sec.origin);

        String xml =
            "<e>" +
                "<ope><apl>SNP</apl><tobj>TercerosyCuentas</tobj><cmd>LST</cmd><ver>2.0</ver></ope>" +
                "<sec>" +
                "<cli>SAGE-AYTOS</cli>" +
                "<org>" + orgCode + "</org>" +
                "<ent>" + entidad + "</ent>" +
                "<eje>" + eje + "</eje>" +
                "<usu>" + username + "</usu>" +
                "<pwd>" + CryptoSical.encodeSha1Base64(password) + "</pwd>" +
                "<fecha>" + fecha + "</fecha>" +
                "<nonce>" + nonce + "</nonce>" +
                "<token>" + token + "</token>" +
                "<tokenSha1>" + tokenSha1 + "</tokenSha1>" +
                "</sec>" +
                "<par>" +
                "<ruta></ruta>" +                                      
                "<l_tercero>" +
                    "<tercero>" +
                    "<portal>S</portal>" + 
                    (nif   != null ? "<NIFtercero>"   + CryptoSical.encodeBase64(nif)   + "</NIFtercero>"   : "") +
                    (nom   != null ? "<nomTercero>"   + CryptoSical.encodeBase64("%" + nom + "%")   + "</nomTercero>"   : "") +
                    (codigo != null ? "<idenTercero>" + codigo + "</idenTercero>" : "") +
                    "<indice>0</indice>" +
                    "</tercero>" +
                "</l_tercero>" +
                "</par>"+
            "</e>";

            String soapEnvelope =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:impl=\"http://desa-sical-ws:8080/services/Ci\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<impl:servicio>" +
                    "<impl:in0><![CDATA[" + xml + "]]></impl:in0>" +
                "</impl:servicio>" +
                "</soapenv:Body>" +
            "</soapenv:Envelope>";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/xml");
        headers.add(HttpHeaders.ACCEPT, "text/xml");
        headers.add("SOAPAction", "");

        HttpEntity<String> request = new HttpEntity<>(soapEnvelope, headers);

        RestTemplate restTemplate = new RestTemplate();
        String responseXml = restTemplate.postForObject(wsUrl, request, String.class);

        return parseTerceros(responseXml);
        } catch (XmlParsingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XmlParsingException("Error retrieving terceros: " + ex.getMessage(), ex);
        }
    }

    private List<Tercero> parseTerceros(String xml) throws XmlParsingException {
        List<Tercero> result = new ArrayList<>();
        
        try {
            String sml = extractAndUnescapeXmlContent(xml);
            Document doc = parseXmlDocument(sml);
            NodeList tercerosNodes = doc.getElementsByTagName("tercero");

            for (int i = 0; i < tercerosNodes.getLength(); i++) {
                Tercero t = parseTerceroFromElement((Element) tercerosNodes.item(i));
                result.add(t);
            }
        } catch (XmlParsingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XmlParsingException("Error parsing terceros from XML response", ex);
        }

        return result;
    }

    private String extractAndUnescapeXmlContent(String xml) {
        String inner = extractSoapContent(xml);
        return unescapeXmlEntities(inner);
    }

    private String extractSoapContent(String xml) {
        if (xml == null) return "";
        
        int start = xml.indexOf("<servicioReturn");
        if (start < 0) return xml;
        
        int gt = xml.indexOf(">", start);
        int end = xml.indexOf("</servicioReturn>", gt);
        
        if (gt >= 0 && end >= 0) {
            return xml.substring(gt + 1, end);
        }
        return xml;
    }

    private String unescapeXmlEntities(String content) {
        if (content == null || content.isEmpty()) return "";
        
        return content
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'");
    }

    private Document parseXmlDocument(String sml) throws com.example.backend.exception.XmlParsingException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(sml.getBytes(StandardCharsets.UTF_8)));
        } catch (javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException | java.io.IOException ex) {
            throw new com.example.backend.exception.XmlParsingException("Failed to parse XML document", ex);
        }
    }

    private Tercero parseTerceroFromElement(Element e) {
        Tercero t = new Tercero();
        
        NodeList detterNodes = e.getElementsByTagName("detter");
        if (detterNodes.getLength() > 0) {
            populateTerceroFromDetter(t, detterNodes.item(0).getTextContent());
            deriveMissingApellido(t);
        } else {
            populateTerceroFromTags(t, e);
        }
        
        return t;
    }

    private void populateTerceroFromDetter(Tercero t, String detter) {
        String[] parts = detter.split(Pattern.quote("-@-"), -1);

        t.setIdenTercero(unescapePart(parts, 0));
        t.setNIFtercero(unescapePart(parts, 1));
        t.setTipoDocumento(unescapePart(parts, 2));
        t.setAlias(unescapePart(parts, 3));
        t.setNomTercero(unescapePart(parts, 4));
        t.setDomicilio(unescapePart(parts, 5));
        t.setPoblacion(unescapePart(parts, 6));
        t.setCodigoPostal(unescapePart(parts, 7));
        t.setProvincia(unescapePart(parts, 8));
        t.setTelefono(unescapePart(parts, 9));
        t.setFax(unescapePart(parts, 10));
        t.setTipoTercero(unescapePart(parts, 11));
        t.setObservaciones(unescapePart(parts, 18));
        t.setEmbargado(unescapePart(parts, 19));
        t.setEmail(unescapePart(parts, 20));
        t.setNombreCompleto(unescapePart(parts, 21));
        t.setApellido1(unescapePart(parts, 22));
        t.setApellido2(unescapePart(parts, 23));
    }

    private void populateTerceroFromTags(Tercero t, Element e) {
        t.setIdenTercero(getTagValue(e, "idenTercero"));
        t.setNIFtercero(getTagValue(e, "NIFtercero"));
        t.setNomTercero(getTagValue(e, "nomTercero"));
        t.setApellTercero(getTagValue(e, "apellTercero"));
    }

    private void deriveMissingApellido(Tercero t) {
        if (t.getApellTercero() != null && !t.getApellTercero().isEmpty()) {
            return;
        }

        String a1 = t.getApellido1();
        String a2 = t.getApellido2();

        if ((a1 != null && !a1.isEmpty()) || (a2 != null && !a2.isEmpty())) {
            String combined = ((a1 == null ? "" : a1) + " " + (a2 == null ? "" : a2)).trim();
            t.setApellTercero(combined);
            return;
        }

        if (t.getNombreCompleto() != null && !t.getNombreCompleto().isEmpty()) {
            parseNombreCompleto(t);
        }
    }

    private void parseNombreCompleto(Tercero t) {
        String[] nm = t.getNombreCompleto().trim().split("\\s+");
        if (nm.length > 1) {
            t.setApellTercero(nm[nm.length - 1]);
            t.setNomTercero(String.join(" ", Arrays.copyOfRange(nm, 0, nm.length - 1)));
        } else {
            t.setNomTercero(t.getNombreCompleto());
        }
    }

   private String getTagValue(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private String unescapePart(String[] parts, int idx) {
        if (idx < 0 || idx >= parts.length) return null;
        String v = parts[idx];
        if (v == null) return null;
        return StringEscapeUtils.unescapeXml(v.trim());
    }
}
