package com.example.backend.controller;

import com.example.backend.dto.FdtResumeDto;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.backend.sqlserver2.model.Fdt;
import com.example.backend.sqlserver2.repository.FdtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fdt")
public class FdtController {
    @Autowired
    private FdtRepository fdtRepository;

    @GetMapping("/{ent}/{eje}/{facnum}")
    public ResponseEntity<?> getFde(
            @PathVariable Integer ent,
            @PathVariable String eje,
            @PathVariable Integer facnum
    ) {
        try {
            List<Fdt> descuentos = fdtRepository.findByENTAndEJEAndFACNUM(ent, eje, facnum);
        
            if(descuentos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sin resultado");
            }
            
            List<FdtResumeDto> result = descuentos.stream()
                .map(ft -> new FdtResumeDto(
                    ft.getFDTARE(),
                    ft.getFDTORG(),
                    ft.getFDTFUN(),
                    ft.getFDTECO(),
                    ft.getFDTBSE(),
                    ft.getFDTPRE(),
                    ft.getFDTDTO(),
                    ft.getFDTTXT()
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + ex.getMostSpecificCause().getMessage());
        }
    }
}