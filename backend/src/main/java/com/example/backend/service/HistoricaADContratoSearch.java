package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.sqlserver2.repository.CohRepository;
import com.example.backend.dto.HistoricaContratos;

@Service 
public class HistoricaADContratoSearch {
    @Autowired
    private CohRepository cohRepository;

    public List<HistoricaContratos> historicaADContratoSearch(
        Integer ent,
        String eje,
        String cge,
        String contrato,
        String proveedor
    ) {
        List<HistoricaContratos> contratos = new ArrayList<>();
        if ((proveedor != null && !proveedor.isBlank()) && (cge == null || cge.isBlank()) && (contrato == null || contrato.isBlank())) {
            return findByProvider(ent, eje, proveedor);
        }

        if (cge != null && !cge.isBlank()) {
            if (contrato != null && !contrato.isBlank()) {
                if (isNumbersOnly(contrato)) {
                    contratos = cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECODAndConn_CONCOD(
                        ent, eje, 3, cge, Integer.parseInt(contrato)
                    );
                } else {
                    contratos = cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECODAndConn_CONDESContaining(
                        ent, eje, 3, cge, contrato
                    );
                }
                if (proveedor != null && !proveedor.isBlank()) {
                    contratos = filterByProvider(contratos, proveedor);
                }
                return contratos;
            } else {
                contratos = cohRepository.findByENTAndEJEAndCon_CONTIPAndCge_CGECOD(ent, eje, 3, cge);
                if (proveedor != null && !proveedor.isBlank()) {
                    contratos = filterByProvider(contratos, proveedor);
                }
                return contratos;
            }
        }

        if ((contrato != null && !contrato.isBlank()) && (cge == null || cge.isBlank())) {
            if (isNumbersOnly(contrato)) {
                contratos = cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_CONCOD(
                    ent, eje, 3, Integer.parseInt(contrato)
                );
                if (proveedor != null && !proveedor.isBlank()) {
                    contratos = filterByProvider(contratos, proveedor);
                }
                return contratos;
            } else {
                contratos = cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_CONDESContaining(
                    ent, eje, 3, contrato
                );
                if (proveedor != null && !proveedor.isBlank()) {
                    contratos = filterByProvider(contratos, proveedor);
                }
                return contratos;
            }
        }

        return contratos;
    }
    private boolean isNumbersOnly(String text) {return text.matches("^[0-9]+$");}

    private List<HistoricaContratos> filterByProvider(
        List<HistoricaContratos> contratos,
        String term
    ) {
        return isNumbersOnly(term)
            ? filterTodosByTercodAndTernif(contratos, term)
            : filterTodosByTernomAndTernif(contratos, term);
    }

    private List<HistoricaContratos> findByProvider(
        Integer ent,
        String eje,
        String term
    ) {
        List<HistoricaContratos> matches = new ArrayList<>();

        if (isNumbersOnly(term)) {
            matches.addAll(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERCOD(
                ent, eje, 3, Integer.parseInt(term)
            ));
            matches.addAll(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERNIFContaining(
                ent, eje, 3, term
            ));
        } else {
            matches.addAll(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERNOMContaining(
                ent, eje, 3, term
            ));
            matches.addAll(cohRepository.findByENTAndEJEAndCon_CONTIPAndConn_Cots_Ter_TERNIFContaining(
                ent, eje, 3, term
            ));
        }

        return matches.stream().distinct().toList();
    }

    private List<HistoricaContratos> filterTodosByTercodAndTernif (
        List<HistoricaContratos> contratos,
        String term
    ) {
        String normalizedTerm = normalizeSearchTerm(term);

        return contratos.stream().filter(p -> {
            if (p.getCon() == null || p.getCon().getCots() == null) {
                return false;
            }

            return p.getCon().getCots().stream().anyMatch(cot -> {
                boolean matchesTercod = cot.getTERCOD() != null
                    && cot.getTERCOD().toString().equals(term.trim());
                boolean matchesTernif = cot.getTer() != null
                    && normalizeSearchText(cot.getTer().getTERNIF()).contains(normalizedTerm);

                return matchesTercod || matchesTernif;
            });
        }).toList();
    }

    private List<HistoricaContratos> filterTodosByTernomAndTernif(
        List<HistoricaContratos> contratos,
        String term
    ) {
        String normalizedTerm = normalizeSearchTerm(term);

        return contratos.stream().filter(p -> {
            if (p.getCon() == null || p.getCon().getCots() == null) {
                return false;
            }

            return p.getCon().getCots().stream().anyMatch(cot -> {
                if (cot.getTer() == null) {
                    return false;
                }

                HistoricaContratos.TerProjection ter = cot.getTer();
                return normalizeSearchText(ter.getTERNOM()).contains(normalizedTerm)
                    || normalizeSearchText(ter.getTERNIF()).contains(normalizedTerm);
            });
        }).toList();
    }

    private String normalizeSearchTerm(String term) {
        return normalizeSearchText(term == null ? "" : term.trim());
    }

    private String normalizeSearchText(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
