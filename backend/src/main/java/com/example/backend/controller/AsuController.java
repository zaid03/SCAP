package com.example.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.sqlserver2.model.Asu;
import com.example.backend.sqlserver2.model.AsuId;
import com.example.backend.sqlserver2.repository.AsuRepository;

@RestController
@RequestMapping("/api/asu")
public class AsuController {

    @Autowired
    private AsuRepository asuRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //find an art name
    @GetMapping("/art-name/{ent}/{afacod}/{asucod}")
    public ResponseEntity<?> getArtName(
        @PathVariable int ent,
        @PathVariable String afacod,
        @PathVariable String asucod
    ) {
        try {
            List<Asu> subfamilias = asuRepository.findByENTAndAFACODAndASUCOD(ent, afacod, asucod);
            if(subfamilias.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(subfamilias);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //filtering subs by ent and afacod
    @GetMapping("/by-ent-afacod/{ent}/{afacod}")
    public ResponseEntity<?> getSubfamilias(
        @PathVariable int ent,
        @PathVariable String afacod
    ) {
        try {
            List<Asu> subfamilias = asuRepository.findByENTAndAFACOD(ent, afacod);
            if(subfamilias.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(subfamilias);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //for updating subs
    public record NewSubFamilia(String ASUDES, String ASUECO, Integer MTACOD) {}

    @PatchMapping("/update-subfamilia/{ent}/{afacod}/{asucod}")
    public ResponseEntity<?> updateSubFamilia(
        @PathVariable Integer ent,
        @PathVariable String afacod,
        @PathVariable("asucod") String asucod,
        @RequestBody NewSubFamilia payload
    ) {
        try {
            if (payload == null || payload.ASUDES() == null || payload.ASUECO() == null || payload.MTACOD() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            AsuId id = new AsuId(ent, afacod, asucod);
            Optional<Asu> subfamilia = asuRepository.findById(id);
            if (subfamilia.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Asu subfamiliaUpdate = subfamilia.get();
            subfamiliaUpdate.setASUDES(payload.ASUDES());
            subfamiliaUpdate.setASUECO(payload.ASUECO());
            subfamiliaUpdate.setMTACOD(payload.MTACOD());

            asuRepository.save(subfamiliaUpdate);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //subfamilia Add
    public record NewSub(Integer ent, String afacod, String asucod, String asudes, String asueco, Integer mtacod) {}

    @PostMapping("/Insert-Subfamilia")
    public ResponseEntity<?> insertSub(
        @RequestBody NewSub payload
    ) {
        try {
            if (payload == null || payload.ent() == null || payload.afacod() == null || payload.asucod() == null || payload.asudes() == null || payload.asueco() == null || payload.mtacod() == null) {
            return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            if (!asuRepository.findByENTAndAFACODAndASUCOD(payload.ent(), payload.afacod(), payload.asucod()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(SIN_RESULTADO);
            }

            Asu nueva = new Asu();
            nueva.setENT(payload.ent());
            nueva.setAFACOD(payload.afacod());
            nueva.setASUCOD(payload.asucod());
            nueva.setASUDES(payload.asudes());
            nueva.setASUECO(payload.asueco());
            nueva.setMTACOD(payload.mtacod());

            asuRepository.save(nueva);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}
