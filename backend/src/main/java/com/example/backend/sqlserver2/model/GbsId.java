package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class GbsId implements Serializable {
    private Integer ENT;
    private String EJE;
    private String CGECOD;
    private String GBSREF;

    public GbsId() {}

    public GbsId (Integer ENT, String EJE, String CGECOD, String GBSREF) {
        this.ENT = ENT;
        this.EJE = EJE;
        this.CGECOD = CGECOD;
        this.GBSREF = GBSREF;
    }

    public Integer getENT() { return ENT; }
    public void setENT(Integer ENT) { this.ENT = ENT; }
    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}
    public String getCGECOD() { return CGECOD; }
    public void setCGECOD(String CGECOD) { this.CGECOD = CGECOD; }
    public String getGSBREF() { return GBSREF; }
    public void setGSBREF (String GBSREF) { this.GBSREF = GBSREF; }
    
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GbsId gbsId = (GbsId) o;
        return  Objects.equals(ENT, gbsId.ENT) &&
                Objects.equals(EJE, gbsId.EJE) &&
                Objects.equals(CGECOD, gbsId.CGECOD) &&
                Objects.equals(GBSREF, gbsId.GBSREF);
    }

    @Override
    public int hashCode(){
        return Objects.hash(ENT, EJE, CGECOD, GBSREF);
    }
}
