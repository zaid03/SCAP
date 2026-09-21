package com.example.backend.controller;

import com.example.backend.dto.ProveedoresArticleProjection;
import com.example.backend.sqlserver2.model.Apr;
import com.example.backend.sqlserver2.model.AprId;
import com.example.backend.sqlserver2.repository.AprRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/more")
public class AprController {
    @Autowired
    private AprRepository aprRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";

    //selecting proveedores for an article
    @GetMapping("/proveedores-por-articulo/{ent}/{afacod}/{asucod}/{artcod}")
    public ResponseEntity<?> proveedoresPorArticulo (
        @PathVariable Integer ent,
        @PathVariable String afacod,
        @PathVariable String asucod,
        @PathVariable String artcod
    ) {
        try {
            List<ProveedoresArticleProjection> proveedores = aprRepository.findByENTAndAFACODAndASUCODAndARTCOD(ent, afacod, asucod, artcod);
            if(proveedores.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(proveedores);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    //modifying an article's proveedor 
    public record provUpdate(String APRREF, String APROBS, Double APRUEM) {}
    @PatchMapping("/update-prov-info/{ent}/{afacod}/{asucod}/{artcod}/{tercod}")
    public ResponseEntity<?>  proveedorInfoUpdate(
        @PathVariable Integer ent,
        @PathVariable String afacod,
        @PathVariable String asucod,
        @PathVariable String artcod,
        @PathVariable Integer tercod,
        @RequestBody provUpdate payload
    ) {
        try {
            if (payload == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            AprId id = new AprId(ent, tercod, afacod, asucod, artcod);
            Optional<Apr> aprCheck = aprRepository.findById(id);

            if (aprCheck.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sin resultado");
            }

            Apr proveedorInfo = aprCheck.get();
            proveedorInfo.setAPRREF(payload.APRREF());
            proveedorInfo.setAPROBS(payload.APROBS());
            proveedorInfo.setAPRUEM(payload.APRUEM());
            aprRepository.save(proveedorInfo);

            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    //deleting a proveedor from an article
    @DeleteMapping("/delete-proveedor-art/{ent}/{afacod}/{asucod}/{artcod}/{tercod}")
    public ResponseEntity<?> artProveedorDelete(
        @PathVariable Integer ent,
        @PathVariable String afacod,
        @PathVariable String asucod,
        @PathVariable String artcod,
        @PathVariable Integer tercod
    ) {
        try {
            AprId id = new AprId(ent, tercod, afacod, asucod, artcod);
            if (!aprRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("Proveedor no existe");
            }

            aprRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    //adding proveedores to an articulo
    public record provsAdd(Integer ENT, String AFACOD, String ASUCOD, String ARTCOD, List<Integer> tercods) {};
    @PostMapping("/add-terceros")
    public ResponseEntity<?> tercerodAdd (
        @RequestBody provsAdd payload
    ) {
        try {
            if (payload == null || payload.ENT() == null || payload.AFACOD() == null || payload.ASUCOD() == null || payload.ARTCOD() == null || payload.tercods() == null) {
                return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
            }

            Integer count = 0;
            for(Integer tercod : payload.tercods()) {
                AprId id = new AprId(payload.ENT(), tercod, payload.AFACOD() ,payload.ASUCOD(), payload.ARTCOD());
                if (!aprRepository.existsById(id)) {
                    Apr articuloAdd = new Apr();
                    articuloAdd.setENT(payload.ENT());
                    articuloAdd.setAFACOD(payload.AFACOD());
                    articuloAdd.setASUCOD(payload.ASUCOD());
                    articuloAdd.setARTCOD(payload.ARTCOD());
                    articuloAdd.setTERCOD(tercod);
                    articuloAdd.setAPRACU(0);
                    articuloAdd.setAPRPRE(0.00);
                    articuloAdd.setAPRUEM(0.00);
                    aprRepository.save(articuloAdd);
                    count = count + 1;
                }

            }

            return ResponseEntity.ok("Se guardaron " + count + " artículos");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    // fetching articulos fr proveedor
    @GetMapping("/by-apr/{ent}/{tercod}")
    public ResponseEntity<?> getApr(
        @PathVariable Integer ent, 
        @PathVariable Integer tercod
    ) {
        try {
            List<Apr> articulos = aprRepository.findByENTAndTERCOD(ent, tercod);
            if (articulos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }
            return ResponseEntity.ok(articulos);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    // Modifying an articulo
    public record Articulo(String aprref, Double aprpre, Double apruem, String aprobs, Integer apracu) {}
    @PatchMapping("/update-apr/{ent}/{tercod}/{afacod}/{asucod}/{artcod}")
    public ResponseEntity<?> updateArticulo(
        @PathVariable Integer ent,
        @PathVariable Integer tercod,
        @PathVariable String afacod,
        @PathVariable String asucod,
        @PathVariable String artcod,
        @RequestBody Articulo payload
    ) {
        try {

            AprId id = new AprId(ent, tercod, afacod, asucod, artcod);
            Optional<Apr> articulo = aprRepository.findById(id);
            if(articulo.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }
            
            Apr articulosUpdate = articulo.get();
            articulosUpdate.setAPRREF(payload.aprref());
            articulosUpdate.setAPRPRE(payload.aprpre());
            articulosUpdate.setAPRUEM(payload.apruem());
            articulosUpdate.setAPROBS(payload.aprobs());
            articulosUpdate.setAPRACU(payload.apracu());

            aprRepository.save(articulosUpdate);
            return ResponseEntity.noContent().build();
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error:" + ex.getMostSpecificCause().getMessage());
        }
    }
    
    // Deleting data
    @DeleteMapping("/delete-apr")
    public ResponseEntity<String> deleteApr(
        @RequestParam Integer ent,
        @RequestParam Integer tercod,
        @RequestParam String afacod,
        @RequestParam String asucod,
        @RequestParam String artcod
    ) {
        try {
            AprId id = new AprId(ent, tercod, afacod, asucod, artcod);
            if (!aprRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SIN_RESULTADO);
            }
            aprRepository.deleteById(id);
            return ResponseEntity.ok("articulo eliminado exitosamente");
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    //adding data
    @PostMapping("/add-apr")
    public ResponseEntity<?> addApr(
        @RequestBody List<Apr> requestPayload
    ) {
        try {
            List<ArticuloInfo> savedArticulos = new ArrayList<>();
            List<ArticuloInfo> unsavedArticulos = new ArrayList<>();

            for (Apr articulo: requestPayload) {

                AprId id = new AprId(articulo.getENT(), articulo.getTERCOD(), articulo.getAFACOD(), articulo.getASUCOD(), articulo.getARTCOD());
                Optional<Apr> articuloSearch = aprRepository.findById(id);
                if (articuloSearch.isPresent()) {
                    Apr ArticuloExists = articuloSearch.get();
                    unsavedArticulos.add(
                        new ArticuloInfo (
                            ArticuloExists.getAFACOD(),
                            ArticuloExists.getASUCOD(),
                            ArticuloExists.getARTCOD()
                        )
                    );
                } else {
                    Apr aprAdd = new Apr();
                    aprAdd.setENT(articulo.getENT());
                    aprAdd.setAFACOD(articulo.getAFACOD());
                    aprAdd.setASUCOD(articulo.getASUCOD());
                    aprAdd.setARTCOD(articulo.getARTCOD());
                    aprAdd.setTERCOD(articulo.getTERCOD());
                    aprAdd.setAPRACU(0);
                    aprAdd.setAPRPRE(0.00);
                    aprAdd.setAPRUEM(0.00);
                    aprRepository.save(aprAdd);

                    savedArticulos.add(
                        new ArticuloInfo (
                            articulo.getAFACOD(),
                            articulo.getASUCOD(),
                            articulo.getARTCOD()
                        )
                    );
                }
            }

            NamesResponse result = new NamesResponse(savedArticulos, unsavedArticulos);
            return ResponseEntity.ok(result);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR + ex.getMostSpecificCause().getMessage());
        }
    }

    public record NamesResponse (
        List<ArticuloInfo> savedArticulos,
        List<ArticuloInfo> unsavedArticulos
    ) {}

    public record ArticuloInfo(
        String afacod,
        String asucod,
        String artcod
    ) {}
}
