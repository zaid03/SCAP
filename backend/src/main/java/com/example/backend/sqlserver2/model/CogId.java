package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class CogId implements Serializable {
    private Integer ENT;
    private String EJE;
    private Integer CONCOD;
    private String CGECOD;

    public CogId() {}

    public CogId(Integer ENT, String EJE, Integer CONCOD, String CGECOD) {
        this.ENT = ENT;
        this.EJE = EJE;
        this.CONCOD = CONCOD;
        this.CGECOD = CGECOD;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    public String getCGECOD() {return CGECOD;}
    public void setCGECOD(String CGECOD) {this.CGECOD = CGECOD;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CogId cogId = (CogId) o;
        return Objects.equals(ENT, cogId.ENT) &&
            Objects.equals(EJE, cogId.EJE) &&
            Objects.equals(CONCOD, cogId.CONCOD) &&
            Objects.equals(CGECOD, cogId.CGECOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, EJE, CONCOD, CGECOD);
    }
}