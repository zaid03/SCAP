package com.example.backend.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.sqlserver2.model.Ter;
import com.example.backend.sqlserver2.repository.TerRepository;

@Service
public class ProveedoresSearch {
    @Autowired
    private TerRepository terRepository;

    public List<Ter> searchProveedoers (
        Integer ent,
        String searchMode, 
        String term
    ) {
        term = term.replaceAll("\\s+", " ").trim();
        List<Ter> proveedores = terRepository.findByENT(ent);

        if (proveedores != null && !proveedores.isEmpty()) {
            if (searchMode.equals("todos")) {
                if (isNumbersOnly(term)) {
                    if (term.length() < 5) {
                        proveedores = filterTodosByTercod(proveedores, term);
                    } else if (term.length() >= 5) {
                        proveedores = filterTodosByTercodAndTernif(proveedores, term);
                    }
                } else if (isMixed(term)) {
                    proveedores = filterTodosByTercodAndTernomAndTerali(proveedores, term);
                }
            }

            if (searchMode.equals("Nobloqueado")) {
                if (isNumbersOnly(term)) {
                    if (term.length() < 5) {
                        proveedores = filterNoBloqueadoByTercodAndTerblo(proveedores, term, 0);
                    } else if (term.length() >= 5) {
                        proveedores = filterNoBloqueadoByTercodAndTernifAndTerblo(proveedores, term, 0);
                    }
                } else if (isMixed(term)) {
                    proveedores = filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo(proveedores, term, 0);
                }
            }

            if (searchMode.equals("bloqueado")) {
                if (isNumbersOnly(term)) {
                    if (term.length() < 5) {
                        proveedores = filterNoBloqueadoByTercodAndTerblo(proveedores, term, 1);
                    } else if (term.length() >= 5) {
                        proveedores = filterNoBloqueadoByTercodAndTernifAndTerblo(proveedores, term, 1);
                    }
                } else if (isMixed(term)) {
                    proveedores = filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo(proveedores, term, 1);
                }
            }
        }

        return proveedores;
    }

    private boolean isNumbersOnly(String text) {return text.matches("^[0-9]+$");}
    private boolean isMixed(String text) {return !isNumbersOnly(text);}

    private List<Ter> filterTodosByTercod (
        List<Ter> proveedores,
        String term
    ) {
        return proveedores.stream().filter(p -> (p.getTERCOD() != null && p.getTERCOD().toString().equals(term))).toList();
    }
    private List<Ter> filterTodosByTercodAndTernif (
        List<Ter> proveedores,
        String term
    ) {
        String termLower = term.toLowerCase();
        return proveedores.stream().filter(p -> (p.getTERCOD() != null && p.getTERCOD().toString().equals(term)) || (p.getTERNIF() != null && p.getTERNIF().replaceAll("\\s+", " ").toLowerCase().contains(termLower))).toList();
    }
    private List<Ter> filterTodosByTercodAndTernomAndTerali (
        List<Ter> proveedores,
        String term
    ) {
        String termLower = term.toLowerCase();
        return proveedores.stream().filter(p -> (p.getTERNIF() != null && p.getTERNIF().replaceAll("\\s+", " ").toLowerCase().contains(termLower)) || (p.getTERNOM() != null && p.getTERNOM().replaceAll("\\s+", " ").toLowerCase().contains(termLower)) || (p.getTERALI() != null && p.getTERALI().replaceAll("\\s+", " ").toLowerCase().contains(termLower))).toList();
    }

    private List<Ter> filterNoBloqueadoByTercodAndTerblo (
        List<Ter> proveedores,
        String term,
        Integer terblo
    ) {
        return proveedores.stream().filter(p -> 
            (p.getTERCOD() != null && p.getTERCOD().toString().equals(term)) 
            && Objects.equals(p.getTERBLO(), terblo)).toList();

            
    }
    private List<Ter> filterNoBloqueadoByTercodAndTernifAndTerblo (
        List<Ter> proveedores,
        String term,
        Integer terblo
    ) {
        String termLower = term.toLowerCase();
        return proveedores.stream().filter(p -> 
            ((p.getTERCOD() != null && p.getTERCOD().toString().equals(term)) 
            || (p.getTERNIF() != null && p.getTERNIF().replaceAll("\\s+", " ").toLowerCase().contains(termLower))) 
            && Objects.equals(p.getTERBLO(), terblo)).toList(); 
    }
    private List<Ter> filterNoBloqueadoByTercodAndTernomAndTeraliAndTerblo (
        List<Ter> proveedores,
        String term,
        Integer terblo
    ) {
        String termLower = term.toLowerCase();
        return proveedores.stream().filter(p -> 
            ((p.getTERNIF() != null && p.getTERNIF().replaceAll("\\s+", " ").toLowerCase().contains(termLower)) 
            || (p.getTERNOM() != null && p.getTERNOM().replaceAll("\\s+", " ").toLowerCase().contains(termLower)) 
            || (p.getTERALI() != null && p.getTERALI().replaceAll("\\s+", " ").toLowerCase().contains(termLower)))
            && Objects.equals(p.getTERBLO(), terblo)).toList();
    }
}
