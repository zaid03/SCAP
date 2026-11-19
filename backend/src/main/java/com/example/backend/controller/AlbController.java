package com.example.backend.controller;

import com.example.backend.dto.AlbResumeDto;
import com.example.backend.dto.albFacturaDto;
import com.example.backend.dto.AsuEcoImpProjection;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.backend.sqlserver2.model.Alb;
import com.example.backend.sqlserver2.model.AlbId;
import com.example.backend.sqlserver2.repository.AlbRepository;
import com.example.backend.sqlserver2.model.Fac;
import com.example.backend.sqlserver2.model.FacId;
import com.example.backend.sqlserver2.model.Fde;
import com.example.backend.sqlserver2.repository.FacRepository;
import com.example.backend.sqlserver2.repository.AdeRepository;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.example.backend.service.AlbaranConsultaSearch;
import com.example.backend.dto.AlbaranConsultaDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Map;

import jakarta.transaction.Transactional;

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
    @Autowired
    private AlbaranConsultaSearch albaranConsultaSearch;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";
    private static final int PAGE_SIZE = 20;

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
    @GetMapping("/albaranes-factura/{concod}/{ent}/{tercod}/{eje}/{cgecod}")
    public ResponseEntity<?> fetchAlbaranesByServices(
        @PathVariable Integer concod,
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<albFacturaDto> albaranes = albRepository.findAlbFactura(concod, ent, eje, cgecod, tercod);
            if (albaranes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(albaranes);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //searching in albaranes for adding to a factura
    @GetMapping("/search-albaranes-Desde/{concod}/{ent}/{tercod}/{albdat}/{eje}/{cgecod}")
    public ResponseEntity<?> searchAlbaranesByDesde(
        @PathVariable Integer concod,
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PathVariable LocalDateTime albdat,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<albFacturaDto> albaranes = albRepository.findAlbFacturaGreaterThanEqual(concod, ent, eje, cgecod, tercod, albdat);
            if (albaranes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(albaranes);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    @GetMapping("/search-albaranes-Hasta/{concod}/{ent}/{tercod}/{albdat}/{eje}/{cgecod}")
    public ResponseEntity<?> searchAlbaranesByHasta(
        @PathVariable Integer concod,
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PathVariable LocalDateTime albdat,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<albFacturaDto> albaranes = albRepository.findAlbFacturaLessThanEqual(concod, ent, eje, cgecod, tercod, albdat);
            if (albaranes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(albaranes);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    @GetMapping("/search-albaranes-both/{concod}/{ent}/{tercod}/{albdat}/{albdat2}/{eje}/{cgecod}")
    public ResponseEntity<?> searchAlbaranesByBothDates(
        @PathVariable Integer concod,
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PathVariable LocalDateTime albdat,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PathVariable LocalDateTime albdat2,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            List<albFacturaDto> albaranes = albRepository.findAlbFacturaBothDates(concod, ent, eje, cgecod, tercod, albdat, albdat2);
            if (albaranes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(albaranes);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding an albaranes and updating required fields in other models
    public record Albaranes(Integer ENT, String EJE, Integer ALBNUM, String CONCTP, String CONCPR, String CONCCR, Double ALBBIM, Integer FACNUM, Integer CONCOD) {}
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

                    if (albData.CONCOD() != null) {
                        factura.setCONCOD(albData.CONCOD());
                    }
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
                        Double currentFDEIMP = applicacion.getFDEIMP() != null
                            ? applicacion.getFDEIMP()
                            : 0.0;
                        Double albaranIMP = result.getIMP() != null
                            ? result.getIMP()
                            : 0.0;
                        Double newFDEIMP = currentFDEIMP + albaranIMP;
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
    
    //fetch for consulta albaranes
    @GetMapping("/consulta-albaranes")
    @Transactional
    public ResponseEntity<?> albaranesConsulta(
        @RequestParam Integer ent,
        @RequestParam(required = false) String main_search,
        @RequestParam(required = false) String centroDep,
        @RequestParam(required = false) String comprador,
        @RequestParam(required = false) String ejeSearch,
        @RequestParam(required = false) String fechaTipo,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime toDate,
        @RequestParam(required = false) String estadoTipo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sort,
        @RequestParam(defaultValue = "asc") String direction
    ) {
        try {
            List<AlbaranConsultaDto> albaranes = albaranConsultaSearch.searchAlbaranConsulta(ent,main_search, centroDep, comprador, ejeSearch, fechaTipo, fromDate, toDate, estadoTipo, page, size, sort, direction);
            if (albaranes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(albaranes);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //fetching amount of rows for C.albaranes
    @GetMapping("/count-albaranes")
    public ResponseEntity<?> fetchRowsAlbaranes(
        @RequestParam Integer ent,
        @RequestParam(required = false) String main_search,
        @RequestParam(required = false) String centroDep,
        @RequestParam(required = false) String comprador,
        @RequestParam(required = false) String ejeSearch,
        @RequestParam(required = false) String fechaTipo,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fromDate,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime toDate,
        @RequestParam(required = false) String estadoTipo
    ) {
        try {
            long count = albaranConsultaSearch.countAlbaranConsulta(ent, main_search, centroDep, comprador, ejeSearch, fechaTipo, fromDate, toDate, estadoTipo);

            return ResponseEntity.ok(count);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //update albaranes from its gestion
    public record updateAlbaran(String ALBOBS) {}
    @PatchMapping("/update-albaran/{ent}/{albnum}")
    public ResponseEntity<?> albaranUpdate(
        @PathVariable Integer ent,
        @PathVariable Integer albnum,
        @RequestBody updateAlbaran payload
    ) {
        try {
            if (payload == null || payload.ALBOBS() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }
            AlbId id = new AlbId(ent, albnum);
            Optional<Alb> albaran = albRepository.findById(id);
            if (albaran.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            } 

            Alb albaranUpdate = albaran.get();
            albaranUpdate.setALBOBS(payload.ALBOBS());
            albRepository.save(albaranUpdate);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //marcar facturacion for gestion de albaranes
    public record updateFacturacion(Integer FACNUM) {}
    @PatchMapping("/facturacion/{ent}/{albnum}")
    public ResponseEntity<?> facturacion(
        @PathVariable Integer ent,
        @PathVariable Integer albnum,
        @RequestBody updateFacturacion payload
    ) {
        try {
            if (payload == null || payload.FACNUM() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            AlbId id = new AlbId(ent, albnum);
            Optional<Alb> albaran = albRepository.findById(id);
            if (albaran.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            } 

            Alb albaranUpdate = albaran.get();
            albaranUpdate.setFACNUM(payload.FACNUM());
            albRepository.save(albaranUpdate);

            return ResponseEntity.noContent().build();

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}
