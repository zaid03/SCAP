package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.backend.dto.ServiceMagsProjection;
import com.example.backend.dto.existenciasProjection;
import com.example.backend.dto.magcodOnly;
import com.example.backend.sqlserver2.repository.DpeRepository;
import com.example.backend.sqlserver2.repository.MagRepository;
import com.example.backend.sqlserver2.repository.MeaRepository;

@Service
public class existenciaAlmacenFetches {
    @Autowired
    private DpeRepository dpeRepository;
    @Autowired
    private MeaRepository meaRepository;
    @Autowired
    private MagRepository magRepository;

    private static final int PAGE_SIZE = 20;

    public NamesResponse existenciasService(
        Integer ent,
        String cge,
        String percod,
        String eje,
        String magcod_main,
        String main_search,
        String afacod,
        String asucod,
        int page
    ) {
        List<ServiceMagsProjection> almacenes = new ArrayList<> ();
        List<existenciasProjection> existencias = new ArrayList<> ();

        if (ent == null || cge == null || cge.isBlank() || percod == null || percod.isBlank() || eje == null || eje.isBlank()) {
            throw new IllegalArgumentException("Faltan datos obligatorios.");
        }

        if ((main_search == null || main_search.isBlank()) && (afacod == null || afacod.isBlank()) && (asucod == null || asucod.isBlank())) {
            if (magcod_main != null) {
                Optional<magcodOnly> almacen = magRepository.findByENTAndDEPCOD(ent, magcod_main);
                if (almacen.isEmpty()) {
                    throw new IllegalArgumentException("Almacen sin resultado.");
                }

                Integer magcod = almacen.get().getMAGCOD();
                List<existenciasProjection> existenciasList = meaRepository.findByENTAndMAGCODAndArt_ARTBLONot(ent, magcod, 0, PageRequest.of(page, PAGE_SIZE));
                if (existenciasList.isEmpty()) {
                    throw new IllegalArgumentException("Sin resultado");
                }
                existencias.addAll(existenciasList);
            } else {
                List<ServiceMagsProjection> services = dpeRepository.findByENTAndEJEAndPERCODAndDep_Cge_CGECODAndDep_DEPALM(ent, eje, percod, cge, 1);
                if (services.isEmpty()) {
                    throw new IllegalArgumentException("Servicios sin resultado.");
                }
                String depcod = services.get(0).getDEPCOD();
                almacenes.addAll(services);

                Optional<magcodOnly> almacen = magRepository.findByENTAndDEPCOD(ent, depcod);
                if (almacen.isEmpty()) {
                    throw new IllegalArgumentException("Almacen sin resultado.");
                }

                Integer magcod = almacen.get().getMAGCOD();
                List<existenciasProjection> existenciasList = meaRepository.findByENTAndMAGCODAndArt_ARTBLONot(ent, magcod, 0, PageRequest.of(page, PAGE_SIZE));
                if (existenciasList.isEmpty()) {
                    throw new IllegalArgumentException("Sin resultado");
                }
                existencias.addAll(existenciasList);
            }
        } else {
            if (magcod_main != null) {
                if ((afacod != null && !afacod.isBlank()) && (asucod == null || asucod.isBlank())) {
                    Optional<magcodOnly> almacen = magRepository.findByENTAndDEPCOD(ent, magcod_main);
                    if (almacen.isEmpty()) {
                        throw new IllegalArgumentException("Almacen sin resultado.");
                    }

                    Integer magcod = almacen.get().getMAGCOD();
                    List<existenciasProjection> existenciasList =  meaRepository.findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACOD(ent, magcod, 0, afacod);
                    if (existenciasList.isEmpty()) {
                        throw new IllegalArgumentException("Sin resultado");
                    }
                    if (main_search != null && !main_search.isBlank()) {
                        existencias.addAll(applyMainSearch(existenciasList, main_search));
                    } else {
                        existencias.addAll(existenciasList);
                    }
                } else if ((asucod != null && !asucod.isBlank()) && (afacod == null || afacod.isBlank())) {
                    Optional<magcodOnly> almacen = magRepository.findByENTAndDEPCOD(ent, magcod_main);
                    if (almacen.isEmpty()) {
                        throw new IllegalArgumentException("Almacen sin resultado.");
                    }

                    Integer magcod = almacen.get().getMAGCOD();
                    List<existenciasProjection> existenciasList =  meaRepository.findByENTAndMAGCODAndArt_ARTBLONotAndArt_ASUCOD(ent, magcod, 0, asucod);
                    if (existenciasList.isEmpty()) {
                        throw new IllegalArgumentException("Sin resultado");
                    }
                    if (main_search != null && !main_search.isBlank()) {
                        existencias.addAll(applyMainSearch(existenciasList, main_search));
                    } else {
                        existencias.addAll(existenciasList);
                    }
                } else if ((asucod != null && !asucod.isBlank()) && (afacod != null && !afacod.isBlank())) {
                    Optional<magcodOnly> almacen = magRepository.findByENTAndDEPCOD(ent, magcod_main);
                    if (almacen.isEmpty()) {
                        throw new IllegalArgumentException("Almacen sin resultado.");
                    }

                    Integer magcod = almacen.get().getMAGCOD();
                    List<existenciasProjection> existenciasList =  meaRepository.findByENTAndMAGCODAndArt_ARTBLONotAndArt_AFACODAndArt_ASUCOD(ent, magcod, 0, afacod, asucod);
                    if (existenciasList.isEmpty()) {
                        throw new IllegalArgumentException("Sin resultado");
                    }
                    if (main_search != null && !main_search.isBlank()) {
                        existencias.addAll(applyMainSearch(existenciasList, main_search));
                    } else {
                        existencias.addAll(existenciasList);
                    }
                } else {
                    Optional<magcodOnly> almacen = magRepository.findByENTAndDEPCOD(ent, magcod_main);
                    if (almacen.isEmpty()) {
                        throw new IllegalArgumentException("Almacen sin resultado.");
                    }

                    Integer magcod = almacen.get().getMAGCOD();
                    List<existenciasProjection> existenciasList =  meaRepository.findAllByENTAndMAGCODAndArt_ARTBLONot(ent, magcod, 0);
                    if (existenciasList.isEmpty()) {
                        throw new IllegalArgumentException("Sin resultado");
                    }
                    existencias.addAll(applyMainSearch(existenciasList, main_search));
                }
            } else {
                throw new IllegalArgumentException("Magcod sin resultado.");
            }
        }
        return new NamesResponse(almacenes, existencias);
    }

    public record NamesResponse (
        List<ServiceMagsProjection> almacenes,
        List<existenciasProjection> existencias
    ) {}

    private List<existenciasProjection> filterByArtCodRefDes(
        List<existenciasProjection> data,
        String search
    ) {
        String value = search.toLowerCase().trim();

        return data.stream().filter(e ->
            (e.getArt_ARTCOD() != null && e.getArt_ARTCOD().toLowerCase().equals(value)) ||
            (e.getArt_ARTREF() != null && e.getArt_ARTREF().toLowerCase().equals(value)) ||
            (e.getArt_ARTDES() != null && e.getArt_ARTDES().toLowerCase().contains(value))
        ).toList();
    }

    private List<existenciasProjection> filterByArtRefDes(
        List<existenciasProjection> data,
        String search
    ) {
        String value = search.toLowerCase().trim();

        return data.stream().filter(e ->
            (e.getArt_ARTREF() != null && e.getArt_ARTREF().toLowerCase().equals(value)) ||
            (e.getArt_ARTDES() != null && e.getArt_ARTDES().toLowerCase().contains(value))
        ).toList();
    }

    private List<existenciasProjection> applyMainSearch(
        List<existenciasProjection> data,
        String main_search
    ) {
        if (main_search == null || main_search.isBlank()) {
            return data;
        }

        if (main_search.length() < 11) {
            return filterByArtCodRefDes(data, main_search);
        }

        return filterByArtRefDes(data, main_search);
    }
}