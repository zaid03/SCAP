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

import com.example.backend.dto.ArticuloFamilia;
import com.example.backend.dto.ArticuloSubfamilia;
import com.example.backend.dto.ArticuloArticulo;
import com.example.backend.sqlserver2.model.Afa;
import com.example.backend.sqlserver2.model.AfaId;
import com.example.backend.sqlserver2.repository.AfaRepository;
import com.example.backend.sqlserver2.repository.AsuRepository;
import com.example.backend.sqlserver2.repository.ArtRepository;

@RestController
@RequestMapping("/api/afa")
public class AfaController {
    @Autowired
    private AfaRepository afaRepository;
    @Autowired
    private AsuRepository asuRepository;
    @Autowired
    private ArtRepository artRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";
    
    @GetMapping("/by-ent/{ent}/{afacod}")
    public ResponseEntity<?> getByEntAndAfacod(
        @PathVariable int ent, 
        @PathVariable String afacod
    ) {
        try{
            List<Afa> familias = afaRepository.findByENTAndAFACOD(ent, afacod);
            if(familias.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(familias);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //method to fetch articulos to add to proveedor
    private boolean isNumbersOnly(String text) {return text.matches("^[0-9]+$");}
    @GetMapping("/fetch-articulos-proveedor/{ent}/{searchType}/{term}")
    public ResponseEntity<?> proveedorArticulosFetch (
        @PathVariable Integer ent,
        @PathVariable String searchType,
        @PathVariable String term
    ) {
        try {
            if (isNumbersOnly(term)) {
                if (searchType.equals("familia")) {
                    List<ArticuloFamilia> articulos = afaRepository.findAllByENTAndAFACOD(ent, term);
                    return ResponseEntity.ok(articulos);
                } else if (searchType.equals("subfamilia")) {
                    List<ArticuloSubfamilia> articulos = asuRepository.findByENTAndAFACODOrENTAndASUCOD(ent, term, ent, term);
                    return ResponseEntity.ok(articulos);
                } else if (searchType.equals("articulo")) {
                    List<ArticuloArticulo> articulos = artRepository.findByENTAndAFACODOrENTAndASUCODOrENTAndARTCOD(ent, term, ent, term, ent, term);
                    return ResponseEntity.ok(articulos);
                }
            } else {
                if (searchType.equals("familia")) {
                    List<ArticuloFamilia> articulos = afaRepository.findByENTAndAFADESContaining(ent, term);
                    return ResponseEntity.ok(articulos);
                } else if (searchType.equals("subfamilia")) {
                    List<ArticuloSubfamilia> articulos = asuRepository.findByENTAndASUDESContaining(ent, term);
                    return ResponseEntity.ok(articulos);
                } else if (searchType.equals("articulo")) {
                    List<ArticuloArticulo> articulos = artRepository.findByENTAndARTDESContaining(ent, term);
                    return ResponseEntity.ok(articulos);
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
        }catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        } 
    }

    //find familias by ent
    @GetMapping("/by-ent/{ent}")
    public ResponseEntity<?> getAfaByEnt(
        @PathVariable int ent
    ) {
        try {
            List<Afa> familias = afaRepository.findByENT(ent);
            if(familias.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(familias);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //update description of familias
    public record UpdateFamilia(String AFADES) {}
    @PatchMapping("/update-familia/{ent}/{afacod}")
    public ResponseEntity<?> updateFamilia(
        @PathVariable Integer ent,
        @PathVariable String afacod,
        @RequestBody UpdateFamilia payload
    ) {
        try {
            if(payload == null || payload.AFADES() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            AfaId id = new AfaId(ent, afacod);
            Optional<Afa> familia = afaRepository.findById(id);
            if(familia.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Afa familiaUpdate = familia.get();
            familiaUpdate.setAFADES(payload.AFADES());

            afaRepository.save(familiaUpdate);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Update failed: " + ex.getMostSpecificCause().getMessage());
        }
    }

    //familia add
    public record NewFamilia(Integer ent, String afacod, String afades) {}
    @PostMapping("/Insert-familia")
    public ResponseEntity<?> insertFamilia(
        @RequestBody NewFamilia payload
    )
    {
        if (payload == null || payload.ent() == null || payload.afacod() == null || payload.afades() == null) {
            return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
        }

        if (!afaRepository.findByENTAndAFACOD(payload.ent(), payload.afacod()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(SIN_RESULTADO);
        }

        Afa nueva = new Afa();
        nueva.setENT(payload.ent());
        nueva.setAFACOD(payload.afacod());
        nueva.setAFADES(payload.afades());

        afaRepository.save(nueva);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
