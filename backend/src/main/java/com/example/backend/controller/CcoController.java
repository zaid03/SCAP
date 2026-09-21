package com.example.backend.controller;

import com.example.backend.sqlserver2.model.Cco;
import com.example.backend.sqlserver2.model.CcoId;
import com.example.backend.sqlserver2.repository.CcoRepository;
import com.example.backend.sqlserver2.repository.DepRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cco")
public class CcoController {
    @Autowired
    private CcoRepository ccoRepository;

    @Autowired 
    private DepRepository depRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //selecting all centros de coste
    @GetMapping("/fetch-all/{ent}/{eje}")
    public ResponseEntity<?> fetchCosteAll(
        @PathVariable Integer ent,
        @PathVariable String eje
    ) {
        try {
            List<?> centros =  ccoRepository.findByENTAndEJE(ent, eje);
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

    //search by ccocod
    @GetMapping("/filter-by/{ent}/{eje}/{ccocod}")
    public ResponseEntity<?> searchCoste(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String ccocod
    ) {
        try {
            List<?> centros =  ccoRepository.findByENTAndEJEAndCCOCOD(ent, eje, ccocod);
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

    //search by ccodes
    @GetMapping("/filter-by-des/{ent}/{eje}/{ccodes}")
    public ResponseEntity<?> searchCosteLike(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String ccodes
    ) {
        try {
            List<?> centros =  ccoRepository.findByENTAndEJEAndCCODESContaining(ent, eje, ccodes);
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

    //to add a centro de coste
    public record NewCentro(Integer ENT, String EJE, String CCOCOD, String CCODES) {}
    @PostMapping("/Insert-centro")
    public ResponseEntity<?> insertCentro(
        @RequestBody NewCentro payload
    ) {
        try {
            if (payload == null || payload.ENT() == null || payload.EJE() == null || payload.CCOCOD() == null || payload.CCODES() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            if(!ccoRepository.findByENTAndEJEAndCCOCOD(payload.ENT(), payload.EJE(), payload.CCOCOD()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(SIN_RESULTADO);
            }

            Cco nueva = new Cco();
            nueva.setENT(payload.ENT());
            nueva.setEJE(payload.EJE());
            nueva.setCCOCOD(payload.CCOCOD());
            nueva.setCCODES(payload.CCODES());

            ccoRepository.save(nueva);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //update a centro coste
    public record UpdateCentro(String CCODES) {}
    @PatchMapping("/update-centro/{ent}/{eje}/{ccocod}")
    public ResponseEntity<?> UpdateCentro(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String ccocod,
        @RequestBody UpdateCentro payload
    ) {
        try {
            if (payload == null || payload.CCODES() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            CcoId id = new CcoId(ent, eje, ccocod);
            Optional<Cco> coste = ccoRepository.findById(id);
            if (coste.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Cco costeUpdate = coste.get();
            costeUpdate.setCCODES(payload.CCODES());

            ccoRepository.save(costeUpdate);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //delete a centro de coste
    @DeleteMapping("/delete-coste/{ent}/{eje}/{ccocod}")
    public ResponseEntity<?> deleteCoste(
        @PathVariable Integer ent,
        @PathVariable String eje,
        @PathVariable String ccocod
    ) {
        try {
            CcoId id = new CcoId(ent, eje, ccocod);
            if(!ccoRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(SIN_RESULTADO);
            }

            long costes = depRepository.countByENTAndEJEAndCCOCOD(ent, eje, ccocod);
            if (costes > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede borrar un centro de coste con centros gestores asociados");
            }

            ccoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}
