package com.example.backend.controller;

import com.example.backend.sqlserver1.repository.AytRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ayt")
public class AytController {
    @Autowired
    private AytRepository aytRepository;

    //to fetch ws parameters
    @GetMapping("/fetch-all/{ent}")
    public ResponseEntity<?> fetchAll(@PathVariable int ent) {
        try {
            List<com.example.backend.sqlserver1.model.Ayt> list = aytRepository.findByENTCOD(ent);
            if (list == null || list.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
