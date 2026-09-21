package com.example.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.sqlserver2.model.Mta;
import com.example.backend.sqlserver2.model.MtaId;
import com.example.backend.sqlserver2.repository.MtaRepository;
import com.example.backend.sqlserver2.repository.AsuRepository;
import com.example.backend.dto.AlmacenajeAddDto;

@RestController
@RequestMapping("/api/mta")
public class MtaController {
    @Autowired
    private MtaRepository mtaRepository;
    @Autowired
    private AsuRepository asuRepository;

    //to fetch all MTAs
    @GetMapping("/all-mta/{ent}")
    public List<Mta> getAlmacenaje(
        @PathVariable Integer ent
    ) {
        return mtaRepository.findByENT(ent);
    }

    //to fetch by ent and mtacod
    @GetMapping("/mta-filter/{ent}/{mtacod}")
    public ResponseEntity<?> filterAlmacenaje(
        @PathVariable Integer ent,
        @PathVariable Integer mtacod
    ) {
        try {
            List<Mta> result = mtaRepository.findByENTAndMTACOD(ent, mtacod);
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }
            return ResponseEntity.ok(result);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //search by mtades
    @GetMapping("/search-almacenaje/{ent}/{mtades}")
    public ResponseEntity<?> searchAlmacenaje(
        @PathVariable Integer ent,
        @PathVariable String mtades
    ) {
        try {
            List<Mta> almacenajes = mtaRepository.findByENTAndMTADESContaining(ent, mtades);
            if (almacenajes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }
            return ResponseEntity.ok(almacenajes);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //updating almacenaje
    public record updateAlmacenaje(String MTADES) {}
    @PatchMapping("/update-almacenaje/{ent}/{mtacod}")
    public ResponseEntity<?> updateAlmacenaje(
        @PathVariable Integer ent,
        @PathVariable Integer mtacod,
        @RequestBody updateAlmacenaje payload
    ) {
        try {
            if (payload == null || payload.MTADES() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios");
            }

            MtaId id = new MtaId(ent, mtacod);
            Optional<Mta> almacenajeOptio = mtaRepository.findById(id);
            if (almacenajeOptio.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }

            Mta almacenaje = almacenajeOptio.get();
            almacenaje.setMTADES(payload.MTADES());
            mtaRepository.save(almacenaje);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding almacenaje
    public record addAlmacenaje(Integer ENT ,String MTADES) {}
    @PostMapping("/add-almacenaje")
    public ResponseEntity<?> addAlmacenaje(
        @RequestBody addAlmacenaje payload
    ) {
        try {
            if (payload == null || payload.ENT() == null || payload.MTADES() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios");
            }

            List<AlmacenajeAddDto> codigo = mtaRepository.findDtoByENT(payload.ENT());

            Integer newCodigo = codigo.stream()
                .map(dto -> dto.getMTACOD())
                .max(Integer::compareTo)
                .orElse(0) + 1;
            System.out.println("newCodigo: " + newCodigo);

            Mta newAlmacenaje = new Mta();
            newAlmacenaje.setENT(payload.ENT());
            newAlmacenaje.setMTACOD(newCodigo);
            newAlmacenaje.setMTADES(payload.MTADES());


            mtaRepository.save(newAlmacenaje);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //deleting almacenaje
    @DeleteMapping("/delete-almacenaje/{ent}/{mtacod}")
    public ResponseEntity<?> deleteAlmacenaje(
        @PathVariable Integer ent,
        @PathVariable Integer mtacod
    ) {
        try {
            int subfamilias = asuRepository.countByENTAndMTACOD(ent, mtacod);
            if (subfamilias != 0) {
                return ResponseEntity.badRequest().body("No se puede borrar porque está asociado a una subfamilia"); 
            }

            MtaId id = new MtaId(ent, mtacod);
            if (!mtaRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }

            mtaRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }
}
