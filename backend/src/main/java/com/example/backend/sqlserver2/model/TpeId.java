package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class TpeId implements Serializable {
    private Integer ENT;
    private Integer TERCOD;
    private Integer TPECOD;

    public TpeId() {}

    public TpeId(Integer ENT, Integer TERCOD, Integer TPECOD) {
        this.ENT = ENT;
        this.TERCOD = TERCOD;
        this.TPECOD = TPECOD;
    }
    
    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }
    public Integer getTERCOD() { return TERCOD; }
    public void setTERCOD(Integer TERCOD) { this.TERCOD = TERCOD; }
    public Integer getTPECOD() { return TPECOD; }
    public void setTPECOD(Integer TPECOD) { this.TPECOD = TPECOD; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TpeId)) return false;
        TpeId tpeId = (TpeId) o;
        return Objects.equals(ENT, tpeId.ENT) &&
               Objects.equals(TERCOD, tpeId.TERCOD) &&
               Objects.equals(TPECOD, tpeId.TPECOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, TERCOD, TPECOD);
    }
}
