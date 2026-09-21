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

import com.example.backend.sqlserver2.model.Aun;
import com.example.backend.sqlserver2.model.AunId;
import com.example.backend.sqlserver2.repository.ArtRepository;
import com.example.backend.sqlserver2.repository.AunRepository;

@RestController
@RequestMapping("/api/aun")
public class AunController {
    @Autowired
    private AunRepository aunRepository;
    @Autowired
    private ArtRepository artRepository;

    //fetching main list
    @GetMapping("/fetch-list/{ent}")
    public ResponseEntity<?> fetchList(
        @PathVariable Integer ent
    ) {
        try {
            List<Aun> unidades = aunRepository.findByENT(ent);
            if(unidades.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }

            return ResponseEntity.ok(unidades);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //search by auncod
    @GetMapping("/search-codigo/{ent}/{auncod}")
    public ResponseEntity<?> searchByCodigo(
        @PathVariable Integer ent,
        @PathVariable String auncod
    ) {
        try {
            List<Aun> unidades = aunRepository.findByENTAndAUNCOD(ent, auncod);
            if(unidades.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }

            return ResponseEntity.ok(unidades);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //search by descripcion
    @GetMapping("/search-decripcion/{ent}/{aundes}")
    public ResponseEntity<?> searchByDescripcion(
        @PathVariable Integer ent,
        @PathVariable String aundes
    ) {
        try {
            List<Aun> unidades = aunRepository.findByENTAndAUNDESContaining(ent, aundes);
            if (unidades.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }

            return ResponseEntity.ok(unidades);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //modifying a tipo de unidades
    public record updateUnidad(String AUNDES) {}
    @PatchMapping("/update-unidad/{ent}/{auncod}")
    public ResponseEntity<?> updateUnidad(
        @PathVariable Integer ent,
        @PathVariable String auncod,
        @RequestBody updateUnidad payload
    ) {
        try {
            if (payload == null || payload.AUNDES() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            AunId id = new AunId(ent, auncod);
            Optional<Aun> unidadOptio = aunRepository.findById(id);
            if (unidadOptio.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }

            Aun unidad = unidadOptio.get();
            unidad.setAUNDES(payload.AUNDES());
            aunRepository.save(unidad);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding a unidad
    public record addUnidad(Integer ENT, String AUNCOD, String AUNDES) {}
    @PostMapping("/add-unidad")
    public ResponseEntity<?> addUnidad(
        @RequestBody addUnidad payload
    ) {
        try {
            if (payload == null || payload.ENT() == null || payload.AUNCOD() == null || payload.AUNDES == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            AunId id = new AunId(payload.ENT(), payload.AUNCOD());
            if (aunRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("El tipo de unidad ya existe");
            }

            Aun unidad = new Aun();
            unidad.setENT(payload.ENT());
            unidad.setAUNCOD(payload.AUNCOD());
            unidad.setAUNDES(payload.AUNDES());
            aunRepository.save(unidad);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //deleting a tipo de unidad
    @DeleteMapping("/delete-unidad/{ent}/{auncod}")
    public ResponseEntity<?> deleteUnidad(
        @PathVariable Integer ent,
        @PathVariable String auncod
    ) {
        try {
            int articulos = artRepository.countByENTAndAUNCOD(ent, auncod);
            if (articulos > 0) {
                return ResponseEntity.badRequest().body("No se puede borrar porque está asociado a un artículo");
            }

            AunId id = new AunId(ent, auncod);
            if (!aunRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("El tipo de unidad no existe");
            }
            aunRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //needed for select lists in mantinimiento general de articulos
    @GetMapping("/get-all/{ent}")
    public ResponseEntity<?> allGet (
        @PathVariable Integer ent
    ) {
        try {
            if (ent == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            List<Aun> tipos = aunRepository.findByENT(ent);
            if (tipos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }

            return ResponseEntity.ok(tipos);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }
}