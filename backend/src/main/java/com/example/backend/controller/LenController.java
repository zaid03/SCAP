package com.example.backend.controller;

import com.example.backend.sqlserver2.model.Len;
import com.example.backend.sqlserver2.repository.LenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/Len")
public class LenController {
    @Autowired
    private LenRepository lenRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //selecting all lugares de entrega
    @GetMapping("/fetch-all")
    public ResponseEntity<?> fetchLugares(
    ) {
        try {
            List<?> lugares = lenRepository.findAll();
            if (lugares.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(lugares);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding a lugar de entrega
    @PostMapping("/add-lugar")
    public ResponseEntity<?> addLugar(
        @RequestBody Len newLugar
    ) {
        try {
            Len lastLen = lenRepository.findFirstByOrderByLENCODDesc();
            int nextLencod = (lastLen == null ? 1 : lastLen.getLENCOD() + 1);

            newLugar.setLENCOD(nextLencod);
            lenRepository.save(newLugar);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //update a lugar de entrega
    public record lugarUpdate(String LENDES, String LENTXT) {}

    @PatchMapping("/update-lugar/{LENCOD}")
    public ResponseEntity<?> updateLugar(
        @PathVariable Integer LENCOD,
        @RequestBody lugarUpdate payload
    ) {
        try {
            if (payload == null || payload.LENDES() == null || payload.LENTXT() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            Optional<Len> entrega = lenRepository.findById(LENCOD);
            if(entrega.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }

            Len entregaUpdate = entrega.get();
            entregaUpdate.setLENDES(payload.LENDES());
            entregaUpdate.setLENTXT(payload.LENTXT());

            lenRepository.save(entregaUpdate);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //delete a lugare de entrega
    @DeleteMapping("/delete-lugar/{LENCOD}")
    public ResponseEntity<?> deleteLugar(
        @PathVariable Integer LENCOD
    ) {
        try {
            if (!lenRepository.existsById(LENCOD)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(SIN_RESULTADO);
            }
            lenRepository.deleteById(LENCOD);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //search with lencod
    @GetMapping("/filter-lencod/{LENCOD}")
    public ResponseEntity<?> filterLugaresLen(
        @PathVariable Integer LENCOD
    ) {
        try {
            List<?> lugares = lenRepository.findByLENCOD(LENCOD);
            if (lugares.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(lugares);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //search with lendes
    @GetMapping("/filter-lendes/{LENDES}")
    public ResponseEntity<?> filterLugaresDes(
        @PathVariable String LENDES
    ) {
        try {
            List<?> lugares = lenRepository.findByLENDESContaining(LENDES);
            if (lugares.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(lugares);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}