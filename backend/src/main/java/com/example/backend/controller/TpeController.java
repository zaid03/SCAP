package com.example.backend.controller;

import com.example.backend.sqlserver2.model.Tpe;
import com.example.backend.sqlserver2.model.TpeId;
import com.example.backend.sqlserver2.repository.TpeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/more")
public class TpeController {
    @Autowired
    private TpeRepository tpeRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    // Custom query to find Tpe by ENT and TERCOD
    @GetMapping("/by-tpe/{ent}/{tercod}")
    public ResponseEntity<?> getByEntAndTercod(
        @PathVariable int ent,
        @PathVariable int tercod
    ) {
        try {
            List<Tpe> personas = tpeRepository.findByENTAndTERCOD(ent, tercod);
            if(personas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(personas);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error" + ex.getMostSpecificCause().getMessage());
        }
    }

    // modifying personas de contacto
    public record personaContacto(String tpenom, String tpetel, String tpetmo, String tpecoe, String tpeobs) {}
    @PutMapping("/modify/{ent}/{tercod}/{tpecod}")
    public ResponseEntity<?> modifyTpe(
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @PathVariable Integer tpecod,
        @RequestBody personaContacto payload
    ) {
        try {
            if (payload == null || payload.tpenom() == null) {
                return ResponseEntity.badRequest().body("nombre requerido.");
            }

            TpeId id = new TpeId(ent, tercod, tpecod);
            Optional<Tpe> persona = tpeRepository.findById(id);
            if(persona.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Tpe updatePersona = persona.get();
            updatePersona.setTPENOM(payload.tpenom());
            updatePersona.setTPETEL(payload.tpetel());
            updatePersona.setTPETMO(payload.tpetmo());
            updatePersona.setTPECOE(payload.tpecoe());
            updatePersona.setTPEOBS(payload.tpeobs());
  
            tpeRepository.save(updatePersona);
            return ResponseEntity.noContent().build();
        } catch(DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding a persona de contacto
    public record personaAdd(String tpenom, String tpetel, String tpetmo, String tpecoe, String tpeobs) {}
    @PostMapping("/add/{ent}/{tercod}")
    @Transactional
    public ResponseEntity<?> addTpe(
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @RequestBody personaAdd payload
    ) {
        try {
            if (payload == null || payload.tpenom() == null) {
                return ResponseEntity.badRequest().body("Nombre requerido.");
            }

            boolean name = tpeRepository.existsByENTAndTERCODAndTPENOM(ent, tercod, payload.tpenom());
            if (name){
                return ResponseEntity.badRequest().body("Nombre existe.");
            }

            Tpe lastTpe = tpeRepository.findFirstByENTAndTERCODOrderByTPECODDesc(ent, tercod);
            int nextTpecod = (lastTpe == null ? 1 : lastTpe.getTPECOD() + 1);   
            Tpe tpe = new Tpe();
            tpe.setENT(ent);
            tpe.setTERCOD(tercod);
            tpe.setTPECOD(nextTpecod);
            tpe.setTPENOM(payload.tpenom());
            tpe.setTPETEL(payload.tpetel());
            tpe.setTPETMO(payload.tpetmo());
            tpe.setTPECOE(payload.tpecoe());
            tpe.setTPEOBS(payload.tpeobs());

            tpeRepository.save(tpe);
            return ResponseEntity.status(HttpStatus.CREATED).body("Persona de contacto agregada correctamente");
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("La inserción falló: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //Deleting a persona de contacto
    @DeleteMapping("/delete/{ent}/{tercod}/{tpecod}")
    @Transactional
    public ResponseEntity<?> deleteTpe(
        @PathVariable Integer ent, 
        @PathVariable Integer tercod,
        @PathVariable Integer tpecod
    ) {
        try {
            TpeId id = new TpeId(ent, tercod, tpecod);
            if(!tpeRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }
            tpeRepository.deleteById(id);
            return ResponseEntity.ok("La persona ha sido eliminada con éxito");
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}