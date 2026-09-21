package com.example.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.ContratoDto;
import com.example.backend.service.CotContratoProjection;
import com.example.backend.service.ContratosSearch;
import com.example.backend.sqlserver2.repository.CotRepository;
import com.example.backend.sqlserver2.model.Cot;
import com.example.backend.sqlserver2.model.Conn;
import com.example.backend.sqlserver2.model.ConId;
import com.example.backend.sqlserver2.repository.ConRepository;

@RestController
@RequestMapping("/api/con")
public class ConController {
    @Autowired
    private CotRepository cotRepository;
    @Autowired
    private ConRepository conRepository;
    @Autowired
    private ContratosSearch contratosSearch;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    private ContratoDto buildContratoDto(CotContratoProjection.ConnInfo c, CotContratoProjection.TerInfo t) {
        return new ContratoDto.Builder()
            .concod(c.getCONCOD())
            .conlot(c.getCONLOT())
            .condes(c.getCONDES())
            .confin(c.getCONFIN())
            .conffi(c.getCONFFI())
            .conblo(c.getCONBLO())
            .tercod(t.getTERCOD())
            .ternom(t.getTERNOM())
            .build();
    }

    //selecting all contratos
    @GetMapping("/fetch-contratos/{ent}/{eje}")
    public ResponseEntity<?> fetchContratos(
        @PathVariable Integer ent,
        @PathVariable String eje
    ) {
        try {
            List<CotContratoProjection> rows = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje);
            if (rows == null || rows.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            List<ContratoDto> dto = rows.stream()
                .map(p -> buildContratoDto(p.getConn(), p.getTer()))
                .collect(Collectors.toList());

            return ResponseEntity.ok(dto);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //search by in contratos
    @GetMapping("/searchByCodigoBloque/{ent}/{eje}/{searchMode}")
    public ResponseEntity<?> searchContratosCodigoBloqueado(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String searchMode,
        @RequestParam(required = false) String term
    ) {
        try {
            List<ContratoDto> dto = contratosSearch.searchContrtos(ent, eje, searchMode, term);
            if (dto == null || dto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(dto);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //update a contrato
    public record CUpdate(Integer CONBLO, LocalDateTime CONFIN, LocalDateTime CONFFI, String CONDES) {}
    @PatchMapping("/update-contrato/{ent}/{eje}/{concod}")
    public ResponseEntity<?> updateContrato(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer concod,
        @RequestBody CUpdate payload
    ) {
        try {
            if(payload == null || payload.CONDES() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            ConId id = new ConId(ent, eje, concod);
            Optional<Conn> contrato = conRepository.findById(id);
            if (contrato.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Conn conUpdate = contrato.get();
            conUpdate.setCONBLO(payload.CONBLO());
            conUpdate.setCONFIN(payload.CONFIN());
            conUpdate.setCONFFI(payload.CONFFI());
            conUpdate.setCONDES(payload.CONDES());
            conRepository.save(conUpdate);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //add a contrato
    private int nextConcod(Integer ent, String eje) {
        return conRepository.findFirstByENTAndEJEOrderByCONCODDesc(ent, eje)
            .map(conn -> (conn.getCONCOD() == null ? 0 : conn.getCONCOD()) + 1)
            .orElse(1);
    }
    public record CAdd(Integer ENT, String EJE, String CONLOT, Integer CONBLO, LocalDateTime CONFIN, LocalDateTime CONFFI, String CONDES, Integer TERCOD) {}
    @PostMapping("/add-contrato")
    public ResponseEntity<?> addContrato(
        @RequestBody CAdd payload
    ) {
        try {
            if(payload == null || payload.ENT() == null || payload.EJE() == null || payload.CONDES() == null || payload.CONLOT() == null || payload.TERCOD() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            int concod = nextConcod(payload.ENT(), payload.EJE());
            Conn conAdd = new Conn();
            conAdd.setENT(payload.ENT());
            conAdd.setEJE(payload.EJE());
            conAdd.setCONCOD(concod);
            conAdd.setCONLOT(payload.CONLOT());
            conAdd.setCONDES(payload.CONDES());
            conAdd.setCONTIP(3);
            conAdd.setCONPRE(1);
            conAdd.setCONFIN(payload.CONFIN());
            conAdd.setCONFFI(payload.CONFFI());
            conAdd.setCONIMP(0.00);
            conAdd.setCONBLO(payload.CONBLO());
            conRepository.save(conAdd);

            Cot cotAdd = new Cot();
            cotAdd.setENT(payload.ENT());
            cotAdd.setEJE(payload.EJE());
            cotAdd.setCONCOD(concod);
            cotAdd.setTERCOD(payload.TERCOD());
            cotAdd.setCOTLIN(1);
            cotRepository.save(cotAdd);

            Optional<CotContratoProjection> newContrato = cotRepository.findProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, payload.ENT(), payload.EJE(), concod);
            if (newContrato.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error añadir contrato");
            }
            List<ContratoDto> dto = newContrato.stream()
                .map(p -> buildContratoDto(p.getConn(), p.getTer()))
                .collect(Collectors.toList());

            return ResponseEntity.ok(dto);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}