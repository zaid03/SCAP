package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.dto.ArticleProjection;
import com.example.backend.sqlserver2.repository.ArtRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExistenciasSearch {
    @Autowired
    private ArtRepository artRepository;

    public List<ArticleProjection> searchExistencias(
        Integer ent,
        String campo,
        String afacod,
        String asucod
    ) {
        List<ArticleProjection> existencias = new ArrayList<>();
        if (((afacod != null && !afacod.isBlank()) || (asucod != null && !asucod.isBlank())) && (campo == null || campo.isBlank())){
            existencias = artRepository.findByENTAndARTBLONotAndAFACODOrENTAndARTBLONotAndASUCOD(ent, 0, afacod, ent, 0, asucod);
        } else if (campo != null && !campo.isBlank()) {
            if (campo.length() < 11) {
                existencias = artRepository.findByENTAndARTBLONotAndARTCODOrENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(ent, 0, campo, ent, 0, campo, ent, 0, campo);
            } else {
                existencias = artRepository.findByENTAndARTBLONotAndARTDESContainingOrENTAndARTBLONotAndARTREF(ent, 0, campo, ent, 0, campo);
            }

            if (afacod != null && !afacod.isBlank()) {
                existencias.removeIf(e -> !afacod.equals(e.getAFACOD()));
            } 
            if (asucod != null && !asucod.isBlank()) {
                existencias.removeIf(e -> !asucod.equals(e.getASUCOD()));
            }
        }

        return existencias;
    }
}