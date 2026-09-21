package com.example.backend.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.backend.dto.Operaciones;
import com.example.backend.dto.Operaciones.Dto;
import com.example.backend.dto.Operaciones.Iva;
import com.example.backend.dto.Operaciones.Linea;
import com.example.backend.dto.Operaciones.Relacion;
import com.example.sical.CryptoSical;

@Service
public class OperacionesService {

    @Value("${sical.ws.url}")
    private String wsUrl;

    @Value("${sical.username}")
    private String username;

    @Value("${sical.password}")
    private String password;

    @Value("${sical.public.key}")
    private String publicKey;

    public List<Operaciones> getOperaciones(
        String orgCode,
        String entidad,
        String numeroOperDesde,
        String numeroOperHasta,
        String codigoOperacion,
        String organica,
        String funcional,
        String economica,
        String referencia,
        String expediente,
        String grupoApunte,
        String oficina,
        String eje ) throws Exception {
        return getOperaciones(
                orgCode,
                entidad,
                numeroOperDesde,
                numeroOperHasta,
                codigoOperacion,
                organica,
                funcional,
                economica,
                referencia,
                expediente,
                grupoApunte,
                oficina,
                null,
                null,
                eje);
    }

    public List<Operaciones> getOperaciones(
        String orgCode,
        String entidad,
        String numeroOperDesde,
        String numeroOperHasta,
        String codigoOperacion,
        String organica,
        String funcional,
        String economica,
        String referencia,
        String expediente,
        String grupoApunte,
        String oficina,
        String indice,
        String numRegDev,
        String eje ) throws Exception {
            if (eje == null || eje.isBlank()) {
                throw new IllegalArgumentException("eje is required");
            }

        CryptoSical.SecurityFields sec = CryptoSical.calculateSecurityFields(publicKey);
        String fecha = sec.created;
        String nonce = sec.nonce;
        String token = sec.token;
        String tokenSha1 = CryptoSical.encodeSha1Base64(sec.origin);

        String filtroXml =
            "<filtro>" +
            (numeroOperDesde != null ? "<numeroOperDesde>" + numeroOperDesde + "</numeroOperDesde>" : "") +
            (numeroOperHasta != null ? "<numeroOperHasta>" + numeroOperHasta + "</numeroOperHasta>" : "") +
            (codigoOperacion != null ? "<codigoOperacion>" + CryptoSical.encodeBase64(codigoOperacion) + "</codigoOperacion>" : "") +
            (organica != null ? "<organica>" + CryptoSical.encodeBase64(organica) + "</organica>" : "") +
            (funcional != null ? "<funcional>" + CryptoSical.encodeBase64(funcional) + "</funcional>" : "") +
            (economica != null ? "<economica>" + CryptoSical.encodeBase64(economica) + "</economica>" : "") +
            (referencia != null ? "<referencia>" + referencia + "</referencia>" : "") +
            (expediente != null ? "<expediente>" + CryptoSical.encodeBase64(expediente) + "</expediente>" : "") +
            (grupoApunte != null ? "<grupoApunte>" + CryptoSical.encodeBase64(grupoApunte) + "</grupoApunte>" : "") +
            (oficina != null ? "<oficina>" + CryptoSical.encodeBase64(oficina) + "</oficina>" : "") +
            (indice != null ? "<indice>" + indice + "</indice>" : "") +
            (numRegDev != null ? "<NumRegDev>" + numRegDev + "</NumRegDev>" : "") +
            "</filtro>";

        String xml =
            "<e>" +
            "<ope><apl>SNP</apl><tobj>ConOpeGastos</tobj><cmd>LST</cmd><ver>2.0</ver></ope>" +
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
            "<desdetalle>S</desdetalle>" +
            filtroXml +
            "</par>" +
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

        RestTemplate restTemplate = new RestTemplate();
        String endpoint = (wsUrl != null && wsUrl.contains("?")) ? wsUrl.substring(0, wsUrl.indexOf("?")) : wsUrl;
        System.out.println("REQUEST XML: " + soapEnvelope);
        String responseXml = restTemplate.postForObject(endpoint, new HttpEntity<>(soapEnvelope, headers), String.class);
        System.out.println("RESPONSE XML: " + responseXml);

        return parseOperaciones(responseXml);
    }

    private List<Operaciones> parseOperaciones(String xml) throws Exception {
        List<Operaciones> result = new ArrayList<>();

        String inner = null;
        int start = xml != null ? xml.indexOf("<servicioReturn") : -1;
        if (start >= 0) {
            int gt = xml.indexOf(">", start);
            int end = xml.indexOf("</servicioReturn>", gt);
            if (gt >= 0 && end >= 0) {
                inner = xml.substring(gt + 1, end);
            }
        }
        if (inner == null) inner = xml == null ? "" : xml;

        
        String sml = inner
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(sml.getBytes(StandardCharsets.UTF_8)));

            NodeList operNodes = findElementsByName(doc, "operacion");
            for (int i = 0; i < operNodes.getLength(); i++) {
                Element opEl = (Element) operNodes.item(i);
                Operaciones op = new Operaciones();

                op.setNumope(toLong(getTagValue(opEl, "numope")));
                op.setCodope(decodeOrNull(getTagValue(opEl, "codope")));
                op.setSigno(decodeOrNull(getTagValue(opEl, "signo")));
                op.setFase(decodeOrNull(getTagValue(opEl, "fase")));
                op.setArea(decodeOrNull(getTagValue(opEl, "area")));
                op.setAgrupacion(decodeOrNull(getTagValue(opEl, "agrupacion")));
                op.setNifter(decodeOrNull(getTagValue(opEl, "nifter")));
                op.setNifend(decodeOrNull(getTagValue(opEl, "nifend")));
                op.setCuenta(decodeOrNull(getTagValue(opEl, "cuenta")));
                op.setFechaentrada(getTagValue(opEl, "fechaentrada"));
                op.setFecope(getTagValue(opEl, "fecope"));
                op.setGapuntes(decodeOrNull(getTagValue(opEl, "gapuntes")));
                op.setDocumento(decodeOrNull(getTagValue(opEl, "documento")));
                op.setFechadocu(getTagValue(opEl, "fechadocu"));
                op.setOrdinal(decodeOrNull(getTagValue(opEl, "ordinal")));
                op.setFechapago(getTagValue(opEl, "fechapago"));
                op.setTipopago(decodeOrNull(getTagValue(opEl, "tipopago")));
                op.setTipoexp(decodeOrNull(getTagValue(opEl, "tipoexp")));
                op.setNexp(decodeOrNull(getTagValue(opEl, "nexp")));
                op.setFechaexp(getTagValue(opEl, "fechaexp"));
                op.setAreages(decodeOrNull(getTagValue(opEl, "areages")));
                op.setOficina(decodeOrNull(getTagValue(opEl, "oficina")));
                op.setImporte(toDouble(getTagValue(opEl, "importe")));
                op.setImpiva(toDouble(getTagValue(opEl, "impiva")));
                op.setImpdto(toDouble(getTagValue(opEl, "impdto")));
                op.setTexto(decodeOrNull(getTagValue(opEl, "texto")));
                op.setNumcaja(toLong(getTagValue(opEl, "numcaja")));
                op.setAnoprestamo(toInteger(getTagValue(opEl, "anoprestamo")));
                op.setTipoprestamo(decodeOrNull(getTagValue(opEl, "tipoprestamo")));
                op.setNumprestamo(decodeOrNull(getTagValue(opEl, "numprestamo")));
                op.setTerite(toLong(getTagValue(opEl, "terite")));
                op.setEndite(toLong(getTagValue(opEl, "endite")));
                op.setNumOpePrev(toLong(getTagValue(opEl, "NumOpePrev")));
                op.setTipContrato(decodeOrNull(getTagValue(opEl, "tipContrato")));
                op.setProContrato(decodeOrNull(getTagValue(opEl, "proContrato")));
                op.setCriContrato(decodeOrNull(getTagValue(opEl, "criContrato")));
                op.setNExpElec(decodeOrNull(getTagValue(opEl, "nExpElec")));

                op.setDtoList(parseDtoList(opEl));
                op.setIvaList(parseIvaList(opEl));
                op.setRelacionList(parseRelacionList(opEl));
                op.setLineaList(parseLineaList(opEl));

                if (op.getNumope() == null) {
                    NodeList lineaNodes = findElementsByName(opEl, "linea");
                    if (lineaNodes.getLength() > 0) {
                        String raw = lineaNodes.item(0).getTextContent();
                        if (raw != null && !raw.isBlank()) {
                            String[] p = raw.split(java.util.regex.Pattern.quote(SICAL_DELIM), -1);
                            op.setNumope(toLong(get(p, 0)));
                        }
                    }
                }

                result.add(op);
            }

            return result;
        } catch (Exception ex) {
            throw ex;
        }
    }

    private List<Dto> parseDtoList(Element opEl) {
        List<Dto> list = new ArrayList<>();
        NodeList dtoNodes = findElementsByName(opEl, "dto");
        for (int i = 0; i < dtoNodes.getLength(); i++) {
            Element dtoEl = (Element) dtoNodes.item(i);
            Dto dto = new Dto();
            dto.setNumdto(toLong(getTagValue(dtoEl, "numdto")));
            dto.setDtocuenta(decodeOrNull(getTagValue(dtoEl, "dtocuenta")));
            dto.setDtoeje(toInteger(getTagValue(dtoEl, "dtoeje")));
            dto.setDtoorg(decodeOrNull(getTagValue(dtoEl, "dtoorg")));
            dto.setDtofun(decodeOrNull(getTagValue(dtoEl, "dtofun")));
            dto.setDtoeco(decodeOrNull(getTagValue(dtoEl, "dtoeco")));
            dto.setDtoimp(toDouble(getTagValue(dtoEl, "dtoimp")));
            dto.setDtosaldo(toDouble(getTagValue(dtoEl, "dtosaldo")));
            dto.setDtobase(toDouble(getTagValue(dtoEl, "dtobase")));
            dto.setDtosaldobase(toDouble(getTagValue(dtoEl, "dtosaldobase")));
            dto.setDtopretencion(toDouble(getTagValue(dtoEl, "dtopretencion")));
            dto.setDtonumopecan(toDouble(getTagValue(dtoEl, "dtonumopecan")));
            dto.setDtolinopecan(toLong(getTagValue(dtoEl, "dtolinopecan")));
            dto.setDtotipoopecan(decodeOrNull(getTagValue(dtoEl, "dtotipoopecan")));
            dto.setDtotexto(decodeOrNull(getTagValue(dtoEl, "dtotexto")));
            dto.setDtonif(decodeOrNull(getTagValue(dtoEl, "dtonif")));
            dto.setDtoanodevengo(toInteger(getTagValue(dtoEl, "dtoanodevengo")));
            dto.setDtoclave(decodeOrNull(getTagValue(dtoEl, "dtoclave")));
            dto.setDtolobtencion(decodeOrNull(getTagValue(dtoEl, "dtolobtencion")));
            dto.setDtocargo(decodeOrNull(getTagValue(dtoEl, "dtocargo")));
            dto.setDtobase1(toDouble(getTagValue(dtoEl, "dtobase1")));
            dto.setDtobase2(toDouble(getTagValue(dtoEl, "dtobase2")));
            dto.setDtobase3(toDouble(getTagValue(dtoEl, "dtobase3")));
            dto.setDtoiva1(toDouble(getTagValue(dtoEl, "dtoiva1")));
            dto.setDtoiva2(toDouble(getTagValue(dtoEl, "dtoiva2")));
            dto.setDtoiva3(toDouble(getTagValue(dtoEl, "dtoiva3")));
            dto.setDtotiva1(toDouble(getTagValue(dtoEl, "dtotiva1")));
            dto.setDtotiva2(toDouble(getTagValue(dtoEl, "dtotiva2")));
            dto.setDtotiva3(toDouble(getTagValue(dtoEl, "dtotiva3")));
            dto.setDtoporcent1(toDouble(getTagValue(dtoEl, "dtoporcent1")));
            dto.setDtoporcent2(toDouble(getTagValue(dtoEl, "dtoporcent2")));
            dto.setDtoporcent3(toDouble(getTagValue(dtoEl, "dtoporcent3")));
            list.add(dto);
        }
        return list;
    }

    private List<Iva> parseIvaList(Element opEl) {
        List<Iva> list = new ArrayList<>();
        NodeList ivaNodes = findElementsByName(opEl, "iva");
        for (int i = 0; i < ivaNodes.getLength(); i++) {
            Element ivaEl = (Element) ivaNodes.item(i);
            Iva iva = new Iva();
            iva.setIvabase1(toDouble(getTagValue(ivaEl, "ivabase1")));
            iva.setIvabase2(toDouble(getTagValue(ivaEl, "ivabase2")));
            iva.setIvabase3(toDouble(getTagValue(ivaEl, "ivabase3")));
            iva.setIvasbase1(toDouble(getTagValue(ivaEl, "ivasbase1")));
            iva.setIvasbase2(toDouble(getTagValue(ivaEl, "ivasbase2")));
            iva.setIvasbase3(toDouble(getTagValue(ivaEl, "ivasbase3")));
            iva.setIvativa1(toDouble(getTagValue(ivaEl, "ivativa1")));
            iva.setIvativa2(toDouble(getTagValue(ivaEl, "ivativa2")));
            iva.setIvativa3(toDouble(getTagValue(ivaEl, "ivativa3")));
            iva.setIvaporcent1(toDouble(getTagValue(ivaEl, "ivaporcent1")));
            iva.setIvaporcent2(toDouble(getTagValue(ivaEl, "ivaporcent2")));
            iva.setIvaporcent3(toDouble(getTagValue(ivaEl, "ivaporcent3")));
            iva.setIvaimp1(toDouble(getTagValue(ivaEl, "ivaimp1")));
            iva.setIvaimp2(toDouble(getTagValue(ivaEl, "ivaimp2")));
            iva.setIvaimp3(toDouble(getTagValue(ivaEl, "ivaimp3")));
            iva.setIvabexenta(toDouble(getTagValue(ivaEl, "ivabexenta")));
            iva.setIvaciv1(decodeOrNull(getTagValue(ivaEl, "ivaciv1")));
            iva.setIvaciv2(decodeOrNull(getTagValue(ivaEl, "ivaciv2")));
            list.add(iva);
        }
        return list;
    }

    private static final String SICAL_DELIM = "-@-";

    private List<Linea> parseLineaList(Element opEl) {
        List<Linea> list = new ArrayList<>();
        NodeList lineaNodes = findElementsByName(opEl, "linea");
        for (int i = 0; i < lineaNodes.getLength(); i++) {
            String raw = lineaNodes.item(i).getTextContent();
            if (raw == null || raw.isBlank()) continue;
            String[] p = raw.split(java.util.regex.Pattern.quote(SICAL_DELIM), -1);

            Linea linea = new Linea();
            linea.setNlinea(toInteger(get(p, 1)));
            linea.setOpeasc(toLong(get(p, 2)));
            linea.setLineasc(toLong(get(p, 3)));
            linea.setLincta(get(p, 4));    
            linea.setPrya(toInteger(get(p, 5)));
            linea.setPryt(get(p, 6));          
            linea.setPryo(get(p, 7));           
            linea.setPryn(get(p, 8));           
            linea.setPryx(toInteger(get(p, 9)));
            linea.setLineje(toInteger(get(p, 10)));
            linea.setLinorg(get(p, 11));        
            linea.setLinfun(get(p, 12));        
            linea.setLineco(get(p, 13));        
            linea.setReferencia(toLong(get(p, 14)));
            linea.setLimporte(toDouble(get(p, 15)));
            linea.setSaldo(toDouble(get(p, 16)));
            linea.setSaldop(toDouble(get(p, 17)));
            linea.setLincte(get(p, 18));         
            linea.setLinpam(get(p, 19));         
            list.add(linea);
        }
        return list;
    }

    private List<Relacion> parseRelacionList(Element opEl) {
        List<Relacion> list = new ArrayList<>();
        NodeList relNodes = findElementsByName(opEl, "Relacion");
        for (int i = 0; i < relNodes.getLength(); i++) {
            String raw = relNodes.item(i).getTextContent();
            if (raw == null || raw.isBlank()) continue;
            String[] parts = raw.split(java.util.regex.Pattern.quote(SICAL_DELIM), -1);

            Relacion rel = new Relacion();
            rel.setTipoRelacion(get(parts, 1)); 
            rel.setAnnoRelacion(toInteger(get(parts, 2)));
            rel.setOrdenRelacion(toInteger(get(parts, 3)));
            list.add(rel);
        }
        return list;
    }

    private String get(String[] arr, int idx) {
        return (idx >= 0 && idx < arr.length && !arr[idx].isBlank()) ? arr[idx] : null;
    }
    
    private String getTagValue(Element parent, String tagName) {
        NodeList nodes = findElementsByName(parent, tagName);
        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }
        Node node = nodes.item(0);
        return node != null ? node.getTextContent() : null;
    }

    private NodeList findElementsByName(Node parent, String tagName) {
        List<Node> matches = new ArrayList<>();
        collectElementsByName(parent, tagName, matches);
        return new NodeList() {
            @Override
            public Node item(int index) {
                return (index >= 0 && index < matches.size()) ? matches.get(index) : null;
            }

            @Override
            public int getLength() {
                return matches.size();
            }
        };
    }

    private void collectElementsByName(Node node, String tagName, List<Node> matches) {
        if (node == null || tagName == null) {
            return;
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String childName = normalizeTagName(child.getNodeName());
            if (tagName.equalsIgnoreCase(childName)) {
                matches.add(child);
            }

            collectElementsByName(child, tagName, matches);
        }
    }

    private String normalizeTagName(String name) {
        if (name == null) {
            return "";
        }

        String normalized = name;
        int colonIndex = normalized.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 1 < normalized.length()) {
            normalized = normalized.substring(colonIndex + 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private Double toDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        String normalized = value.replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private Long toLong(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer toInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String decodeOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return CryptoSical.decodeBase64(value);
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }
}