package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class ConId implements Serializable {
    private Integer ENT;
    private String EJE;
    private Integer CONCOD;

    public ConId() {}

    public ConId(Integer ENT,String EJE, Integer CONCOD) {
        this.ENT = ENT;
        this.EJE = EJE;
        this.CONCOD = CONCOD;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConId conId = (ConId) o;
        return Objects.equals(ENT, conId.ENT) &&
            Objects.equals(EJE, conId.EJE) &&
            Objects.equals(CONCOD, conId.CONCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, EJE, CONCOD);
    }
}
