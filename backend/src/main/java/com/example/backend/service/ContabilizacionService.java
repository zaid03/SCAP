package com.example.backend.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.backend.sqlserver2.model.Cog;
import com.example.backend.sqlserver2.repository.CogRepository;
import com.example.backend.dto.ContabilizacionRequestDto;
import com.example.backend.dto.ContabilizacionResponseDto;
import com.example.backend.dto.Operaciones;
import com.example.backend.exception.SmlBuildingException;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.Fdt;
import com.example.sical.CryptoSical;

@Service
public class ContabilizacionService {
    @Value("${sical.ws.url:http://desa-sical-ws:8080/services/Ci}")
    private String sicalWsUrl;
    @Autowired
    private OperacionesService operacionesService;
    @Autowired
    private CogRepository cogRepository;

    private double nz(Double d) { return d == null ? 0.0 : d; }
    private Double round2(double d) { return Math.round(d * 100.0) / 100.0; }

    /** Reads <l_linea><saldo> from WS 2.49 for one operation. */
    private Double consultarSaldo(String orgCode, String entidad, String eje, String ope, String ref, String org, String fun, String eco) throws Exception {
        System.out.println("[CONTRATO] consultarSaldo -> ope=" + ope + " ref=" + ref
            + " org=" + org + " fun=" + fun + " eco=" + eco + " eje=" + eje);
        List<Operaciones> res = operacionesService.getOperaciones(orgCode, entidad, ope, ope, null, org, fun, eco, ref, null, null, null, eje);

                    System.out.println("[CONTRATO] consultarSaldo: operaciones devueltas = " + (res == null ? "null" : res.size()));

        if (res == null || res.size() != 1) {
            throw new SmlBuildingException("WS 2.49: se esperaba 1 operacion para " + ope
                    + " y llegaron " + (res == null ? 0 : res.size()));
        }
        List<Operaciones.Linea> lineas = res.get(0).getLineaList();
            System.out.println("[CONTRATO] consultarSaldo: lineas devueltas = " + (lineas == null ? "null" : lineas.size()));

        if (lineas == null || lineas.size() != 1) {
            throw new SmlBuildingException("WS 2.49: se esperaba 1 linea para " + ope);
        }
        Double saldo = lineas.get(0).getSaldo();   // <-- assumes getSaldo() exists on Operaciones.Linea
            System.out.println("[CONTRATO] consultarSaldo: saldo = " + saldo + " (limporte=" + lineas.get(0).getLimporte() + ")");

        if (saldo == null) {
            throw new SmlBuildingException("WS 2.49: saldo vacio para " + ope);
        }
        return saldo;
    }

    /**
     * _ESCONTRATO = TRUE: rebalances the two FDE lines so line 1 never exceeds the WS saldo.
     * Returns KImporteTotal.
     */
    public record ContratoPrep(double kImporteTotal, Fde lineaPrincipal) {}
    public ContratoPrep prepararLineasContrato(ContabilizacionRequestDto req, List<Fde> fdeList) throws Exception {    
         System.out.println("[CONTRATO] ===== prepararLineasContrato INICIO =====");
    System.out.println("[CONTRATO] numero de lineas FDE = " + fdeList.size());    
        if (fdeList.size() != 2) {
            throw new SmlBuildingException("Contrato: se esperaban 2 lineas FDE y hay " + fdeList.size());
        }

        Fde l1 = null, l2 = null;
        for (int i = 0; i < fdeList.size(); i++) {
            Fde f = fdeList.get(i);
            boolean tieneImporte = (nz(f.getFDEIMP()) + nz(f.getFDEDIF())) > 0 && f.getFDEECO() != null;
            if (tieneImporte && l1 == null) l1 = f; else l2 = f;
            System.out.println("[CONTRATO] FDE[" + i + "] org=" + f.getFDEORG()
                + " fun=" + f.getFDEFUN()
                + " eco=" + f.getFDEECO()
                + " ope=" + f.getFDEOPE()
                + " ref=" + f.getFDEREF()
                + " imp=" + f.getFDEIMP()
                + " dif=" + f.getFDEDIF());
        }
        if (l1 == null || l2 == null) {
            throw new SmlBuildingException("Contrato: no se pudo identificar la linea con importe y la de reserva");
        }

         System.out.println("[CONTRATO] linea 1 (con importe): ope=" + l1.getFDEOPE() + " eco=" + l1.getFDEECO());
    System.out.println("[CONTRATO] linea 2 (reserva):     ope=" + l2.getFDEOPE() + " eco=" + l2.getFDEECO());


        // 1. KImporteTotal
        double kImporteTotal = nz(l1.getFDEIMP()) + nz(l1.getFDEDIF());

            System.out.println("[CONTRATO] KImporteTotal = " + kImporteTotal);

        // 2. Saldo of line 1's operation
        double saldo = consultarSaldo(req.getOrg(), req.getEnt(), req.getEje(),
                String.valueOf(l1.getFDEOPE()),
                l1.getFDEREF() != null ? String.valueOf(l1.getFDEREF()) : null,
                l1.getFDEORG(), l1.getFDEFUN(), l1.getFDEECO());

                    System.out.println("[CONTRATO] saldo linea 1 = " + saldo + " | KImporteTotal = " + kImporteTotal);

        if (saldo >= kImporteTotal) {
            return new ContratoPrep(kImporteTotal, l1);
        }

        // 1.1 line 2 gets the same economic code as line 1
        l2.setFDEECO(l1.getFDEECO());

        System.out.println("[CONTRATO] saldo INSUFICIENTE -> se divide entre linea 1 y linea 2");
    l2.setFDEECO(l1.getFDEECO());
    System.out.println("[CONTRATO] linea 2 FDEECO puesto a " + l2.getFDEECO());
        // 1.2.1 missing amount
        double kImpFalta = kImporteTotal - saldo;
        double dif1 = nz(l1.getFDEDIF());
        double imp1 = nz(l1.getFDEIMP());

            System.out.println("[CONTRATO] kImpFalta = " + kImpFalta + " | dif1 = " + dif1 + " | imp1 = " + imp1);

        if (kImpFalta <= dif1) {
            // 1.2.2 take it all from FDEDIF
                    System.out.println("[CONTRATO] caso 1.2.2: todo sale de FDEDIF");

            l1.setFDEDIF(round2(dif1 - kImpFalta));
            l2.setFDEDIF(round2(kImpFalta));
        } else {
                    System.out.println("[CONTRATO] caso 1.2.3: todo FDEDIF + resto de FDEIMP");

            // 1.2.3 take all FDEDIF + the rest from FDEIMP
            double resto = kImpFalta - dif1;
            l2.setFDEIMP(round2(resto));
            l2.setFDEDIF(round2(dif1));
            l1.setFDEIMP(round2(imp1 - resto));
            l1.setFDEDIF(0.0);
        }

        double total1 = nz(l1.getFDEIMP()) + nz(l1.getFDEDIF());
    double total2 = nz(l2.getFDEIMP()) + nz(l2.getFDEDIF());
    System.out.println("[CONTRATO] RESULTADO linea 1: imp=" + l1.getFDEIMP() + " dif=" + l1.getFDEDIF() + " total=" + total1);
    System.out.println("[CONTRATO] RESULTADO linea 2: imp=" + l2.getFDEIMP() + " dif=" + l2.getFDEDIF() + " total=" + total2);
    System.out.println("[CONTRATO] suma = " + round2(total1 + total2) + " (debe ser " + kImporteTotal + ")");
    System.out.println("[CONTRATO] ===== prepararLineasContrato FIN =====");
        return new ContratoPrep(kImporteTotal, l1);
    }
    @Transactional("sqlServer2TransactionManager")
    public String actualizarSaldosContrato(ContabilizacionRequestDto req, Fac fac, Fde lineaPrincipal, double kImporteTotal) throws Exception {

        System.out.println("[CONTRATO] ===== actualizarSaldosContrato INICIO =====");
    System.out.println("[CONTRATO] buscando COG: ent=" + req.getEntcod() + " eje=" + fac.getEJE()
            + " concod=" + fac.getCONCOD() + " cgecod=" + fac.getCGECOD());
    System.out.println("[CONTRATO] kImporteTotal a restar = " + kImporteTotal);
    System.out.println("[CONTRATO] lineaPrincipal: org=" + (lineaPrincipal != null ? lineaPrincipal.getFDEORG() : "NULL")
            + " fun=" + (lineaPrincipal != null ? lineaPrincipal.getFDEFUN() : "NULL")
            + " eco=" + (lineaPrincipal != null ? lineaPrincipal.getFDEECO() : "NULL"));

        Cog cog = cogRepository.findOneByENTAndEJEAndCONCODAndCGECOD(
                        req.getEntcod(), fac.getEJE(), fac.getCONCOD(), fac.getCGECOD())
                .orElseThrow(() -> new SmlBuildingException("COG no encontrado para el contrato"));

                System.out.println("[CONTRATO] COG ANTES:  COGIAP=" + cog.getCOGIAP()
            + " COGIMP=" + cog.getCOGIMP() + " COGIM2=" + cog.getCOGIM2()
            + " COGOPD=" + cog.getCOGOPD() + " COGRFD=" + cog.getCOGRFD()
            + " COGOP2=" + cog.getCOGOP2() + " COGRF2=" + cog.getCOGRF2());
        cog.setCOGIAP(round2(nz(cog.getCOGIAP()) - kImporteTotal));

            System.out.println("[CONTRATO] COGIAP nuevo = " + cog.getCOGIAP());

        StringBuilder avisos = new StringBuilder();

        if (cog.getCOGOPD() != null && !cog.getCOGOPD().isBlank()) {
            try {
                Double nuevo = consultarSaldo(req.getOrg(), req.getEnt(), req.getEje(), cog.getCOGOPD(), cog.getCOGRFD(), lineaPrincipal.getFDEORG(), lineaPrincipal.getFDEFUN(), lineaPrincipal.getFDEECO());
                cog.setCOGIMP(nuevo);

                System.out.println("[CONTRATO] COGIMP actualizado = " + nuevo);
            } catch (Exception e) {
                System.out.println("[CONTRATO] ERROR refrescando AD principal: " + e.getMessage());
                e.printStackTrace();
                avisos.append("saldo AD principal: ").append(e.getMessage()).append(". ");
            }
        } else {
            System.out.println("[CONTRATO] COGOPD vacio -> no se consulta AD principal");
        }
        if (cog.getCOGOP2() != null && !cog.getCOGOP2().isBlank()) {
            try {
                Double nuevo = consultarSaldo(req.getOrg(), req.getEnt(), req.getEje(), cog.getCOGOP2(), cog.getCOGRF2(), lineaPrincipal.getFDEORG(), lineaPrincipal.getFDEFUN(), lineaPrincipal.getFDEECO());
                cog.setCOGIM2(nuevo);
                System.out.println("[CONTRATO] COGIM2 actualizado = " + nuevo);
            } catch (Exception e) {
                System.out.println("[CONTRATO] ERROR refrescando AD secundaria: " + e.getMessage());
                e.printStackTrace();
                avisos.append("saldo AD secundaria: ").append(e.getMessage()).append(". ");
            }
        } else {
            System.out.println("[CONTRATO] COGOP2 vacio -> no se consulta AD secundaria");
        }

        // TODO: liquidar AD principal (histórico + mover secundaria a principal)
        cogRepository.save(cog);
        System.out.println("[CONTRATO] COG DESPUES: COGIAP=" + cog.getCOGIAP()
            + " COGIMP=" + cog.getCOGIMP() + " COGIM2=" + cog.getCOGIM2());
        System.out.println("[CONTRATO] avisos = " + (avisos.length() == 0 ? "ninguno" : avisos));
        System.out.println("[CONTRATO] ===== actualizarSaldosContrato FIN =====");
        return avisos.length() == 0 ? null : avisos.toString();
    }

    public String buildSmlInput(ContabilizacionRequestDto req, Fac fac, List<Fde> fdeList, List<Fdt> fdtList, String terAyt) throws Exception {
        if (req == null) {
        throw new SmlBuildingException("Request cannot be null");
        }

        if (fac == null) {
            throw new SmlBuildingException("Fac cannot be null");
        }

        if (fdeList == null) {
            throw new SmlBuildingException("Fde list cannot be null");
        }

        if (fdtList == null) {
            throw new SmlBuildingException("Fdt list cannot be null");
        }

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

        String fechaContable = formatFechaContable(req.getFechaContable());

        String codope;
        if (Boolean.TRUE.equals(req.getEsContrato())) {
            codope = "400";
        } else {
            codope = "250";
        }

        String numope = fac.getEJE() + "-" + fac.getFACNUM();

        StringBuilder sb = new StringBuilder();
        sb.append("<e>");
        sb.append("<ope>");
        sb.append("<apl>SNP</apl>");
        sb.append("<tobj>GenOpeGasto</tobj>");
        sb.append("<cmd>CRE</cmd>");
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
        sb.append("<portal>S</portal>");  
        sb.append("<gensinalmacenar>0</gensinalmacenar>");

        sb.append("<l_operacion>");
        sb.append("<operacion>");
        sb.append("<prevdef>").append(CryptoSical.encodeBase64("P")).append("</prevdef>");
        sb.append("<numope>").append(numope).append("</numope>");  
        sb.append("<codope>").append(CryptoSical.encodeBase64(codope)).append("</codope>");
        sb.append("<signo>0</signo>");
        sb.append("<areGes>").append(CryptoSical.encodeBase64(fac.getCGECOD())).append("</areGes>");

        if (terAyt != null && !terAyt.isEmpty()) {
            sb.append("<nif>").append(CryptoSical.encodeBase64(terAyt)).append("</nif>");
        }
        
        if (fac.getFACOPG() != null) {
            sb.append("<ort>").append(CryptoSical.encodeBase64(fac.getFACOPG())).append("</ort>");
        }
        
        sb.append("<fecont>").append(fechaContable).append("</fecont>");
        

        if (fac.getFACDOC() != null) {
            sb.append("<ndoc>").append(CryptoSical.encodeBase64(fac.getFACDOC())).append("</ndoc>");
        }

        if (fac.getFACDAT() != null) {
            sb.append("<fdoc>").append(formatDate(fac.getFACDAT())).append("</fdoc>");
        }
        
        if (fac.getFACOCT() != null) {
            sb.append("<obp>").append(CryptoSical.encodeBase64(String.valueOf(fac.getFACOCT()))).append("</obp>");
        }

        if (fac.getFACFPG() != null && !fac.getFACFPG().isEmpty() && fac.getFACFPG().length() >= 8) {
            sb.append("<fpago>").append(formatDateString(fac.getFACFPG())).append("</fpago>");
        }
        
        if (fac.getFACTPG() != null) {
            sb.append("<tpago>").append(CryptoSical.encodeBase64(fac.getFACTPG())).append("</tpago>");
        }
        
        sb.append("<ofig>").append(CryptoSical.encodeBase64("AL")).append("</ofig>");
        
        if (fac.getFACTXT() != null) {
            sb.append("<text>").append(CryptoSical.encodeBase64(fac.getFACTXT())).append("</text>");
        }
        
        sb.append("<usuope>").append(CryptoSical.encodeBase64(usu)).append("</usuope>");
        
        sb.append("<ivabex>0</ivabex>");
        sb.append("<ivabse1>0</ivabse1>");
        sb.append("<ivabse2>0</ivabse2>");
        sb.append("<ivabse3>0</ivabse3>");
        sb.append("<iva1>0</iva1>");
        sb.append("<iva2>0</iva2>");
        sb.append("<iva3>0</iva3>");
        sb.append("<piva1>0</piva1>");
        sb.append("<piva2>0</piva2>");
        sb.append("<piva3>0</piva3>");
        
        String tipContrato = (fac.getCONCTP() != null && !fac.getCONCTP().isEmpty()) ? fac.getCONCTP() : "Suministro";
        String proContrato = (fac.getCONCPR() != null && !fac.getCONCPR().isEmpty()) ? fac.getCONCPR() : "AdDirec";
        String criContrato = (fac.getCONCCR() != null && !fac.getCONCCR().isEmpty()) ? fac.getCONCCR() : "SinC";
        
        sb.append("<tipContrato>").append(CryptoSical.encodeBase64(tipContrato)).append("</tipContrato>");
        sb.append("<proContrato>").append(CryptoSical.encodeBase64(proContrato)).append("</proContrato>");
        sb.append("<criContrato>").append(CryptoSical.encodeBase64(criContrato)).append("</criContrato>");

        sb.append("<l_factura>");
        sb.append("<factura>");
        if (fac.getFACTDC() != null) {
            sb.append("<tipoF>").append(CryptoSical.encodeBase64(fac.getFACTDC())).append("</tipoF>");
        }
        sb.append("<ejeF>").append(fac.getFACANN()).append("</ejeF>");
        if (fac.getFACFAC() != null) {
            sb.append("<numeF>").append(fac.getFACFAC()).append("</numeF>");
        }
        sb.append("</factura>");
        sb.append("</l_factura>");

        sb.append("<l_linea>");
        java.util.Set<String> lineasIncluidas = new java.util.HashSet<>();
        for (Fde fde : fdeList) {
            Double imp = (fde.getFDEIMP() != null ? fde.getFDEIMP() : 0.0) +
                        (fde.getFDEDIF() != null ? fde.getFDEDIF() : 0.0);

        System.out.println("FDE candidate: org=" + fde.getFDEORG()
        + " fun=" + fde.getFDEFUN()
        + " eco=" + fde.getFDEECO()
        + " ope=" + fde.getFDEOPE()
        + " imp=" + imp);

            if (imp <= 0) {
                System.out.println("  -> SKIPPED (imp<=0) eco=" + fde.getFDEECO());
                continue;
            }

            LineaGastoDefinitivo datosWs = consultarOperacionGastoDefinitivo(fde, org, ent, eje);
            if (datosWs == null) {
                System.out.println("WS 2.49: se omite linea por no poder resolver datos de operacion para FDEOPE=" + fde.getFDEOPE());
                continue;
            }

            lineasIncluidas.add(claveLinea(fde.getFDEORG(), fde.getFDEFUN(), fde.getFDEECO()));


            sb.append("<linea>");
            sb.append("<lineje>").append(eje).append("</lineje>");
            if (fde.getFDEORG() != null) {
                sb.append("<org>").append(CryptoSical.encodeBase64(fde.getFDEORG())).append("</org>");
            }
            if (fde.getFDEFUN() != null) {
                sb.append("<fun>").append(CryptoSical.encodeBase64(fde.getFDEFUN())).append("</fun>");
            }
            if (fde.getFDEECO() != null) {
                sb.append("<eco>").append(CryptoSical.encodeBase64(fde.getFDEECO())).append("</eco>");
            }
            if (fde.getFDEOPE() != null) {
                sb.append("<oan>").append(fde.getFDEOPE()).append("</oan>");
            }
            if (datosWs.nlinea() != null) {
                sb.append("<naa>").append(datosWs.nlinea()).append("</naa>");
            }
            if (fde.getFDEREF() != null) {
                sb.append("<refe>").append(fde.getFDEREF()).append("</refe>");
            }
            if (datosWs.prya() != null) sb.append("<prya>").append(datosWs.prya()).append("</prya>");
            if (datosWs.pryt() != null) sb.append("<pryt>").append(datosWs.pryt()).append("</pryt>");
            if (datosWs.pryo() != null) sb.append("<pryo>").append(CryptoSical.encodeBase64(datosWs.pryo())).append("</pryo>");
            if (datosWs.pryn() != null) sb.append("<pryn>").append(datosWs.pryn()).append("</pryn>");
            if (datosWs.pryx() != null) sb.append("<pryx>").append(datosWs.pryx()).append("</pryx>");
            sb.append("<imp>").append(imp).append("</imp>");
            sb.append("</linea>");
        }
        sb.append("</l_linea>");

        sb.append("<l_dto>");
        for (Fdt fdt : fdtList) {
            Double impDto = fdt.getFDTDTO() != null ? fdt.getFDTDTO() : 0.0;
            if (impDto <= 0) {
                System.out.println("fdtdto that shant pass ");
                System.out.println(impDto);
                continue;
            }

            String claveDto = claveLinea(fdt.getFDTORG(), fdt.getFDTFUN(), fdt.getFDTECO());
            if (!lineasIncluidas.contains(claveDto)) {
                System.out.println("Se omite dto por no existir la linea correspondiente (" + claveDto + ") en la operacion, FDTECO=" + fdt.getFDTECO());
                continue;
            }

            System.out.println("fdtdto that should pass ");
            System.out.println(impDto);
            sb.append("<dto>");
            if (fdt.getFDTARE() != null) {
                sb.append("<areaD>").append(fdt.getFDTARE()).append("</areaD>");
            }
            sb.append("<ejeD>").append(eje).append("</ejeD>");
            if (fdt.getFDTORG() != null) {
                sb.append("<orgD>").append(CryptoSical.encodeBase64(fdt.getFDTORG())).append("</orgD>");
            }
            if (fdt.getFDTFUN() != null) {
                sb.append("<funD>").append(CryptoSical.encodeBase64(fdt.getFDTFUN())).append("</funD>");
            }
            if (fdt.getFDTECO() != null) {
                sb.append("<ecoD>").append(CryptoSical.encodeBase64(fdt.getFDTECO())).append("</ecoD>");
            }
            sb.append("<impD>").append(impDto).append("</impD>");
            if (fdt.getFDTBSE() != null) {
                sb.append("<baseRet>").append(fdt.getFDTBSE()).append("</baseRet>");
            }
            if (fdt.getFDTPRE() != null) {
                sb.append("<porcRet>").append(fdt.getFDTPRE()).append("</porcRet>");
            }
            if (fdt.getFDTTXT() != null) {
                sb.append("<textoD>").append(CryptoSical.encodeBase64(fdt.getFDTTXT())).append("</textoD>");
            }
            sb.append("</dto>");
        }
        sb.append("</l_dto>");

        sb.append("</operacion>");
        sb.append("</l_operacion>");
        sb.append("</par>");
        sb.append("</e>");

        String sml = sb.toString();

        System.out.println("========== SML REQUEST ==========");
        System.out.println(sml);
        System.out.println("=================================");

        return sml;
    }

    private String claveLinea(String org, String fun, String eco) {
        return (org != null ? org : "") + "|" + (fun != null ? fun : "") + "|" + (eco != null ? eco : "");
    }

    public String sendSmlRequest(String smlInput, String url) {
        String rawEndpoint = (url != null && !url.isEmpty()) ? url : sicalWsUrl;
        String endpoint = rawEndpoint.contains("?") ? rawEndpoint.substring(0, rawEndpoint.indexOf("?")) : rawEndpoint;

        
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(60000);
            RestTemplate restTemplate = new RestTemplate(factory);
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

                System.out.println("========== SOAP ENVELOPE ==========");
                System.out.println(soapEnvelope);
                System.out.println("===================================");

            HttpEntity<String> entity = new HttpEntity<>(soapEnvelope, headers);
            System.out.println("Sending to:");
            System.out.println(endpoint);

            System.out.println("Before POST...");
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

            System.out.println("After POST...");

            System.out.println("========== HTTP STATUS ==========");
            System.out.println(response.getStatusCode());

            System.out.println("========== SOAP RESPONSE ==========");
            System.out.println(response.getBody());
            System.out.println("==================================");
            
            return response.getBody();
        } catch (Exception e) {
            System.out.println("========== REQUEST FAILED ==========");
            e.printStackTrace();
            return null;
        }
    }

    public ContabilizacionResponseDto parseResponse(String soapResponse) {
        System.out.println("========== RAW SOAP ==========");
        System.out.println(soapResponse);
        System.out.println("==============================");

        ContabilizacionResponseDto dto = new ContabilizacionResponseDto();
        
        try {
            String sml = extractSmlFromSoap(soapResponse);
            System.out.println("====== EXTRACTED SML ======");
            System.out.println(sml);
            System.out.println("====== END EXTRACTED SML ======");
            
            if (sml == null) {
                dto.setExito(false);
                dto.setMensaje("Respuesta SOAP inválida");
                return dto;
            }

            javax.xml.parsers.DocumentBuilderFactory dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(new java.io.ByteArrayInputStream(sml.getBytes()));

            String exito = getTagValue(doc, "exito");
            System.out.println("Exito value: " + exito);
            
            
            if ("-1".equals(exito)) {
                dto.setExito(true);
                
                org.w3c.dom.NodeList opNodes = doc.getElementsByTagName("operacion");
                if (opNodes.getLength() > 0) {
                    org.w3c.dom.Node opNode = opNodes.item(0);
                    org.w3c.dom.NodeList children = opNode.getChildNodes();
                    
                    if (opNodes.getLength() > 0) {
                        String operationText = opNodes.item(0).getTextContent().trim();
                        String[] fields = operationText.split("-@-", -1);

                        if (fields.length > 1) {
                            dto.setOpeext(fields[0]);
                            dto.setOpesical(fields[1]);
                        }

                        if (fields.length > 2) dto.setNap(fields[2]);
                        if (fields.length > 3) dto.setReferencia(fields[3]);
                        if (fields.length > 4) dto.setImporte(fields[4]);
                        if (fields.length > 5) dto.setEjercicio(fields[5]);
                    }
                }
                dto.setMensaje("Operación generada correctamente");
            } else {
                dto.setExito(false);
                String desc = getTagValue(doc, "desc");
                String codigo = getTagValue(doc, "codigo");
                
                org.w3c.dom.NodeList errorNodes = doc.getElementsByTagName("error");
                StringBuilder errors = new StringBuilder();
                for (int i = 0; i < errorNodes.getLength(); i++) {
                    if (i > 0) errors.append("; ");
                    errors.append(errorNodes.item(i).getTextContent());
                }
                
                String mensaje = "";
                if (codigo != null && !codigo.isEmpty()) {
                    mensaje += "Código: " + codigo + ". ";
                }
                if (desc != null && !desc.isEmpty()) {
                    mensaje += desc;
                }
                if (errors.length() > 0) {
                    mensaje += " Errores: " + errors.toString();
                }
                dto.setMensaje(mensaje.isEmpty() ? "Error desconocido del servicio" : mensaje);
            }
        } catch (Exception e) {
            dto.setExito(false);
            dto.setMensaje("Error al procesar respuesta: " + e.getMessage());
            e.printStackTrace();
        }
        
        return dto;
    }

    private String formatFechaContable(String fecha) {
        if (fecha == null) return "";
        if (fecha.length() == 8 && !fecha.contains("-")) {
            return fecha;
        }
        return fecha.replace("-", "");
    }

    private String formatDateString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        if (dateStr.length() == 8 && !dateStr.contains("-")) {
            return dateStr;
        }
        String cleaned = dateStr.replace("-", "");

        if (cleaned.length() >= 8) {
            return cleaned.substring(0, 8);
        }
        return cleaned; 
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String extractSmlFromSoap(String soap) {
        try {
            int start = soap.indexOf("<servicioReturn");
            if (start < 0) return null;
            start = soap.indexOf(">", start) + 1;
            int end = soap.indexOf("</servicioReturn>", start);
            if (end > start) {
                String sml = soap.substring(start, end)
                    .replace("&lt;", "<")
                    .replace("&gt;", ">");
                return sml;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getTagValue(org.w3c.dom.Document doc, String tag) {
        org.w3c.dom.NodeList nodes = doc.getElementsByTagName(tag);
        if (nodes.getLength() > 0 && nodes.item(0).getFirstChild() != null) {
            return nodes.item(0).getFirstChild().getNodeValue();
        }
        return null;
    }

    private String decodeIfBase64(String value) {
        if (value == null || value.isEmpty()) return value;
        try {
            return CryptoSical.decodeBase64(value);
        } catch (Exception e) {
            System.out.println("========== REQUEST FAILED ==========");
    e.printStackTrace();
            return value;
        }
    }

    private record LineaGastoDefinitivo(String nlinea, String prya, String pryt,
                                     String pryo, String pryn, String pryx) {}

    private LineaGastoDefinitivo consultarOperacionGastoDefinitivo(Fde fde, String orgCode, String entidad, String eje) {
        try {
            String numeroOperDesde = fde.getFDEOPE() != null ? String.valueOf(fde.getFDEOPE()) : null;
            String numope = fde.getFDEOPE() != null ? String.valueOf(fde.getFDEOPE()) : null;
            String referencia = fde.getFDEREF() != null ? String.valueOf(fde.getFDEREF()) : null;
            String oficina = "AL";

            List<Operaciones> resultado = operacionesService.getOperaciones(orgCode, entidad, numeroOperDesde, numeroOperDesde, null, fde.getFDEORG(), fde.getFDEFUN(), fde.getFDEECO(), referencia, null, null, null, eje);

            if (!resultado.isEmpty()) {
    Operaciones op = resultado.get(0);
    System.out.println("Operacion " + op.getNumope() + " eco=" + fde.getFDEECO()
        + " codope=" + op.getCodope()
        + " fase=" + op.getFase()
        + " signo=" + op.getSigno());
}

            if (resultado.isEmpty()) {
                System.out.println("WS 2.49: sin resultado para FDEOPE=" + numope + " refe=" + referencia);
                return null;
            }
            if (resultado.size() != 1) {
                System.out.println("WS 2.49: se esperaba 1 operacion, llegaron " + resultado.size() + " para FDEOPE=" + numope);
                return null;
            }

            List<Operaciones.Linea> lineas = resultado.get(0).getLineaList();
            if (lineas == null || lineas.isEmpty()) {
                System.out.println("WS 2.49: operacion sin lineas para FDEOPE=" + numope);
                return null;
            }
            if (lineas.size() != 1) {
                System.out.println("WS 2.49: se esperaba 1 linea, llegaron " + lineas.size() + " para FDEOPE=" + numope);
                return null;
            }

            Operaciones.Linea l = lineas.get(0);
            return new LineaGastoDefinitivo(
                    l.getNlinea() != null ? String.valueOf(l.getNlinea()) : null,
                    l.getPrya() != null ? String.valueOf(l.getPrya()) : null,
                    l.getPryt(),
                    l.getPryo(),
                    l.getPryn(),
                    l.getPryx() != null ? String.valueOf(l.getPryx()) : null
            );
        } catch (Exception e) {
            System.out.println("========== WS 2.49 CONSULTA FALLIDA ==========");
            e.printStackTrace();
            return null;
        }
    }
}