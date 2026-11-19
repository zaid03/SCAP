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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.sqlserver2.model.Ade;
import com.example.backend.sqlserver2.model.AdeId;
import com.example.backend.sqlserver2.repository.AdeRepository;
import com.example.backend.dto.articulosPorAlbaran;

@RestController
@RequestMapping("/api/ade")
public class AdeController {
    @Autowired
    private AdeRepository adeRespository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    @GetMapping("/fetch-articulos/{ent}/{albnum}")
    public ResponseEntity<?> fetchArticulos(
        @PathVariable Integer ent,
        @PathVariable Integer albnum
    ) {
        try {
            List<articulosPorAlbaran> articulos = adeRespository.findByENTAndALBNUM(ent, albnum);
            if (articulos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(articulos);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //updating an articulo for an almacen
    public record updateArticulo(String ADEREF, String ADEOBS) {}
    @PatchMapping("/update-articulo/{ent}/{albnum}/{adenum}")
    public ResponseEntity<?> articuloUpdate(
        @PathVariable Integer ent,
        @PathVariable Integer albnum,
        @PathVariable Integer adenum,
        @RequestBody updateArticulo payload
    ) {
        try {
            if (payload == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }
            AdeId id = new AdeId(ent, albnum, adenum);
            Optional<Ade> articulo = adeRespository.findById(id);
            if (articulo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Ade articuloUpdate = articulo.get();
            articuloUpdate.setADEREF(payload.ADEREF());
            articuloUpdate.setADEOBS(payload.ADEOBS());
            adeRespository.save(articuloUpdate);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}