package com.example.backend.sqlserver2.model;

import java.io.Serializable;
import java.util.Objects;

public class CoaId implements Serializable{
    private Integer ENT;
    private String EJE;
    private Integer CONCOD;
    private String AFACOD;
    private String ASUCOD;
    private String ARTCOD;

    public CoaId() {}

    public CoaId(Integer ENT, String EJE, Integer CONCOD, String AFACOD, String ASUCOD, String ARTCOD) {
        this.ENT = ENT;
        this.EJE = EJE;
        this.CONCOD = CONCOD;
        this.AFACOD = AFACOD;
        this.ASUCOD = ASUCOD;
        this.ARTCOD = ARTCOD;
    }

    public Integer getENT() {return ENT;}
    public void setENT(Integer ENT) {this.ENT = ENT;}

    public String getEJE() {return EJE;}
    public void setEJE(String EJE) {this.EJE = EJE;}

    public Integer getCONCOD() {return CONCOD;}
    public void setCONCOD(Integer CONCOD) {this.CONCOD = CONCOD;}

    public String getAFACOD() {return AFACOD;}
    public void setAFACOD(String AFACOD) {this.AFACOD = AFACOD;}

    public String getASUCOD() {return ASUCOD;}
    public void setASUCOD(String ASUCOD) {this.ASUCOD = ASUCOD;}

    public String getARTCOD() {return ARTCOD;}
    public void setARTCOD(String ARTCOD) {this.ARTCOD = ARTCOD;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoaId coaId = (CoaId) o;
        return Objects.equals(ENT, coaId.ENT) &&
            Objects.equals(EJE, coaId.EJE) &&
            Objects.equals(CONCOD, coaId.CONCOD)&&
            Objects.equals(AFACOD, coaId.AFACOD)&&
            Objects.equals(ASUCOD, coaId.ASUCOD)&&
            Objects.equals(ARTCOD, coaId.ARTCOD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ENT, EJE, CONCOD, AFACOD, AFACOD, ARTCOD);
    }
}
