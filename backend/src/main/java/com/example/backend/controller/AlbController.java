package com.example.backend.controller;

import com.example.backend.dto.AlbResumeDto;
import com.example.backend.dto.albFacturaDto;
import com.example.backend.dto.AsuEcoImpProjection;

import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.backend.sqlserver2.model.Alb;
import com.example.backend.sqlserver2.model.AlbId;
import com.example.backend.sqlserver2.repository.AlbRepository;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.FacId;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.repository.FacRepository;
import com.example.backend.sqlserver2.repository.AdeRepository;
import com.example.backend.sqlserver2.repository.FdeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/alb")
public class AlbController {
    @Autowired
    private AlbRepository albRepository;
    @Autowired
    private FacRepository facRepository;
    @Autowired
    private AdeRepository adeRepository;
    @Autowired
    private FdeRepository fdeRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //fetch albaranes for facturas
    @GetMapping("/albaranes/{ent}/{eje}/{facnum}")
    public ResponseEntity<?> getAlbaranesByFactura(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable Integer facnum
    ) {
        try {
            List<Alb> albaranes = albRepository.findByENTAndEJEAndFACNUM(ent, eje, facnum);
            
            if(albaranes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            List<AlbResumeDto> result = albaranes.stream()
                .map(a -> new AlbResumeDto(
                    a.getALBNUM(),
                    a.getALBREF(),
                    a.getALBDAT(),
                    a.getALBBIM(),
                    a.getSOLNUM(),
                    a.getSOLSUB(),
                    a.getALBOBS()
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //fetching albaranes for adding to a factura
    @GetMapping("/albaranes-factura/{ent}/{tercod}/{eje}/{cgecod}")
    public ResponseEntity<?> fetchAlbaranesByServices(
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<albFacturaDto> albaranes = albRepository.findAlbFactura(ent, tercod, 0, eje, cgecod);
            if (albaranes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(albaranes);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //searching in albaranes for adding to a factura
    @GetMapping("/search-albaranes-Desde/{ent}/{tercod}/{albdat}/{eje}/{cgecod}")
    public ResponseEntity<?> searchAlbaranesByDesde(
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PathVariable LocalDateTime albdat,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<albFacturaDto> albaranes = albRepository.findAlbFacturaGreaterThanEqual(ent, tercod, 0, albdat, eje, cgecod);
            if (albaranes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(albaranes);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    @GetMapping("/search-albaranes-Hasta/{ent}/{tercod}/{albdat}/{eje}/{cgecod}")
    public ResponseEntity<?> searchAlbaranesByHasta(
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PathVariable LocalDateTime albdat,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<albFacturaDto> albaranes = albRepository.findAlbFacturaLessThanEqual(ent, tercod, 0, albdat, eje, cgecod);
            if (albaranes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(albaranes);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding an albaranes and updating required fields in other models
    public record Albaranes(Integer ENT, String EJE, Integer ALBNUM, String CONCTP, String CONCPR, String CONCCR, Double ALBBIM, Integer FACNUM) {}
    @PatchMapping("/add-albaranes")
    public ResponseEntity<?> addingAlbaranes(
        @RequestBody List<Albaranes> payload
    ) {
        try {
            if (payload == null || payload.isEmpty()) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            for (Albaranes albData : payload) {
                AlbId id = new AlbId(albData.ENT(), albData.ALBNUM());
                Optional<Alb> albaranOpt = albRepository.findById(id);
                if (albaranOpt.isPresent()) {
                    Alb albaran = albaranOpt.get();
                    albaran.setEJE(albData.EJE());
                    albaran.setFACNUM(albData.FACNUM());
                    albRepository.save(albaran);
                }

                FacId facId = new FacId(albData.ENT(), albData.EJE(), albData.FACNUM());
                Optional<Fac> facturaOpt = facRepository.findById(facId);
                if (facturaOpt.isPresent()) {
                    Fac factura = facturaOpt.get();
                    Double newFaciec = factura.getFACIEC() + albData.ALBBIM();
                    factura.setCONCTP(albData.CONCTP());
                    factura.setCONCPR(albData.CONCPR());
                    factura.setCONCCR(albData.CONCCR());
                    factura.setFACIEC(newFaciec);
                    facRepository.save(factura);
                }

                Optional<AsuEcoImpProjection> resultOpt = adeRepository.findSumByEntAndAlbnum(albData.ENT() , albData.ALBNUM());

                if (resultOpt.isPresent()) {
                    AsuEcoImpProjection result = resultOpt.get();
                    Optional<Fde> applicacionOptio = fdeRepository.findByENTAndEJEAndFACNUMAndFDEECO(
                        albData.ENT(), 
                        albData.EJE(), 
                        albData.FACNUM(),
                        result.getASUECO()
                    );

                    if (applicacionOptio.isPresent()) {
                        Fde applicacion = applicacionOptio.get();
                        Double newFDEIMP = result.getIMP();
                        applicacion.setFDEIMP(newFDEIMP);
                        fdeRepository.save(applicacion);
                    }
                }
            }

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //quitar albaranes
    public record Quitar(Integer ENT, String EJE, Integer ALBNUM, Integer FACNUM, Double ALBBIM) {}
    @PatchMapping("/quitar-albaranes")
    public ResponseEntity<?> quitarAlbaranes(
        @RequestBody Quitar payload
    ) {
        try {
            if (payload == null || payload.ENT() == null || payload.ALBNUM() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            AlbId id = new AlbId(payload.ENT(), payload.ALBNUM());
            Optional<Alb> albaranOpt = albRepository.findById(id);
            if (albaranOpt.isPresent()) {
                Alb albaran = albaranOpt.get();
                albaran.setEJE(null);
                albaran.setFACNUM(0);
                albRepository.save(albaran);
            }

            FacId facId = new FacId(payload.ENT(), payload.EJE(), payload.FACNUM());
            Optional<Fac> facturaOpt = facRepository.findById(facId);
            if (facturaOpt.isPresent()) {
                Fac factura = facturaOpt.get();
                Double newFACIEC = factura.getFACIEC() - payload.ALBBIM();
                factura.setFACIEC(newFACIEC);
                facRepository.save(factura);
            }

            Optional<AsuEcoImpProjection> resultOpt = adeRepository.findSumByEntAndAlbnum(payload.ENT() , payload.ALBNUM());
            if (resultOpt.isPresent()) {
                AsuEcoImpProjection result = resultOpt.get();
                Optional<Fde> applicacionOptio = fdeRepository.findByENTAndEJEAndFACNUMAndFDEECO(
                    payload.ENT(), 
                    payload.EJE(), 
                    payload.FACNUM(),
                    result.getASUECO()
                );

                if (applicacionOptio.isPresent()) {
                    Fde applicacion = applicacionOptio.get();
                    Double newFDEIMP = applicacion.getFDEIMP() - result.getIMP();
                    applicacion.setFDEIMP(newFDEIMP);
                    fdeRepository.save(applicacion);
                }
            }

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}
