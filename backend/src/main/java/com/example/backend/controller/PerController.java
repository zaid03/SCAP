package com.example.backend.controller;

import com.example.backend.sqlserver2.model.Per;
import com.example.backend.sqlserver2.repository.PerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/Per")
public class PerController {
    @Autowired
    private PerRepository perRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //for selecting all personas
    @GetMapping("/fetch-all")
    public ResponseEntity<?> fetchPersona(
    ) {
        try {
            List<Per> personas = perRepository.findAll();
            if(personas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(personas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //for search in personas first case
    @GetMapping("/search-cod-nom/{search}")
    public ResponseEntity<?> searchPersonaFirst(
        @PathVariable String search
    ) {
        try {
            List<Per> personas = perRepository.findByPERCODOrPERNOMContaining(search, search);
            if(personas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(personas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //for search in personas second case
    @GetMapping("/search-nom/{search}")
    public ResponseEntity<?> searchPersonaSecond(
        @PathVariable String search
    ) {
        try {
            List<Per> personas = perRepository.findByPERNOMContaining(search);
            if(personas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(personas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //for adding a persona
    public record personaAdd(String PERCOD, String PERNOM, String PERCOE, String PERTEL, String PERTMO, String PERCAR, String PEROBS) {}
    @PostMapping("/Insert-persona")
    public ResponseEntity<?> addPersona(
        @RequestBody personaAdd payload
    ) {
        try {
            if(payload == null || payload.PERCOD() == null || payload.PERNOM() == null ) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios");
            }
            if(perRepository.existsById(payload.PERCOD())) {
                return ResponseEntity.badRequest().body("Esta persona ya existe");
            }

            Per nueva = new Per();
            nueva.setPERCOD(payload.PERCOD());
            nueva.setPERNOM(payload.PERNOM());
            nueva.setPERCOE(payload.PERCOE());
            nueva.setPERTEL(payload.PERTEL());
            nueva.setPERTMO(payload.PERTMO());
            nueva.setPERCAR(payload.PERCAR());
            nueva.setPEROBS(payload.PEROBS());
            perRepository.save(nueva);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //for updating a persona
    public record personaUpdate(String PERCOD, String PERNOM, String PERCOE, String PERTEL, String PERTMO, String PERCAR, String PEROBS) {}
    @PatchMapping("/update-persona")
    public ResponseEntity<?> updatePersona(
        @RequestBody personaAdd payload
    ) {
        try {
            if(payload == null || payload.PERCOD() == null || payload.PERNOM() == null ) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios");
            }

            Optional<Per> persona = perRepository.findById(payload.PERCOD());
            if(persona.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Per personaUpdate = persona.get();
            personaUpdate.setPERCOD(payload.PERCOD());
            personaUpdate.setPERNOM(payload.PERNOM());
            personaUpdate.setPERCOE(payload.PERCOE());
            personaUpdate.setPERTEL(payload.PERTEL());
            personaUpdate.setPERTMO(payload.PERTMO());
            personaUpdate.setPERCAR(payload.PERCAR());
            personaUpdate.setPEROBS(payload.PEROBS());

            perRepository.save(personaUpdate);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}