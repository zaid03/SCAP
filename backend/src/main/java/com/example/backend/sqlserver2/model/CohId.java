package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class CohId {
    private Integer ENT;
    private String EJE;
    private Integer CONCOD;
    private Integer COHCOD;

    public CohId() {}
    public CohId(Integer ENT, String EJE, Integer CONCOD, Integer COHCOD) {
        this.ENT = ENT;
        this.EJE = EJE;
        this.CONCOD = CONCOD;
        this.COHCOD = COHCOD;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    public Integer getCOHCOD() {return COHCOD;}
    public void setCOHCOD(Integer COHCOD) {this.COHCOD = COHCOD;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohId cohId = (CohId) o;
        return Objects.equals(ENT, cohId.ENT) &&
            Objects.equals(EJE, cohId.EJE) &&
            Objects.equals(CONCOD, cohId.CONCOD) &&
            Objects.equals(COHCOD, cohId.COHCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, EJE, CONCOD, COHCOD);
    }
}
