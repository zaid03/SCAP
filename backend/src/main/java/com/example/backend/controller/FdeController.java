package com.example.backend.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.FacId;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.FdeId;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.example.backend.sqlserver2.repository.FacRepository;
import com.example.backend.dto.FdeFacTerProjection;
import com.example.backend.dto.FdeResumeDto;
import com.example.backend.dto.ProjectionContabilizar;
import com.example.backend.service.ContabilizadoSearch;
import com.example.backend.service.ContabilizarSearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fde")
public class FdeController {
    @Autowired
    private FdeRepository fdeRepository;
    @Autowired
    private FacRepository facRepository;
    @Autowired
    private ContabilizarSearch contabilizarSearch;
    @Autowired
    private ContabilizadoSearch contabilizadoSearch;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    @GetMapping("/{ent}/{eje}/{facnum}")
    public ResponseEntity<?> getFde(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer facnum
    ) {
        try {
            List<Fde> detalles = fdeRepository.findByENTAndEJEAndFACNUM(ent, eje, facnum);
        
            if(detalles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            List<FdeResumeDto> result = detalles.stream()
                .map(fd -> new FdeResumeDto(
                    fd.getFDEREF(),
                    fd.getFDEECO(),
                    fd.getFDEIMP(),
                    fd.getFDEDIF()
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //updating diferencias and related apl diferencias
    public record diffUpdate(Double FDEDIF, Double FACIDI) {};

    @PatchMapping("/update-diferencias/{ent}/{eje}/{facnum}/{fderef}")
    public ResponseEntity<?> updateDiferencias(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer facnum,
        @PathVariable String fderef,
        @RequestBody diffUpdate payload
    ) {
        try {
            if (payload == null || payload.FDEDIF() == null || payload.FACIDI() == null) {
                return ResponseEntity.badRequest().body("faltan datos obligatorios");
            }

            FdeId id = new FdeId(ent, eje, facnum, fderef);
            Optional<Fde> applicacionOptio = fdeRepository.findById(id);
            if (applicacionOptio.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Fde applicacion = applicacionOptio.get();
            applicacion.setFDEDIF(payload.FDEDIF());
            fdeRepository.save(applicacion);

            FacId facId = new FacId(ent, eje, facnum);
            Optional<Fac> facturaOptio = facRepository.findById(facId);
            if (facturaOptio.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Fac factura = facturaOptio.get();
            factura.setFACIDI(payload.FACIDI());
            facRepository.save(factura);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //selecting all facturas in consulta del pendiente de contabilizado
    @GetMapping("/fetch-pendiente-del-contabilizar/{ent}/{eje}")
    public ResponseEntity<?> fetchContabilizado (
        @PathVariable Integer ent,
        @PathVariable String eje
    ) {
        try {
            List<ProjectionContabilizar> facturas = fdeRepository.findPendienteContabilizar(ent, eje);

            if (facturas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(facturas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMessage());
        }
    }

    //search in consulta del pendiente de contabilizado
    @GetMapping("/search-pendiente-contabilizar")
    public ResponseEntity<?> searchContabilizado (
        @RequestParam Integer ent,
        @RequestParam String eje,
        @RequestParam(required = false) String proveedor,
        @RequestParam(required = false) String centroGestor,
        @RequestParam(required = false) String economica,
        @RequestParam(required = false) Integer ano
    ) {
        try {
            List<ProjectionContabilizar> facturas = contabilizarSearch.searchContabilizado(ent, eje, proveedor, centroGestor, economica, ano);

            if (facturas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(facturas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //selecting all facturas in consulta de del contabilizado
    @GetMapping("/fetch-contabilizado/{ent}/{eje}")
    public ResponseEntity<?> fetchContabilizadoSecond (
        @PathVariable Integer ent,
        @PathVariable String eje
    ) {
        try {
            List<FdeFacTerProjection> facturas = fdeRepository.findByENTAndEJEAndFac_FACFCOIsNotNullAndFDEIMPGreaterThanOrENTAndEJEAndFac_FACFCOIsNotNullAndFDEDIFGreaterThan(ent, eje, 0.0, ent, eje, 0.0);

            if (facturas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(facturas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMessage());
        }
    }

    //search in consulta del contabilizado
    @GetMapping("/search-contabilizado")
    public ResponseEntity<?> searchContabilizadoSecond (
        @RequestParam Integer ent,
        @RequestParam String eje,
        @RequestParam(required = false) String proveedor,
        @RequestParam(required = false) String centroGestor,
        @RequestParam(required = false) String economica,
        @RequestParam(required = false) Integer ano
    ) {
        try {
            List<FdeFacTerProjection> facturas = contabilizadoSearch.searchContabilizado(ent, eje, proveedor, centroGestor, economica, ano);

            if (facturas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(facturas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}