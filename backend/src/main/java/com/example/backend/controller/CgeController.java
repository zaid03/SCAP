package com.example.backend.controller;

import com.example.backend.dto.CentroGestorLogin;
import com.example.backend.dto.shortCentroContrato;
import com.example.backend.sqlserver2.model.Cge;
import com.example.backend.sqlserver2.model.CgeId;
import com.example.backend.sqlserver2.model.Dep;
import com.example.backend.sqlserver2.model.Dpe;
import com.example.backend.sqlserver2.repository.CgeRepository;
import com.example.backend.sqlserver2.repository.GbsRepository;
import com.example.backend.sqlserver2.repository.DepRepository;
import com.example.backend.sqlserver2.repository.DpeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataAccessException;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cge")
public class CgeController {
    @Autowired
    private CgeRepository cgeRepository;
    @Autowired
    private GbsRepository gbsRepository;
    @Autowired
    private DepRepository depRepository;
    @Autowired
    private DpeRepository dpeRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //selecting centro gestor for login
    @GetMapping("/{ent}/{eje}/{percod}")
    public ResponseEntity<?> getCentrosGestores(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String percod
    ) {
        try {
            List<Dpe> dpes = dpeRepository.findByENTAndEJEAndPERCOD(ent, eje, percod);
            if (dpes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            List<String> depcods = dpes.stream()
                .map(Dpe::getDEPCOD)
                .distinct()
                .toList();

            List<Dep> deps = depRepository.findByENTAndEJEAndDEPCODIn(ent, eje, depcods);
            if (deps.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Map<String, Dep> depByCgecod = deps.stream().collect(Collectors.toMap(
                Dep::getCGECOD,
                dep -> dep,
                this::mergeDeps 
            ));

            List<String> cgecods = List.copyOf(depByCgecod.keySet());
            List<Cge> cges = cgeRepository.findByENTAndEJEAndCGECODIn(ent, eje, cgecods);
            if (cges.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            List<CentroGestorLogin> result = cges.stream().map(cge -> {
                Dep dep = depByCgecod.get(cge.getCGECOD());
                return new CentroGestorLogin(
                    cge,
                    dep.getDEPINT(),
                    dep.getDEPALM(),
                    dep.getDEPCOM()
                );
            }).toList();

            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMessage());
        }
    }

    private Dep mergeDeps(Dep existing, Dep replacement) {
        existing.setDEPINT(Math.max(
            existing.getDEPINT() != null ? existing.getDEPINT() : 0, 
            replacement.getDEPINT() != null ? replacement.getDEPINT() : 0
        ));
        existing.setDEPALM(Math.max(
            existing.getDEPALM() != null ? existing.getDEPALM() : 0, 
            replacement.getDEPALM() != null ? replacement.getDEPALM() : 0
        ));
        existing.setDEPCOM(Math.max(
            existing.getDEPCOM() != null ? existing.getDEPCOM() : 0, 
            replacement.getDEPCOM() != null ? replacement.getDEPCOM() : 0
        ));
        return existing;
    }

    //selecting all centro gestores
    @GetMapping("/fetch-all/{ent}/{eje}")
    public ResponseEntity<?> fetchAllCentroGestores(
        @PathVariable Integer ent,
        @PathVariable String eje
    ) {
        try {
            List<Cge> centros = cgeRepository.findByENTAndEJE(ent, eje);
            if (centros.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(centros);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //update centro gestor
    public record CentroUpdate(String cgedes, String cgeorg, String cgefun, String cgedat, Integer cgecic) {}

    @PatchMapping("/update-cge/{ent}/{eje}/{cge}")
    @Transactional
    public ResponseEntity<?> updateCentro(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String cge,
        @RequestBody CentroUpdate payload
    ) {
        try {
            if (payload == null || payload.cgedes() == null || payload.cgeorg() == null || payload.cgefun() == null || payload.cgedat() == null || payload.cgecic() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            CgeId id = new CgeId(ent, eje, cge);
            Optional<Cge> opt = cgeRepository.findById(id);

            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Cge entity = opt.get();
            entity.setCGEDES(payload.cgedes());
            entity.setCGEORG(payload.cgeorg());
            entity.setCGEFUN(payload.cgefun());
            entity.setCGEDAT(payload.cgedat());
            entity.setCGECIC(payload.cgecic());

            cgeRepository.save(entity);
            return ResponseEntity.noContent().build();

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
    //add centro gestor
    public record CentroAdd(Integer ent, String eje, String cgecod, String cgedes, String cgeorg, String cgefun, String cgedat, Integer cgecic) {}

    @PostMapping("/Insert-familia")
    public ResponseEntity<?> addCentroGestor(
        @RequestBody CentroAdd payload
    ) {
        try {
            if (payload == null || payload.ent() == null || payload.eje() == null || payload.cgecod() == null || payload.cgedes() == null || payload.cgeorg() == null || payload.cgefun() == null || payload.cgedat() == null || payload.cgecic() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            if(!cgeRepository.findByENTAndEJEAndCGECOD(payload.ent(), payload.eje(), payload.cgecod()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(SIN_RESULTADO);
            }
            
            Cge nueva = new Cge();
            nueva.setENT(payload.ent());
            nueva.setEJE(payload.eje());
            nueva.setCGECOD(payload.cgecod());
            nueva.setCGEDES(payload.cgedes());
            nueva.setCGEORG(payload.cgeorg());
            nueva.setCGEFUN(payload.cgefun());
            nueva.setCGEDAT(payload.cgedat());
            nueva.setCGECIC(payload.cgecic());

            cgeRepository.save(nueva);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch(DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //to delete a centro gestor
    @DeleteMapping("/delete-centro-gestor/{ent}/{eje}/{cgecod}")
    public ResponseEntity<?> deleteSubFamilia(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String cgecod
    ) {
        try {
            Long bolsas = gbsRepository.countByENTAndEJEAndCGECOD(ent, eje, cgecod);
            if (bolsas > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("No puede borrar un centro gestor que tiene bolsas de crédito");
            }

            long services = depRepository.countByENTAndEJEAndCGECOD(ent, eje, cgecod);
            if (services > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("No puede borrar un centro gestor que tiene servicios");
            }

            CgeId id = new CgeId(ent, eje, cgecod);
            if (!cgeRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }
            cgeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch(DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //fetching description for services
    @GetMapping("/fetch-description-services/{ent}/{eje}/{cgecod}")
    public ResponseEntity<String> fetchDescriptionForCge(
            @PathVariable Integer ent,
            @PathVariable String eje,
            @PathVariable String cgecod) {
        try {
            Optional<String> description = cgeRepository.findFirstByENTAndEJEAndCGECOD(ent, eje, cgecod).map(Cge::getCGEDES);
            if (description.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(description.get());
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //search in cge
    @GetMapping("/search-centros/{ent}/{eje}/{term}")
    public ResponseEntity<?> searchCentros(
            @PathVariable Integer ent,
            @PathVariable String eje,
            @PathVariable String term) {
        try {
            List<Cge> centros =
            cgeRepository.findByENTAndEJEAndCGECODOrENTAndEJEAndCGEDESContaining(
                ent, eje, term,
                ent, eje, term
            );
            if (centros.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(centros);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //searching centos for contratos
    @GetMapping("/search-centros-codigo/{ent}/{eje}/{cgecod}")
    public ResponseEntity<?> searchCentrosCodigo(
            @PathVariable Integer ent,
            @PathVariable String eje,
            @PathVariable String cgecod) {
        try {
            List<shortCentroContrato> centros = cgeRepository.findByENTAndEJEAndCGECOD(ent, eje, cgecod);
            if (centros.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(centros);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //searching centos for contratos
    @GetMapping("/search-centros-description/{ent}/{eje}/{term}")
    public ResponseEntity<?> searchCentrosDesc(
            @PathVariable Integer ent,
            @PathVariable String eje,
            @PathVariable String term) {
        try {
            List<shortCentroContrato> centros = cgeRepository.findByENTAndEJEAndCGEDESContaining(ent, eje, term);
            if (centros.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(centros);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}