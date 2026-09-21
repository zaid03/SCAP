package com.example.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.service.FacContabilizacionSpecification;
import com.example.backend.service.FacturaInsertService;
import com.example.backend.service.FacturaSearch;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.model.Gbs;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.FacId;
import com.example.backend.sqlserver2.repository.FacRepository;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.example.backend.sqlserver2.repository.GbsRepository;
import com.example.backend.dto.FacWithTerProjection;
import com.example.backend.dto.FacturaInsertDto;

@RestController
@RequestMapping("/api/fac")
public class FacController {
    @Autowired
    private FacRepository facRepository;
    @Autowired
    private FacturaInsertService facturaInsertService;
    @Autowired
    private FdeRepository fdeRepository;
    @Autowired
    private GbsRepository gbsRepository;
    @Autowired
    private FacturaSearch facturaSearch;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //for the main list
    @GetMapping("/{ent}/{eje}/{cgecod}")
    public ResponseEntity<?> getFacturas(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<FacWithTerProjection> facturas = facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(ent, eje, cgecod);
            if (facturas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            
            return ResponseEntity.ok(facturas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //same but for gestion
    @GetMapping("/noCont/{ent}/{eje}/{cgecod}")
    public ResponseEntity<?> getFacturasNoCont(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<FacWithTerProjection> facturas = facRepository.findByENTAndEJEAndCGECODAndFACADOIsNullOrderByFACFREAsc(ent, eje, cgecod);
            if (facturas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            
            return ResponseEntity.ok(facturas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //search in gestion de factura
    @GetMapping("/search-factura")
    public ResponseEntity<?> searchFacturas (
        @RequestParam Integer ent,
        @RequestParam String eje,
        @RequestParam(required = false) String cgecod,
        @RequestParam(required = false) String main_filter,
        @RequestParam(required = false) Integer ej_factura,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) String fecha,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        try {
            FacturaSearch.FacturaSearchCriteria criteria = new FacturaSearch.FacturaSearchCriteria.Builder()
                .ent(ent)
                .eje(eje)
                .cgecod(cgecod)
                .mainFilter(main_filter)
                .ejFactura(ej_factura)
                .estado(estado)
                .fecha(fecha)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
            
            List<FacWithTerProjection> facturas = facturaSearch.searchFactura(criteria);

            if (facturas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(facturas);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMessage());
        }
    }

    //adding a factura
    @PostMapping("/add-facturas")
    public ResponseEntity<?> addFacturas(
        @RequestBody List<FacturaInsertDto> facturas
    ) {
        try {
            FacturaInsertService.NamesResponse response = facturaInsertService.insertFacturas(facturas);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMessage());
        }
    }

    //modifying a factura
    public record facturaUpdate(String FACOBS, String CONCTP, String CONCPR, String CONCCR, String FACFPG, String FACOPG, String FACTPG, Integer FACOCT) {}
    @PatchMapping("/update-factura/{ent}/{eje}/{facnum}")
    public ResponseEntity<?> updateFactura(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer facnum,
        @RequestBody facturaUpdate payload
    ) {
        try {
            if (payload == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falta un dato obligatorio");
            }

            FacId id = new FacId(ent, eje, facnum);
            Optional<Fac> facOptio = facRepository.findById(id);
            if (facOptio.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Fac factura = facOptio.get();
            factura.setFACOBS(payload.FACOBS());
            factura.setCONCTP(payload.CONCTP());
            factura.setCONCPR(payload.CONCPR());
            factura.setCONCCR(payload.CONCCR());
            factura.setFACFPG(payload.FACFPG());
            factura.setFACOPG(payload.FACOPG());
            factura.setFACTPG(payload.FACTPG());
            factura.setFACOCT(payload.FACOCT());
            facRepository.save(factura);

            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMessage());
        }
    }

    //search in contabilizacion
    @GetMapping("/contabilizacion/search")
    public ResponseEntity<?> searchContabilizacion(
        @RequestParam Integer ent,
        @RequestParam String eje,
        @RequestParam String cgecod,
        @RequestParam(defaultValue = "registro") String fechaType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
        @RequestParam(required = false) Integer facann,
        @RequestParam(required = false) String search 
    ) {
        try {
            FacContabilizacionSpecification.SearchCriteria criteria = new FacContabilizacionSpecification.SearchCriteria.Builder()
                .ent(ent)
                .eje(eje)
                .cgecod(cgecod)
                .fechaType(fechaType)
                .desde(desde)
                .hasta(hasta)
                .facann(facann)
                .search(search)
                .build();
            Specification<Fac> spec = FacContabilizacionSpecification.searchContabilizacion(criteria);

            List<Fac> facturas = facRepository.findAll(spec);

            if (facturas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(facturas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //selecting a factura for info updates upon a crud op done on facturas
    @GetMapping("/factura/{ent}/{eje}/{facnum}")
    public ResponseEntity<?> getFactura(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer facnum
    ) {
        try {
            Optional<FacWithTerProjection> factura = facRepository.findByENTAndEJEAndFACNUM(ent, eje, facnum);
            if (factura.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(factura);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //contabilizar a factura
    public record Contabilizar(Integer ENT, String EJE, Integer FACNUM, String FACADO, LocalDateTime FACFCO, String CGECOD, Boolean ESCONTRATO) {}

    @PatchMapping("/contabilizar-facturas")
    public ResponseEntity<?> contabilizarFactura(
        @RequestBody Contabilizar payload
    ) {
        try {
            ResponseEntity<?> validation = validateContabilizarPayload(payload);
            if (validation != null) return validation;
            
            ResponseEntity<?> facturaResult = updateFacturaRecord(payload);
            if (facturaResult != null) return facturaResult;
            
            if (!payload.ESCONTRATO()) {
                ResponseEntity<?> bolsaResult = procesarBolsas(payload);
                if (bolsaResult != null) return bolsaResult;
            }
            
            return ResponseEntity.noContent().build();
            
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    private ResponseEntity<?> validateContabilizarPayload(Contabilizar payload) {
        if (payload == null || payload.ENT() == null || payload.EJE() == null || 
            payload.FACNUM() == null || payload.FACADO() == null || payload.FACFCO() == null || 
            payload.CGECOD() == null || payload.ESCONTRATO() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falta un dato obligatorio");
        }
        return null;
    }

    private ResponseEntity<?> updateFacturaRecord(Contabilizar payload) {
        FacId id = new FacId(payload.ENT(), payload.EJE(), payload.FACNUM());
        Optional<Fac> facturaOptio = facRepository.findById(id);
        
        if (facturaOptio.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Factura no encontrada");
        }
        
        Fac factura = facturaOptio.get();
        factura.setFACADO(payload.FACADO());
        factura.setFACFCO(payload.FACFCO());
        facRepository.save(factura);
        
        return null;
    }

    private ResponseEntity<?> procesarBolsas(Contabilizar payload) {
        List<Fde> applicacionesList = fdeRepository.findByENTAndEJEAndFACNUM(
            payload.ENT(), payload.EJE(), payload.FACNUM()
        );

        List<String> errores = new ArrayList<>();
        
        for (Fde fde : applicacionesList) {
            procesarFdeYBolsa(payload, fde, errores);
        }
        
        if (!errores.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("No existe bolsa para: " + String.join(", ", errores));
        }
        
        return null;
    }

    private void procesarFdeYBolsa(Contabilizar payload, Fde fde, List<String> errores) {
        Optional<Gbs> bolsaOptio = gbsRepository.findByENTAndEJEAndCGECODAndGBSORGAndGBSFUNAndGBSECO(
            payload.ENT(), payload.EJE(), payload.CGECOD(), 
            fde.getFDEORG(), fde.getFDEFUN(), fde.getFDEECO()
        );
        
        if (bolsaOptio.isEmpty()) {
            errores.add(fde.getFDEORG() + "/" + fde.getFDEFUN() + "/" + fde.getFDEECO());
            return;
        }
        
        Gbs bolsa = bolsaOptio.get();
        Double fdeimp = fde.getFDEIMP() != null ? fde.getFDEIMP() : 0.0;
        Double fdedif = fde.getFDEDIF() != null ? fde.getFDEDIF() : 0.0;
        
        Double newGbsius = bolsa.getGBSIUS() + fdeimp + fdedif;
        Double newGbsiut = bolsa.getGBSIUT() + fdeimp + fdedif;
        
        bolsa.setGBSIUS(newGbsius);
        bolsa.setGBSIUT(newGbsiut);
        gbsRepository.save(bolsa);
    }
}