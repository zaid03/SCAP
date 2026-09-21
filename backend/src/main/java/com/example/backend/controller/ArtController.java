package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ArtAsuContratoProjection;
import com.example.backend.dto.ArtNameProjection;
import com.example.backend.sqlserver2.repository.AfaRepository;
import com.example.backend.sqlserver2.repository.ArtRepository;
import com.example.backend.sqlserver2.repository.AsuRepository;

@RestController
@RequestMapping("/api/art")
public class ArtController {

    @Autowired
    private ArtRepository artRepository;
    @Autowired
    private AfaRepository afaRepository;
    @Autowired
    private AsuRepository asuRepository;
    
    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //find an art name
    @GetMapping("/art-name/{ent}/{afacod}/{asucod}/{artcod}")
    public ResponseEntity<?> getArtName(
        @PathVariable int ent,
        @PathVariable String afacod,
        @PathVariable String asucod,
        @PathVariable String artcod
    ) {
        try {
            List<ArtNameProjection> articulo = artRepository.findByENTAndAFACODAndASUCODAndARTCOD(ent, afacod, asucod, artcod);
            if(articulo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(articulo);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //deleting a familia
    @DeleteMapping("/delete-familia/{ent}/{afacod}")
    public ResponseEntity<?> deleteFamilia(
        @PathVariable Integer ent,
        @PathVariable String afacod
    ) {
        try {
            long articulos = artRepository.countByENTAndAFACOD(ent, afacod);
            if (articulos > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede borrar una familia con artículos asociados");
            }

            asuRepository.deleteByENTAndAFACOD(ent, afacod);
            int removed = afaRepository.deleteByENTAndAFACOD(ent, afacod);
            return removed == 0
                ? ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SIN_RESULTADO)
                : ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //to delete a subfamilia
    @DeleteMapping("/delete-sub-familia/{ent}/{afacod}/{asucod}")
    public ResponseEntity<?> deleteSubFamilia(
        @PathVariable Integer ent,
        @PathVariable String afacod,
        @PathVariable String asucod
    ) {
        try {
            long articulos = artRepository.countByENTAndASUCOD(ent, asucod);
            if (articulos > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede borrar una subfamilia con artículos asociados");
            }

            int removed = asuRepository.deleteByENTAndAFACODAndASUCOD(ent, afacod, asucod);
            return removed == 0
                ? ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO)
                : ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //selecting articulos for contratos
    @GetMapping("/art-cont/{ent}/{conlot}")
    public ResponseEntity<?> getArticulosContratos(
        @PathVariable int ent,
        @PathVariable String conlot
    ) {
        try {
            List<ArtAsuContratoProjection> articulos = artRepository.findDistinctByENTAndAsuASUECO(ent, conlot);
            if(articulos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No hay artículos para la económica indicada");
            }

            return ResponseEntity.ok(articulos);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //searching in articulos for contratos by nums
    @GetMapping("/search-art-cont/{ent}/{conlot}/{term}")
    public ResponseEntity<?> searchArticulosContratosNum(
        @PathVariable int ent,
        @PathVariable String conlot,
        @PathVariable String term
    ) {
        try {
            List<ArtAsuContratoProjection> articulos = artRepository.findDistinctByENTAndAsuASUECOAndAFACODOrENTAndAsuASUECOAndASUCOD(ent, conlot, term, ent, conlot, term);
            if(articulos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(articulos);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //selecting articulos for contratos
    @GetMapping("/search-art-cont-des/{ent}/{conlot}/{artdes}")
    public ResponseEntity<?> searchArticulosContratosDes(
        @PathVariable int ent,
        @PathVariable String conlot,
        @PathVariable String artdes
    ) {
        try {
            List<ArtAsuContratoProjection> articulos = artRepository.findDistinctByENTAndAsuASUECOAndARTDESContaining(ent, conlot, artdes);
            if(articulos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(articulos);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}