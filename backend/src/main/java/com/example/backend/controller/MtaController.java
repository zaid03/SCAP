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

import com.example.backend.sqlserver2.model.Mta;
import com.example.backend.sqlserver2.repository.MtaRepository;

@RestController
@RequestMapping("/api/mta")
public class MtaController {
    @Autowired
    private MtaRepository mtaRepository;

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
}
