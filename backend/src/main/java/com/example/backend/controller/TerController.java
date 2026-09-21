package com.example.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.TerDto;
import com.example.backend.sqlserver2.model.Ter;
import com.example.backend.sqlserver2.model.TerId;
import com.example.backend.sqlserver2.repository.TerRepository;
import com.example.backend.service.ProveedoresSearch;

@RestController
@RequestMapping("/api/ter")
public class TerController {
    @Autowired
    private TerRepository terRepository;
    @Autowired
    private ProveedoresSearch proveedoresSearch;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    // Get all Ter records for a specific ENT
    @GetMapping("/by-ent/{ent}")
    public ResponseEntity<?> getByEnt(
        @PathVariable int ent
    ) {
        try {
            List<Ter> proveedorees = terRepository.findByENT(ent);
            if(proveedorees.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(proveedorees);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //searching in proveedores
    @GetMapping("/search-proveedores")
    public ResponseEntity<?> searchProveedores (
        @RequestParam Integer ent,
        @RequestParam String searchMode,
        @RequestParam String term
    ) {
        try {
            List<Ter> proveedores = proveedoresSearch.searchProveedoers(ent, searchMode, term);
            if (proveedores.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(proveedores);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMessage());
        }
    }

    //for modifying a Ter record
    public record updateProveedor(String TERWEB, String TEROBS, Integer TERBLO, Integer TERACU, String TERTEL, String  TERCOE) {};
    @PutMapping("/updateFields/{ent}/{tercod}")
    public ResponseEntity<?> updateTerFields(
        @PathVariable Integer ent,
        @PathVariable Integer tercod, 
        @RequestBody updateProveedor payload
    ) {
        try {
            TerId id = new TerId(ent, tercod);
            Optional<Ter> proveedor = terRepository.findById(id);
            if (proveedor.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);  
            }

            Ter updateProveedor = proveedor.get();
            updateProveedor.setTERWEB(payload.TERWEB());
            updateProveedor.setTEROBS(payload.TEROBS());
            updateProveedor.setTERBLO(payload.TERBLO());
            updateProveedor.setTERACU(payload.TERACU());
            updateProveedor.setTERTEL(payload.TERTEL());
            updateProveedor.setTERCOE(payload.TERCOE());
            terRepository.save(updateProveedor);
            return ResponseEntity.noContent().build();

        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //for selected proveedores to be added from sicalwin
    @PostMapping(value = "/save-proveedores/{ent}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<?> createMultipleForEnt(
        @PathVariable int ent, 
        @RequestBody(required = false) List<TerDto> dtos
    ) {
        try {
            if (dtos == null || dtos.isEmpty()) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios");
            }
            for (TerDto dto : dtos) {
                if (dto.getTERNOM() == null || dto.getTERNOM().trim().isEmpty()
                        || dto.getTERNIF() == null || dto.getTERNIF().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("Datos incompletos");
                }
            }

            Integer next = terRepository.findNextTercodForEnt(ent);
            if (next == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            List<String> savedNames = new ArrayList<>();
            List<String> unsavedNames = new ArrayList<>();

            for (TerDto dto : dtos) {
                List<Ter> proveedores = terRepository.findAllByENTAndTERNIF(ent, dto.getTERNIF());
                if (!proveedores.isEmpty()) {
                    unsavedNames.add(proveedores.get(0).getTERNIF());
                } else {
                    Ter t = new Ter();
                    t.setENT(ent);
                    t.setTERCOD(next++);
                    t.setTERNOM(dto.getTERNOM());
                    t.setTERALI(dto.getTERALI());
                    t.setTERNIF(dto.getTERNIF());
                    t.setTERDOM(dto.getTERDOM());
                    t.setTERCPO(dto.getTERCPO());
                    t.setTERTEL(dto.getTERTEL());
                    t.setTERFAX(dto.getTERFAX());
                    t.setTERWEB(dto.getTERWEB());
                    t.setTERCOE(dto.getTERCOE());
                    t.setTEROBS(dto.getTEROBS());
                    t.setPROCOD(dto.getPROCOD());
                    t.setTERPOB(dto.getTERPOB());
                    t.setTERAYT(dto.getTERAYT());
                    t.setTERACU(0);
                    t.setTERBLO(0);
                    terRepository.save(t);

                    savedNames.add(dto.getTERNIF());
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(new NamesResponse(savedNames, unsavedNames));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Server error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    public record NamesResponse (
        List<String> savedNames,
        List<String> unsavedNames
    ) {}

    //to actualiazr a proveedor
    public record proveedorUpdate(String TERNOM, String TERALI, String TERNIF, String TERDOM, String TERCPO, String TERTEL, String TERFAX, String TERCOE, String PROCOD, String TERPOB, Integer TERAYT) {}
    @PostMapping("/actualizar-proveedor/{ent}/{tercod}")
    public ResponseEntity<?> proveedorActualizar (
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @RequestBody proveedorUpdate payload
    ) {
        try {
            if (payload == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios");
            }

            TerId id = new TerId(ent, tercod);
            Optional<Ter> proveedor = terRepository.findById(id);
            if (proveedor.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            Ter proveedorUpdate = proveedor.get();
            proveedorUpdate.setTERNOM(payload.TERNOM());
            proveedorUpdate.setTERALI(payload.TERALI());
            proveedorUpdate.setTERNIF(payload.TERNIF());
            proveedorUpdate.setTERDOM(payload.TERDOM());
            proveedorUpdate.setTERCPO(payload.TERCPO());
            proveedorUpdate.setTERTEL(payload.TERTEL());
            proveedorUpdate.setTERFAX(payload.TERFAX());
            proveedorUpdate.setTERCOE(payload.TERCOE());
            proveedorUpdate.setPROCOD(payload.PROCOD());
            proveedorUpdate.setTERPOB(payload.TERPOB());
            proveedorUpdate.setTERAYT(payload.TERAYT());
            terRepository.save(proveedorUpdate);

            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //selecting proveedor data after actualizar
    @GetMapping("/proveedorDetail/{ent}/{tercod}")
    public ResponseEntity<?> detailProveedor (
        @PathVariable Integer ent,
        @PathVariable Integer tercod
    ) {
        try {
            Optional<Ter> proveedor = terRepository.findByENTAndTERCOD(ent, tercod);
            if (proveedor.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(proveedor);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }
}