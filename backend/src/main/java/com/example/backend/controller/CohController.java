package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.HistoricaContratos;
import com.example.backend.sqlserver2.repository.CohRepository;

@RestController
@RequestMapping("/api/coh")
public class CohController {
    @Autowired
    private CohRepository cohRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //main fetch for historica de ad
    @GetMapping("/historia-ADcontrato/{ent}/{eje}")
    public ResponseEntity<?> fetchHistoriaADContrato (
        @PathVariable Integer ent,
        @PathVariable String eje
    ) {
        try {
            List<HistoricaContratos> contratos = cohRepository.findByENTAndEJEAndCon_CONTIP(ent, eje, 3);
            if (contratos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(contratos);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}
