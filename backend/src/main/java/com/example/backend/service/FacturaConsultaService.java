package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.backend.dto.FacturaConsultaRequestDto;
import com.example.backend.exception.SmlProcessingException;
import com.example.sical.CryptoSical;


@Service
public class FacturaConsultaService {
    @Value("${sical.ws.url:http://desa-sical-ws:8080/services/Ci?wsdl}")
    private String sicalWsUrl;
    
    public String buildSmlInput(FacturaConsultaRequestDto req) {
        try {
            String org = req.getOrg();
            String ent = req.getEnt();
            String eje = req.getEje();
            String usu = req.getUsu();
            String pwd = req.getPwd();
            String publicKey = req.getPublicKey();

            CryptoSical.SecurityFields sec = CryptoSical.calculateSecurityFields(publicKey);

            String fecha = sec.created;
            String nonce = sec.nonce;
            String token = sec.token;
            String tokenSha1 = CryptoSical.encodeSha1Base64(sec.origin);
            String pwdSha1Base64 = CryptoSical.encodeSha1Base64(pwd);

            StringBuilder sb = new StringBuilder();
            sb.append("<e>");
            sb.append("<ope>");
            sb.append("<apl>SNP</apl>");
            sb.append("<tobj>Justificantes</tobj>");
            sb.append("<cmd>LST</cmd>");
            sb.append("<ver>2.0</ver>");
            sb.append("</ope>");
            sb.append("<sec>");
            sb.append("<cli>SAGE-AYTOS</cli>");
            sb.append("<org>").append(org).append("</org>");
            sb.append("<ent>").append(ent).append("</ent>");
            sb.append("<eje>").append(eje).append("</eje>");
            sb.append("<usu>").append(usu).append("</usu>");
            sb.append("<pwd>").append(pwdSha1Base64).append("</pwd>");
            sb.append("<fecha>").append(fecha).append("</fecha>");
            sb.append("<nonce>").append(nonce).append("</nonce>");
            sb.append("<token>").append(token).append("</token>");
            sb.append("<tokenSha1>").append(tokenSha1).append("</tokenSha1>");
            sb.append("</sec>");
            sb.append("<par>");
            if (req.getTipoRegistro() != null) sb.append("<tipoRegistro>").append(CryptoSical.encodeBase64(req.getTipoRegistro())).append("</tipoRegistro>");
            if (req.getTipoDocumento() != null) sb.append("<tipoDocumento>").append(req.getTipoDocumento()).append("</tipoDocumento>");
            if (req.getCge() != null) sb.append("<cge>").append(CryptoSical.encodeBase64(req.getCge())).append("</cge>");
            if (req.getSituacionIgual() != null) sb.append("<situacionIgual>").append(CryptoSical.encodeBase64(req.getSituacionIgual())).append("</situacionIgual>");
            if (req.getEstado() != null) sb.append("<estado>").append(CryptoSical.encodeBase64(req.getEstado())).append("</estado>");
            if (req.getTercero() != null) sb.append("<tercero>").append(CryptoSical.encodeBase64(req.getTercero())).append("</tercero>");
            if (req.getDocProveedor() != null) sb.append("<docProveedor>").append(CryptoSical.encodeBase64(req.getDocProveedor())).append("</docProveedor>");
            if (req.getFecRegDesde() != null) sb.append("<fecRegDesde>").append(formatDate(req.getFecRegDesde())).append("</fecRegDesde>");
            if (req.getFecRegHasta() != null) sb.append("<fecRegHasta>").append(formatDate(req.getFecRegHasta())).append("</fecRegHasta>");
            if (req.getFecDocDesde() != null) sb.append("<fecDocDesde>").append(formatDate(req.getFecDocDesde())).append("</fecDocDesde>");
            if (req.getFecDocHasta() != null) sb.append("<fecDocHasta>").append(formatDate(req.getFecDocHasta())).append("</fecDocHasta>");
            sb.append("</par>");
            sb.append("</e>");
            return sb.toString();
        } catch (Exception ex) {
            throw new SmlProcessingException("Error al generar la entrada SML: " + ex.getMessage(), ex);
        }
    }

    public String sendSmlRequest(String smlInput, String url) {
        String endpoint = (url != null && !url.isEmpty()) ? url : sicalWsUrl;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.add("SOAPAction", ""); 

        String soapEnvelope =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Body>" +
            "<ns1:servicio xmlns:ns1=\"http://desa-sical-ws:8080/services/Ci\">" +
            "<in0><![CDATA[" + smlInput + "]]></in0>" +
            "</ns1:servicio>" +
            "</soapenv:Body>" +
            "</soapenv:Envelope>";

        HttpEntity<String> entity = new HttpEntity<>(soapEnvelope, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
        return response.getBody();
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}