package com.example.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.dto.FacWithTerProjection;
import com.example.backend.sqlserver2.repository.FacRepository;

@Service
public class FacturaSearch {
    @Autowired
    private FacRepository facRepository;

    public static class FacturaSearchCriteria {
        public final Integer ent;
        public final String eje;
        public final String cgecod;
        public final String mainFilter;
        public final Integer ejFactura;
        public final String estado;
        public final String fecha;
        public final LocalDate fromDate;
        public final LocalDate toDate;

        private FacturaSearchCriteria(Builder builder) {
            this.ent = builder.ent;
            this.eje = builder.eje;
            this.cgecod = builder.cgecod;
            this.mainFilter = builder.mainFilter;
            this.ejFactura = builder.ejFactura;
            this.estado = builder.estado;
            this.fecha = builder.fecha;
            this.fromDate = builder.fromDate;
            this.toDate = builder.toDate;
        }

        public static class Builder {
            private Integer ent;
            private String eje;
            private String cgecod;
            private String mainFilter;
            private Integer ejFactura;
            private String estado;
            private String fecha;
            private LocalDate fromDate;
            private LocalDate toDate;

            public Builder ent(Integer ent) { this.ent = ent; return this; }
            public Builder eje(String eje) { this.eje = eje; return this; }
            public Builder cgecod(String cgecod) { this.cgecod = cgecod; return this; }
            public Builder mainFilter(String mainFilter) { this.mainFilter = mainFilter; return this; }
            public Builder ejFactura(Integer ejFactura) { this.ejFactura = ejFactura; return this; }
            public Builder estado(String estado) { this.estado = estado; return this; }
            public Builder fecha(String fecha) { this.fecha = fecha; return this; }
            public Builder fromDate(LocalDate fromDate) { this.fromDate = fromDate; return this; }
            public Builder toDate(LocalDate toDate) { this.toDate = toDate; return this; }

            public FacturaSearchCriteria build() {
                return new FacturaSearchCriteria(this);
            }
        }
    }

    public List<FacWithTerProjection> searchFactura(FacturaSearchCriteria criteria) {
        LocalDateTime fromDateTime = criteria.fromDate != null ? criteria.fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = criteria.toDate != null ? criteria.toDate.atTime(23, 59, 59) : null;

        List<FacWithTerProjection> facturas = new ArrayList<>();
        if (criteria.cgecod == null || criteria.cgecod.isEmpty()) {
            facturas = facRepository.findByENTAndEJEOrderByFACFREAsc(criteria.ent, criteria.eje);
        } else {
            facturas = facRepository.findByENTAndEJEAndCGECODOrderByFACFREAsc(criteria.ent, criteria.eje, criteria.cgecod);
        }
        
        if (facturas != null && !facturas.isEmpty()) {
            if (criteria.mainFilter != null && !criteria.mainFilter.isEmpty()) {
                if (isNumbersOnly(criteria.mainFilter)) {
                    if (criteria.mainFilter.length() <= 5) {
                        facturas = filterByTercodOrFacfac(facturas, criteria.mainFilter);
                    } else {
                        facturas = filterByTernifOrFacado(facturas, criteria.mainFilter);
                    }
                }
                else if (isMixed(criteria.mainFilter)) {
                    facturas = filterByTernifOrTernomOrFacdoc(facturas, criteria.mainFilter);
                } else {
                    facturas = filterByTernomOrFacdoc(facturas, criteria.mainFilter);
                }
            }

            if (criteria.ejFactura != null) {
                facturas = filterByFacann(facturas, criteria.ejFactura);
            }

            if (criteria.estado != null && !criteria.estado.isEmpty()) {
                if (criteria.estado.equals("contabilizadas")) {
                    facturas = filterByFacadoNNull(facturas);
                } else if (criteria.estado.equals("noContabilizadas")) {
                    facturas = filterByFacadoNull(facturas);
                } else if (criteria.estado.equals("ptApplidas")) {
                    facturas = filterByFacadoNullAndEMath(facturas);
                } else if (criteria.estado.equals("sinPtApplicar")) {
                    facturas = filterByFacadoNullAndNotEMath(facturas);
                }
            }

            if (criteria.fecha != null && !criteria.fecha.isEmpty()) {
                if (criteria.fecha.equals("registro")) {
                    if (fromDateTime != null && toDateTime != null) {
                        facturas = filterByFacfreBetween(facturas, fromDateTime, toDateTime);
                    } else if (fromDateTime != null && toDateTime == null) {
                        facturas = filterByFacfreFrom(facturas, fromDateTime);
                    } else if (toDateTime != null && fromDateTime == null) {
                        facturas = filterByFacfreTo(facturas, toDateTime);
                    }
                } else if (criteria.fecha.equals("factura")) {
                    if (fromDateTime != null && toDateTime != null) {
                        facturas = filterByFacdatBetween(facturas, fromDateTime, toDateTime);
                    } else if (fromDateTime != null && toDateTime == null) {
                        facturas = filterByFacdatFrom(facturas, fromDateTime);
                    } else if (toDateTime != null && fromDateTime == null) {
                        facturas = filterByFacdatTo(facturas, toDateTime);
                    }
                } else if (criteria.fecha.equals("contable")) {
                    if (fromDateTime != null && toDateTime != null) {
                        facturas = filterByFacfcoBetween(facturas, fromDateTime, toDateTime);
                    } else if (fromDateTime != null && toDateTime == null) {
                        facturas = filterByFacfcoFrom(facturas, fromDateTime);
                    } else if (toDateTime != null && fromDateTime == null) {
                        facturas = filterByFacfcoTo(facturas, toDateTime);
                    }
                }
            }
        }
        
        return facturas;
    }

    private boolean isNumbersOnly(String text) {return text.matches("^[0-9]+$");}
    private boolean isMixed(String text) {return !isNumbersOnly(text);}

    private List<FacWithTerProjection> filterByTercodOrFacfac (
        List<FacWithTerProjection> facturas, 
        String main_filter
    ) {
        return facturas.stream().filter(f -> 
            (f.getTERCOD() != null && f.getTERCOD().toString().equals(main_filter)) ||
            (f.getFACFAC() != null && f.getFACFAC().toString().equals(main_filter))
        ).toList();
    }
    private List<FacWithTerProjection> filterByTernifOrFacado (
        List<FacWithTerProjection> facturas, 
        String main_filter
    ) {
        return  facturas.stream().filter(f -> 
            (f.getTer_TERNIF() != null && f.getTer_TERNIF().equals(main_filter)) || 
            (f.getFACADO() != null && f.getFACADO().contains(main_filter))
        ).toList();
    }
    private List<FacWithTerProjection> filterByTernifOrTernomOrFacdoc (
        List<FacWithTerProjection> facturas, 
        String main_filter
    ) {
        return facturas.stream().filter(f -> (f.getTer_TERNIF() != null && f.getTer_TERNIF().toLowerCase().contains(main_filter.toLowerCase())) || (f.getTer_TERNOM() != null && f.getTer_TERNOM().toLowerCase().contains(main_filter.toLowerCase())) || (f.getFACDOC() != null && f.getFACDOC().toLowerCase().contains(main_filter.toLowerCase()))).toList();
    }
    private List<FacWithTerProjection> filterByTernomOrFacdoc (
        List<FacWithTerProjection> facturas, 
        String main_filter
    ) {
        return facturas.stream().filter(f -> (f.getTer_TERNOM() != null && f.getTer_TERNOM().toLowerCase().contains(main_filter.toLowerCase())) || (f.getFACDOC() != null && f.getFACDOC().toLowerCase().contains(main_filter.toLowerCase()))).toList();
    }
    private List<FacWithTerProjection> filterByFacann (
        List<FacWithTerProjection> facturas, 
        Integer ej_factura
    ) {
        return facturas.stream().filter(f -> (f.getFACANN() != null && f.getFACANN().equals(ej_factura))).toList();
    }
    private List<FacWithTerProjection> filterByFacadoNNull (
        List<FacWithTerProjection> facturas
    ) {
        return facturas.stream().filter(f -> f.getFACADO() != null).toList();
    }
    private List<FacWithTerProjection> filterByFacadoNull (
        List<FacWithTerProjection> facturas
    ) {
        return facturas.stream().filter(f -> f.getFACADO() == null).toList();
    }
    private List<FacWithTerProjection> filterByFacadoNullAndEMath (
        List<FacWithTerProjection> facturas
    ) {
        return facturas.stream().filter(f -> (f.getFACADO() == null && 
            Math.round(f.getFACIMP() * 100.0) / 100.0 == Math.round((f.getFACIEC() + f.getFACIDI()) * 100.0) / 100.0
        )).toList();
    }
    private List<FacWithTerProjection> filterByFacadoNullAndNotEMath (
        List<FacWithTerProjection> facturas
    ) {
        return facturas.stream().filter(f -> (f.getFACADO() == null && 
            Math.round(f.getFACIMP() * 100.0) / 100.0 != Math.round((f.getFACIEC() + f.getFACIDI()) * 100.0) / 100.0
        )).toList();
    }
    private List<FacWithTerProjection> filterByFacfreFrom (
        List<FacWithTerProjection> facturas,
        LocalDateTime fromDateTime
    ) {
        return facturas.stream().filter(f -> 
            (f.getFACFRE() != null && f.getFACFRE().isAfter(fromDateTime)) ||
            (f.getFACFRE() != null && f.getFACFRE().isEqual(fromDateTime))
        ).toList();
    }
    private List<FacWithTerProjection> filterByFacfreTo (
        List<FacWithTerProjection> facturas,
        LocalDateTime fromDateTime
    ) {
        return facturas.stream().filter(f -> 
            (f.getFACFRE() != null && f.getFACFRE().isBefore(fromDateTime)) ||
            (f.getFACFRE() != null && f.getFACFRE().isEqual(fromDateTime))
        ).toList();
    }
    private List<FacWithTerProjection> filterByFacfreBetween (
        List<FacWithTerProjection> facturas,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime
    ) {
        return facturas.stream().filter(f -> 
            (f.getFACFRE() != null && !f.getFACFRE().isBefore(fromDateTime) && !f.getFACFRE().isAfter(toDateTime))
        ).toList();
    }
    private List<FacWithTerProjection> filterByFacdatFrom (
        List<FacWithTerProjection> facturas,
        LocalDateTime fromDateTime
    ) {
        return facturas.stream().filter(f -> 
            (f.getFACDAT() != null && f.getFACDAT().isAfter(fromDateTime)) ||
            (f.getFACDAT() != null && f.getFACDAT().isEqual(fromDateTime))
        ).toList();
    }
    private List<FacWithTerProjection> filterByFacdatTo (
        List<FacWithTerProjection> facturas,
        LocalDateTime fromDateTime
    ) {
        return facturas.stream().filter(f -> 
            (f.getFACDAT() != null && f.getFACDAT().isBefore(fromDateTime)) ||
            (f.getFACDAT() != null && f.getFACDAT().isEqual(fromDateTime))
        ).toList();
    }
    private List<FacWithTerProjection> filterByFacdatBetween (
        List<FacWithTerProjection> facturas,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime
    ) {
        return facturas.stream().filter(f -> 
            (f.getFACDAT() != null && !f.getFACDAT().isBefore(fromDateTime) && !f.getFACDAT().isAfter(toDateTime))
        ).toList();
    }
    private List<FacWithTerProjection> filterByFacfcoFrom (
        List<FacWithTerProjection> facturas,
        LocalDateTime fromDateTime
    ) {
        return facturas.stream().filter(f -> 
            (f.getFACFCO() != null && f.getFACFCO().isAfter(fromDateTime)) ||
            (f.getFACFCO() != null && f.getFACFCO().isEqual(fromDateTime))
        ).toList();
    }
    private List<FacWithTerProjection> filterByFacfcoTo (
        List<FacWithTerProjection> facturas,
        LocalDateTime fromDateTime
    ) {
        return facturas.stream().filter(f -> 
            (f.getFACFCO() != null && f.getFACFCO().isBefore(fromDateTime)) ||
            (f.getFACFCO() != null && f.getFACFCO().isEqual(fromDateTime))
        ).toList();
    }
    private List<FacWithTerProjection> filterByFacfcoBetween (
        List<FacWithTerProjection> facturas,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime
    ) {
        return facturas.stream().filter(f -> 
            (f.getFACFCO() != null && !f.getFACFCO().isBefore(fromDateTime) && !f.getFACFCO().isAfter(toDateTime))
        ).toList();
    }
}