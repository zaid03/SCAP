package com.example.backend.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.Operaciones;
import com.example.backend.service.OperacionesService;

@RestController
@RequestMapping("/api/sical")
@CrossOrigin(origins = "http://localhost:4200")
public class OperacionesController {

    private final OperacionesService operacionesService;

    public OperacionesController(OperacionesService operacionesService) {
        this.operacionesService = operacionesService;
    }

    @GetMapping("/operaciones")
    public ResponseEntity<?> getOperaciones(
        @RequestParam(name = "orgCode")String orgCode,
        @RequestParam(name = "entidad") String entidad,
        @RequestParam(name = "numeroOperDesde", required = false) String numeroOperDesde,
        @RequestParam(name = "numeroOperHasta", required = false) String numeroOperHasta,
        @RequestParam(name = "codigoOperacion", required = false) String codigoOperacion,
        @RequestParam(name = "organica", required = false) String organica,
        @RequestParam(name = "funcional", required = false) String funcional,
        @RequestParam(name = "economica", required = false) String economica,
        @RequestParam(name = "referencia", required = false) String referencia,
        @RequestParam(name = "expediente", required = false) String expediente,
        @RequestParam(name = "grupoApunte", required = false) String grupoApunte,
        @RequestParam(name = "oficina", required = false) String oficina,
        @RequestParam(name = "indice", required = false) String indice,
        @RequestParam(name = "numRegDev", required = false) String numRegDev,
        @RequestParam(name = "eje") String eje
    ) {
        try {
            List<Operaciones> operaciones = operacionesService.getOperaciones(
                    orgCode,
                    entidad,
                    numeroOperDesde,
                    numeroOperHasta,
                    codigoOperacion,
                    organica,
                    funcional,
                    economica,
                    referencia,
                    expediente,
                    grupoApunte,
                    oficina,
                    indice,
                    numRegDev,
                    eje);
            return ResponseEntity.ok(operaciones);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", ex.getMessage()));
        }
    }
}