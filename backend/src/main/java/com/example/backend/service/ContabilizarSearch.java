package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.dto.ProjectionContabilizar;
import com.example.backend.sqlserver2.repository.FdeRepository;

import java.util.List;

@Service
public class ContabilizarSearch {
    @Autowired
    private FdeRepository fdeRepository;

    public List<ProjectionContabilizar> searchContabilizado (
        Integer ent,
        String eje,
        String proveedor,
        String centroGestor,
        String economica,
        Integer ano
    ) {
        List<ProjectionContabilizar> facturas = fdeRepository.findPendienteContabilizar(ent, eje);

        if (facturas != null && !facturas.isEmpty()) {
            if (proveedor != null && !proveedor.isEmpty()) {
                if (isNumbersOnly(proveedor)) {
                    facturas = filterByProveedorAll(facturas, proveedor);
                } else if (isMixed(proveedor)) {
                    facturas = filterByProveedorNombre(facturas, proveedor);
                }
            }
            if (centroGestor != null && !centroGestor.isEmpty()) {
                facturas = filterByCentroGestor(facturas, centroGestor);
            }
            if (economica != null && !economica.isEmpty()) {
                facturas = filterByEconomica(facturas, economica);
            }
            if (ano != null) {
                facturas = filterByAno(facturas, ano);
            }
        }
        return facturas;
    }

    private boolean isNumbersOnly(String text) {return text.matches("^[0-9]+$");}
    private boolean isMixed(String text) {return !isNumbersOnly(text);}

    private List<ProjectionContabilizar> filterByProveedorAll (
        List<ProjectionContabilizar> facturas,
        String proveedor
    ) {
        return facturas.stream().filter(f -> {
            if (f.getTERNIF() == null && f.getTERNOM() == null) return false;
            return (f.getTERNIF() != null && f.getTERNIF().toLowerCase().equals(proveedor.toLowerCase())) ||
                (f.getTERNOM() != null && f.getTERNOM().toLowerCase().contains(proveedor.toLowerCase()));
        }).toList();
    }

    private List<ProjectionContabilizar> filterByProveedorNombre (
        List<ProjectionContabilizar> facturas,
        String proveedor
    ) {
        return facturas.stream().filter(f -> 
            f.getTERNOM() != null && 
            f.getTERNOM().toLowerCase().contains(proveedor.toLowerCase())
        ).toList();
    }

    private List<ProjectionContabilizar> filterByCentroGestor (
        List<ProjectionContabilizar> facturas,
        String centroGestor
    ) {
        return facturas.stream().filter(f -> 
            f.getCGECOD() != null && 
            f.getCGECOD().toLowerCase().equals(centroGestor.toLowerCase())
        ).toList();
    }
    private List<ProjectionContabilizar> filterByEconomica (
        List<ProjectionContabilizar> facturas,
        String economica
    ) {
        return facturas.stream().filter(f -> 
            (f.getFDEECO() != null && f.getFDEECO().toLowerCase().equals(economica.toLowerCase()))
        ).toList();
    }
    private List<ProjectionContabilizar> filterByAno (
        List<ProjectionContabilizar> facturas,
        Integer ano
    ) {
        return facturas.stream().filter(f -> 
            f.getFACANN() != null && 
            f.getFACANN().equals(ano)
        ).toList();
    }
}