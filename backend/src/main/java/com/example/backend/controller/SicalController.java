package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.Tercero;
import com.example.backend.service.SicalService;

@RestController
@RequestMapping("/api/sical") 
@CrossOrigin(origins = "http://localhost:4200")
public class SicalController {

    private final SicalService sicalService;

    public SicalController(SicalService sicalService) {
        this.sicalService = sicalService;
    }

    @GetMapping("/terceros")
    public ResponseEntity<?> getTerceros(
            @RequestParam(required = false) String nif,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String codigo,
            @RequestParam String orgCode, 
            @RequestParam String entidad, 
            @RequestParam String eje
    ) {
        try {
            List<Tercero> result = sicalService.getTerceros(nif, nom, codigo, orgCode, entidad, eje);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
}
