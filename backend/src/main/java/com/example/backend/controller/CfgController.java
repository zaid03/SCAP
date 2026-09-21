package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import com.example.backend.sqlserver2.model.Cfg;
import com.example.backend.sqlserver2.repository.CfgRepository;

import java.util.List;

@RestController
@RequestMapping("/api/cfg")
public class CfgController {
    
    @Autowired
    private CfgRepository cfgRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //method to ejercicio in Cfg table by entidad and CFGEST
    @GetMapping("/by-ent/{ent}")
    public ResponseEntity<?> getEJE(
        @PathVariable int ent
    ) {
        try {
            List<Cfg> results = cfgRepository.findEjeByENTAndCFGEST(ent, 0);
            
            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            
            return ResponseEntity.ok(results);
            
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMostSpecificCause().getMessage());
        }
    }

    //to fetch all ejes
    @GetMapping("/fetch-Eje/{ent}")
    public ResponseEntity<?> fetchAllEjercicios(
        @PathVariable Integer ent
    ) {
        try {
            List<Cfg> Eje = cfgRepository.findByENT(ent);
            if(Eje.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(Eje);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //to search in eje
    @GetMapping("/search-Eje/{ent}/{eje}")
    public ResponseEntity<?> searchEjercicios(
        @PathVariable Integer ent,
        @PathVariable String eje
    ) {
        try {
            List<Cfg> Eje = cfgRepository.findByENTAndEJE(ent, eje);
            if(Eje.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(Eje);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}
