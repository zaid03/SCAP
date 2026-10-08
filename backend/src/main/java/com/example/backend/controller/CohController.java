package com.example.backend.controller;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.HistoricaContratos;
import com.example.backend.sqlserver2.repository.CohRepository;
import com.example.backend.sqlserver2.model.CogId;
import com.example.backend.sqlserver2.model.Cog;
import com.example.backend.sqlserver2.model.Coh;
import com.example.backend.sqlserver2.repository.CogRepository;
import com.example.backend.service.HistoricaADContratoSearch;

@RestController
@RequestMapping("/api/coh")
public class CohController {
    @Autowired
    private CohRepository cohRepository;
    @Autowired
    private HistoricaADContratoSearch historicaADContratoSearch;
    @Autowired
    private CogRepository cogRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //main fetch for historica de ad
    @GetMapping("/historia-ADcontrato/{ent}/{eje}")
    public ResponseEntity<?> fetchHistoriaADContrato (
        @PathVariable Integer ent,
        @PathVariable String eje
    ) {
        try {
            List<HistoricaContratos> contratos = cohRepository.findByENTAndEJEAndCon_CONTIP(ent, eje, 3);
            if (contratos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(contratos);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //filtering for historica de ad
    @GetMapping("/search-historia-ADcontrato/{ent}/{eje}")
    public ResponseEntity<?> searchHistoriaContrato (
        @PathVariable Integer ent,
        @PathVariable String eje,
        @RequestParam(required = false) String cge,
        @RequestParam(required = false) String contrato,
        @RequestParam(required = false) String proveedor
    ) {
        try {
            if (cge == null && contrato == null && proveedor == null) {
            return ResponseEntity.badRequest().body("Faltan datos obligatorios");
            }

            List<HistoricaContratos> contratos = historicaADContratoSearch.historicaADContratoSearch(ent, eje, cge, contrato, proveedor);
            if (contratos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(contratos);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }  
    }

    //historico de D
    public record historico(Integer ENT, String EJE, Integer CONCOD, Double COGIMP, String CGECOD, String COGOPD, String COGRFD, Double COGIM2, String COGOP2, String COGRF2) {}

    @Transactional("sqlServer2TransactionManager")
    @PatchMapping("/set-historico")
    public ResponseEntity<?> historicoSet(
        @RequestBody historico payload
    ) {
        try {
            if (payload == null || payload.ENT() == null || payload.EJE() == null || payload.CONCOD() == null || payload.CGECOD() == null || payload.COGIMP() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios");
            }

            // Optional<Coh> historica = cohRepository.findTopByOrderByCOHCODDesc();
            // Integer cohcod;
            // if (historica.isEmpty() || historica.get().getCOHCOD() == null) {
            //     cohcod = 1;
            // } else {
            //     cohcod = historica.get().getCOHCOD() + 1;
            // }
            Integer cohcod = cohRepository.getNextCohcod();
            LocalDateTime date = LocalDateTime.now();
            Coh newHistorica = new Coh();
            newHistorica.setENT(payload.ENT());
            newHistorica.setEJE(payload.EJE());
            newHistorica.setCONCOD(payload.CONCOD());
            newHistorica.setCOHCOD(cohcod);
            newHistorica.setCGECOD(payload.CGECOD());
            newHistorica.setCOHOPD(payload.COGOPD());
            newHistorica.setCOHRFD(payload.COGRFD());
            newHistorica.setCOHFEC(date);
            cohRepository.save(newHistorica);

            CogId id = new CogId(payload.ENT(), payload.EJE(), payload.CONCOD(), payload.CGECOD());
            Optional<Cog> cog = cogRepository.findOneByENTAndEJEAndCONCODAndCGECOD(payload.ENT(), payload.EJE(), payload.CONCOD(), payload.CGECOD());
            if (cog.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Cog cogUpdate = cog.get();

System.out.println("COGIMP: " + cogUpdate.getCOGIMP() + " -> " + payload.COGIM2());
System.out.println("COGOPD: " + cogUpdate.getCOGOPD() + " -> " + payload.COGOP2());
System.out.println("COGRFD: " + cogUpdate.getCOGRFD() + " -> " + payload.COGRFD());
System.out.println("COGIM2: " + cogUpdate.getCOGIM2() + " -> 0.00");
System.out.println("COGOP2: " + cogUpdate.getCOGOP2() + " -> null");
System.out.println("COGRF2: " + cogUpdate.getCOGRF2() + " -> null");
            cogUpdate.setCOGIMP(payload.COGIM2());
            cogUpdate.setCOGOPD(payload.COGOP2());
            cogUpdate.setCOGRFD(payload.COGRF2());
            cogUpdate.setCOGIM2(0.00);
            cogUpdate.setCOGOP2("");
            cogUpdate.setCOGRF2("");
            cogRepository.saveAndFlush(cogUpdate);
            System.out.println("COG flushed successfully");

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }  
    }
}