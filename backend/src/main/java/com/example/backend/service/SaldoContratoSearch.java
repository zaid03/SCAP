package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.sqlserver2.repository.CogRepository;
import com.example.backend.dto.SaldoContrato;

@Service 
public class SaldoContratoSearch {
    @Autowired
    private CogRepository cogRepository;

    public List<SaldoContrato> searchSaldoContratos(
        Integer ent,
        String eje,
        String cge,
        String contrato,
        String proveedor
    ) {
        List<SaldoContrato> contratos = new ArrayList<>();
        if ((proveedor != null && !proveedor.isBlank()) && (cge == null || cge.isBlank()) && (contrato == null || contrato.isBlank())) {
            if (isNumbersOnly(proveedor)) {
                Integer tercod = Integer.parseInt(proveedor);
                contratos = cogRepository.findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERCODOrENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERNIFContaining(ent, eje, 3, 1, tercod, ent, eje, 3, 1, proveedor);
                return contratos;
            } else {
                contratos = cogRepository.findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERNOMContainingOrENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_ter_TERNIFContaining(ent, eje, 3, 1, proveedor, ent, eje, 3, 1, proveedor);
                return contratos;
            }
        }

        if (cge != null && !cge.isBlank()) {
            if (contrato != null && !contrato.isBlank()) {
                if (isNumbersOnly(contrato)) {
                    Integer concod = Integer.parseInt(contrato);
                    contratos = cogRepository.findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECODAndCONCOD(ent, eje, 3, 1, cge, concod);
                } else {
                    contratos = cogRepository.findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECODAndCot_conn_CONDESContaining(ent, eje, 3, 1, cge, contrato);
                }
                if (proveedor != null && !proveedor.isBlank()) {
                    if (isNumbersOnly(proveedor)) {
                        contratos = filterTodosByTercodAndTernif(contratos, proveedor);
                    } else {
                        contratos = filterTodosByTercodAndTernomAndTernif(contratos, proveedor);
                    }
                }
                return contratos;
            } else {
                contratos = cogRepository.findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCge_CGECOD(ent, eje, 3, 1, cge);
                if (proveedor != null && !proveedor.isBlank()) {
                    if (isNumbersOnly(proveedor)) {
                        contratos = filterTodosByTercodAndTernif(contratos, proveedor);
                    } else {
                        contratos = filterTodosByTercodAndTernomAndTernif(contratos, proveedor);
                    }
                }
                return contratos;
            }
        }

        if ((contrato != null && !contrato.isBlank()) && (cge == null || cge.isBlank())) {
            if (isNumbersOnly(contrato)) {
                contratos = cogRepository.findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCONCOD(ent, eje, 3, 1, Integer.parseInt(contrato));
                if (proveedor != null && !proveedor.isBlank()) {
                    if (isNumbersOnly(proveedor)) {
                        contratos = filterTodosByTercodAndTernif(contratos, proveedor);
                    } else {
                        contratos = filterTodosByTercodAndTernomAndTernif(contratos, proveedor);
                    }
                }
                return contratos;
            } else {
                contratos = cogRepository.findByENTAndEJEAndCot_conn_CONTIPAndCot_conn_CONBLONotAndCot_conn_CONDESContaining(ent, eje, 3, 1, contrato);
                if (proveedor != null && !proveedor.isBlank()) {
                    if (isNumbersOnly(proveedor)) {
                        contratos = filterTodosByTercodAndTernif(contratos, proveedor);
                    } else {
                        contratos = filterTodosByTercodAndTernomAndTernif(contratos, proveedor);
                    }
                }
                return contratos;
            }
        }

        return contratos;
    }

    private boolean isNumbersOnly(String text) {return text.matches("^[0-9]+$");}
    private List<SaldoContrato> filterTodosByTercodAndTernif (
        List<SaldoContrato> contratos,
        String term
    ) {
        String termLower = term.toLowerCase();
        return contratos.stream().filter(p -> { SaldoContrato.CotProjection cot = p.getCot();
            if (cot == null) {return false;}
            boolean matchesTercod = cot.getTERCOD() != null && cot.getTERCOD().toString().equals(term);
            boolean matchesTernif = cot.getTer() != null && cot.getTer().getTERNIF() != null && cot.getTer().getTERNIF().replaceAll("\\s+", " ").toLowerCase().contains(termLower);

            return matchesTercod || matchesTernif;
        }).toList();
    }
    private List<SaldoContrato> filterTodosByTercodAndTernomAndTernif(
        List<SaldoContrato> contratos,
        String term
    ) {
        String termLower = term.toLowerCase();
        return contratos.stream().filter(p -> {
            if (p.getCot() == null || p.getCot().getTer() == null) {return false;}
                SaldoContrato.TerProjection ter = p.getCot().getTer();
                String ternif = ter.getTERNIF();
                String ternom = ter.getTERNOM();
                return (ternif != null && ternif.replaceAll("\\s+", " ").toLowerCase().contains(termLower)) || (ternom != null && ternom.replaceAll("\\s+", " ").toLowerCase().contains(termLower));
        }).toList();
    }
}