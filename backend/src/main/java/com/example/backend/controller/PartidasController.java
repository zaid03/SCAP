package com.example.backend.controller;

import java.util.List;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.Partida;
import com.example.backend.service.PartidasService;

@RestController
@RequestMapping("/api/sical") 
@CrossOrigin(origins = "http://localhost:4200")
public class PartidasController {
    private final PartidasService partidasService;

    public PartidasController(PartidasService partidasService) {
        this.partidasService = partidasService;
    }

    @GetMapping("/partidas")
    public ResponseEntity<List<Partida>> getPartidas(
        @RequestParam(required = false) String cenges,
        @RequestParam(required = false) String alias,
        @RequestParam(required = false) String clorg,
        @RequestParam(required = false) String clfun,
        @RequestParam(required = false) String cleco,
        @RequestParam(required = false) String clcte,
        @RequestParam(required = false) String clpam,
        @RequestParam(required = false) String usucenges,
        @RequestParam String orgCode,
        @RequestParam String entidad,
        @RequestParam String eje
    ) {
        try {
            PartidasService.SearchCriteria criteria = new PartidasService.SearchCriteria.Builder().cenges(cenges).alias(alias).clorg(clorg).clfun(clfun).cleco(cleco).clcte(clcte).clpam(clpam).usucenges(usucenges).orgCode(orgCode).entidad(entidad).eje(eje).build();
            List<Partida> partidas = partidasService.getPartidas(criteria);
            return ResponseEntity.ok(partidas);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}
