package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.sqlserver2.repository.AdeRepository;
import com.example.backend.dto.articulosPorAlbaran;

@RestController
@RequestMapping("/api/ade")
public class AdeController {
    @Autowired
    private AdeRepository adeRespository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //not tested

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
}