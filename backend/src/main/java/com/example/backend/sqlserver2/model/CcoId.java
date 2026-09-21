package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class CcoId implements Serializable {
    private Integer ENT;
    private String EJE;
    private String CCOCOD; 

    public CcoId() {}
    
    public CcoId(Integer ENT, String EJE, String CCOCOD) {
        this.ENT = ENT;
        this.EJE = EJE;
        this.CCOCOD = CCOCOD;
    }

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }

    public String getEJE() { return EJE; }
    public void setEJE(String EJE) { this.EJE = EJE; }

    public String getCCOCOD() { return CCOCOD; }
    public void setCCOCOD(String CCOCOD) { this.CCOCOD = CCOCOD; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CcoId ccoId = (CcoId) o;
        return Objects.equals(ENT, ccoId.ENT) &&
               Objects.equals(EJE, ccoId.EJE) &&
               Objects.equals(CCOCOD, ccoId.CCOCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, EJE, CCOCOD);
    }
}
