package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class CotId implements Serializable {
    private Integer ENT;
    private String EJE;
    private Integer CONCOD;
    private Integer TERCOD;

    public CotId() {}

    public CotId(Integer ENT,String EJE, Integer CONCOD, Integer TERCOD) {
        this.ENT = ENT;
        this.EJE = EJE;
        this.CONCOD = CONCOD;
        this.TERCOD = TERCOD;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    public Integer getTERCOD() {return TERCOD;}
    public void setTERCOD(Integer TERCOD) {this.TERCOD = TERCOD;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CotId cotId = (CotId) o;
        return Objects.equals(ENT, cotId.ENT) &&
            Objects.equals(EJE, cotId.EJE) &&
            Objects.equals(CONCOD, cotId.CONCOD) &&
            Objects.equals(TERCOD, cotId.TERCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, EJE, CONCOD, TERCOD);
    }
}
