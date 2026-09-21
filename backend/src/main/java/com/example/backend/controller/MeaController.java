package com.example.backend.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.service.existenciaAlmacenFetches;
import com.example.backend.dto.ExistenciasMeaProjection;
import com.example.backend.dto.ExistenciasMeaProjectionDTO;
import com.example.backend.dto.ServiceMagsProjection;
import com.example.backend.dto.existenciasProjection;
import com.example.backend.dto.magcodOnly;
import com.example.backend.dto.ArticulosPorAlmcenProjection;
import com.example.backend.sqlserver2.repository.MeaRepository;
import com.example.backend.sqlserver2.repository.MagRepository;
import com.example.backend.sqlserver2.model.Mea;

@RestController
@RequestMapping("/api/mea")
public class MeaController {
    @Autowired
    private MeaRepository meaRepository;
    @Autowired
    private existenciaAlmacenFetches ExistenciaAlmacenFetches;
    @Autowired
    private MagRepository magRepository;

    private static final String SIN_RESULTADO = "Sin resultado";
    private static final String ERROR = "Error :";
    private static final int PAGE_SIZE = 20;

    //selecting existencias for articles and articulos por almacen and exporting data
    @GetMapping("/fetch-articulos-por-almacen/{ent}")
    public ResponseEntity<?> fetchArticulosPorAlmacen(
        @PathVariable Integer ent,
        @RequestParam(defaultValue = "0") int page
    ) {
        try {
            List<ArticulosPorAlmcenProjection> Almacenes = meaRepository.findByENT(ent, PageRequest.of(page, PAGE_SIZE));
            if (Almacenes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(Almacenes);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }
    @GetMapping("/export/{ent}")
    public ResponseEntity<?> fetchArticulosPorAlmacenExport(
        @PathVariable Integer ent
    ) {
        try {
            List<ArticulosPorAlmcenProjection> Almacenes = meaRepository.findAllByENT(ent);
            if (Almacenes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(Almacenes);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    //getting pagination number
    @GetMapping("/get-pag/{ent}")
    public ResponseEntity<?> getPag(
        @PathVariable Integer ent
    ) {
        try {
            Integer pagination = meaRepository.countByENT(ent);
            if (pagination == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(pagination);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    //search query for articulos por almacen
    @GetMapping("/search-articulos/{ent}")
    public ResponseEntity<?> searchArticulos(
        @PathVariable Integer ent,
        @RequestParam(required = false) String mainSearch,
        @RequestParam(required = false) String afaCod,
        @RequestParam(required = false) String asuCod,
        @RequestParam(defaultValue = "Todos") String bloqueado,
        @RequestParam(required = false) String almacen
    ) {
        try {
            List<ArticulosPorAlmcenProjection> articulos = meaRepository.searchArticulos(
                ent,
                mainSearch,
                afaCod,
                asuCod,
                bloqueado,
                almacen
            );
            
            if (articulos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            return ResponseEntity.ok(articulos);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    //selecting existencias for articles
    @GetMapping("/existencias-por-articulo/{ent}/{afacod}/{asucod}/{artcod}")
    public ResponseEntity<?> existenciasPorArticulo(
        @PathVariable Integer ent,
        @PathVariable String afacod,
        @PathVariable String asucod,
        @PathVariable String artcod
    ) {
        try {
            List<Mea> existencias = meaRepository.findByENTAndAFACODAndASUCODAndARTCOD(ent, afacod, asucod, artcod);

            if (existencias.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SIN_RESULTADO);
            }

            List<ExistenciasMeaProjection> result = existencias.stream()
            .map(mea -> new ExistenciasMeaProjectionDTO(
                mea.getMAGCOD(),
                mea.getMag() != null ? mea.getMag().getDEPCOD() : null,
                mea.getMag() != null && mea.getMag().getDep() != null ? mea.getMag().getDep().getDEPDES() : null,
                mea.getMEAUNI(),
                mea.getMEALOC()
            ))
            .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    //selecting and searching in C.existencias por almacen
    @GetMapping("/existencias-almacen/{ent}")
    public ResponseEntity<?> existenciasAlmacen (
        @PathVariable Integer ent,
        @RequestParam(required = false) String cge,
        @RequestParam(required = false) String percod,
        @RequestParam(required = false) String eje,
        @RequestParam(required = false) String magcod_main,
        @RequestParam(required = false) String main_search,
        @RequestParam(required = false) String afacod,
        @RequestParam(required = false) String asucod,
        @RequestParam(defaultValue = "0") int page
    ) {
        try {
            existenciaAlmacenFetches.NamesResponse response = ExistenciaAlmacenFetches.existenciasService( ent, cge, percod, eje, magcod_main, main_search, afacod, asucod, page
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    //getting total pages for it
    @GetMapping("/get-pag/{ent}/{depcod}")
    public ResponseEntity<?> paginationExistencias (
        @PathVariable Integer ent,
        @PathVariable String depcod
    ) {
        try {
            Optional<magcodOnly> almacen = magRepository.findByENTAndDEPCOD(ent, depcod);
            if (almacen.isEmpty()) {
                throw new IllegalArgumentException("Almacen sin resultado.");
            }
            Integer magcod = almacen.get().getMAGCOD();

            Long totalPages = meaRepository.countByMAGCODAndArt_ARTBLONot(magcod, 0);

            return ResponseEntity.ok(totalPages);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }

    //fetch all for exporting data on C.existencias almacen
    @GetMapping("/export/{ent}/{depcod}")
    public ResponseEntity<?> dataExport(
        @PathVariable Integer ent,
        @PathVariable String depcod
    ) {
        try {
            Optional<magcodOnly> almacen = magRepository.findByENTAndDEPCOD(ent, depcod);
            if (almacen.isEmpty()) {
                throw new IllegalArgumentException("Almacen sin resultado.");
            }
            Integer magcod = almacen.get().getMAGCOD();
            List<existenciasProjection> existencias = meaRepository.findAllByENTAndMAGCODAndArt_ARTBLONot(ent, magcod, 0);

            return ResponseEntity.ok(existencias);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR + ex.getMessage());
        }
    }
}