package com.example.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.example.backend.dto.ContratoDto;
import com.example.backend.sqlserver2.repository.CotRepository;

@Service
public class ContratosSearch {
    @Autowired
    private CotRepository cotRepository;   

    public List<ContratoDto> searchContrtos (
        Integer ent,
        String eje,
        String searchMode,
        @Nullable String term
    ) {
        List<CotContratoProjection> contratos = null;

        if (searchMode.equals("todos")) {
            if (term == null || term.isEmpty()) {
                contratos = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJE(3, ent, eje);
            }
            else if (isNumbersOnly(term)) {
                contratos = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCOD(3, ent, eje, Integer.parseInt(term));
            }
            else {
                contratos = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContaining(3, ent, eje, term);
                System.out.println("contratos size = " + contratos.size());
            }
        }
        else if (searchMode.equals("bloque")) {
            if (term == null || term.isEmpty()) {
                contratos = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLONot(3, ent, eje, 0);
            }
            else if (isNumbersOnly(term)) {
                contratos = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCODAndConnCONBLONot(3, ent, eje, Integer.parseInt(term), 0);
            }
            else {
                contratos = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContainingAndConnCONBLONot(3, ent, eje, term, 0);
            }
        }
        else if (searchMode.equals("noBloque")) {
            if (term == null || term.isEmpty()) {
                contratos = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONBLO(3, ent, eje, 0);
            }
            else if (isNumbersOnly(term)) {
                contratos = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONCODAndConnCONBLO(3, ent, eje, Integer.parseInt(term), 0);
            }
            else {
                contratos = cotRepository.findAllProjectedByConnCONTIPAndConnENTAndConnEJEAndConnCONDESContainingAndConnCONBLO(3, ent, eje, term, 0);
            }
        }

        return contratos != null ? contratos.stream().map(p -> buildContratoDto(p.getConn(), p.getTer())).collect(Collectors.toList()) : List.of();
    }

    private boolean isNumbersOnly(String text) {return text != null && text.matches("^[0-9]+$");}

    private ContratoDto buildContratoDto(CotContratoProjection.ConnInfo c, CotContratoProjection.TerInfo t) {
        return new ContratoDto.Builder()
            .concod(c.getCONCOD())
            .conlot(c.getCONLOT())
            .condes(c.getCONDES())
            .confin(c.getCONFIN())
            .conffi(c.getCONFFI())
            .conblo(c.getCONBLO())
            .tercod(t.getTERCOD())
            .ternom(t.getTERNOM())
            .build();
    }
}